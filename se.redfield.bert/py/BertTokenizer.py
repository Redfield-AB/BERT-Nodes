import tempfile
import os
import tensorflow as tf
import numpy as np

from zipfile import ZipFile
from bert.tokenization import FullTokenizer
from transformers import AutoTokenizer, AutoConfig
from ProgressCallback import ProgressCallback
from bert_utils import load_bert_layer

#temporary fix for the UnparsedFlagAccessError
from absl import flags
flags.FLAGS([''])

class TokenizerBase:
    def __init__(self, tokenizer, max_seq_length, sentence_column, second_sentence_column=None):
        self.tokenizer = tokenizer
        self.max_seq_length = max_seq_length
        self.sentence_column = sentence_column
        self.second_sentence_column = second_sentence_column

    def tokenize(self, table, progress_logger):
        selector = [self.sentence_column]
        if(self.second_sentence_column):
            selector.append(self.second_sentence_column)

        input_ids, input_masks, input_segments = [], [], []

        current_count = 0

        for row in table[selector].values:
            ids,masks,segments = self.create_single_input(row)
 
            input_ids.append(ids)
            input_masks.append(masks)
            input_segments.append(segments)

            current_count += 1
            if(current_count % 10 == 0 and progress_logger): 
                progress_logger.on_tokenize_rows_end(current_count)

        input_ids = np.array(input_ids)
        input_masks = np.array(input_masks)
        input_segments = np.array(input_segments)
        return input_ids, input_masks, input_segments

    def create_single_input(self, row):
        raise NotImplementedError()

    def save_to(self, model):
        raise NotImplementedError()


    @classmethod
    def run(cls, input_table,
        bert_model_handle,
        sentence_column,
        tfhub_cache_dir = None,
        max_seq_length = 128,
        second_sentence_column = None,
        ids_column = 'ids',
        masks_column = 'masks',
        segments_column = 'segments'
    ):
        tokenizer = cls.from_pretrained(bert_model_handle, sentence_column, second_sentence_column, max_seq_length, tfhub_cache_dir)        

        progress_logger = ProgressCallback(len(input_table))
        ids, masks, segments = tokenizer.tokenize(input_table, progress_logger)

        output_table = input_table.copy()
        output_table[ids_column] = ids
        output_table[masks_column] = masks
        output_table[segments_column] = segments

        return output_table

    @classmethod
    def from_pretrained(cls, bert_model_handle, sentence_column, second_sentence_column = None, max_seq_length = 128, cache_dir = None):
        raise NotImplementedError()

    @classmethod
    def from_saved_model(cls, saved_model, sentence_column, second_sentence_column = None, max_seq_length=128):
        raise NotImplementedError()
        
class BertTokenizer(TokenizerBase):
    def __init__(self, vocab_file, do_lower_case, max_seq_length, sentence_column, second_sentence_column = None):
        self.vocab_file = vocab_file
        self.do_lower_case = do_lower_case
        tokenizer = FullTokenizer(vocab_file.asset_path.numpy(), do_lower_case.numpy())

        super().__init__(tokenizer, max_seq_length, sentence_column, second_sentence_column)

    def create_single_input(self, row):
        stokens = self.tokenizer.tokenize(row[0])
        stokens = stokens[:self.max_seq_length - 2]
        stokens = ["[CLS]"] + stokens + ["[SEP]"]

        if(row.size > 1):
            stokens2 = self.tokenizer.tokenize(row[1])
            trim_length = self.max_seq_length - len(stokens) - 1
            if trim_length > 0:
                stokens2 = stokens2[:trim_length] + ["[SEP]"]
                stokens = stokens + stokens2

        ids = self.get_ids(stokens)
        masks = self.get_masks(stokens)
        segments = self.get_segments(stokens)
 
        return ids, masks, segments


    def get_ids(self, tokens):
        """Token ids from Tokenizer vocab"""
        token_ids = self.tokenizer.convert_tokens_to_ids(tokens)
        input_ids = token_ids + [0] * (self.max_seq_length - len(token_ids))
        return input_ids

    def get_masks(self, tokens):
        return [1] * len(tokens) + [0] * (self.max_seq_length - len(tokens))

    def get_segments(self, tokens):
        """Segments: 0 for the first sequence, 1 for the second"""
        segments = []
        current_segment_id = 0
        for token in tokens:
            segments.append(current_segment_id)
            if token == "[SEP]":
                current_segment_id = 1
        return segments + [0] * (self.max_seq_length - len(tokens))
    
    def save_to(self, model):
        model.vocab_file = self.vocab_file
        model.do_lower_case = self.do_lower_case

    @classmethod
    def from_pretrained(cls, bert_model_handle, sentence_column, second_sentence_column=None, max_seq_length=128, cache_dir=None):
        bert_layer = load_bert_layer(bert_model_handle, cache_dir)
        return BertTokenizer(bert_layer.resolved_object.vocab_file,
            bert_layer.resolved_object.do_lower_case, max_seq_length,
            sentence_column, second_sentence_column)

    @classmethod
    def from_saved_model(cls, saved_model, sentence_column, second_sentence_column=None, max_seq_length=128):
        return BertTokenizer(saved_model.vocab_file, saved_model.do_lower_case, max_seq_length, sentence_column, second_sentence_column)

class HFTokenizerWrap(TokenizerBase):
    ZIP_FILE_NAME = 'tokenizer.zip'

    def __init__(self, tokenizer, tokenizer_config, max_seq_length, sentence_column, second_sentence_column = None):
        self.tokenizer_config = tokenizer_config
        super().__init__(tokenizer, max_seq_length, sentence_column, second_sentence_column)

    def create_single_input(self, row):
        text = row[0]
        text_pair = row[1] if row.size > 1 else None

        res = self.tokenizer(text=text, text_pair=text_pair, padding='max_length', truncation=True,
                max_length=self.max_seq_length, return_attention_mask=True, return_token_type_ids=True, return_length=True)
        return res['input_ids'], res['attention_mask'], res['token_type_ids']

    def save_to(self, model):
        temp_dir =  tempfile.TemporaryDirectory()
        self.tokenizer.save_pretrained(temp_dir.name)
        self.tokenizer_config.save_pretrained(temp_dir.name)

        zip_file_path = os.path.join(tempfile.tempdir, self.ZIP_FILE_NAME)
        with ZipFile(zip_file_path, 'w') as zip:
            for root, _, files in os.walk(temp_dir.name):
                for file in files:
                    file_path = os.path.join(root, file)
                    zip.write(file_path, os.path.relpath(file_path, temp_dir.name))
        
        model.tokenizer_file = tf.saved_model.Asset(zip_file_path)

    @classmethod
    def from_pretrained(cls, bert_model_handle, sentence_column, second_sentence_column = None, max_seq_length = 128, cache_dir = None):
        config = AutoConfig.from_pretrained(bert_model_handle, cache_dir=cache_dir)
        tokenizer = AutoTokenizer.from_pretrained(bert_model_handle, cache_dir=cache_dir)
        return HFTokenizerWrap(tokenizer, config, max_seq_length, sentence_column, second_sentence_column)

    @classmethod
    def from_saved_model(cls, saved_model, sentence_column, second_sentence_column=None, max_seq_length=128):
        zip_file_path = saved_model.tokenizer_file.asset_path.numpy().decode()
        extracted_path = os.path.join(os.path.dirname(zip_file_path), 'tokenizer')

        with ZipFile(zip_file_path, 'r') as zip:
            zip.extractall(extracted_path)
        
        tokenizer = AutoTokenizer.from_pretrained(extracted_path)
        config = AutoConfig.from_pretrained(extracted_path)
        return HFTokenizerWrap(tokenizer, config, max_seq_length, sentence_column, second_sentence_column)
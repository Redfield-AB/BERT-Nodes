import tensorflow as tf
import numpy as np
import pandas as pd
import knime_io as knio
from tensorflow.keras.models import Model

from ProgressCallback import ProgressCallback
from BertTokenizer import BertTokenizer, HFTokenizerWrap
from bert_utils import load_bert_layer, load_hf_bert_layer
from BertModelType import BertModelType

class BertEmbedder:
    def __init__(self, bert_layer, tokenizer):
        self.tokenizer = tokenizer

        input_ids = tf.keras.layers.Input(shape=(tokenizer.max_seq_length,), dtype=tf.int32, name="input_ids")
        input_masks = tf.keras.layers.Input(shape=(tokenizer.max_seq_length,), dtype=tf.int32, name="input_masks")
        input_segments = tf.keras.layers.Input(shape=(tokenizer.max_seq_length,), dtype=tf.int32, name="input_segments")

        pooled_output, sequence_output = bert_layer([input_ids, input_masks, input_segments])
        if(pooled_output.shape.ndims == 3 and sequence_output.shape.ndims == 2):
            # For Hugging Face models
            pooled_output, sequence_output = sequence_output, pooled_output
        
        self.pooled_output = pooled_output
        self.sequence_output = sequence_output
        self.inputs = [input_ids, input_masks, input_segments]
        
        self.model = Model(inputs=self.inputs, outputs=[pooled_output, sequence_output])

    def predict(self, input_table, batch_size, progress_logger):
        ids, masks, segments = self.tokenizer.tokenize(input_table, progress_logger)

        pooled_emb, sequence_emb = self.model.predict([ids, masks, segments],
            batch_size=batch_size, callbacks=[progress_logger])
        return pooled_emb, sequence_emb

    def compute_embeddings(self, input_table, batch_size,
        embeddings_column = 'embeddings',
        sequence_embedding_column_prefix = 'sequence_embeddings_',
        include_sequence_embeddings = False
    ):
        progress_logger = ProgressCallback(len(input_table), predict=True, batch_size=batch_size)
        pooled_emb, sequence_emb = self.predict(input_table, batch_size, progress_logger)

        output_table = input_table.copy()
        output_table[embeddings_column] = pooled_emb.tolist()

        if(include_sequence_embeddings):
            columns = [sequence_embedding_column_prefix + str(i) for i in range(len(sequence_emb[0]))]
            se = pd.DataFrame(sequence_emb.tolist(), columns = columns, index = output_table.index).astype(str)
            output_table = pd.concat([output_table, se], axis=1)

        return output_table

    @classmethod
    def run_from_pretrained(cls,
        input_table,
        bert_model_type_key,
        bert_model_handle,
        sentence_column,
        cache_dir = None,
        max_seq_length = 128,
        second_sentence_column = None,
        batch_size = 20,
        embeddings_column = 'embeddings',
        sequence_embedding_column_prefix = 'sequence_embeddings_',
        include_sequence_embeddings = False
    ):
        model_type = BertModelType.from_key(bert_model_type_key)
        embedder = cls.from_pretrained(model_type, bert_model_handle, sentence_column, second_sentence_column, max_seq_length, cache_dir)
        output_table = embedder.compute_embeddings(input_table, batch_size, embeddings_column, sequence_embedding_column_prefix, include_sequence_embeddings)
        knio.output_tables[0] = knio.write_table(output_table)
    
    @classmethod
    def run_from_classifier(cls,
        input_table,
        bert_model_type_key,
        file_store,
        sentence_column,
        max_seq_length = 128,
        second_sentence_column = None,
        batch_size = 20,
        embeddings_column = 'embeddings',
        sequence_embedding_column_prefix = 'sequence_embeddings_',
        include_sequence_embeddings = False
    ):
        saved_model = tf.keras.models.load_model(file_store)
        model_type = BertModelType.from_key(bert_model_type_key)
        embedder = cls.from_saved_model(model_type, saved_model, sentence_column, second_sentence_column, max_seq_length)
        output_table = embedder.compute_embeddings(input_table, batch_size, embeddings_column, sequence_embedding_column_prefix, include_sequence_embeddings)
        knio.output_tables[0] = knio.write_table(output_table)

    @classmethod
    def from_pretrained(cls, model_type:BertModelType, bert_model_handle, sentence_column, second_sentence_column=None, max_seq_length=128, cache_dir=None):
        bert_layer = model_type.load_bert_layer(bert_model_handle, cache_dir)
        tokenizer = model_type.tokenizer_cls.from_pretrained(bert_model_handle, sentence_column, second_sentence_column, max_seq_length, cache_dir)
        return BertEmbedder(bert_layer, tokenizer)

    @classmethod
    def from_saved_model(cls, model_type:BertModelType, saved_model, sentence_column, second_sentence_column=None, max_seq_length=128):
        bert_layer = saved_model.layers[3]
        tokenizer = model_type.tokenizer_cls.from_saved_model(saved_model, sentence_column, second_sentence_column, max_seq_length)
        return BertEmbedder(bert_layer, tokenizer)
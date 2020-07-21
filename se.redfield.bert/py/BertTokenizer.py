import tensorflow as tf
import tensorflow_hub as hub
import bert
from tensorflow.keras.models import  Model
from tqdm import tqdm
import numpy as np
import pandas
from bert.tokenization import bert_tokenization

def get_ids(tokens, tokenizer, max_seq_length):
    """Token ids from Tokenizer vocab"""
    token_ids = tokenizer.convert_tokens_to_ids(tokens,)
    input_ids = token_ids + [0] * (max_seq_length-len(token_ids))
    return input_ids

def get_masks(tokens, max_seq_length):
    return [1] * len(tokens) + [0] * (max_seq_length - len(tokens))
 
def get_segments(tokens, max_seq_length):
    """Segments: 0 for the first sequence, 1 for the second"""
    segments = []
    current_segment_id = 0
    for token in tokens:
        segments.append(current_segment_id)
        if token == "[SEP]":
            current_segment_id = 1
    return segments + [0] * (max_seq_length - len(tokens))

def create_single_input(sentence, MAX_LEN):
  
  stokens = tokenizer.tokenize(sentence)
  
  stokens = stokens[:MAX_LEN]
  
  stokens = ["[CLS]"] + stokens + ["[SEP]"]
 
  ids = get_ids(stokens, tokenizer, MAX_SEQ_LEN)
  masks = get_masks(stokens, MAX_SEQ_LEN)
  segments = get_segments(stokens, MAX_SEQ_LEN)
 
  return ids, masks, segments

def create_input_array(sentences):
 
  input_ids, input_masks, input_segments = [], [], []
 
  for sentence in tqdm(sentences, position=0, leave=True):
  
    ids,masks,segments = create_single_input(sentence, MAX_SEQ_LEN - 2)
 
    input_ids.append(ids)
    input_masks.append(masks)
    input_segments.append(segments)
 
  return input_ids, input_masks, input_segments

bert_layer = hub.KerasLayer(BERT_URL, trainable=True)
FullTokenizer = bert_tokenization.FullTokenizer
train_sentences = input_table[TARGET_COLUMN].values

vocab_file = bert_layer.resolved_object.vocab_file.asset_path.numpy() 
do_lower_case = bert_layer.resolved_object.do_lower_case.numpy()
 
tokenizer = FullTokenizer(vocab_file, do_lower_case)

ids, masks, segments = create_input_array(train_sentences)

output_table = input_table.copy()
output_table[IDS_COLUMN] = ids
output_table[MASKS_COLUMN] = masks
output_table[SEGMENTS_COLUMN] = segments

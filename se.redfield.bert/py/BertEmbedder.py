import tensorflow as tf
import numpy as np
import pandas as pd
from tensorflow.keras.models import Model

from ProgressCallback import ProgressCallback
from BertTokenizer import BertTokenizer
from bert_utils import load_bert_layer

class BertEmbedder:
    def __init__(self, bert_layer, tokenizer, batch_size):
        self.tokenizer = tokenizer
        self.batch_size = batch_size

        input_ids = tf.keras.layers.Input(shape=(tokenizer.max_seq_length,), dtype=tf.int32, name="input_ids")
        input_masks = tf.keras.layers.Input(shape=(tokenizer.max_seq_length,), dtype=tf.int32, name="input_masks")
        input_segments = tf.keras.layers.Input(shape=(tokenizer.max_seq_length,), dtype=tf.int32, name="input_segments")

        pooled_output, sequence_output = bert_layer([input_ids, input_masks, input_segments])
        self.model = Model(inputs=[input_ids, input_masks, input_segments], outputs=[pooled_output, sequence_output])

    def compute_embeddings(self, input_table, progress_logger):
        ids, masks, segments = self.tokenizer.tokenize(input_table, progress_logger)
        ids = np.array(ids)
        masks = np.array(masks)
        segments = np.array(segments)

        pooled_emb, sequence_emb = self.model.predict([ids, masks, segments],
            batch_size=self.batch_size, callbacks=[progress_logger])
        return pooled_emb, sequence_emb

    @classmethod
    def run(cls,
        input_table,
        bert_model_handle,
        sentence_column,
        tfhub_cache_dir = None,
        max_seq_length = 128,
        second_sentence_column = None,
        batch_size = 20,
        embeddings_column = 'embeddings',
        sequence_embedding_column_prefix = 'sequence_embeddings_',
        include_sequence_embeddings = False
    ):
        bert_layer = load_bert_layer(bert_model_handle, tfhub_cache_dir)
        tokenizer = BertTokenizer(bert_layer.resolved_object.vocab_file,
            bert_layer.resolved_object.do_lower_case, max_seq_length,
            sentence_column, second_sentence_column)
        embedder = BertEmbedder(bert_layer, tokenizer, batch_size)

        progress_logger = ProgressCallback(len(input_table), predict=True, batch_size=batch_size)
        pooled_emb, sequence_emb = embedder.compute_embeddings(input_table, progress_logger)

        output_table = input_table.copy()
        output_table[embeddings_column] = pooled_emb.tolist()

        if(include_sequence_embeddings):
            columns = [sequence_embedding_column_prefix + str(i) for i in range(len(sequence_emb[0]))]
            se = pd.DataFrame(sequence_emb.tolist(), columns = columns, index = output_table.index)
            output_table = pd.concat([output_table, se], axis=1)

        return output_table

import tensorflow as tf
import pandas as pd
import knime_io as knio
from tensorflow.keras.models import Model

from ProgressCallback import ProgressCallback
from BertTokenizer import TokenizerBase
from BertModelType import BertModelType

class BertEmbedder:
    def __init__(self, bert_layer, tokenizer: TokenizerBase):
        self.tokenizer = tokenizer

        input_ids = tf.keras.layers.Input(shape=(tokenizer.max_seq_length,), dtype=tf.int32, name="input_ids")
        input_masks = tf.keras.layers.Input(shape=(tokenizer.max_seq_length,), dtype=tf.int32, name="input_masks")
        input_segments = tf.keras.layers.Input(shape=(tokenizer.max_seq_length,), dtype=tf.int32, name="input_segments")

        if bert_layer.__class__.__module__.startswith('transformers'):
            res = bert_layer(input_ids, input_masks, input_segments)
            self.pooled_output =  res.pooler_output
            self.sequence_output = res.last_hidden_state
        else:
            self.pooled_output, self.sequence_output = bert_layer([input_ids, input_masks, input_segments])

        self.inputs = [input_ids, input_masks, input_segments]
        
        self.model = Model(inputs=self.inputs, outputs=[self.pooled_output, self.sequence_output])

    def predict(self, input_table: pd.DataFrame, batch_size, progress_logger):
        ids, masks, segments = self.tokenizer.tokenize(input_table, progress_logger)

        pooled_emb, sequence_emb = self.model.predict([ids, masks, segments],
            batch_size=batch_size, callbacks=[progress_logger])
        return pooled_emb, sequence_emb

    def compute_embeddings(self, input_table: pd.DataFrame, batch_size, progress_logger,
        embeddings_column = 'embeddings',
        sequence_embedding_column_prefix = 'sequence_embeddings_',
        include_sequence_embeddings = False
    ):
        pooled_emb, sequence_emb = self.predict(input_table, batch_size, progress_logger)

        output_table = pd.DataFrame(index=input_table.index)
        output_table[embeddings_column] = pooled_emb.tolist()

        if(include_sequence_embeddings):
            columns = [sequence_embedding_column_prefix + str(i) for i in range(len(sequence_emb[0]))]
            se = pd.DataFrame(sequence_emb.tolist(), columns = columns, index = output_table.index)
            output_table = pd.concat([output_table, se], axis=1)

        return output_table

    @classmethod
    def run_from_pretrained(cls,
        input_table:knio.ReadTable,
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
        write_table = knio.batch_write_table()
        progress_done = 0
        for batch in input_table.batches():
            pd_batch = batch.to_pandas()
            progress_logger = ProgressCallback(len(pd_batch), predict=True, batch_size=batch_size,
                initial_progress=progress_done, subprogress_factor=1/input_table.num_batches)
            output_batch = embedder.compute_embeddings(pd_batch, batch_size, progress_logger, embeddings_column, sequence_embedding_column_prefix, include_sequence_embeddings)
            write_table.append(output_batch)
            progress_done = progress_logger.last_progress
        knio.output_tables[0] = write_table
    
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
        write_table = knio.batch_write_table()
        progress_done = 0
        for batch in input_table.batches():
            pd_batch = batch.to_pandas()
            progress_logger = ProgressCallback(len(pd_batch), predict=True, batch_size=batch_size,
                initial_progress=progress_done, subprogress_factor=1/input_table.num_batches)
            output_table = embedder.compute_embeddings(pd_batch, batch_size, progress_logger, embeddings_column, sequence_embedding_column_prefix, include_sequence_embeddings)
            write_table.append(output_table)
            progress_done = progress_logger.last_progress
        knio.output_tables[0] = write_table

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
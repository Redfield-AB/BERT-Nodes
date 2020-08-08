import tensorflow as tf
import numpy as np
from tensorflow.keras.models import Model
from ProgressCallback import ProgressCallback

class BertEmbedder:
    def __init__(self, bert_layer, tokenizer):
        self.batch_size = 20

        input_ids = tf.keras.layers.Input(shape=(tokenizer.max_seq_length,), dtype=tf.int32, name="input_ids")
        input_masks = tf.keras.layers.Input(shape=(tokenizer.max_seq_length,), dtype=tf.int32, name="input_masks")
        input_segments = tf.keras.layers.Input(shape=(tokenizer.max_seq_length,), dtype=tf.int32, name="input_segments")

        pooled_output, sequence_output = bert_layer([input_ids, input_masks, input_segments])
        self.model = Model(inputs=[input_ids, input_masks, input_segments], outputs=[pooled_output, sequence_output])

    def compute_embeddings(self, ids, masks, segments):
        ids = np.array(ids)
        masks = np.array(masks)
        segments = np.array(segments)

        progress_callback = ProgressCallback(len(ids), self.batch_size)

        pooled_emb, sequence_emb = self.model.predict([ids, masks, segments],
            batch_size=self.batch_size, callbacks=[progress_callback])
        return pooled_emb, sequence_emb

import tempfile
import tensorflow as tf
import numpy as np
import pandas as pd
from transformers import TFAutoModel

from BertEmbedder import BertEmbedder
from BertModelType import BertModelType
from BertTokenizer import BertTokenizer, HFTokenizerWrap
from ProgressCallback import ProgressCallback

class BertClassifier:
    def __init__(self, embedder = None, tokenizer = None, class_count = 0, model = None, multi_label = False):
        if(model):
            assert tokenizer is not None
            self.tokenizer = tokenizer
            self.model = model
        else:
            assert embedder is not None
            assert class_count > 0
            self.tokenizer = embedder.tokenizer
            self.multi_label = multi_label
            self.create_model(embedder, class_count)

    def create_model(self, embedder, class_count):
        x = tf.keras.layers.GlobalAveragePooling1D()(embedder.sequence_output)
        x = tf.keras.layers.Dropout(0.2)(x)
        x = tf.keras.layers.Dense(128, activation='relu')(x)

        activation = 'sigmoid' if self.multi_label else 'softmax'
        output = tf.keras.layers.Dense(class_count, activation=activation)(x)

        self.model = tf.keras.models.Model(inputs=embedder.inputs, outputs=output)

    def train(self, table, class_column, batch_size, epochs, optimizer, progress_logger, fine_tune_bert = False, validation_table = None):
        ids, masks, segments = self.tokenizer.tokenize(table, progress_logger)
        y_train = np.array(list(table[class_column]))

        if(not fine_tune_bert):
            self.model.layers[3].trainable = False

        validation_data = None
        if(validation_table is not None):
            ids_val, masks_val, segments_val = self.tokenizer.tokenize(validation_table, None)
            y_val = np.array(list(validation_table[class_column]))
            validation_data = ([ids_val, masks_val, segments_val], y_val)

        loss = 'binary_crossentropy' if self.multi_label else 'categorical_crossentropy'

        self.model.compile(loss=loss,
                  optimizer=optimizer,
                  metrics=self.get_metrics())
        self.model.fit(x=[ids, masks, segments], y=y_train,epochs=epochs, batch_size=batch_size,
            shuffle=True, validation_data=validation_data, callbacks=[progress_logger])

    def get_metrics(self):
        if(self.multi_label):
            return [tf.keras.metrics.BinaryAccuracy('accuracy', dtype=tf.float32), tf.keras.metrics.AUC(name='AUC')]
        else:
            return ['accuracy']

    def save(self, path):
        self.tokenizer.save_to(self.model)
        self.model.save(path)
    
    def predict(self, table, batch_size, progress_logger):
        ids, masks, segments = self.tokenizer.tokenize(table, progress_logger)

        output = self.model.predict([ids, masks, segments],
            batch_size=batch_size, callbacks=[progress_logger])
        return output
    
    @classmethod
    def run_train(cls,
        input_table,
        bert_model_type_key,
        bert_model_handle,
        sentence_column,
        class_column,
        class_count,
        file_store,
        optimizer,
        cache_dir = None,
        max_seq_length = 128,
        batch_size = 20,
        epochs = 3,
        fine_tune_bert = False,
        validation_table = None,
        multi_label = False
    ):
        model_type = BertModelType.from_key(bert_model_type_key)
        embedder = BertEmbedder.from_pretrained(model_type, bert_model_handle, sentence_column, max_seq_length=max_seq_length, cache_dir=cache_dir)
        classifier = BertClassifier(embedder=embedder, class_count=class_count, multi_label=multi_label)
        progress_logger = ProgressCallback(len(input_table), train=True, batch_size=batch_size, epochs_count=epochs)

        classifier.train(input_table, class_column, batch_size, epochs, optimizer, progress_logger, fine_tune_bert, validation_table)
        classifier.save(file_store)

        output_table = pd.DataFrame(progress_logger.logs)
        return output_table

    @classmethod
    def run_predict(cls,
        input_table,
        sentence_column,
        file_store,
        bert_model_type_key,
        max_seq_length = 128,
        batch_size = 20
    ):
        model = tf.keras.models.load_model(file_store)
        model_type = BertModelType.from_key(bert_model_type_key)
        tokenizer = model_type.tokenizer_cls.from_saved_model(model, sentence_column, max_seq_length=max_seq_length)
        classifier = BertClassifier(tokenizer=tokenizer, model=model)
        progress_logger = ProgressCallback(len(input_table), predict=True, batch_size=batch_size)

        output = classifier.predict(input_table, batch_size, progress_logger)

        return pd.DataFrame(output, index=input_table.index)

import tensorflow as tf
import tensorflow_hub as hub
import numpy as np
import pandas as pd
from tensorflow.keras.utils import to_categorical

from BertTokenizer import BertTokenizer
from ProgressCallback import ProgressCallback

class BertClassifier:
    def __init__(self, tokenizer, bert_layer = None, class_count = 0, model = None):
        self.tokenizer = tokenizer

        if(bert_layer):
            self.create_model(bert_layer, class_count)
        elif(model):
            self.restore_model(model)
        else:
            raise ValueError("Either bert_layer or model parameter should be specified")

    def create_model(self, bert_layer, class_count):
        self.vocab_file = bert_layer.resolved_object.vocab_file
        self.do_lower_case = bert_layer.resolved_object.do_lower_case

        input_ids = tf.keras.layers.Input(shape=(self.tokenizer.max_seq_length,), dtype=tf.int32, name="input_ids")
        input_masks = tf.keras.layers.Input(shape=(self.tokenizer.max_seq_length,), dtype=tf.int32, name="input_masks")
        input_segments = tf.keras.layers.Input(shape=(self.tokenizer.max_seq_length,), dtype=tf.int32, name="input_segments")

        pooled_output, sequence_output = bert_layer([input_ids, input_masks, input_segments])
        x = tf.keras.layers.GlobalAveragePooling1D()(sequence_output)
        x = tf.keras.layers.Dropout(0.2)(x)
        x = tf.keras.layers.Dense(128, activation='relu')(x)
        output = tf.keras.layers.Dense(class_count, activation='softmax')(x)

        self.model = tf.keras.models.Model(inputs=[input_ids, input_masks, input_segments], outputs=output)
    
    def restore_model(self, model):
        self.model = model
        self.vocab_file = model.vocab_file
        self.do_lower_case = model.do_lower_case

        self.class_dict = {}
        for index, label in enumerate(model.class_dict.numpy()):
            self.class_dict[label.decode()] = index

    def train(self, table, class_column, batch_size, epochs, progress_logger):
        ids, masks, segments = self.tokenize(table, progress_logger)

        self.class_dict, y_train = self.classes_to_ids(table, class_column)

        for layer in self.model.layers[:-3]:
            layer.trainable = False

        self.model.compile(loss='categorical_crossentropy',
                  optimizer=tf.keras.optimizers.Adam(1e-3),
                  metrics=['accuracy'])
        self.model.fit(x=[ids, masks, segments],y=y_train,epochs=epochs, batch_size=batch_size, shuffle=False, callbacks=[progress_logger])
    
    def save(self, path):
        self.model.class_dict = tf.Variable(initial_value=list(self.class_dict.keys()),
            trainable=False, name="classes_dict")
        self.model.vocab_file = self.vocab_file
        self.model.do_lower_case = self.do_lower_case

        self.model.save(path)
    
    def predict(self, table, progress_logger):
        ids, masks, segments = self.tokenize(table, progress_logger)

        output = self.model.predict([ids, masks, segments],
            batch_size=32, callbacks=[progress_logger])
        return output


    def tokenize(self, table, progress_logger):
        ids, masks, segments = self.tokenizer.tokenize(table, progress_logger)
        ids = np.array(ids)
        masks = np.array(masks)
        segments = np.array(segments)
        return ids, masks, segments

    def classes_to_ids(self, table, class_column):
        class_dict = {}
        for index, label in enumerate(table[class_column].unique()):
            class_dict[label] = index

        one_hot = to_categorical(table[class_column].map(class_dict).values)    
        return class_dict, one_hot
    
    @classmethod
    def run_train(cls,
        input_table,
        bert_model_handle,
        sentence_column,
        class_column,
        class_count,
        file_store,
        max_seq_length = 128,
        second_sentence_column = None,
        batch_size = 20,
        epochs = 3
    ):
        bert_layer = hub.KerasLayer(bert_model_handle, trainable=True)
        tokenizer = BertTokenizer(bert_layer.resolved_object.vocab_file, bert_layer.resolved_object.do_lower_case,
            max_seq_length, sentence_column, second_sentence_column)
        classifier = BertClassifier(tokenizer=tokenizer, bert_layer=bert_layer, class_count=class_count)
        progress_logger = ProgressCallback(len(input_table), train=True, batch_size=batch_size, epochs_count=epochs)

        classifier.train(input_table, class_column, batch_size, epochs, progress_logger)
        classifier.save(file_store)
    
    @classmethod
    def run_predict(cls,
        input_table,
        sentence_column,
        file_store,
        max_seq_length = 128,
        second_sentence_column = None,
        batch_size = 20
    ):
        model = tf.keras.models.load_model(file_store)
        tokenizer = BertTokenizer(model.vocab_file, model.do_lower_case, max_seq_length,
            sentence_column, second_sentence_column)
        classifier = BertClassifier(tokenizer=tokenizer, model=model)
        progress_logger = ProgressCallback(len(input_table), predict=True, batch_size=batch_size)

        output = classifier.predict(input_table, progress_logger)

        output_table = pd.DataFrame(output, columns=classifier.class_dict.keys(), index=input_table.index)
        output_table = pd.concat([input_table, output_table], axis=1)
        return output_table

import tempfile
import tensorflow as tf
import numpy as np
import pandas as pd
from tensorflow.keras.utils import to_categorical
from transformers import TFAutoModel

from BertTokenizer import BertTokenizer, HFTokenizerWrap
from ProgressCallback import ProgressCallback
from bert_utils import load_bert_layer

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
        try:
            self.vocab_file = bert_layer.resolved_object.vocab_file
            self.do_lower_case = bert_layer.resolved_object.do_lower_case
        except AttributeError:
            # do nothing, HF model
            pass

        input_ids = tf.keras.layers.Input(shape=(self.tokenizer.max_seq_length,), dtype=tf.int32, name="input_ids")
        input_masks = tf.keras.layers.Input(shape=(self.tokenizer.max_seq_length,), dtype=tf.int32, name="input_masks")
        input_segments = tf.keras.layers.Input(shape=(self.tokenizer.max_seq_length,), dtype=tf.int32, name="input_segments")

        pooled_output, sequence_output = bert_layer([input_ids, input_masks, input_segments])
        if(pooled_output.shape.ndims == 3 and sequence_output.shape.ndims == 2):
            # For Hugging Face models
            pooled_output, sequence_output = sequence_output, pooled_output

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

    def train(self, table, class_column, batch_size, epochs, optimizer, progress_logger, fine_tune_bert = False, validation_table = None):
        ids, masks, segments = self.tokenize(table, progress_logger)

        self.class_dict, y_train = self.classes_to_ids(table, class_column)

        if(not fine_tune_bert):
            self.model.layers[3].trainable = False

        validation_data = None
        if(validation_table is not None):
            ids_val, masks_val, segments_val = self.tokenize(validation_table, None)
            y_val = to_categorical(validation_table[class_column].map(self.class_dict).values)
            validation_data = ([ids_val, masks_val, segments_val], y_val)

        self.model.compile(loss='categorical_crossentropy',
                  optimizer=optimizer,
                  metrics=['accuracy'])
        self.model.fit(x=[ids, masks, segments], y=y_train,epochs=epochs, batch_size=batch_size,
            shuffle=True, validation_data=validation_data, callbacks=[progress_logger])
    
    def save(self, path):
        self.model.class_dict = tf.Variable(initial_value=list(self.class_dict.keys()),
            trainable=False, name="classes_dict")
        self.model.vocab_file = self.vocab_file
        self.model.do_lower_case = self.do_lower_case

        self.model.save(path)
    
    def predict(self, table, batch_size, progress_logger):
        ids, masks, segments = self.tokenize(table, progress_logger)

        output = self.model.predict([ids, masks, segments],
            batch_size=batch_size, callbacks=[progress_logger])
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
        optimizer,
        tfhub_cache_dir = None,
        max_seq_length = 128,
        second_sentence_column = None,
        batch_size = 20,
        epochs = 3,
        fine_tune_bert = False,
        validation_table = None
    ):
        classifier = cls.create_classifier(bert_model_handle, tfhub_cache_dir, max_seq_length,
            sentence_column, second_sentence_column, class_count)
        progress_logger = ProgressCallback(len(input_table), train=True, batch_size=batch_size, epochs_count=epochs)

        classifier.train(input_table, class_column, batch_size, epochs, optimizer, progress_logger, fine_tune_bert, validation_table)
        classifier.save(file_store)

        output_table = pd.DataFrame(progress_logger.logs)
        return output_table
    
    @classmethod
    def create_classifier(cls, bert_model_handle, tfhub_cache_dir, max_seq_length,
            sentence_column, second_sentence_column, class_count):
        bert_layer = load_bert_layer(bert_model_handle, tfhub_cache_dir)
        tokenizer = BertTokenizer(bert_layer.resolved_object.vocab_file, bert_layer.resolved_object.do_lower_case,
            max_seq_length, sentence_column, second_sentence_column)
        return BertClassifier(tokenizer=tokenizer, bert_layer=bert_layer, class_count=class_count)
        

    @classmethod
    def run_predict(cls,
        input_table,
        sentence_column,
        file_store,
        max_seq_length = 128,
        second_sentence_column = None,
        batch_size = 20,
        prediction_column_name = 'Prediction',
        output_probabilities = True,
        probabilities_column_suffix = ''
    ):
        model = tf.keras.models.load_model(file_store)
        tokenizer = BertTokenizer(model.vocab_file, model.do_lower_case, max_seq_length,
            sentence_column, second_sentence_column)
        classifier = BertClassifier(tokenizer=tokenizer, model=model)
        progress_logger = ProgressCallback(len(input_table), predict=True, batch_size=batch_size)

        output = classifier.predict(input_table, batch_size, progress_logger)
        output_table = input_table.copy()

        class_by_index = {value: key for (key, value) in classifier.class_dict.items()}
        prediction = [class_by_index[idx] for idx in output.argmax(axis=1)]
        output_table[prediction_column_name] = prediction

        if(output_probabilities):
            columns = [f'P ({label}){probabilities_column_suffix}' for label in classifier.class_dict.keys()]
            probabilities = pd.DataFrame(output, columns=columns, index=output_table.index)
            output_table = pd.concat([output_table, probabilities], axis=1)

        return output_table

class HFBertClassifier(BertClassifier):

    def save(self, path):
        temp_dir = tempfile.TemporaryDirectory()
        vocab_files = self.tokenizer.tokenizer.save_vocabulary(temp_dir.name)

        self.model.vocab_file = tf.saved_model.Asset(vocab_files[0])
        self.model.class_dict = tf.Variable(initial_value=list(self.class_dict.keys()),
            trainable=False, name="classes_dict")
        self.model.do_lower_case = tf.Variable(initial_value=self.tokenizer.tokenizer.do_lower_case,
            trainable=False, name="do_lower_case")

        self.model.save(path)

    @classmethod
    def create_classifier(cls, bert_model_handle, cache_dir, max_seq_length,
            sentence_column, second_sentence_column, class_count):
        tokenizer = HFTokenizerWrap.create_tokenizer(bert_model_handle, cache_dir,
            sentence_column, second_sentence_column, max_seq_length)
        auto_model = TFAutoModel.from_pretrained(bert_model_handle, cache_dir=cache_dir)
        return HFBertClassifier(tokenizer=tokenizer, bert_layer=auto_model.layers[0], class_count=class_count)

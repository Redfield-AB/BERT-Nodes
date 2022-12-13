import tensorflow as tf
import pandas as pd
import numpy as np
import knime.scripting.io as knio
from transformers import TFAutoModelForSequenceClassification, AutoTokenizer
from ProgressCallback import ProgressCallback




class ZeroShotTextClassifier:

    def __init__(self , model=None, tokenizer=None, multi_label=False, hypothesis= "This example is {}", padding=True, truncation='only_first'):
        
        if(model):
            assert tokenizer is not None
            self.tokenizer = tokenizer
            self.model = model

        self.padding = padding
        self.truncation = truncation
        self.multi_label = multi_label
        self.hypothesis = hypothesis
        
        if hypothesis.format("TEST") == hypothesis:
            raise ValueError(("The provided hypothesis ‘{}‘ was not able to be formatted with candidate labels. "
                             "Make sure your hypothesis is including formatting syntax such as {{}}."
                             ).format(hypothesis))

    def predict(self, input_table, sentence_column, candidate_labels, batch_size):
        """
        Classify a given sentence without any previously labeled data.

        Args 
            input_table : A dataFrame contains at least one String column.
            sentence_column : A string column that contains text to be classified.
            candidate_labels : A list of labels to use for prediction.
        
        Return 
            output_table : A dataFrame that contains the senetence_column and the model predictions.
        """
        
        sequence_pairs = []
        sentence_column = input_table[sentence_column]
   
        if len(sentence_column) == 0 or len(candidate_labels) == 0:
            raise ValueError("You must provide at least one label and at least one sequence.")

        else : 
            if isinstance(sentence_column, str):
                self.sentence_column = [sentence_column]
            else:
                self.sentence_column = sentence_column

        sequences_length = len(self.sentence_column) 
        labels_length = len(candidate_labels)   


        for sequence in self.sentence_column:
            sequence_pairs.extend([[sequence, self.hypothesis.format(label)] for label in candidate_labels])

        progress_logger = ProgressCallback(len(sequence_pairs), predict=True, batch_size=batch_size)

        # Tokenization
        input_ids = self.tokenizer(sequence_pairs, return_tensors='tf', padding=self.padding, truncation=self.truncation)

        progress_logger.on_tokenize_rows_end(len(sequence_pairs))
        input_ids = [input_ids[name] for name in self.tokenizer.model_input_names]

        # Model logits
        logits = self.model.predict(input_ids, batch_size=batch_size, callbacks=[progress_logger]).logits
        reshaped_logits = logits.reshape((sequences_length, labels_length, -1))

        if (not self.multi_label) and (labels_length > 1):
            # softmax the "entailment" logits over all candidate labels.
            entailment = reshaped_logits[..., -1]
            predictions = tf.nn.softmax(entailment).numpy().astype('float64')
        else:
            # softmax over the entailment vs contradiction for each lable independently 
            entail_contra_logits = reshaped_logits[..., [0, -1]]
            probabilities = tf.nn.softmax(entail_contra_logits)
            predictions = probabilities[..., 1].numpy().astype('float64')

        output_table = pd.DataFrame(predictions, index=input_table.index)    
        return output_table     
        

    @classmethod
    def run_zstc(cls,
                 input_table, 
                 sentence_column, 
                 candidate_labels,  
                 hypothesis, 
                 bert_model_handle, 
                 bert_model_type_key,
                 cache_dir=None,
                 multi_label=False,
                 batch_size=20):
                 
        input_table = input_table.to_pandas()
        model = TFAutoModelForSequenceClassification.from_pretrained(bert_model_handle, cache_dir = cache_dir)
        tokenizer = AutoTokenizer.from_pretrained(bert_model_handle, cache_dir = cache_dir)
        classifier = ZeroShotTextClassifier(model, tokenizer, multi_label, hypothesis)
        output_table  = classifier.predict(input_table, sentence_column, candidate_labels, batch_size) 

        knio.output_tables[0] = knio.Table.from_pandas(output_table)

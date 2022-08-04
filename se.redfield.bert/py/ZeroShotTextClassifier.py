import tensorflow as tf
import pandas as pd
import numpy as np
import knime_io as knio
from transformers import TFAutoModelForSequenceClassification, AutoTokenizer




class ZeroShotTextClassifier:

    def __init__(self , model=None, tokenizer=None, multi_label=False,threshold=0.5, hypothesis= "This example is {}", append_probabilites=False, padding=True, truncation='only_first'):
        
        if(model):
            assert tokenizer is not None
            self.tokenizer = tokenizer
            self.model = model

        self.padding = padding
        self.truncation = truncation
        self.multi_label = multi_label
        self.threshold = threshold
        self.hypothesis = hypothesis
        self.append_probabilities = append_probabilites
        
        if self.multi_label and (0 >= self.threshold <= 1) : 
          raise ValueError("Threshold {} must be within the open interval ]0, 1[".format(self.threshold))
        
        if hypothesis.format("TEST") == hypothesis:
            raise ValueError(("The provided hypothesis ‘{}‘ was not able to be formatted with candidate labels. "
                             "Make sure your hypothesis is including formatting syntax such as {{}}."
                             ).format(hypothesis))


    def _parse_labels(self, labels):
      if isinstance(labels, str):
          labels = [label.strip() for label in labels.split(',')]
      return labels

    def add_probabilities_prefix(self, labels):
        return ["P({})".format(label) for label in labels]



    def predict(self, input_table, sentence_column, candidate_labels):
        """
        Classify a given sentence without any previously labeled data.

        Args 
            input_table : A dataFrame contains at least one String column.
            sentence_column : A string column that contains text to be classified.
            candidate_labels : A list of labels to use for prediction.
        
        Return 
            output_table : A dataFrame that contains the senetence_column and the model predictions.
        """
        
        data = []
        sequence_pairs = []
        sentence_column = input_table[sentence_column]
   
        if len(sentence_column) == 0 or len(candidate_labels) == 0:
            raise ValueError("You must provide at least one label and at least one sequence.")

        else : 
            if isinstance(sentence_column, str):
                self.sentence_column = [sentence_column]
            else:
                self.sentence_column = sentence_column

            self.candidate_labels = self._parse_labels(candidate_labels)

        probabilities_column = self.add_probabilities_prefix(self.candidate_labels)
        sequences_length = len(self.sentence_column) 
        labels_length = len(self.candidate_labels)   


        for sequence in self.sentence_column:
            sequence_pairs.extend([[sequence, self.hypothesis.format(label)] for label in self.candidate_labels])

        # Tokenization
        input_ids = self.tokenizer(sequence_pairs, return_tensors='tf', padding=self.padding, truncation=self.truncation)

        # Model logits
        logits = self.model(input_ids).logits
        reshaped_logits = logits.numpy().reshape((sequences_length, labels_length, -1))

        if (not self.multi_label) and (labels_length > 1):
            # softmax the "entailment" logits over all candidate labels.
            entailment = reshaped_logits[..., -1]
            predictions = tf.nn.softmax(entailment).numpy().astype('float64')
            for i in range(sequences_length):
                top_ids = list(reversed(predictions[i].argsort()))
                if(self.append_probabilities):
                    data.append([self.sentence_column[i], np.array(self.candidate_labels)[top_ids][0]] + list(predictions[i]))
                    columns = ['Sentence', 'Prediction'] + probabilities_column
                else:
                    data.append([self.sentence_column[i], np.array(self.candidate_labels)[top_ids][0]])
                    columns = ['Sentence', 'Prediction']
                


        else:
            # softmax over the entailment vs contradiction for each lable independently 
            entail_contra_logits = reshaped_logits[..., [0, -1]]
            probabilities = tf.nn.softmax(entail_contra_logits)
            entailment_probs = probabilities[..., 1].numpy().astype('float64')
            predictions = entailment_probs.copy()
            predictions[predictions >= self.threshold] = 1
            predictions[predictions < self.threshold] = 0
            predictions = predictions.astype('int32')
            
            for i in range(sequences_length):

                if(self.append_probabilities):
                    data.append([self.sentence_column[i]] + list(predictions[i]) + list(entailment_probs[i]))
                    columns = ["sentence"] + self.candidate_labels + probabilities_column
                else:
                    data.append([self.sentence_column[i]] + list(predictions[i]))
                    columns = ["sentence"] + self.candidate_labels 

        output_table = pd.DataFrame(data, columns=columns)    
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
                 threshold=None,
                 append_probabilities=True):
                 
        input_table = input_table.to_pandas()
        model = TFAutoModelForSequenceClassification.from_pretrained(bert_model_handle, cache_dir = cache_dir)
        tokenizer = AutoTokenizer.from_pretrained(bert_model_handle, cache_dir = cache_dir)
        classifier = ZeroShotTextClassifier(model, tokenizer, multi_label,threshold, hypothesis, append_probabilities)
        output_table  = classifier.predict(input_table, sentence_column, candidate_labels) 

        knio.output_tables[0] = knio.write_table(output_table)

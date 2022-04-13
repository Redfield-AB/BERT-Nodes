import tensorflow as tf
import pandas as pd
import numpy as np
from transformers import TFAutoModelForSequenceClassification, AutoTokenizer




class ZeroShotTextClassifier:

    def __init__(self , model=None, tokenizer=None, multi_label=False,threshold=None, hypothesis= "this example is {}", append_probabilites=False, padding=True, truncation='only_first'):
        
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

        if self.multi_label and (not self.threshold):
          self.threshold = 0.5
        
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

        probabilitiesColumn = self.add_probabilities_prefix(self.candidate_labels)
        self.nbrSequences = len(self.sentence_column)
        self.nbrLabels = len(self.candidate_labels)


        for sequence in self.sentence_column:
            sequence_pairs.extend([[sequence, self.hypothesis.format(label)] for label in self.candidate_labels])

        # Tokenization
        input_ids = self.tokenizer(sequence_pairs,return_tensors='tf', padding=self.padding, truncation=self.truncation)

        # Model logits
        logits = self.model(input_ids).logits

        if not self.multi_label:
            
            entail = logits[..., -1]
            predictions = tf.nn.softmax(entail)[:].numpy().reshape(self.nbrSequences, self.nbrLabels)
            for i in range(self.nbrSequences):
                top_ids = list(reversed(predictions[i].argsort()))
                if(self.append_probabilities):
                    data.append([self.sentence_column[i], np.array(self.candidate_labels)[top_ids][0]] + list(predictions[i]))
                    columns = ['Sentence', 'Prediction'] + probabilitiesColumn
                else:
                    data.append([self.sentence_column[i], np.array(self.candidate_labels)[top_ids][0]])
                    columns = ['Sentence', 'Prediction']
                outputTable = pd.DataFrame(data, columns=columns)


        else:
            entail_contra_logits = logits[:, 0::2]
            probs = tf.nn.softmax(entail_contra_logits)
            predictions = probs[:, 1].numpy().reshape(self.nbrSequences, self.nbrLabels)
            predictionsCopy = predictions.copy()

            predictionsCopy[predictionsCopy >= self.threshold] = 1
            predictionsCopy[predictionsCopy < self.threshold] = 0
            predictionsCopy = predictionsCopy.astype('int32')
            
            for i in range(self.nbrSequences):

                if(self.append_probabilities):
                    data.append([self.sentence_column[i]] + list(predictionsCopy[i]) + list(predictions[i]))
                    columns = ["sentence"] + self.candidate_labels + probabilitiesColumn
                else:
                    data.append([self.sentence_column[i]] + list(predictionsCopy[i]))
                    columns = ["sentence"] + self.candidate_labels 

                outputTable = pd.DataFrame(data, columns=columns)

        
            
        return outputTable     
        

    @classmethod
    def run_zstc(cls,
                 input_table, 
                 sentence_column, 
                 candidate_labels,  
                 bert_model_type_key,
                 hypothesis, 
                 bert_model_handel, 
                 cach_dir=None,
                 multi_label=False,
                 threshold=None,
                 append_probabilities=True):
                 
        
        model = TFAutoModelForSequenceClassification.from_pretrained(bert_model_handel, cache_dir = cach_dir)
        tokenizer = AutoTokenizer.from_pretrained(bert_model_handel, cache_dir = cach_dir)
        classifier = ZeroShotTextClassifier(model, tokenizer, multi_label,threshold, hypothesis, append_probabilities)
        outputTable  = classifier.predict(input_table, sentence_column, candidate_labels) 

        return outputTable
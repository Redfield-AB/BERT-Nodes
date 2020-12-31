import pandas as pd
import numpy as np
import tensorflow_hub as hub
import os
from transformers import TFAutoModel

def load_bert_layer(bert_model_handle, tfhub_cache_dir=None):
    if(tfhub_cache_dir):
        os.environ['TFHUB_CACHE_DIR'] = tfhub_cache_dir
    return hub.KerasLayer(bert_model_handle, trainable=True)

def load_hf_bert_layer(bert_model_handle, tfhub_cache_dir=None):
    auto_model = TFAutoModel.from_pretrained(bert_model_handle, cache_dir=tfhub_cache_dir)
    return auto_model.layers[0]

def compute_predictions(output_propabilities, classes, multi_label, threshold = None):
    if(multi_label or threshold is not None):
        return compute_predictions_with_threshold(output_propabilities, classes, threshold)
    else:
        return [classes[idx] for idx in output_propabilities.argmax(axis=1)]

def compute_predictions_with_threshold(output_propabilities, classes, threshold = None):
    if(threshold is None):
        threshold = 0.5
    classes = pd.Series(classes)

    return np.apply_along_axis(lambda x: compute_classes_for_row(x, classes, threshold), 1, output_propabilities)

def compute_classes_for_row(row, classes, threshold):
    return np.asarray(classes[row > threshold].str.cat(sep=','), dtype=object)


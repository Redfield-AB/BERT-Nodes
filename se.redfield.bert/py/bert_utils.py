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
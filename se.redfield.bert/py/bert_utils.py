import tensorflow_hub as hub
import os

def load_bert_layer(bert_model_handle, tfhub_cache_dir=None):
    if(tfhub_cache_dir):
        os.environ['TFHUB_CACHE_DIR'] = tfhub_cache_dir
    return hub.KerasLayer(bert_model_handle, trainable=True)
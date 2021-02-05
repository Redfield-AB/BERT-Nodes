import os
import tensorflow_hub as hub
from transformers import TFAutoModel

from BertTokenizer import BertTokenizer, HFTokenizerWrap

class BertModelType:
    def __init__(self, tokenizer_cls):
        self.tokenizer_cls = tokenizer_cls

    def load_bert_layer(self, bert_model_handle, cache_dir=None):
        raise NotImplementedError()
    
    @classmethod
    def from_key(cls, model_type_key):
        if(model_type_key == 'TFHUB'):
            return TFHubModel()
        elif(model_type_key == 'HUGGING_FACE'):
            return HuggingFaceModel()
        else:
            raise ValueError('Unknown model type: ', model_type_key)

class TFHubModel(BertModelType):
    def __init__(self):
        super().__init__(BertTokenizer)

    def load_bert_layer(self, bert_model_handle, cache_dir=None):
        if(cache_dir):
            os.environ['TFHUB_CACHE_DIR'] = cache_dir
        return hub.KerasLayer(bert_model_handle, trainable=True)

class HuggingFaceModel(BertModelType):
    def __init__(self):
        super().__init__(HFTokenizerWrap)

    def load_bert_layer(self, bert_model_handle, cache_dir=None):
        auto_model = TFAutoModel.from_pretrained(bert_model_handle, cache_dir=cache_dir)
        return auto_model.layers[0]

def load_bert_layer(bert_model_type_key, bert_model_handle, cache_dir=None):
    model_type = BertModelType.from_key(bert_model_type_key)
    return model_type.load_bert_layer(bert_model_handle, cache_dir)
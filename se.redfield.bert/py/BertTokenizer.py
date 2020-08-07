from bert.tokenization import bert_tokenization

class BertTokenizer:
    def __init__(self, bert_layer, max_seq_length, sentence_column, second_sentence_column = None):
        self.max_seq_length = max_seq_length
        self.sentence_column = sentence_column
        self.second_sentence_column = second_sentence_column

        vocab_file = bert_layer.resolved_object.vocab_file.asset_path.numpy() 
        do_lower_case = bert_layer.resolved_object.do_lower_case.numpy()
        self.tokenizer = bert_tokenization.FullTokenizer(vocab_file, do_lower_case)

    def tokenize(self, table):
        selector = [self.sentence_column]
        if(self.second_sentence_column):
            selector.append(self.second_sentence_column)

        input_ids, input_masks, input_segments = [], [], []

        total_count = len(table)
        current_count = 0

        for row in table[selector].values:
            ids,masks,segments = self.create_single_input(row)
 
            input_ids.append(ids)
            input_masks.append(masks)
            input_segments.append(segments)

            current_count += 1
            if(current_count % 100 == 0 or current_count == total_count):
                self.report_progress(total_count, current_count)   

        return input_ids, input_masks, input_segments

    def create_single_input(self, row):
        stokens = self.tokenizer.tokenize(row[0])
        stokens = stokens[:self.max_seq_length - 2]
        stokens = ["[CLS]"] + stokens + ["[SEP]"]

        if(row.size > 1):
            stokens2 = self.tokenizer.tokenize(row[1])
            trim_length = self.max_seq_length - len(stokens) - 1
            if trim_length > 0:
                stokens2 = stokens2[:trim_length] + ["[SEP]"]
                stokens = stokens + stokens2

        ids = self.get_ids(stokens)
        masks = self.get_masks(stokens)
        segments = self.get_segments(stokens)
 
        return ids, masks, segments


    def get_ids(self, tokens):
        """Token ids from Tokenizer vocab"""
        token_ids = self.tokenizer.convert_tokens_to_ids(tokens)
        input_ids = token_ids + [0] * (self.max_seq_length - len(token_ids))
        return input_ids

    def get_masks(self, tokens):
        return [1] * len(tokens) + [0] * (self.max_seq_length - len(tokens))

    def get_segments(self, tokens):
        """Segments: 0 for the first sequence, 1 for the second"""
        segments = []
        current_segment_id = 0
        for token in tokens:
            segments.append(current_segment_id)
            if token == "[SEP]":
                current_segment_id = 1
        return segments + [0] * (self.max_seq_length - len(tokens))

    def report_progress(self, total, done):
        print('progress:', 100 * done // total)

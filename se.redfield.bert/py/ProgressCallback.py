from tensorflow.keras.callbacks import Callback

class ProgressCallback(Callback):
    def __init__(self, total_count, predict=False, batch_size=0):
        super(ProgressCallback, self).__init__()
        self.total_count = total_count
        self.batch_size = batch_size

        if predict:
            self.tokenization_factor = 0.1
            self.prediction_factor = 0.9
        else:
            self.tokenization_factor = 1
            self.prediction_factor = 0

        self.tokenized_count = 0
        self.processed_batches = 0

    def on_tokenize_rows_end(self, rows):
        self.tokenized_count = rows
        self.report_progress()

    def on_predict_batch_end(self, batch, logs=None):
        self.processed_batches = batch + 1
        self.report_progress()

    def report_progress(self):
        if(self.processed_batches == 0):
            progress = self.tokenization_factor * self.tokenized_count / self.total_count
        else:
            prediction_progress = trim(self.batch_size * self.processed_batches / self.total_count)
            progress = self.tokenization_factor + self.prediction_factor * prediction_progress

        print('progress:', int(progress * 100))

def trim(fraction):
    if(fraction > 1):
        return 1
    if(fraction < 0):
        return 0
    return fraction

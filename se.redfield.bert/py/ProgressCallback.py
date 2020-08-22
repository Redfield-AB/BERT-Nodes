from tensorflow.keras.callbacks import Callback

class ProgressCallback(Callback):
    def __init__(self, total_count, predict=False, train=False, batch_size=0, epochs_count=1):
        super(ProgressCallback, self).__init__()
        self.total_count = total_count
        self.batch_size = batch_size
        self.epochs_count = epochs_count

        if predict:
            self.tokenization_factor = 0.1
            self.prediction_factor = 0.9
        elif train:
            self.tokenization_factor = 0.05
            self.prediction_factor = 0.95
        else:
            self.tokenization_factor = 1
            self.prediction_factor = 0

        self.tokenized_count = 0
        self.processed_batches = 0
        self.processed_epochs = 0
        self.last_progress = 0

    def on_tokenize_rows_end(self, rows):
        self.tokenized_count = rows
        self.report_progress()

    def on_predict_batch_end(self, batch, logs=None):
        self.processed_batches = batch + 1
        self.report_progress()

    def on_train_batch_end(self, batch, logs=None):
        self.on_predict_batch_end(batch, logs=logs)

    def on_epoch_end(self, epoch, logs=None):
        self.processed_epochs = epoch + 1
        self.processed_batches = 0
        self.report_progress()

    def report_progress(self):
        if(self.processed_batches == 0 and self.processed_epochs == 0):
            progress = self.tokenization_factor * self.tokenized_count / self.total_count
        else:
            epoch_progress = self.processed_epochs / self.epochs_count
            batch_progress = trim(self.batch_size * self.processed_batches / self.total_count)

            progress = self.tokenization_factor + self.prediction_factor * (epoch_progress + batch_progress / self.epochs_count)

        progress = int(trim(progress) * 100)
        if(self.last_progress < progress):
            self.last_progress = progress
            print('')
            print('progress:', progress)

def trim(fraction):
    if(fraction > 1):
        return 1
    if(fraction < 0):
        return 0
    return fraction

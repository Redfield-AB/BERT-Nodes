from tensorflow.keras.callbacks import Callback

class ProgressCallback(Callback):
    def __init__(self, total_count, batch_size):
        super(ProgressCallback, self).__init__()
        self.total_count = total_count
        self.batch_size = batch_size
        self.processed_batches = 0

    def on_predict_batch_end(self, batch, logs=None):
        self.processed_batches = batch + 1
        self.report_progress()

    def report_progress(self):
        progress = self.batch_size * self.processed_batches * 100 // self.total_count
        if(progress > 100):
            progress = 100
        print('progress:', progress)
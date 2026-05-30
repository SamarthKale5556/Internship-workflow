import queue
import sounddevice as sd
import json

from vosk import Model, KaldiRecognizer

class VoiceListener:

    def __init__(self):

        self.q = queue.Queue()

        self.model = Model("vosk-model-small-en-us-0.15")

        self.recognizer = KaldiRecognizer(
            self.model,
            16000
        )

    def callback(self, indata, frames, time, status):

        self.q.put(bytes(indata))

    def listen_for_yes(self):

        print("Listening for response...")

        with sd.RawInputStream(
            samplerate=16000,
            blocksize=8000,
            dtype='int16',
            channels=1,
            callback=self.callback
        ):

            while True:

                data = self.q.get()

                if self.recognizer.AcceptWaveform(data):

                    result = json.loads(
                        self.recognizer.Result()
                    )

                    text = result.get("text", "")

                    print("USER SAID:", text)

                    if "yes" in text:

                        return True

                    elif "no" in text:

                        return False
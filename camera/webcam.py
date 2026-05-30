import cv2
from config.settings import *

class WebcamStream:

    def __init__(self):

        self.cap = cv2.VideoCapture(CAMERA_INDEX)

        self.cap.set(cv2.CAP_PROP_FRAME_WIDTH, FRAME_WIDTH)
        self.cap.set(cv2.CAP_PROP_FRAME_HEIGHT, FRAME_HEIGHT)

    def read(self):

        ret, frame = self.cap.read()

        return ret, frame

    def release(self):

        self.cap.release()
CAMERA_INDEX = 0

FRAME_WIDTH = 640
FRAME_HEIGHT = 480

MODEL_PATH = "yolov8n.pt"

CONFIDENCE_THRESHOLD = 0.5


# Important objects only
PRIORITY = {

    "person": 1,

    "car": 1,
    "truck": 1,
    "bus": 1,

    "motorcycle": 2,
    "bicycle": 2,

    "chair": 3,
    "bench": 3
}
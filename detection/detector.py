from ultralytics import YOLO
from config.settings import *

class ObjectDetector:

    def __init__(self):

        self.model = YOLO(MODEL_PATH)

    def detect(self, frame):

        frame_height, frame_width, _ = frame.shape

        results = self.model(frame)

        detections = []

        for result in results:

            for box in result.boxes:

                confidence = float(box.conf[0])

                if confidence < CONFIDENCE_THRESHOLD:
                    continue

                class_id = int(box.cls[0])

                class_name = self.model.names[class_id]

                x1, y1, x2, y2 = map(int, box.xyxy[0])

                # Object center
                center_x = (x1 + x2) // 2

                # Direction Logic
                if center_x < frame_width // 3:

                    direction = "left"

                elif center_x < 2 * (frame_width // 3):

                    direction = "center"

                else:

                    direction = "right"

                # Box area
                box_area = (x2 - x1) * (y2 - y1)

                # DEBUG
                print("BOX AREA:", box_area)

                # Improved Distance Estimation
                if box_area > 70000:

                    distance = "very close"

                elif box_area > 30000:

                    distance = "close"

                else:

                    distance = "far"

                detections.append({
                    "name": class_name,
                    "confidence": confidence,
                    "bbox": (x1, y1, x2, y2),
                    "direction": direction,
                    "distance": distance
                })

        return detections
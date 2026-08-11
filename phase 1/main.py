import cv2

from audio.speaker import VoiceAssistant
from camera.webcam import WebcamStream
from detection.detector import ObjectDetector
from utils.fps import FPSCounter

speaker = VoiceAssistant()

camera = WebcamStream()

detector = ObjectDetector()

fps_counter = FPSCounter()

while True:

    ret, frame = camera.read()

    if not ret:
        break

    detections = detector.detect(frame)

    for detection in detections:

        x1, y1, x2, y2 = detection["bbox"]

        label = detection["name"]

        distance = detection["distance"]

        direction = detection["direction"]

        cv2.rectangle(
            frame,
            (x1, y1),
            (x2, y2),
            (0,255,0),
            2
        )

        text = f"{label} | {direction} | {distance}"

        cv2.putText(
            frame,
            text,
            (x1, y1 - 10),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.6,
            (0,255,0),
            2
        )

        # Only alert for very close objects
        if distance == "very close":

            # PERSON
            if label == "person":

                speaker.speak("person")

            # VEHICLES
            elif label in [
                "car",
                "bus",
                "truck",
                "motorcycle",
                "bicycle"
            ]:

                speaker.speak("vehicle")

            # OBSTACLES
            elif label in [
                "chair",
                "bench",
                "couch",
                "dining table"
            ]:

                speaker.speak("obstacle")

            # COMMON PATH OBJECTS
            elif label in [
                "backpack",
                "suitcase",
                "handbag"
            ]:

                speaker.speak("object")

    fps = fps_counter.get_fps()

    cv2.putText(
        frame,
        f"FPS: {fps}",
        (20,40),
        cv2.FONT_HERSHEY_SIMPLEX,
        1,
        (0,0,255),
        2
    )

    cv2.imshow("Blind Assist AI", frame)

    key = cv2.waitKey(1)

    if key == ord('q'):
        break

camera.release()

cv2.destroyAllWindows()
import torch
import cv2
import pyttsx3

# Initialize YOLOv5 model
model = torch.hub.load('ultralytics/yolov5', 'yolov5s', pretrained=True)

# Initialize camera
cap = cv2.VideoCapture(0)

# Initialize TTS engine
engine = pyttsx3.init()

while True:
    ret, frame = cap.read()
    if not ret:
        break

    # YOLOv5 expects RGB
    results = model(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))

    for det in results.xyxy[0]:
        xmin, ymin, xmax, ymax, conf, cls = det
        label = model.names[int(cls)]
        confidence = float(conf)

        # Only speak if confidence is high
        if confidence > 0.5:
            # Direction detection based on x-coordinate
            center_x = (xmin + xmax) / 2
            frame_width = frame.shape[1]

            if center_x < frame_width / 3:
                direction = "to your left"
            elif center_x > 2 * frame_width / 3:
                direction = "to your right"
            else:
                direction = "in front of you"

            # Distance estimation (based on object size in frame)
            pixel_height = ymax - ymin
            KNOWN_HEIGHT_CM = 170 if label == "person" else 30  # Approximate
            FOCAL_LENGTH = 650
            distance = int((KNOWN_HEIGHT_CM * FOCAL_LENGTH) / pixel_height)

            spoken_text = f"{label}, {distance} centimeters {direction}"
            print(spoken_text)

            engine.say(spoken_text)
            engine.runAndWait()

    cv2.imshow('YOLOv5 Detection', frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()

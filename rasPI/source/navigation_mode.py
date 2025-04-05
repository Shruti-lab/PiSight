import torch
import numpy as np
from picamzero import Camera
import os
import cv2
from time import sleep

def speak(text):
    print("Assistant:", text)
    os.system(f'espeak "{text}"')
    
# Initialize YOLOv5 model
speak("Initializing mode...")
model = torch.hub.load('ultralytics/yolov5', 'yolov5s', pretrained=True)
speak("Initialization complete. Navigation turned on")


# Initialize PiCameraZero
camera = Camera()
camera.start_preview()



while True:
    frame = camera.capture_array()
    frame = cv2.cvtColor(frame, cv2.COLOR_RGB2BGR)

    results = model(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))

    for det in results.xyxy[0]:
        xmin, ymin, xmax, ymax, conf, cls = det
        label = model.names[int(cls)]
        confidence = float(conf)

        if confidence > 0.5:
            center_x = (xmin + xmax) / 2
            frame_width = frame.shape[1]

            if center_x < frame_width / 3:
                direction = "to your left"
            elif center_x > 2 * frame_width / 3:
                direction = "to your right"
            else:
                direction = "in front of you"

            pixel_height = ymax - ymin
            KNOWN_HEIGHT_CM = 170 if label == "person" else 30
            FOCAL_LENGTH = 650
            distance = int((KNOWN_HEIGHT_CM * FOCAL_LENGTH) / pixel_height)

            spoken_text = f"{label}, {distance} centimeters {direction}"
            speak(spoken_text)

    # Comment this out if you don't want to display the frame
    # cv2.imshow("Camera", frame)
    # if cv2.waitKey(1) & 0xFF == ord('q'):
    #     break

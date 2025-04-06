import os
import cv2
import numpy as np
from picamzero import Camera
from inference_sdk import InferenceHTTPClient
from PIL import Image

# Initialize PiCamZero camera
camera = Camera()
camera.start_preview()

# Initialize Roboflow Inference Client
CLIENT = InferenceHTTPClient(
    api_url="https://detect.roboflow.com",
    api_key="sr42JDvQra7kcCDJUcn5"
)

def speak(text):
    print("Assistant:", text)
    os.system(f'espeak \"{text}\"')

def play_alarm():
    os.system("aplay beep-15.wav &")

try:
    while True:
        # Capture image from camera (RGB)
        frame = camera.capture_array()
        frame_width = frame.shape[1]

        # Convert NumPy array to PIL Image
        image = Image.fromarray(frame)

        # Run inference
        result = CLIENT.infer(image, model_id="weapon-detection-m7qso/1")

        for prediction in result["predictions"]:
            label = prediction["class"]
            x = prediction["x"]
            y = prediction["y"]
            width = prediction["width"]
            height = prediction["height"]
            confidence = prediction["confidence"]

            # Direction logic
            if x < frame_width / 3:
                direction = "to your left"
            elif x > 2 * frame_width / 3:
                direction = "to your right"
            else:
                direction = "in front of you"

            # Distance estimate
            KNOWN_HEIGHT_CM = 30
            FOCAL_LENGTH = 650
            distance = int((KNOWN_HEIGHT_CM * FOCAL_LENGTH) / height)

            message = f"{label} detected {distance} centimeters {direction}"

            # Sound & Speech
            play_alarm()
            speak(message)

except KeyboardInterrupt:
    print("Stopped by user.")
finally:
    camera.stop_preview()

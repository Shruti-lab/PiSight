import cv2
import requests
from picamzero import Camera
import numpy as np
import time

server_url = 'http://192.168.29.163:5050/process_image'

print("[INFO] Initializing camera...")
camera = Camera()
camera.start_preview()

print("[INFO] Starting capture loop... Press Ctrl+C to exit.")
try:
    while True:
        frame = camera.capture_array()
        frame_bgr = cv2.cvtColor(frame, cv2.COLOR_RGB2BGR)

        _, img_encoded = cv2.imencode('.jpg', frame_bgr)
        img_bytes = img_encoded.tobytes()

        try:
            print("[INFO] Sending frame to server...")
            response = requests.post(server_url, files={'image': img_bytes}, timeout=5000)
            response.raise_for_status()
            results = response.json()
            print("[RESULTS]", results if results else "No text detected.")
        except Exception as e:
            print("[ERROR] While sending frame:", e)

        time.sleep(1)  # 1-second delay between frames
except KeyboardInterrupt:
    print("\n[INFO] Stopping camera loop.")
finally:
    camera.stop_preview()

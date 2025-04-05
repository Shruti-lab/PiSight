import cv2
import easyocr
import pyttsx3
import time

# Initialize OCR reader and TTS engine
reader = easyocr.Reader(['en'])  # Add other language codes if needed
engine = pyttsx3.init()

# Initialize camera
cap = cv2.VideoCapture(0)
last_spoken = ""
last_time = 0

while True:
    ret, frame = cap.read()
    if not ret:
        print("Failed to capture frame.")
        break

    # Resize frame for faster processing
    resized = cv2.resize(frame, (640, 480))

    # Run OCR
    results = reader.readtext(resized)

    # Draw boxes and read out loud
    for (bbox, text, prob) in results:
        if prob > 0.5:
            # Only speak new text, avoid repeating
            current_time = time.time()
            if text != last_spoken and current_time - last_time > 3:
                print("Detected:", text)
                engine.say(text)
                engine.runAndWait()
                last_spoken = text
                last_time = current_time

            # Draw bounding box
            (top_left, top_right, bottom_right, bottom_left) = bbox
            top_left = tuple(map(int, top_left))
            bottom_right = tuple(map(int, bottom_right))
            cv2.rectangle(resized, top_left, bottom_right, (0, 255, 0), 2)
            cv2.putText(resized, text, top_left, cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)

    # Show camera feed
    cv2.imshow("Text Detection - EasyOCR", resized)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# Cleanup
cap.release()
cv2.destroyAllWindows()

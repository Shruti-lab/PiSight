import cv2
import easyocr

# Initialize EasyOCR reader
reader = easyocr.Reader(['en'], gpu=False)  # Set gpu=True if you have CUDA

# Open camera
cap = cv2.VideoCapture(0)  # Change to 'video.mp4' for video files

while True:
    ret, frame = cap.read()
    if not ret:
        break

    # Optional: Resize for faster processing
    resized = cv2.resize(frame, (640, 480))

    # Convert BGR to RGB
    rgb = cv2.cvtColor(resized, cv2.COLOR_BGR2RGB)

    # Perform OCR
    results = reader.readtext(rgb)

    # Display results
    for (bbox, text, prob) in results:
        (top_left, top_right, bottom_right, bottom_left) = bbox
        top_left = tuple(map(int, top_left))
        bottom_right = tuple(map(int, bottom_right))

        cv2.rectangle(resized, top_left, bottom_right, (0, 255, 0), 2)
        cv2.putText(resized, text, (top_left[0], top_left[1] - 10),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 255), 2)

    cv2.imshow("Text Detection - EasyOCR", resized)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()

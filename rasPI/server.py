from flask import Flask, request, jsonify
import easyocr
import cv2
import numpy as np

app = Flask(__name__)
reader = easyocr.Reader(['en'])  # Initialize the OCR reader

@app.route('/process_image', methods=['POST'])
def process_image():
    if 'image' not in request.files:
        return jsonify({'error': 'No image file'}), 400

    file = request.files['image']
    img_bytes = file.read()
    np_img = np.frombuffer(img_bytes, np.uint8)
    img = cv2.imdecode(np_img, cv2.IMREAD_COLOR)

    # Run OCR on the image
    results = reader.readtext(img)

    # Format the results
    output_text = ' '.join([text for (_, text, _) in results])
    return jsonify({'text': output_text})


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5050)
import subprocess
import os

def run_command(command, cwd=None):
    try:
        print(f"\n>>> Running: {command}")
        subprocess.run(command, shell=True, check=True, cwd=cwd)
    except subprocess.CalledProcessError as e:
        print(f"Error executing command: {e}")

def main():
    # Install PyTorch CPU-only
    run_command("pip install torch torchvision torchaudio --extra-index-url https://download.pytorch.org/whl/cpu")

    # Install OpenCV and pyttsx3
    run_command("pip install opencv-python pyttsx3")

    # Clone YOLOv5 repo if not already cloned
    if not os.path.exists("yolov5"):
        run_command("git clone https://github.com/ultralytics/yolov5")

    # Install YOLOv5 requirements
    yolov5_dir = os.path.join(os.getcwd(), "yolov5")
    run_command("pip install -r requirements.txt", cwd=yolov5_dir)

    print("\n✅ Environment setup complete!")

if __name__ == "__main__":
    main()

<div align="center">
  <h1><strong>PiSIGHT</strong> 👓✨</h1>
  <em>Team Hershey's</em> 👩🏻‍💻| Built at CodHer'25, ACM-CEG
  <p>Wearable and Voice-Controlled Assistive Glasses, designed to support individuals with visual impairments</p>
</div>

## 🔍 **Problem Statement**

Millions of individuals with visual impairments face challenges in navigating their surroundings, recognizing obstacles, reading text, and responding to emergencies independently. Existing assistive technologies are often bulky, expensive, or lack real-time feedback and intelligent interaction. There is a need for an affordable, lightweight, and intelligent wearable solution that can **capture the environment, interpret visual data, detect hazards, and respond to voice commands** — all in real-time, without relying on internet connectivity. And that is exactly what we are implementing through PiSIGHT!

**Track - Automation** | [Project Demo↗️](https://youtu.be/xeHEWLYYxPo)

## 🌟 **Key Features of our Solution**

- 👁️ **Live Environment Capture:** Real-time video feed from Raspberry Pi 4 camera mounted on wearable glasses, adding portability to our solution.
- 📖 **Text-to-Speech OCR:** Converts captured text (like signs, menus, labels) into speech using Optical Character Recognition, transmitted via bluetooth.
- 🧭 **Surrounding Object Detection:** Detects nearby objects (like people, vehicles, furniture) and announces their position and distance.
- 🚨 **Emergency Mode:** Automatically triggers alerts and alarms if weapons or threats are detected in the vicinity. In case of threat activity, a message alert will be sent to the user's emergency contact through a mobile app.
- 🎙️ **Voice-Activated Commands:** Users can activate navigation, read text, or trigger emergency modes using simple voice commands.

## ⚒️ **System Architecture & Implementation**

<table align="center">
  <tr>
    <th>Architecture</th>
    <th>Hardware Implementation</th>
    <th>App UI</th>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/aee16bb0-9434-4d38-af6e-e40ec942a5ef" width="300"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/753392ae-15ac-47b4-a96f-a78fa33dd3b9" alt="Hardware Implementation" height="300">
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/2c204e5b-17c5-410d-96d2-21a82e303030" alt="App UI" height="300">
    </td>
  </tr>
</table>

## 🧰 **Tech Stack**

| Layer             | Tools / Technologies Used                                    |
|------------------|--------------------------------------------------------------|
| **Hardware**      | Raspberry Pi 4, Pi Camera Module v2, Bone Conduction Speakers |
| **Programming**   | Python, Shell Scripts                                        |
| **Computer Vision** | OpenCV, Roboflow Inference API, OCR (Tesseract)             |
| **Speech & Audio**| eSpeak (Text-to-Speech), SpeechRecognition Library           |
| **Edge Computing**| On-device inference using YOLOv5 for object & weapon detection |
| **Voice Commands**| Google Speech Recognition API                                |
| **Interface**     | Minimal CLI + Audio feedback system                          |


## 💻 **Developer Setup**

#### 🔧 PREREQUISITES 
- Set up your **Raspberry Pi 4** using [this official guide](https://www.raspberrypi.com/documentation/computers/getting-started.html)  
- Connect and configure the **Camera Module v2** using [this tutorial](https://projects.raspberrypi.org/en/projects/getting-started-with-picamera/2)

  
1. **On your Raspberry Pi OS, clone the repository**  
   ```bash
   git clone https://github.com/unmani-shinde/PiSight
   ```

2. **Navigate to the project directory**  
   ```bash
   cd PiSight/rasPI
   ```

3. **Install all required dependencies**  
   ```bash
   python setup_env.py
   ```

That’s it! Your PiSIGHT environment is ready to explore and build on.
PS. If there are any installation errors, try creating a new virtual environment using `pip` as follows: 
```bash
python3 -m venv venv
source venv/bin/activate  # On Windows use: venv\Scripts\activate
pip install -r requirements.txt
```



## 👩🏻‍💻**Contributors**
1. [Unmani Shinde](https://github.com/unmani-shinde)
2. [Shruti Patil](https://github.com/Shruti-lab)
3. [Narayani Bokde](https://github.com/unmani-shinde)



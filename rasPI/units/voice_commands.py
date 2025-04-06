import speech_recognition as sr
import pyttsx3
import subprocess
import time

# Initialize recognizer and TTS
r = sr.Recognizer()
engine = pyttsx3.init()

def speak(text):
    """Speak the given text using TTS engine."""
    print("Assistant:", text)
    engine.say(text)
    engine.runAndWait()

def listen_command():
    """Listen to user command."""
    with sr.Microphone() as source:
        audio = r.listen(source)
        try:
            command = r.recognize_google(audio).lower()
            print("You said:", command)
            return command
        except sr.UnknownValueError:
            # Check if neither mode is active
            if not nav_mode_active and not reader_mode_active:
                speak("Sorry, I didn't understand that.")
        except sr.RequestError:
            speak("Speech service is down.")
        return ""

def run_script(script_name, mode):
    """Run a script corresponding to the mode."""
    try:
        proc = subprocess.Popen(["python", script_name])
        return proc
    except Exception as e:
        speak(f"Error running {script_name}: {str(e)}")
        return None

def stop_process(proc, mode_name):
    """Stop the process for a given mode."""
    if proc and proc.poll() is None:
        proc.terminate()
        speak(f"{mode_name} mode disabled.")
        time.sleep(1)
    else:
        speak(f"{mode_name} mode is not currently running.")

if __name__ == "__main__":
    nav_proc = None
    reader_proc = None
    nav_mode_active = False
    reader_mode_active = False

    speak("Listening for a command...")

    while True:
        command = listen_command()

        # Exit condition
        if "exit" in command or "stop listening" in command:
            speak("Goodbye!")
            stop_process(nav_proc, "Navigation")
            stop_process(reader_proc, "Reader")
            break

        # Activate navigation
        elif "activate navigation" in command or "switch to navigation" in command:
            if nav_mode_active:
                # No speech if mode is already active
                pass
            else:
                if reader_mode_active:
                    speak("Switching from reader to navigation mode.")
                    stop_process(reader_proc, "Reader")
                    reader_mode_active = False

                nav_proc = run_script("source/navigation_mode.py", "Navigation")
                nav_mode_active = True
                speak("Navigation mode activated.")  # Speak only when mode is actually activated

        # Disable navigation
        elif "disable navigation" in command:
            if nav_mode_active:
                stop_process(nav_proc, "Navigation")
                nav_mode_active = False
            else:
                pass  # No speech if navigation is not active

        # Activate reader
        elif "activate reader" in command or "switch to reader" in command:
            if reader_mode_active:
                # No speech if mode is already active
                pass
            else:
                if nav_mode_active:
                    speak("Switching from navigation to reader mode.")
                    stop_process(nav_proc, "Navigation")
                    nav_mode_active = False

                reader_proc = run_script("source/reader_mode.py", "Reader")
                reader_mode_active = True
                speak("Reader mode activated.")  # Speak only when mode is actually activated

        # Disable reader
        elif "disable reader" in command:
            if reader_mode_active:
                stop_process(reader_proc, "Reader")
                reader_mode_active = False
            else:
                pass  # No speech if reader is not active

import os
import time
import subprocess
from flask import Flask, jsonify, send_file
from gpiozero import Button
from threading import Timer, Lock

app = Flask(__name__)
start_time = time.time()

# ==========================================
# HARDWARE CONFIGURATION
# ==========================================
# Make sure your button is connected between GPIO 17 (Pin 11) and GND (Pin 6 or 9)
BUTTON_PIN = 17

# ==========================================
# STATE MANAGEMENT
# ==========================================
events_queue = []
events_lock = Lock()
last_alert = None

# ==========================================
# PUSH BUTTON LOGIC (Single vs Double Click)
# ==========================================
press_count = 0
click_timer = None
is_long_press = False

def evaluate_clicks():
    """Evaluates how many times the button was pressed within the time window."""
    global press_count
    with events_lock:
        if press_count == 1:
            events_queue.append("SINGLE_CLICK")
            print("[BUTTON] Action: SINGLE_CLICK detected -> Queued for Android")
        elif press_count >= 2:
            events_queue.append("DOUBLE_CLICK")
            print("[BUTTON] Action: DOUBLE_CLICK detected -> Queued for Android")
    
    # Reset count for the next interaction
    press_count = 0

def button_pressed():
    global click_timer
    # Cancel the previous timer if they click again quickly
    if click_timer is not None:
        click_timer.cancel()
        click_timer = None

def button_held():
    global is_long_press, press_count, click_timer
    is_long_press = True
    press_count = 0 # Reset any orphaned clicks
    if click_timer is not None:
        click_timer.cancel()
        click_timer = None
    with events_lock:
        events_queue.append("LONG_PRESS")
        print("[BUTTON] Action: LONG_PRESS (3 sec) detected -> Queued for Android")

def button_released():
    """Triggered every time the button is physically released."""
    global press_count, click_timer, is_long_press
    
    if is_long_press:
        # We just released from a long hold, do NOT count it as a short click
        is_long_press = False
        return
        
    press_count += 1
    
    # Wait 400 milliseconds to see if a second click happens
    click_timer = Timer(0.4, evaluate_clicks)
    click_timer.start()

# Initialize the hardware button safely
try:
    # pull_up=False turns off the internal pull-up and turns ON the internal pull-down resistor.
    # Since your button module has a VCC pin, it likely outputs HIGH (3.3V) when pressed.
    # bounce_time=0.05 prevents ghost/double reads from a single physical click.
    button = Button(BUTTON_PIN, pull_up=False, bounce_time=0.05, hold_time=3.0)
    button.when_pressed = button_pressed
    button.when_held = button_held
    button.when_released = button_released
    print(f"[SYSTEM] Hardware button successfully initialized on GPIO {BUTTON_PIN} (Active High)")
except Exception as e:
    print(f"[WARNING] Could not initialize button on GPIO {BUTTON_PIN}. Is it wired correctly? Error: {e}")


# ==========================================
# API ENDPOINTS FOR ANDROID APP
# ==========================================

@app.route('/api/v1/status', methods=['GET'])
def get_status():
    """
    Android pings this every 2 seconds. We send the events and immediately clear them.
    This GUARANTEES the Android app will never get stuck in an infinite loop!
    """
    global events_queue
    with events_lock:
        current_events = list(events_queue)
        events_queue.clear() # CRITICAL: Clear after reading

    return jsonify({
        "batteryPercent": 100,
        "batteryVoltage": 5.0,
        "estimatedRuntimeMinutes": 120,
        "cpuTemperature": 45.0,
        "uptimeSeconds": int(time.time() - start_time),
        "cameraStatus": "ACTIVE",
        "tofStatus": "ACTIVE",
        "earbudStatus": "ACTIVE",
        "aiEngineStatus": "ACTIVE",
        "faceRecognitionStatus": "ACTIVE",
        "inferenceFps": 30.0,
        "firmwareVersion": "1.0",
        "lastAlert": last_alert,
        "events": current_events,
        "tofDistanceMm": 0.0
    })

@app.route('/api/v1/camera/snapshot', methods=['GET'])
def get_snapshot():
    """Captures an image and sends it to the Android app for Gemini AI processing."""
    image_path = "/tmp/snapshot.jpg"
    try:
        print("[CAMERA] Taking snapshot...")
        # Uses standard Bookworm rpicam-jpeg. Fast capture, no preview window.
        subprocess.run(
            ["rpicam-jpeg", "-o", image_path, "-t", "1", "--width", "800", "--height", "600", "--nopreview"],
            check=True,
            timeout=5
        )
        return send_file(image_path, mimetype='image/jpeg')
    except Exception as e:
        print(f"[CAMERA ERROR] {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/api/v1/trigger/ai', methods=['POST'])
def trigger_ai():
    """Starts recording audio for the AI Assistant query."""
    audio_path = "/tmp/recording.wav"
    try:
        print("[AUDIO] Recording user query for 5 seconds...")
        # Records 5 seconds of audio using ALSA (hw:0,0 may need adjustment based on your mic)
        subprocess.Popen(["arecord", "-D", "hw:0,0", "-d", "5", "-f", "cd", audio_path])
        return jsonify({"status": "recording_started"})
    except Exception as e:
        print(f"[AUDIO ERROR] {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/api/v1/camera/audio', methods=['GET'])
def get_audio():
    """Android requests the recorded audio to send to Gemini."""
    audio_path = "/tmp/recording.wav"
    if os.path.exists(audio_path):
        print("[AUDIO] Sending recording to Android")
        return send_file(audio_path, mimetype='audio/wav')
    return jsonify({"error": "No audio found"}), 404

if __name__ == '__main__':
    print("[SYSTEM] Starting Blind Assist Server on Port 5000...")
    # host='0.0.0.0' allows the Android phone to connect over the Hotspot
    app.run(host='0.0.0.0', port=5000, debug=False)

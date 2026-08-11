<div align="center">
  <img src="https://img.icons8.com/color/96/000000/vision.png" alt="Logo">
  <h1>Blind Assist Companion</h1>
  <p><strong>A smart, wearable AI companion for the visually impaired using Android, Raspberry Pi, and Gemini 1.5 Flash.</strong></p>

  <p>
    <img src="https://img.shields.io/badge/Kotlin-1.9.0-blue.svg?logo=kotlin" alt="Kotlin">
    <img src="https://img.shields.io/badge/Android-Jetpack%20Compose-4CAF50?logo=android" alt="Jetpack Compose">
    <img src="https://img.shields.io/badge/Hardware-Raspberry%20Pi%20Zero%202W-C51A4A?logo=raspberrypi" alt="Raspberry Pi">
    <img src="https://img.shields.io/badge/AI-Google%20Gemini%201.5-FFD700?logo=google" alt="Gemini AI">
  </p>
</div>

---

## 🌟 Overview

Blind Assist Companion is a full-stack assistive technology solution designed to help visually impaired individuals navigate their surroundings and interact with the world using advanced AI. 

The system consists of a wearable hardware module (Raspberry Pi Zero 2W + Camera + Earbuds) and a modern Android companion app. The user can interact with the environment via a physical push button, triggering real-time scene descriptions and a conversational AI assistant powered by Google's Gemini 1.5 Flash multimodal AI.

## ✨ Key Features

* **👁️ Real-time Scene Description:** Single-click the wearable button to get an instant, concise description of the immediate surroundings (obstacles, people, entrances).
* **🎙️ Conversational AI Assistant:** Double-click to record an audio query. The system sends the photo and your voice request to Gemini 1.5 Flash, providing contextual answers (e.g., "What color is the shirt in front of me?").
* **🗣️ Natural Voice Feedback:** Integrates `gTTS` (Google Text-to-Speech) on the hardware module for smooth, human-like voice responses through connected Bluetooth earbuds.
* **📱 Modern Android Dashboard:** Built with Jetpack Compose, featuring a live telemetry dashboard monitoring the Raspberry Pi's battery, CPU temperature, and component health.
* **⚡ Seamless Connectivity:** Operates entirely over a local mobile hotspot, ensuring low-latency communication between the Android phone and the Raspberry Pi without requiring external Wi-Fi networks.

## 🏗️ System Architecture

1. **Wearable Unit (Raspberry Pi Zero 2W):**
   * Runs a lightweight Python Flask server.
   * Manages hardware inputs (GPIO push button) and sensors.
   * Handles audio recording (ALSA) and playback (`mpg123`).
   * Captures high-res images via the Pi Camera module.
2. **Companion App (Android):**
   * Built entirely in Kotlin using MVVM + Clean Architecture.
   * Asynchronously polls the Pi for hardware telemetry.
   * Orchestrates the complex AI flows, bridging the gap between the Pi's hardware data and Google's Gemini API.

## 🚀 Getting Started

### 1. Hardware Setup
* Raspberry Pi Zero 2W running Raspberry Pi OS (Bookworm or later).
* Connect a physical push button to GPIO Pin 17.
* Pair your Bluetooth earbuds to the Raspberry Pi via `bluetoothctl`.

### 2. Raspberry Pi Configuration
SSH into your Raspberry Pi and install the required dependencies:
```bash
sudo apt update
sudo apt install python3-gpiozero mpg123
pip3 install gTTS Flask
```
Run the Python backend server:
```bash
python3 server.py
```

### 3. Android App Setup
* Open the project in **Android Studio**.
* Add your Gemini API Key in `GeminiGenerativeAiRepositoryImpl.kt`.
* Connect your Android phone to the same Wi-Fi network (or connect the Pi to your phone's hotspot).
* Update the `piCameraUrl` IP address in `PiCameraClientImpl.kt` to match your Raspberry Pi's local IP.
* Build and run the app!

## 🛠️ Tech Stack
* **Android:** Kotlin, Jetpack Compose, Coroutines, StateFlow, Hilt (Dependency Injection), Kotlinx Serialization.
* **Hardware Backend:** Python, Flask, `gpiozero`, `rpicam-jpeg`, `gTTS`, `arecord`.
* **AI Engine:** Google AI client SDK (Gemini 1.5 Flash).

---
<div align="center">
  <i>Designed with accessibility and speed in mind.</i>
</div>
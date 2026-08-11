# 👁️ Blind Assist Companion
> **A Smart Gen-AI Assistive System Integrating Mobile & Edge-Computing for Visually Impaired Empowerment.**

---

## 🚀 Overview

The **Blind Assist Companion** (developed under **NeuroEdge**) is a cutting-edge assistive technology designed to enhance the autonomy, safety, and daily navigation of visually impaired individuals. By combining **local edge processing**, **real-time computer vision**, **local face recognition pipelines**, and state-of-the-art **Generative AI** (`gemini-2.5-flash-lite`), this companion system acts as an intuitive, high-performance visual-to-audio interpreter.

---

## 📊 System Performance & Technical Specifications

These metrics represent actual benchmarks measured and tested during the system validation phase:

| Parameter / Metric | Measured & Tested Result |
| :--- | :--- |
| **Object Detection Frame Rate (Host PC)** | `~30 FPS` |
| **Object Detection Frame Rate (Pi Zero 2 W)** | `~15 FPS` |
| **Object Detection Confidence Threshold** | `>50%` (Configured at `0.5` threshold) |
| **Scene Description Latency (Cloud Inference)** | `~1.2 seconds` (using `gemini-2.5-flash-lite`) |
| **BLE Communication Latency** | `~20 ms` |
| **Camera Feed Resolution (Phase 1)** | `640 × 480` |
| **Camera Snapshot Resolution (Phase 2)** | `800 × 600` (Optimized for size and model input) |
| **Edge Hardware Controller** | `Raspberry Pi Zero 2 W` (Quad-core 1.0GHz CPU, 512MB RAM) |
| **Camera Module** | `CSI OV5647 Camera` |
| **Distance Ranging Sensor** | `VL53L1X Time-of-Flight (ToF)` |

---

## 📅 Project Timeline & Key Milestones

| Timeline | Phase | Core Accomplishments & Deliverables |
| :--- | :--- | :--- |
| **04/05/2026 – 31/05/2026** | **Phase 1: Core Edge Vision & Voice Engine** | 🎙️ **Offline Voice Commands**: Built command detection with Vosk voice listener (`vosk-model-small-en-us-0.15`).<br>📷 **Local Object Detection**: Connected OpenCV feed to a lightweight local detector (`yolov8n.pt`).<br>🔊 **Speech & Configs**: Implemented spatial TTS audio reports and configurable threshold parameters. |
| **01/06/2026 – 30/06/2026** | **Phase 2: Mobile App & Cloud-Edge Integration** | 📱 **Jetpack Compose App**: Developed native app with dark mode glassmorphism UI.<br>🧠 **Gen-AI Scene Analytics**: Integrated `gemini-2.5-flash-lite` for multimodal visual interpretation.<br>👤 **Offline Face Indexing**: Created local Room DB face embedding storage & validation.<br>🔌 **Pi Telemetry**: Integrated real-time BLE diagnostics connection for Pi sensors.<br>🚨 **Emergency SOS System**: Integrated live GPS location sharing, SMS, WhatsApp, and emergency calling. |

---

## 🏗️ System Architecture

The project operates on a hybrid Edge-to-Mobile architecture, splitting computational loads between local real-time sensing and deep cloud visual parsing:

```
┌──────────────────────────────────────┐          Request Snapshot          ┌──────────────────────────────────────┐
│       RASPBERRY PI ZERO 2 W          │ ─────────────────────────────────> │         ANDROID MOBILE APP           │
│         (Edge Hardware Host)         │ <───────────────────────────────── │         (Client Application)         │
│                                      │            Image Bytes             │                                      │
│  ┌────────────────────────────────┐  │                                    │  ┌────────────────────────────────┐  │
│  │       CSI OV5647 Camera        │  │                                    │  │       Compose UI / Views       │  │
│  └────────────────────────────────┘  │                                    │  └────────────────────────────────┘  │
│                  │                   │                                    │                  │                   │
│                  ▼                   │         Bluetooth (BLE)            │                  ▼                   │
│  ┌────────────────────────────────┐  │ <================================> │  ┌────────────────────────────────┐  │
│  │        Flask Web Server        │  │         Sensor Telemetry           │  │        `BLEManager`            │  │
│  └────────────────────────────────┘  │                                    │  └────────────────────────────────┘  │
│                  ▲                   │                                    │                  │                   │
│                  │                   │                                    │                  ▼                   │
│  ┌────────────────────────────────┐  │                                    │  ┌────────────────────────────────┐  │
│  │     Python Control Thread      │  │                                    │  │  `gemini-2.5-flash-lite` Cloud │  │
│  └────────────────────────────────┘  │                                    │  └────────────────────────────────┘  │
│         ▲                 ▲          │                                    │                  │                   │
│         │                 │          │                                    │                  ▼                   │
│  ┌────────────┐     ┌────────────┐   │                                    │  ┌────────────────────────────────┐  │
│  │ VL53L1X ToF│     │ GPIO Alert │   │                                    │  │  TTS Engine (Sarvam / Native)  │  │
│  │   Sensor   │     │   Button   │   │                                    │  └────────────────────────────────┘  │
│  └────────────┘     └────────────┘   │                                    │                                      │
└──────────────────────────────────────┘                                    └──────────────────────────────────────┘
```

---

## 📸 Project Interface & Screenshots

Here is a look at the interactive dashboard and key user screens of the Blind Assist Companion:

| Device Status Diagnostics | Emergency SOS Panel | Add Family Member Screen |
| :---: | :---: | :---: |
| ![Device Status](project%20images/device_status.jpeg) | ![Emergency SOS](project%20images/emergency_sos.jpeg) | ![Add Family Member](project%20images/add_family_member.jpeg) |
| *Real-time specifications, Wi-Fi/Bluetooth telemetry, and sensor connectivity of the Raspberry Pi Zero 2 W companion.* | *Quick action triggers for emergency calling, live GPS coordinate sharing, and automated WhatsApp/SMS notifications.* | *Accessibility-friendly registration panel to log family member names, relationships, and emergency priorities before scanning face embeddings.* |

<br>

| Family & Friends Directory | NeuroEdge Companion Dashboard |
| :---: | :---: |
| ![Family List](project%20images/family_list.jpeg) | ![Dashboard](project%20images/dashboard.jpeg) |
| *Directory listing enrolled family members, managing facial recognition priorities, and offline sync tags.* | *Primary control hub showing quick actions for AI Vision, SOS, Device Diagnostics, and development team details.* |

---

## 🛠️ Technology Stack

* **Mobile App Frontend**: Kotlin, Jetpack Compose, Coroutines, Flow, Hilt (Dependency Injection)
* **Local Storage & Database**: Room DB (SQLite) for storing family members' metadata and face embeddings
* **Machine Learning & AI**: 
  * Google Gemini API (`gemini-2.5-flash-lite`) for multimodal visual explanation
  * FaceNet/MobileNet Pipeline for local face embedding computations
  * Vosk Speech SDK for local, lightweight voice commands
* **Hardware Integration**: Raspberry Pi Zero 2 W, CSI OV5647 Camera, VL53L1X ToF Sensor, Bluetooth Low Energy (BLE)
* **APIs**: Sarvam TTS, Android Location (GPS), Telephony SMS, Twilio / WhatsApp URL Scheme

---

## 👨‍💻 Development Team

This project was built with passion by **NeuroEdge**:
1. **Samarth Kale** (Team Leader & Edge AI Engineer)
2. **Shrushti Shinde** (Hardware & System Integration Engineer)
3. **Shrikant Kudale** (Product Design & Software Quality Engineer)

---

## 📬 Contact the Author

👨‍💻 **Author**: Samarth Kale  
📧 **Email**: [samarthkale1098@example.com](mailto:samarthkale1098@example.com)  
💼 **LinkedIn**: [LinkedIn profile](https://www.linkedin.com/in/samarthkale5556)  
🌐 **Portfolio**: [samarthkale.dev](https://samarthkale.dev)  
🐙 **GitHub**: [@SamarthKale5556](https://github.com/SamarthKale5556)

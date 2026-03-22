<div align="center">

# 🧠 NeuroPocket
**Your Personal, Private, and Powerful AI Assistant—Right in Your Pocket.**

[![Kotlin](https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Gemma 2B](https://img.shields.io/badge/LLM-Gemma_2B-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://ai.google.dev/gemma)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.style=for-the-badge)](https://opensource.org/licenses/MIT)

*100% Offline. Zero Cloud. Total Privacy.*

[Features](#-features) • [UI Showcase](#-ui-showcase) • [Installation](#%EF%B8%8F-installation--setup) • [Tech Stack](#%EF%B8%8F-tech-stack) • [How It Works](#-how-it-works)
</div>

---

## 📖 Introduction
**NeuroPocket** is a native Android application that brings the power of Large Language Models directly to your smartphone without relying on an internet connection. Powered by Google's **Gemma 2B** model and built entirely in **Kotlin**, NeuroPocket ensures that your conversations, data, and thoughts remain strictly on your device. 

Whether you need coding help, creative writing, or just a virtual brainstorming partner, NeuroPocket is always available—even in airplane mode.

---

## 📱 UI Showcase

<p align="center">
  <img src="https://github.com/user-attachments/assets/d7720f77-365d-4c79-8f44-46a7e5dd16ce" width="30%" alt="Home / Chat Screen" />
    <img src="https://github.com/user-attachments/assets/12480d96-88b2-4749-b632-8c9c7ad69b81" width="30%" alt="History / History" />
        <img src="https://github.com/user-attachments/assets/fe9ada39-5da8-40e1-adde-84889d3acc48" width="30%" alt="Chat / Live Chat" />

    

</p>

---

## ✨ Features
* 🚫 **100% Offline Capability:** Run complex AI inference entirely on-device. No API keys, no internet, no data tracking.
* ⚡ **Optimized Performance:** Utilizes quantized Gemma 2B weights for fast response times and low memory consumption on Android hardware.
* 🎨 **Modern Native UI:** Built with Kotlin (Jetpack Compose / XML) for fluid animations, dynamic theming, and a buttery-smooth user experience.
* 🔒 **Absolute Privacy:** Your prompts never leave your phone. What happens in NeuroPocket, stays in NeuroPocket.
* 🌙 **Dark Mode Support:** Seamlessly adapts to your system preferences.

---

## 🛠️ Tech Stack
* **Language:** [Kotlin](https://kotlinlang.org/)
* **Platform:** Android (Min SDK: 24+)
* **AI/LLM:** [Gemma 2B (Google)](https://huggingface.co/google/gemma-1.1-2b-it-tflite/tree/main) 
* **Inference Engine:** MediaPipe LLM Inference API
* **Architecture:** MVVM (Model-View-ViewModel)
* **Asynchronous:** Kotlin Coroutines & Flow

---

## ⚙️ Installation & Setup

### Prerequisites
* Android Studio 
* An Android device with at least 4GB/6GB of RAM (Recommended for running Gemma 2B smoothly).
* The Gemma 2B model weights.

### Step-by-Step Guide
1. **Clone the repository:**
   ```bash
   git clone [https://github.com/bluedorsey/NeuroPocket.git](https://github.com/bluedorsey/NeuroPocket.git)
   cd NeuroPocket

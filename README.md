<div align="center">

<h1>🧠 NeuroPocket</h1>

<p><strong>An on-device AI health assistant powered by Gemma 2B — no internet required.</strong></p>

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-2196F3)
![License](https://img.shields.io/badge/License-MIT-green)

</div>

---

## ✨ Features

- 🤖 **On-device LLM** — Runs Google's Gemma 1.1 2B model locally via MediaPipe
- 🔒 **100% Private** — No data ever leaves your device
- 💬 **Multi-conversation** — New chat on every app launch; full history in a sliding drawer
- 🏷️ **Auto-titled chats** — Conversations are automatically named from your first message
- 📂 **Chat history drawer** — Swipe or tap ☰ to browse and switch between past conversations
- ⚡ **Optimised & smooth** — Token streaming, auto-scroll, background inference, memory limits
- 🎨 **Premium dark UI** — Glassmorphism top bar, animated pulse indicator, gradient send button

---

## 📸 App Architecture

```
NeuroPocket/
├── LLMinference/
│   └── llm.kt                  # Singleton LLM manager (MediaPipe Gemma)
├── ViewModel/
│   └── ViewModel.kt            # Multi-conversation state management
├── chat_managment/
│   ├── Chat.kt                 # Message data class
│   ├── Conversation.kt         # Conversation session data class
│   └── ChatRepository.kt      # SharedPreferences persistence layer
├── ui/theme/
│   ├── Color.kt                # Dark "Intelligent Monolith" color system
│   ├── Theme.kt
│   └── Type.kt
└── MainActivity.kt             # UI: ModalNavigationDrawer + Chat screen
```

---

## 🧩 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| LLM Engine | MediaPipe `tasks-genai` 0.10.14 |
| Model | Gemma 1.1 2B IT (int4 CPU) |
| State | `StateFlow` + `AndroidViewModel` |
| Persistence | SharedPreferences + Gson |
| Async | Kotlin Coroutines |

---
## 📱 UI Showcase

<p align="center">
  <img src="https://github.com/user-attachments/assets/d7720f77-365d-4c79-8f44-46a7e5dd16ce" width="30%" alt="Home / Chat Screen" />
    <img src="https://github.com/user-attachments/assets/12480d96-88b2-4749-b632-8c9c7ad69b81" width="30%" alt="History / History" />
        <img src="https://github.com/user-attachments/assets/fe9ada39-5da8-40e1-adde-84889d3acc48" width="30%" alt="Chat / Live Chat" />

    

</p>

---

## 🚀 Getting Started

### Prerequisites
- Android Studio **Hedgehog** or newer
- Android device / emulator with **API 24+** (Android 7.0+)
- ~1.5 GB of free storage for the model file

### 1. Clone the repo

```bash
git clone https://github.com/bluedorsey/NeuroPocket.git
cd NeuroPocket
```

### 2. Add the Gemma model

The model file (`gemma-1.1-2b-it-cpu-int4.bin`) is **not included** in the repo (it's too large and listed in `.gitignore`).

Download it from [Kaggle — Gemma 1.1 2B IT](https://www.kaggle.com/models/google/gemma) and place it in:

```
app/src/main/assets/gemma-1.1-2b-it-cpu-int4.bin
```

### 3. Build & Run

Open the project in Android Studio and press **Run ▶**, or build via Gradle:

```bash
./gradlew assembleDebug
```

---

## 💬 How Conversations Work

| Behaviour | Details |
|---|---|
| **App launch** | Always opens a **fresh new chat** |
| **Drawer** | Tap **☰** in the top bar to see past conversations |
| **New Chat** | Tap the **+ New Chat** button inside the drawer |
| **Switch chat** | Tap any conversation in the drawer to load it |
| **Delete chat** | Tap the 🗑 icon on any conversation item |
| **Auto-title** | First message text is used as the conversation title (max 30 chars) |
| **Limits** | 50 messages per conversation · 50 total conversations (oldest pruned) |

> First-time users who had existing chat data will have it automatically migrated into a named conversation — no data loss.

---

## ⚙️ Resource Optimisation

- **Memory** — Only the active conversation's messages are kept in RAM; the drawer stores lightweight metadata only
- **Storage** — Messages capped at 50 per conversation; conversations capped at 50 total
- **I/O** — All SharedPreferences writes use `apply()` (asynchronous, non-blocking)
- **Inference** — LLM runs on a background IO thread via Kotlin Coroutines; tokens stream directly into the UI
- **Recomposition** — Drawer and chat occupy separate composable scopes, preventing unnecessary redraws

---

## 📁 Key Files

| File | Purpose |
|---|---|
| [`llm.kt`](app/src/main/java/com/example/personalhealthcareapp/LLMinference/llm.kt) | Loads and manages the Gemma model; exposes token streaming flows |
| [`ViewModel.kt`](app/src/main/java/com/example/personalhealthcareapp/ViewModel/ViewModel.kt) | Orchestrates conversation switching, message sending, and auto-titling |
| [`ChatRepository.kt`](app/src/main/java/com/example/personalhealthcareapp/chat_managment/ChatRepository.kt) | All persistence: conversation CRUD, message storage, legacy migration |
| [`MainActivity.kt`](app/src/main/java/com/example/personalhealthcareapp/MainActivity.kt) | Full UI: drawer, top header, chat list, input field |

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/your-feature`
3. Commit your changes: `git commit -m "feat: Add your feature"`
4. Push: `git push origin feat/your-feature`
5. Open a Pull Request

---


---

<div align="center">
  <sub>Built with ❤️ by GDGC AI/ML Team </a></sub>
</div>

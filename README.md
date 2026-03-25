# 🛡️ Stealth Vault (Fake Calculator App)

**Stealth Vault** is a high-security Android application designed to keep your private files, photos, videos, and apps truly hidden. Disguised as a fully functional, world-class calculator, it provides an impenetrable layer of privacy behind a harmless everyday tool.

![Stealth Vault Banner](https://raw.githubusercontent.com/Subhan-Haider/Stealth-Vault/main/app/src/main/res/drawable/ic_vault_logo.png)

---

## 🌟 Key Features

### 🧮 1. The Ultimate Decoy
*   **Fully Functional Calculator**: Performs standard and scientific calculations (sin, cos, log, etc.) using the `exp4j` math engine.
*   **Silent Gatekeeper**: The vault only opens when your secret PIN is entered and the `=` button is pressed.
*   **Native Look**: Uses a premium, charcoal-and-amber Material 3 design that looks 100% like a system utility.

### 🔐 2. Advanced File Encryption
*   **AES-256 GCM Architecture**: All hidden files (Photos, Videos, Documents) are encrypted locally using the **Android KeyStore (HSM)**.
*   **Invisible to OS**: Files moved to the vault are immediately stripped from the Android Gallery and file explorers, existing only within the encrypted sandbox.

### 📱 3. App Locker with "Fake Crash"
*   **Privacy Injection**: Lock sensitive apps like WhatsApp, Telegram, or Gallery.
*   **Social Engineering Defense**: When a locked app is opened, Stealth Vault shows a convincing **"Application Error"** crash screen to divert intruders.

### 🕵️ 4. Security & Anti-Intruder
*   **Intruder Selfie**: Automatically captures a front-facing photo (`CameraX`) on failed PIN attempts.
*   **Decoy PIN**: Set a secondary "Decoy" PIN that opens a completely empty, realistic-looking vault to fool someone forcing you to open it.
*   **Emergency Self-Destruct**: Optionally wipe all vault data after 5 failed PIN attempts.

---

## 🚀 Technical Stack
*   **Language**: Kotlin
*   **Architecture**: MVVM + Clean Architecture
*   **UI Framework**: Material Design 3 + Jetpack Navigation
*   **Database**: Room (with SQLCipher for encrypted local storage)
*   **Dependency Injection**: Hilt
*   **CI/CD**: GitHub Actions (Auto-APK Release)

---

## 🔨 How to Build

1.  **Clone the Repo**:
    ```bash
    git clone https://github.com/Subhan-Haider/Stealth-Vault.git
    ```
2.  **Open in Android Studio**:
    *   Import the project and let Gradle sync.
3.  **Generate APK**:
    *   Build > Build Bundle(s) / APK(s) > Build APK(s)
    *   The output will be in `app/build/outputs/apk/debug/`.

---

## 🤝 Contribution
This project is built for high-security privacy needs. Feel free to fork, report issues, or suggest new "Stealth" features!

---

## ⚖️ License
This project is licensed under the MIT License - see the `LICENSE` file for details.

# WatchSend

[简体中文](README.md) | **English**

> The watch evolution of OldSend. Retaining all hardcore transfer capabilities, the UI has been completely reshaped into Wear OS native "3D Wheel" and "Pill Cards", designed for small and round screens.

[![Build Android APK](https://github.com/styisme/watchsend/actions/workflows/android.yml/badge.svg)](https://github.com/styisme/watchsend/actions/workflows/android.yml)
[![Version](https://img.shields.io/badge/version-1.2.0-512BD4)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## ⚠️ AI Generation Disclosure
> **This project is heavily assisted by AI.**
> Please check [docs/AI_DISCLOSURE.md](docs/AI_DISCLOSURE.md) for detailed scope and limitations.

---

## 📦 Version & Origin
Current Version: **v1.2.0**.

This is the **Watch Edition** of [OldSend](https://github.com/styisme/oldsend). Core protocols, HTTP server, and cross-network features are inherited from OldSend, while the UI and package name (`org.watchsend`) are completely rewritten.

---

## ✨ Core Features

### ⌚ Deep Wear OS Integration
- **Rotary Input**: Perfectly adapted for Wear OS 3+ rotating crown.
- **Haptic Feedback**: Fine-tuned vibrations for actions.
- **Dynamic Color**: Android 12+ Material You integration, following watch faces/wallpapers.
- **Ambient/Idle Mode**: Transfers continue with minimal UI when screen is off.
- **Voice Input**: Built-in system `RecognizerIntent` support.
- **Low Performance Mode**: Disables 3D effects for older watches.

### 🎨 Extreme UI Experience
- **3D Wheel Effect**: Cards scale down and fade out at the edges while scrolling.
- **Round Screen Optimization**: Dynamic paddings to prevent text cutoff.
- **Predictive Back**: Simulates system-level predictive back animations.
- **Infinite UI Scaling**: Adjust font and control sizes from `0.3x` to `2.5x`.
- **10 Language Variants**: Supports zh, zh_TW, en, ja, ko, de, fr, es, ru, pt.

### 🌐 Cross-network Transfer
- **6-digit PIN**: Transfer files over WAN without a public IP.
- **Public P2P**: Supports FRP/Ngrok/ZeroTier.
- **UPnP / STUN / IPv6**: Native network traversal support.

---

## 🛠️ Building

Toolchain: **AGP 3.6.3 + Gradle 5.6.4 + JDK 8** (to maintain `minSdkVersion 10`).

### Local Build
We provide a Python script for easy local compilation:
```bash
python build.py
```
It auto-detects your environment and can download Gradle if missing.

### Automated CI/CD
Push changes to `CHANGELOG.md` and CI will automatically bump versions, build, sign, and create a GitHub Release.

---

## 🚧 Risk & Limitations
1. **Experimental**: Cross-net features rely on third-party services (ntfy.sh).
2. **Hardware Gap**: Some features (like Tiles) require API 24+.
3. **TLS Legacy**: Android 2.3 only supports TLS 1.0.
4. **Performance**: 3D effects require GPU. Use Low Performance Mode if needed.

---

## 📚 Related Docs
- [CHANGELOG.md](CHANGELOG.md)
- [CONTRIBUTING_EN.md](CONTRIBUTING_EN.md)
- [OldSend](https://github.com/styisme/oldsend)

---

## 📜 License
MIT License. Fully compatible with LocalSend protocols.
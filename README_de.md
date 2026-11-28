**Deutsch** | [简体中文](README.md) | [繁體中文](README_zh_TW.md) | [English](README_EN.md) | [日本語](README_ja.md) | [한국어](README_ko.md) | [Français](README_fr.md) | [Español](README_es.md) | [Русский](README_ru.md) | [Português](README_pt.md)

📖 **Benutzerhandbuch**: [deutsche Ausgabe](docs/USER_GUIDE_de.md)

# WatchSend

> Die Wearable-Version von OldSend. Alle Übertragungsfunktionen bleiben, die Oberfläche wurde für kleine und runde Displays als Wear-OS-natives „3D-Wheel“ mit Pillen-Karten neu gebaut.

[![Build Android APK](https://github.com/styisme/watchsend/actions/workflows/android.yml/badge.svg)](https://github.com/styisme/watchsend/actions/workflows/android.yml)
[![Version](https://img.shields.io/badge/version-1.8.2-512BD4)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Made with AI](https://img.shields.io/badge/made%20with-AI-FF6B6B)](docs/AI_DISCLOSURE.md)
[![Platform](https://img.shields.io/badge/platform-Android%202.3%2B-brightgreen)]()

---

## 📦 Version

Aktuell: **v1.8.2**. 52 Sprachen vollständig (357–358 Einträge für die 10 Basissprachen, 353 für die übrigen, 18.403 insgesamt). Verlauf in [CHANGELOG.md](CHANGELOG.md).

---

## ✨ Hauptfunktionen

### ⌚ Wearable-Optimierung
- **Drehkrone** (Wear OS 3+), **haptisches Feedback**, Display bleibt beim Senden an, Spracheingabe, Kacheln (Android 7.0+).
- **Niedrigleistungsmodus**: schaltet nahezu alle Animationen ab (3D-Wheel, Einblenden, Druck, Lade-Scan, Drei-Punkt-Atmen, Skeleton-Schimmer, Schalter-Übergang, Sheet-Federn), senkt die Parallelität der Fallback-Suche und begrenzt die Übertragungsrate. Ohne manuelle Einstellung automatisch auf schwachen Geräten (Heap ≤256MB / Low-RAM-Flag / ≤2 Kerne).
- **Übertragungslimit**: 2 MB/s im Niedrigleistungsmodus, 512 KB/s bei Akku unter 15 % und ohne Laden – für Senden und Empfangen.
- **Pause bei Inaktivität (standardmäßig aus)**: 3 Minuten nach Bildschirm-aus werden HTTP- und Multicast-Listener pausiert und beim Einschalten fortgesetzt. Bei laufender Übertragung wird automatisch verschoben.

### 🛠️ Geek-Toolbox
- **Stille Installation (Beta)**: APK-Installation über Root oder Shizuku (eigener Binder-IPC-Proxy).
- **Native ZIP-Extraktion**: reine Java-Engine, inkl. Schutz vor Path-Traversal.
- **Verlauf**: langes Drücken für **erneut senden**, teilen, entpacken, löschen. „Erneut senden“ legt die Datei direkt zurück in die Warteliste.
- **Web in 10 Sprachen**: erkennt die Browsersprache, Drag & Drop und Zwischenablage-Sync.

### 🎨 UI und Sprachen
- Physische UI-Skalierung (0,3x–2,5x), Drei-Zustands-Theme, Dynamic Color ab Android 12, 3D-Wheel, Runddisplay-Optimierung, Swipe-Zurück.
- **52 Sprachen vollständig**: mindestens 353 lokalisierte Einträge je Sprache, 18.403 insgesamt, keine rohen Keys. Die zwei Texte zu „In-App-Zurück aktivieren" aus 1.7.13 decken alle 52 Sprachen ab; die vier aus 1.7.12 nur die 10 Basissprachen.
- **Sprachpakete auf Abruf**: beim Start nur Basispakete, der Rest einmalig nach Sprachwahl (spart ca. 1 MB Heap und Kaltstartzeit). Siehe [Sprachleitfaden](docs/LANGUAGES.md).
- **Wear-Uikit**: 44 reine Java-Widgets + 6 Vollbild-Container, keine Fremdabhängigkeiten.

### 🌐 Netzwerk und Kompatibilität
- 6-stellige Einmal-PIN, FRP/Ngrok/ZeroTier, UPnP/STUN/IPv6.
- Vollständig kompatibel mit LocalSend v1/v2.2, ab Android 2.3 (API 10), Weiß-/Schwarzliste und Sperre gefährlicher Endungen.

---

## 📱 Schnellstart
1. **Uhr**: im selben WLAN Dienst starten, Geräte erscheinen automatisch, antippen zum Senden.
2. **Web**: im Browser `http://<Uhr-IP>:53317` öffnen – Upload, Dateiliste, Zwischenablage.
3. **Über Netzgrenzen**: 6-stellige PIN oder Direktverbindung `[IP]:53317`.

---

## 🛠️ Build
**AGP 3.6.3 + Gradle 5.6.4 + JDK 8** (minSdk 10 bleibt). CI veröffentlicht automatisch anhand der Version in `CHANGELOG.md`.

---

## 🚧 Grenzen und Risiken
1. Netzwerkübergreifende Funktionen hängen von Diensten Dritter oder eigener öffentlicher Konfiguration ab.
2. Manche alten Geräte haben keinen Vibrationsmotor oder keine Krone; Kacheln ab Android 7.0.
3. Android 2.3 unterstützt nur TLS 1.0 – HTTPS schlägt dort fehl.
4. 3D-Effekte brauchen GPU-Leistung; der Niedrigleistungsmodus greift automatisch und lässt sich manuell erzwingen.

---

## 📚 Dokumente
- [CHANGELOG.md](CHANGELOG.md), [CONTRIBUTING.md](CONTRIBUTING.md)
- [docs/AI_DISCLOSURE.md](docs/AI_DISCLOSURE.md), [docs/DEVELOPMENT_CONSTRAINTS.md](docs/DEVELOPMENT_CONSTRAINTS.md)
- [docs/LANGUAGES.md](docs/LANGUAGES.md) — Sprachliste, Umschalten, Fallback-Regeln
- [docs/USER_GUIDE_de.md](docs/USER_GUIDE_de.md) — das vollständige Handbuch für Anwender (10 Sprachen)

## 📜 Lizenz
MIT License. Vollständig kompatibel zum LocalSend-Protokoll (Eigenimplementierung).

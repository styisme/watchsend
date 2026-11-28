**Español** | [简体中文](README.md) | [繁體中文](README_zh_TW.md) | [English](README_EN.md) | [日本語](README_ja.md) | [한국어](README_ko.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | [Русский](README_ru.md) | [Português](README_pt.md)

📖 **Guía del usuario**: [edición en español](docs/USER_GUIDE_es.md)

# WatchSend

> La versión de reloj de OldSend. Conserva todas las capacidades de transferencia y reconstruye la interfaz para pantallas pequeñas y redondas como «rueda 3D» y tarjetas píldora, al estilo de Wear OS.

[![Build Android APK](https://github.com/styisme/watchsend/actions/workflows/android.yml/badge.svg)](https://github.com/styisme/watchsend/actions/workflows/android.yml)
[![Version](https://img.shields.io/badge/version-1.9.0-512BD4)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Made with AI](https://img.shields.io/badge/made%20with-AI-FF6B6B)](docs/AI_DISCLOSURE.md)
[![Platform](https://img.shields.io/badge/platform-Android%202.3%2B-brightgreen)]()

---

## 📦 Versión

Versión actual: **v1.9.0**. 52 idiomas completos (357–358 entradas para los 10 idiomas base, 353 para el resto, 18.403 en total). Historial en [CHANGELOG.md](CHANGELOG.md).

---

## ✨ Funciones

### ⌚ Optimización para reloj
- **Corona giratoria** (Wear OS 3+), **respuesta háptica**, pantalla encendida durante la transferencia, entrada por voz, mosaicos (Android 7.0+).
- **Modo de bajo rendimiento**: desactiva casi todas las animaciones (rueda 3D, entrada, pulsación, barrido de carga, respiración de tres puntos, brillo del esqueleto, transición de interruptor, rebote del panel), reduce la concurrencia del escaneo de respaldo y limita la velocidad. Se activa solo en equipos con poca memoria (heap ≤256 MB / marca low-RAM / ≤2 núcleos) si nunca se configuró.
- **Límite de velocidad**: 2 MB/s en bajo rendimiento, 512 KB/s con batería bajo 15 % y sin carga, tanto al enviar como al recibir.
- **Pausa por inactividad (desactivada por defecto)**: 3 minutos tras apagar la pantalla se pausan las escuchas HTTP y multicast, y se reanudan al encender; se pospone sola si hay una transferencia en curso.

### 🛠️ Caja de herramientas geek
- **Instalación silenciosa (beta)**: Root o Shizuku, proxy Binder IPC escrito a mano.
- **Extracción ZIP nativa**: motor Java puro con protección contra path traversal.
- **Historial**: pulsación larga para **reenviar**, compartir, descomprimir o borrar. «Reenviar» devuelve el archivo a la lista de envío.
- **Web en 10 idiomas**: detecta el idioma del navegador, arrastrar y soltar, sincronización del portapapeles.

### 🎨 Interfaz e idiomas
- Escalado físico de la UI (0,3x–2,5x), tema de tres estados, color dinámico en Android 12+, rueda 3D, adaptación a pantalla redonda, gesto de volver.
- **52 idiomas completos**: al menos 353 entradas localizadas por idioma, 18.403 en total, sin claves crudas. Los dos textos "Activar retroceso interno" de 1.7.13 cubren los 52 idiomas; los cuatro de 1.7.12 siguen limitados a los 10 base.
- **Carga de idiomas bajo demanda**: al arrancar solo los paquetes base, el resto se construye una vez conocida la lengua (ahorra ~1 MB de heap y tiempo de arranque). Ver [guía de idiomas](docs/LANGUAGES.md).
- **Wear-Uikit**: 44 widgets Java nativos + 6 contenedores a pantalla completa, sin dependencias externas.

### 🌐 Red y compatibilidad
- PIN de 6 dígitos de un solo uso, FRP/Ngrok/ZeroTier, UPnP/STUN/IPv6.
- Compatible con LocalSend v1/v2.2, desde Android 2.3 (API 10), listas blanca/negra y bloqueo de extensiones peligrosas.

---

## 📱 Inicio rápido
1. **Reloj**: mismo Wi-Fi, iniciar el servicio, los equipos aparecen solos, tocar para enviar.
2. **Web**: abrir `http://<IP del reloj>:53317` en el navegador.
3. **Fuera de la LAN**: PIN de 6 dígitos o conexión directa `[IP]:53317`.

---

## 🛠️ Compilación
**AGP 3.6.3 + Gradle 5.6.4 + JDK 8** (minSdk 10). La CI publica según la versión de `CHANGELOG.md`.

---

## 🚧 Límites y riesgos
1. Las funciones fuera de la LAN dependen de servicios de terceros o de configuración pública propia.
2. Algunos equipos antiguos no tienen vibrador ni corona; mosaicos desde Android 7.0.
3. Android 2.3 solo admite TLS 1.0: HTTPS falla.
4. Los efectos 3D exigen GPU; el modo de bajo rendimiento se activa solo y también puede forzarse.

---

## 📚 Documentos
- [CHANGELOG.md](CHANGELOG.md), [CONTRIBUTING.md](CONTRIBUTING.md)
- [docs/AI_DISCLOSURE.md](docs/AI_DISCLOSURE.md), [docs/DEVELOPMENT_CONSTRAINTS.md](docs/DEVELOPMENT_CONSTRAINTS.md)
- [docs/LANGUAGES.md](docs/LANGUAGES.md) — lista de idiomas, cambio y reglas de respaldo
- [docs/USER_GUIDE_es.md](docs/USER_GUIDE_es.md) — el manual completo para usuarios (10 idiomas)

## 📜 Licencia
MIT License. Totalmente compatible con el protocolo LocalSend (implementación propia).

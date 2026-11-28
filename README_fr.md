**Français** | [简体中文](README.md) | [繁體中文](README_zh_TW.md) | [English](README_EN.md) | [日本語](README_ja.md) | [한국어](README_ko.md) | [Deutsch](README_de.md) | [Español](README_es.md) | [Русский](README_ru.md) | [Português](README_pt.md)

📖 **Guide d’utilisation** : [édition française](docs/USER_GUIDE_fr.md)

# WatchSend

> La version montre d'OldSend. Toutes les capacités de transfert sont conservées, l'interface a été reconstruite pour les petits écrans ronds en « roue 3D » et cartes pilule, façon Wear OS.

[![Build Android APK](https://github.com/styisme/watchsend/actions/workflows/android.yml/badge.svg)](https://github.com/styisme/watchsend/actions/workflows/android.yml)
[![Version](https://img.shields.io/badge/version-1.7.13-512BD4)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Made with AI](https://img.shields.io/badge/made%20with-AI-FF6B6B)](docs/AI_DISCLOSURE.md)
[![Platform](https://img.shields.io/badge/platform-Android%202.3%2B-brightgreen)]()

---

## 📦 Version

Version actuelle : **v1.7.13**. 52 langues complètes (357–358 entrées pour les 10 langues de base, 353 pour les autres, 18 403 au total). Historique dans [CHANGELOG.md](CHANGELOG.md).

---

## ✨ Fonctionnalités

### ⌚ Optimisation montre
- **Couronne rotative** (Wear OS 3+), **retour haptique**, écran maintenu allumé pendant le transfert, saisie vocale, tuiles (Android 7.0+).
- **Mode basse performance** : coupe presque toutes les animations (roue 3D, apparition, appui, balayage de chargement, respiration à trois points, scintillement du squelette, transition d'interrupteur, rebond de feuille), réduit la concurrence de la recherche de secours et plafonne le débit. Activation automatique sur appareils faibles (tas ≤256 Mo / indicateur low-RAM / ≤2 cœurs) si rien n'a été réglé.
- **Plafond de débit** : 2 Mo/s en basse performance, 512 Ko/s sous 15 % de batterie hors charge, en émission comme en réception.
- **Pause d'inactivité (désactivée par défaut)** : 3 minutes après extinction de l'écran, les écoutes HTTP et multicast sont suspendues puis reprennent à l'allumage ; reportée automatiquement pendant un transfert.

### 🛠️ Boîte à outils geek
- **Installation silencieuse (bêta)** : Root ou Shizuku, proxy Binder IPC écrit à la main.
- **Décompression ZIP native** : moteur Java pur, protection contre le path traversal.
- **Historique** : appui long pour **renvoyer**, partager, décompresser, supprimer. « Renvoyer » remet le fichier dans la liste d'envoi.
- **Web en 10 langues** : détection automatique de la langue du navigateur, glisser-déposer et synchronisation du presse-papiers.

### 🎨 Interface et langues
- Mise à l'échelle physique de l'UI (0,3x–2,5x), thème à trois états, couleur dynamique Android 12+, roue 3D, adaptation écran rond, retour par glissement.
- **52 langues complètes** : au moins 353 entrées localisées par langue, 18 403 au total, aucune clé brute. Les deux textes « Activer le retour intégré » de 1.7.13 couvrent les 52 langues ; les quatre de 1.7.12 restent limités aux 10 langues de base.
- **Chargement des langues à la demande** : seuls les paquets de base au démarrage, le reste construit une fois la langue connue (environ 1 Mo de tas et du temps de démarrage économisés). Voir le [guide des langues](docs/LANGUAGES.md).
- **Wear-Uikit** : 44 widgets Java natifs + 6 conteneurs plein écran, sans dépendance externe.

### 🌐 Réseau et compatibilité
- PIN à 6 chiffres à usage unique, FRP/Ngrok/ZeroTier, UPnP/STUN/IPv6.
- Compatible LocalSend v1/v2.2, dès Android 2.3 (API 10), liste blanche/noire et blocage des extensions dangereuses.

---

## 📱 Démarrage rapide
1. **Montre** : même Wi-Fi, démarrer le service, les appareils apparaissent, appuyer pour envoyer.
2. **Web** : ouvrir `http://<IP de la montre>:53317` dans le navigateur.
3. **Hors réseau local** : PIN à 6 chiffres ou connexion directe `[IP]:53317`.

---

## 🛠️ Compilation
**AGP 3.6.3 + Gradle 5.6.4 + JDK 8** (minSdk 10 conservé). La CI publie automatiquement selon la version de `CHANGELOG.md`.

---

## 🚧 Limites et risques
1. Les fonctions hors réseau dépendent de services tiers ou d'une configuration publique personnelle.
2. Certains appareils anciens n'ont ni vibreur ni couronne ; tuiles depuis Android 7.0.
3. Android 2.3 ne gère que TLS 1.0 : HTTPS échoue.
4. Les effets 3D demandent du GPU ; le mode basse performance s'active automatiquement et peut être forcé.

---

## 📚 Documents
- [CHANGELOG.md](CHANGELOG.md), [CONTRIBUTING.md](CONTRIBUTING.md)
- [docs/AI_DISCLOSURE.md](docs/AI_DISCLOSURE.md), [docs/DEVELOPMENT_CONSTRAINTS.md](docs/DEVELOPMENT_CONSTRAINTS.md)
- [docs/LANGUAGES.md](docs/LANGUAGES.md) — liste des langues, changement, règles de repli
- [docs/USER_GUIDE_fr.md](docs/USER_GUIDE_fr.md) — le manuel complet pour les utilisateurs (10 langues)

## 📜 Licence
MIT License. Entièrement compatible avec le protocole LocalSend (implémentation propre).

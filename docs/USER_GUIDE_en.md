[简体中文](USER_GUIDE_zh.md) | [繁體中文](USER_GUIDE_zh_TW.md) | **English** | [日本語](USER_GUIDE_ja.md) | [한국어](USER_GUIDE_ko.md) | [Deutsch](USER_GUIDE_de.md) | [Français](USER_GUIDE_fr.md) | [Español](USER_GUIDE_es.md) | [Русский](USER_GUIDE_ru.md) | [Português](USER_GUIDE_pt.md)

# WatchSend User Guide

> File transfer on your watch - user manual

Applies to v1.8.2 (versionCode 10802)    November 2026

## 1 About This Guide

This guide is written for people who use WatchSend: how to install it, connect, send and receive files, and what every option on the Settings screen actually does. Button and menu names match what the app shows in the language you have selected; if you have switched the interface language, read the edition in that same language.

It covers usage only, not building, packaging or extending the project. For the toolchain, contribution rules and how to maintain language packs, see CONTRIBUTING and the docs folder in the repository. Behaviour described here reflects the 1.8.2 implementation; the cloud build environment cannot run an emulator, so judge gesture feel on real hardware.

## 2 Install and First Launch

WatchSend ships as an APK, supports Android 2.3 (API 10) and above, and runs on a watch or on a phone acting as a sender or receiver. On a watch it is usually installed over adb or pushed by a phone-side watch assistant. Once installed, open WatchSend from the app list; there is no sign-in and no account.

The first launch asks for notification and storage permissions. Notifications carry incoming-file alerts and transfer progress in the background; storage lets files land in the gallery or the history folder. Declining will not crash the app, but you lose background alerts and some receive paths may fail, so granting both is recommended.

The top of the home screen is the service toggle, showing Start or Stop, the running state and the local address. While the service is stopped, other devices cannot discover you. Tap start and the app opens an HTTP service on port 53317 and begins periodic multicast discovery, which is when transfer becomes possible.

## 3 Screen and Basic Controls

The home screen runs from top to bottom: status and address, the device list, then the entries for Send, Record, Cross-Net, Settings and Log. The device list scrolls two ways: on Wear OS 3 and later you can turn the crown, or simply drag with a finger. As you scroll, cards near the edges shrink and fade to show where you are.

- Tap a card to go one level deeper. The back button at the bottom of the page, or the system back action, leaves the current page.
- In-app swipe back is on by default: swipe inward horizontally starting within about 16dp of the left or right edge; the page follows your finger, shrinks, fades and returns. Every screen follows the same rule - full-screen pages, device details, the PIN pad, selected files and the web QR page included. If you prefer the system back gesture, turn off Settings - General - Enable In-App Back; system back then behaves normally and swiping no longer navigates.
- Interface Scale adjusts the whole app between 0.3x and 2.5x. On a round watch, try this first when text feels cramped.
- Round watches should enable round-screen optimisation; watches with a chin such as the Moto 360 should also enable Chin Padding so buttons are not clipped.

## 4 Sending Files

Sending takes four fixed steps: pick the content, pick the device, confirm, wait for progress. You can go back and change the selection at any point; chosen items stay in the pending list.

1. Tap Send on the home screen. A type menu opens: File, Media, App, Folder, Text, Clipboard.
2. Select the items. Files and media allow multiple selection; apps and folders are one at a time; text and clipboard take the current content directly.
3. Return to the device list and tap the target. The other device must have the service running and be on the same network, or already set up for cross-network transfer.
4. Wait for the progress bar. Success shows a message and a haptic tick; failure shows an error code, and the Log screen has the details.

Sending text transmits a LocalSend text message, which the receiver sees in a notification or conversation. Clipboard sends the current clipboard content as one block of text, not as an image. The screen stays awake while a large file is transferring and returns to normal power saving afterwards.

## 5 Receiving Files

While the service is running, other devices on the same network can find you and send to you. A confirmation page appears first, listing the sender name, file count and total size. Accepting proceeds; declining notifies the sender immediately instead of leaving them waiting.

Quick Save decides whether that confirmation is skipped, in three modes: ask every time, auto accept all, auto accept favourites only. The default is ask every time; keep it until you trust the senders, then switch to automatic.

Large files resume: after an interruption, restarting the transfer continues from the bytes already written rather than starting over. Received photos and videos can go straight to the gallery, and everything else is written to history; both can be turned off independently in Settings.

## 6 Web Interface and Cross-Network Transfer

### 6.1 Web interface

With the service running, open the address shown on the home screen from a browser on the same LAN, for example http://192.168.1.100:53317. You get a minimal page: drag files to upload, watch per-file progress, and exchange clipboard text with the watch. Tap the address on the home screen to copy it, or open Web URL to show a QR code for a phone to scan.

The web page follows the browser language and ships with 10: Simplified Chinese, Traditional Chinese, English, Japanese, Korean, German, French, Spanish, Russian and Portuguese. The app itself covers 52 languages; the web page falls back to English when its list has no match.

### 6.2 Cross-network transfer

Outside the same LAN there are two routes. Password mode: one side generates a six-digit one-time code and the other enters it on the cross-network screen, with no public IP involved. Direct mode: expose the port through FRP, Ngrok or ZeroTier, then type IP:53317 in the manual entry box; speed depends on your tunnel.

Cross-network transfer relies on third-party infrastructure and your own network, so stability is not guaranteed by this app. If a connection never establishes, check reachability of the peer address and port on the Log screen first, then whether the tunnel service is online.

## 7 History

Record lists transferred files and text in reverse chronological order, with a clear archive badge on ZIP files. Long-press any entry for the action menu; tap to open details or the file itself.

- Resend puts the files from that entry back into the pending list; go back to the device list and tap a device to send again, with no need to browse folders.
- Share hands the file to another app through the system share sheet.
- Extract is available for archives only and uses the built-in pure Java engine with path-traversal protection, so nothing is written outside the target folder.
- Delete removes the entry; files already saved to the gallery are unaffected.

## 8 Settings Reference

Settings is grouped as shown below. Defaults describe a fresh install that has never been changed; after you edit something, the app shows the actual value. In long languages such as German or Russian a switch description is clipped to three lines with an ellipsis - tap the description to open it in full.

**Table 1  Settings at a glance**

| Group | Item | Default | What it does |
| --- | --- | --- | --- |
| General | Theme Mode | Follow system | Follow system, force light, force dark |
| General | Language | Follow system | 52 languages; debug_keys at the bottom of the list shows raw string keys for troubleshooting |
| General | Interface Scale | Medium (1.0x) | Scales text and controls between 0.3x and 2.5x |
| General | Screen Shape | Auto | Detects a round or square watch; can be forced |
| General | Chin Padding | Off | Shifts content up for round watches with a chin, such as the Moto 360 |
| General | Dynamic Color | On | Follows wallpaper or watch-face colours (Android 12 and later) |
| General | Enable In-App Back | On | On blocks system back and uses the in-app swipe; off restores system back and stops intercepting swipes |
| General | Low Performance | Auto | Reduces or disables UI animation; when never set manually, decided from free heap and core count |
| General | Pause service when idle | Off | After 3 minutes with the screen off, listening pauses to save power and resumes on wake; incoming pushes may be missed |
| Transfer | Quick Save | Ask every time | Ask every time, auto accept all, auto accept favourites |
| Transfer | Favourite devices | Empty | Caches frequent devices; pairs with the option above for automatic receiving |
| Transfer | Save to gallery | On | Puts received photos and videos into the gallery |
| Transfer | Save to history | On | Writes received content into history |
| Network & Automation | Enable HTTPS | Off | Transfers use TLS; very old devices only support TLS 1.0 and may fail the handshake |
| Network & Automation | Port | 53317 | Service port; sender and receiver must match after a change |
| Network & Automation | Enable web transfer | On | Lets a browser reach the local service address |
| Network & Automation | Web URL | — | Shows the address and a QR code; the link can be copied |
| Network & Automation | Share for download | Off | Lets LAN devices pull files from the local folder through LocalSend |
| Network & Automation | Auto start on boot | Off | Starts the service at boot; suited to a dedicated receiver |
| Device & Security | Rename device | Device model | The name other devices see in their list |
| Device & Security | Enable PIN | Off | Send requires the PIN before transfer starts |
| Device & Security | Change PIN | 0000 | Four digits; change it to a value only you know |
| Device & Security | Block dangerous exts | exe,bat,cmd,vbs,sh | Files with these extensions are refused |
| Device & Security | Manage White/Blacklist | Empty | Long-press a device to block it, or whitelist it to pair with Quick Save |
| Geek Mode | Silent Install (Beta) | Off | Installs a received APK automatically through Root or Shizuku |
| Other | Backup / Restore | — | Export settings to the clipboard, or import them from it |
| Other | About WatchSend | — | Version, protocol and credits |

## 9 Troubleshooting

**Table 2  Common symptoms and fixes**

| Symptom | What to do |
| --- | --- |
| The device list keeps saying no devices, tap scan | Make sure the other side has started its service and is on the same Wi-Fi; turn off router AP isolation; tap scan again; if it still fails, enter the IP manually |
| Every send from the other device returns 400 | Usually a protocol field or HTTPS mismatch: turn HTTPS off on both sides, or update both to the same version and retry, then read the status code on the Log screen |
| The browser cannot open the web page | Confirm the service is running, the port is free and the computer shares the watch's subnet; some routers block device-to-device traffic by default |
| The back button does nothing or only shows a toast | In-app back is enabled; turn it off and system back returns to normal |
| Text or buttons are clipped by the round bezel | Enable round-screen optimisation, add chin padding if needed, and drop Interface Scale one step |
| Nothing arrives after the screen has been off a while | Check whether Pause service when idle is on (off by default), and whether the system is battery-optimising WatchSend |
| Battery drains quickly during transfer | Enable Low Performance; below 15% and unplugged the app already throttles to 512 KB/s |

## 10 Security, Power and Known Limits

- Plain HTTP is the default and suits trusted LANs only. For encryption, enable HTTPS and verify the peer certificate fingerprint.
- The PIN defaults to 0000; set your own four digits. Cross-network transfer uses a six-digit one-time code that expires after use.
- Dangerous extensions exe, bat, cmd, vbs and sh are blocked by default; whitelists and blacklists are managed from the device long-press menu.
- Power saving has three layers: Low Performance disables most animation and caps transfer at 2 MB/s; below 15% battery while unplugged the cap drops to 512 KB/s; the service pauses after 3 minutes with the screen off (off by default, and deferred while a transfer is active).
- Known limits: cross-network transfer depends on third-party services; silent install depends on the ROM; some older watches have no vibration motor or crown; extraction and gesture behaviour have only been checked on small real screens.
- This project is MIT licensed, built with AI assistance and reviewed plus tuned on hardware, provided as is without warranty of fitness.

---

*WatchSend · MIT License · https://github.com/styisme/watchsend*

**繁體中文** | [简体中文](README.md) | [English](README_EN.md) | [日本語](README_ja.md) | [한국어](README_ko.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | [Español](README_es.md) | [Русский](README_ru.md) | [Português](README_pt.md)

📖 **使用指南**：[繁體中文版](docs/USER_GUIDE_zh_TW.md)

# WatchSend

> OldSend 的手錶進化版。保留全部硬核傳輸能力，將 UI 徹底重塑為 Wear OS 原生的「3D 滾輪」與「藥丸卡片」形態，為小螢幕與圓錶盤而生。

[![Build Android APK](https://github.com/styisme/watchsend/actions/workflows/android.yml/badge.svg)](https://github.com/styisme/watchsend/actions/workflows/android.yml)
[![Version](https://img.shields.io/badge/version-1.8.2-512BD4)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Made with AI](https://img.shields.io/badge/made%20with-AI-FF6B6B)](docs/AI_DISCLOSURE.md)
[![Platform](https://img.shields.io/badge/platform-Android%202.3%2B-brightgreen)]()

---

## 📦 版本

目前版本：**v1.8.2**。52 種語言全量（10 個基礎語種 357–358 條，其餘 353 條，共 18,403 條詞條）。完整歷史見 [CHANGELOG.md](CHANGELOG.md)。

---

## ✨ 核心特性

### ⌚ 手錶硬體適配
- **錶冠滑動**：適配 Wear OS 3+ 旋轉錶冠；**觸覺回饋**：點擊、成功、失敗皆有震動。
- **低效能模式**：關閉幾乎所有動畫（3D 滾輪、進場、按壓、載入掃描、三點呼吸、骨架微光、開關過渡、彈層回彈），並降低回退掃描並發、限制傳輸速率；未手動設定過時，依裝置能力（可用堆 ≤256MB／低記憶體標記／≤2 核）自動開啟。
- **傳輸速率上限**：低效能模式 2 MB/s；電量低於 15% 且未充電 512 KB/s，傳送與接收同時生效。
- **螢幕關閉閒置暫停（預設關閉）**：螢幕關閉 3 分鐘後暫停 HTTP／群播監聽，亮起自動恢復；有傳輸進行中會自動順延，不會中斷檔案。
- 語音輸入、傳輸常亮、快捷磁貼（Android 7.0+）。

### 🛠️ 極客工具箱
- **靜默安裝（Beta）**：Root／Shizuku 無感安裝 APK（純手寫 Binder IPC）。
- **原生 ZIP 解壓**：純 Java 引擎，內建防路徑穿越。
- **歷史快捷管理**：長按可**重新傳送**、分享、解壓或刪除；重新傳送會把原檔直接放回待傳送清單。
- **網頁端 10 語種**：自動辨識瀏覽器語言，支援拖曳上傳與剪貼簿同步。

### 🎨 UI 與多語言
- 物理級 UI 縮放（0.3x～2.5x）、三態主題、Android 12+ 動態取色、3D 滾輪、圓屏沉浸優化、跟手滑動返回。
- **52 種語言全量**：各語種 353 條以上本地譯文，共 18,403 條，無裸 key。1.7.13 的「啟用軟體內返回」兩條文案已覆蓋全部 52 個語種；1.7.12 的 4 條仍限於 10 個基礎語種。
- **語言包按需載入**：啟動僅載入基礎包，其餘於語言確定後建一次，節省約 1MB 堆與冷啟動時間。詳見 [多語言使用指南](docs/LANGUAGES.md)。
- **Wear-Uikit**：44 個純 Java 原生控件 + 6 個全螢幕容器，零第三方依賴。

### 🌐 跨網與協議
- 6 位一次性密碼跨廣域網、FRP／Ngrok／ZeroTier 直連、UPnP／STUN／IPv6。
- 完整相容 LocalSend v1／v2.2；最低支援 Android 2.3（API 10）；白／黑名單與危險後綴攔截。

---

## 📱 快速上手
1. **手錶端**：同一 Wi-Fi 下啟動服務，列表自動發現裝置，點裝置即可傳送。
2. **網頁端**：瀏覽器輸入 `http://<手錶IP>:53317`，可拖曳上傳、檢視檔案、同步剪貼簿。
3. **跨網**：密碼模式（6 位一次性密碼）或直連模式（`[IP]:53317`）。

---

## 🛠️ 編譯
**AGP 3.6.3 + Gradle 5.6.4 + JDK 8**（維持 minSdk 10）。CI 依 `CHANGELOG.md` 版本號自動發版。

---

## 🚧 風險提示
1. 跨網功能依賴第三方設施或使用者公網設定，穩定性不保證。
2. 部分老裝置無震動馬達或錶冠，快捷磁貼需 Android 7.0+。
3. Android 2.3 僅支援 TLS 1.0，開啟 HTTPS 會握手失敗。
4. 3D 特效對 GPU 有要求；低效能模式會自動開啟，也可手動強制。

---

## 📚 關聯文件
- [CHANGELOG.md](CHANGELOG.md)、[CONTRIBUTING.md](CONTRIBUTING.md)
- [docs/AI_DISCLOSURE.md](docs/AI_DISCLOSURE.md)、[docs/DEVELOPMENT_CONSTRAINTS.md](docs/DEVELOPMENT_CONSTRAINTS.md)
- [docs/LANGUAGES.md](docs/LANGUAGES.md) —— 52 種語言清單、切換方式與回退規則
- [docs/USER_GUIDE_zh_TW.md](docs/USER_GUIDE_zh_TW.md) —— 給使用者的完整操作手冊（10 種語言）

## 📜 協議
MIT License。完美相容 LocalSend 協議（獨立實作，未複製其原始碼）。

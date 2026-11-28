**日本語** | [简体中文](README.md) | [繁體中文](README_zh_TW.md) | [English](README_EN.md) | [한국어](README_ko.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | [Español](README_es.md) | [Русский](README_ru.md) | [Português](README_pt.md)

📖 **取扱説明書**：[日本語版](docs/USER_GUIDE_ja.md)

# WatchSend

> OldSend のウェアラブル進化版。硬核な転送機能はそのままに、UI を Wear OS ネイティブの「3D ホイール」と「ピル型カード」に作り直しました。小型画面と丸い文字盤のために。

[![Build Android APK](https://github.com/styisme/watchsend/actions/workflows/android.yml/badge.svg)](https://github.com/styisme/watchsend/actions/workflows/android.yml)
[![Version](https://img.shields.io/badge/version-1.8.0-512BD4)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Made with AI](https://img.shields.io/badge/made%20with-AI-FF6B6B)](docs/AI_DISCLOSURE.md)
[![Platform](https://img.shields.io/badge/platform-Android%202.3%2B-brightgreen)]()

---

## 📦 バージョン

現在のバージョン：**v1.8.0**。52 言語すべて全量（10 の基本言語は 357〜358 項目、その他は 353 項目、合計 18,403 項目）。履歴は [CHANGELOG.md](CHANGELOG.md) を参照。

---

## ✨ 主な機能

### ⌚ ウェアラブル最適化
- **リューズ入力**（Wear OS 3+）、**触覚フィードバック**、画面常時点灯、音声入力、タイル（Android 7.0+）。
- **低パフォーマンスモード**：ほぼすべてのアニメーション（3D ホイール、進入、押下、読込スキャン、3点呼吸、スケルトンの微光、スイッチ遷移、シートの跳ね返り）を停止し、フォールバック探索の並列度を下げ、転送レートを制限。未設定時は端末能力（ヒープ ≤256MB／低 RAM フラグ／≤2 コア）で自動有効化。
- **転送レート上限**：低パフォーマンス時 2 MB/s、バッテリー 15% 未満かつ非充電時 512 KB/s（送受信の両方）。
- **画面消灯時のアイドル停止（既定オフ）**：消灯 3 分後に HTTP／マルチキャスト待受を停止し、点灯で復帰。転送中は自動で延期され、ファイルを中断しません。

### 🛠️ ギークツール
- **サイレントインストール（Beta）**：Root／Shizuku による APK 無音インストール（Binder IPC を自前実装）。
- **ネイティブ ZIP 解凍**：純 Java エンジン、パス traversal 対策済み。
- **履歴の一括管理**：長押しで**再送信**・共有・解凍・削除。再送信は元ファイルを送信待ちリストに戻すだけ。
- **Web 10 言語**：ブラウザ言語を自動判別、ドラッグ＆ドロップとクリップボード同期に対応。

### 🎨 UI と言語
- 物理レベルの UI スケーリング（0.3x〜2.5x）、3 状態テーマ、Android 12+ のダイナミックカラー、3D ホイール、丸画面最適化、スワイプバック。
- **52 言語すべて全量**：各言語 353 項目以上のローカライズ済み文言、生キーなし。1.7.13 の「アプリ内戻るを有効化」2 項目は 52 言語すべてに反映。1.7.12 の 4 項目は 10 の基本言語のみ。
- **言語パックの遅延読み込み**：起動時は基本パックのみ、残りは言語確定後に一度だけ構築（約 1MB のヒープと起動時間を節約）。詳細は [言語ガイド](docs/LANGUAGES.md)。
- **Wear-Uikit**：純 Java 製 44 ウィジェット＋6 全画面コンテナ、外部依存ゼロ。

### 🌐 クロスネットワークと互換性
- 6 桁ワンタイム PIN、FRP／Ngrok／ZeroTier 直結、UPnP／STUN／IPv6。
- LocalSend v1／v2.2 完全互換、Android 2.3（API 10）以降、ホワイト／ブラックリストと危険拡張子の遮断。

---

## 📱 クイックスタート
1. **ウェアラブル**：同じ Wi-Fi でサービス開始 → 端末を自動検出 → タップで送信。
2. **Web**：ブラウザで `http://<時計のIP>:53317` を開く。アップロード、一覧、クリップボード同期。
3. **クロスネット**：6 桁 PIN モード、または `[IP]:53317` 直結。

---

## 🛠️ ビルド
**AGP 3.6.3 + Gradle 5.6.4 + JDK 8**（minSdk 10 維持）。CI が `CHANGELOG.md` の版番号で自動リリース。

---

## 🚧 制限と注意
1. クロスネットは第三者サービスや利用者の公網設定に依存し、安定性は保証されません。
2. 一部の旧端末には振動モーターやリューズがありません。タイルは Android 7.0+。
3. Android 2.3 は TLS 1.0 のみ。HTTPS を有効にすると握手に失敗します。
4. 3D 効果は GPU を要求します。低パフォーマンスモードは自動で有効になり、手動でも強制できます。

---

## 📚 関連ドキュメント
- [CHANGELOG.md](CHANGELOG.md)、[CONTRIBUTING.md](CONTRIBUTING.md)
- [docs/AI_DISCLOSURE.md](docs/AI_DISCLOSURE.md)、[docs/DEVELOPMENT_CONSTRAINTS.md](docs/DEVELOPMENT_CONSTRAINTS.md)
- [docs/LANGUAGES.md](docs/LANGUAGES.md) —— 52 言語の一覧、切替方法、フォールバック規則
- [docs/USER_GUIDE_ja.md](docs/USER_GUIDE_ja.md) —— 利用者向けの操作マニュアル（10 言語）

## 📜 ライセンス
MIT License。LocalSend プロトコルと完全互換（独自実装、原コードの複製なし）。

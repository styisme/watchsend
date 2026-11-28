**한국어** | [简体中文](README.md) | [繁體中文](README_zh_TW.md) | [English](README_EN.md) | [日本語](README_ja.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | [Español](README_es.md) | [Русский](README_ru.md) | [Português](README_pt.md)

📖 **사용 설명서**: [한국어판](docs/USER_GUIDE_ko.md)

# WatchSend

> OldSend의 웨어러블 진화판. 강력한 전송 기능은 그대로 두고 UI를 Wear OS 네이티브 "3D 휠"과 "필 카드"로 다시 설계했습니다. 작은 화면과 원형 디스플레이를 위해.

[![Build Android APK](https://github.com/styisme/watchsend/actions/workflows/android.yml/badge.svg)](https://github.com/styisme/watchsend/actions/workflows/android.yml)
[![Version](https://img.shields.io/badge/version-1.8.0-512BD4)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Made with AI](https://img.shields.io/badge/made%20with-AI-FF6B6B)](docs/AI_DISCLOSURE.md)
[![Platform](https://img.shields.io/badge/platform-Android%202.3%2B-brightgreen)]()

---

## 📦 버전

현재 버전: **v1.8.0**. 52개 언어 전량(10개 기본 언어 357–358개, 나머지 353개, 총 18,403개 항목). 전체 이력은 [CHANGELOG.md](CHANGELOG.md).

---

## ✨ 주요 기능

### ⌚ 웨어러블 최적화
- **크라운 입력**(Wear OS 3+), **햅틱 피드백**, 전송 중 화면 켜짐, 음성 입력, 타일(Android 7.0+).
- **저성능 모드**: 거의 모든 애니메이션(3D 휠, 진입, 누름, 로딩 스캔, 3점 호흡, 스켈레톤 셔머, 스위치 전환, 시트 바운스)을 끄고, 대체 스캔 동시성을 낮추며 전송 속도를 제한합니다. 수동 설정이 없으면 기기 능력(힙 ≤256MB／저메모리 플래그／≤2코어)으로 자동 활성화됩니다.
- **전송 속도 상한**: 저성능 2 MB/s, 배터리 15% 미만·미충전 시 512 KB/s(송수신 모두 적용).
- **화면 꺼짐 유휴 정지(기본 꺼짐)**: 화면이 꺼진 뒤 3분이 지나면 HTTP／멀티캐스트 수신을 멈추고 화면이 켜지면 복귀합니다. 전송 중이면 자동으로 연기되어 파일이 끊기지 않습니다.

### 🛠️ 긱 툴박스
- **사일런트 설치(Beta)**: Root／Shizuku로 APK 무음 설치(직접 구현한 Binder IPC).
- **네이티브 ZIP 해제**: 순수 Java 엔진, 경로 탈출 방지 내장.
- **기록 빠른 관리**: 길게 눌러 **다시 보내기**, 공유, 해제, 삭제. 다시 보내기는 원본을 대기 목록에 되돌려 놓습니다.
- **웹 10개 언어**: 브라우저 언어 자동 인식, 드래그 앤 드롭과 클립보드 동기화.

### 🎨 UI와 언어
- 물리 수준 UI 배율(0.3x~2.5x), 3단 테마, Android 12+ 다이내믹 컬러, 3D 휠, 원형 화면 최적화, 스와이프 뒤로.
- **52개 언어 전량**: 언어당 353개 이상 현지화 문구, 총 18,403개, 원시 키 없음. 1.7.13의 "앱 내 뒤로 사용" 2개 문구는 52개 언어 전체에 반영. 1.7.12의 4개는 10개 기본 언어만.
- **언어 팩 지연 로딩**: 시작 시 기본 팩만 적재, 나머지는 언어 확정 후 한 번만 생성(약 1MB 힙과 콜드 스타트 시간 절약). 자세히는 [언어 가이드](docs/LANGUAGES.md).
- **Wear-Uikit**: 순수 Java 위젯 44개＋전체 화면 컨테이너 6개, 외부 의존성 없음.

### 🌐 크로스 네트워크 및 호환성
- 6자리 일회용 PIN, FRP／Ngrok／ZeroTier 직접 연결, UPnP／STUN／IPv6.
- LocalSend v1／v2.2 완전 호환, Android 2.3(API 10) 이상, 화이트／블랙리스트와 위험 확장자 차단.

---

## 📱 빠른 시작
1. **워치**: 같은 Wi-Fi에서 서비스 시작 → 기기 자동 검색 → 탭하여 전송.
2. **웹**: 브라우저에서 `http://<워치IP>:53317` 접속. 업로드, 목록, 클립보드 동기화.
3. **크로스 네트워크**: 6자리 PIN 모드 또는 `[IP]:53317` 직접 연결.

---

## 🛠️ 빌드
**AGP 3.6.3 + Gradle 5.6.4 + JDK 8**(minSdk 10 유지). CI가 `CHANGELOG.md` 버전으로 자동 릴리스.

---

## 🚧 제약과 주의
1. 크로스 네트워크는 서드파티 서비스나 사용자 공인망 설정에 의존하며 안정성을 보장하지 않습니다.
2. 일부 구형 기기는 진동 모터나 크라운이 없습니다. 타일은 Android 7.0+.
3. Android 2.3은 TLS 1.0만 지원하므로 HTTPS를 켜면 핸드셰이크가 실패합니다.
4. 3D 효과는 GPU를 요구합니다. 저성능 모드는 자동 활성화되며 수동 강제도 가능합니다.

---

## 📚 관련 문서
- [CHANGELOG.md](CHANGELOG.md), [CONTRIBUTING.md](CONTRIBUTING.md)
- [docs/AI_DISCLOSURE.md](docs/AI_DISCLOSURE.md), [docs/DEVELOPMENT_CONSTRAINTS.md](docs/DEVELOPMENT_CONSTRAINTS.md)
- [docs/LANGUAGES.md](docs/LANGUAGES.md) —— 52개 언어 목록, 전환 방법, 대체 규칙
- [docs/USER_GUIDE_ko.md](docs/USER_GUIDE_ko.md) — 사용자를 위한 전체 조작 매뉴얼(10개 언어)

## 📜 라이선스
MIT License. LocalSend 프로토콜과 완전 호환(독자 구현, 원본 코드 복제 없음).

# WatchSend

> OldSend 的手表进化版。保留全部硬核传输能力，将 UI 彻底重塑为 Wear OS 原生的“3D 滚轮”与“药丸卡片”形态，为小屏幕与圆表盘而生。

[![Build Android APK](https://github.com/styisme/watchsend/actions/workflows/android.yml/badge.svg)](https://github.com/styisme/watchsend/actions/workflows/android.yml)
[![Version](https://img.shields.io/badge/version-1.1.0-512BD4)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Made with AI](https://img.shields.io/badge/made%20with-AI-FF6B6B)](docs/AI_DISCLOSURE.md)
[![Platform](https://img.shields.io/badge/platform-Android%202.3%2B-brightgreen)]()

---

## ⚠️ AI 生成声明

> **本项目由 AI 辅助生成。**
> 
> 遵循 [AI 开发规范](https://github.com/styisme/ai-development-guidelines)，在此明确披露：
> - **AI 深度参与部分**：手表专属的 3D 滚轮算法、预测性返回动画、纯 Java 动态渲染的药丸卡片布局、硬件适配逻辑及完整的 CI/CD 流水线。
> - **开发者负责部分**：需求定义、真机测试（手表/手机）、功能验收、Bug 定位与最终发布决策。所有 AI 生成的代码均经过人工审阅与真机调优。
> 
> 关于 AI 参与的详细范围与比例，请务必查阅 [docs/AI_DISCLOSURE.md](docs/AI_DISCLOSURE.md)。

---

## 📦 版本与溯源

当前版本：**v1.1.0**。

本项目是 [OldSend](https://github.com/styisme/oldsend) 的 **Watch Edition（手表版）**。OldSend 的协议栈、HTTP 服务、组播发现、跨网传输等核心业务逻辑均被完整继承，UI 层与包名（`org.watchsend`）进行了彻底重构。

完整版本历史请见 [CHANGELOG.md](CHANGELOG.md)。

---

## ✨ 核心特性

### ⌚ 深度手表硬件适配 (v1.1 独有)
- **表冠滑动支持 (Rotary Input)**：完美适配 Wear OS 3+ 的旋转表冠，拨动表冠即可丝滑滚动列表，告别手指遮挡。
- **全局触觉反馈 (Haptic)**：点击、传输成功、失败均有精细的震动反馈，无需时刻盯着屏幕确认状态。
- **传输屏幕常亮**：文件传输期间强制屏幕常亮，传输结束自动恢复省电逻辑。
- **快捷磁贴 (Tiles)**：支持下拉快捷面板一键唤起 WatchSend，适配 Android 7.0+（老设备自动降级忽略）。
- **低性能模式**：一键关闭 3D 滚轮、预测性返回等特效，为极低端或老旧手表提供满帧体验。

### 🎨 极致的 Wear OS UI 交互
- **3D 滚轮特效**：上下滑动时，边缘卡片自动缩小并降低透明度，还原原生物理滚轮感。
- **圆屏沉浸优化**：一键开启后，界面自动向内收缩，避免文字与按钮被圆形表盘切割。
- **跟手滑动返回 & 预测性返回**：支持全局滑动退出，卡片缩小浮起，模拟系统级 Predictive Back 动画。
- **无级 UI 缩放**：内置滑动条，可在 0.5x ~ 1.5x 之间实时调整整个 App 的字体与控件大小。
- **全量 10 国语言**：完美适配中、繁、英、日、韩、德、法、西、俄、葡，无死角覆盖。

### 🌐 跨网传输与穿透 (继承自 OldSend)
- **6 位一次性密码**：无需公网 IP，跨广域网传文件。
- **公网 P2P 直连**：支持 FRP/Ngrok/ZeroTier 等内网穿透。
- **UPnP / STUN / IPv6**：自动端口映射、NAT 探测与原生 IPv6 直连。

### 🔄 协议互通与安全
- **完美兼容**：完整兼容 LocalSend v1/v2.2 协议。
- **极低兼容**：最低支持 Android 2.3 (API 10)。
- **白/黑名单与后缀拦截**：长按设备即可拉黑，自定义危险后缀（exe/bat 等）自动拦截。

---

## 📱 快速上手

### 1. 手表端接收与发送
打开 App，确保与对方处于同一 Wi-Fi 或已配置好跨网通道。点击“启动”后，列表会自动发现设备，点击设备即可发送文件。

### 2. 网页端传文件
在电脑浏览器输入 App 首页显示的局域网 IP 及端口（如 `http://192.168.1.100:53317`），直接拖拽文件即可上传至手表。

### 3. 跨网传输
- **密码模式**：发送端生成 6 位密码，接收端输入密码即可连接。
- **直连模式**：双方配置好公网 IP/UPnP 后，输入 `[IP]:53317` 极速直连。

---

## 🛠️ 自行编译与自动发版

与 OldSend 保持一致，本项目使用 **AGP 3.6.3 + Gradle 5.6.4 + JDK 8** 的古早工具链，以维持对 `minSdkVersion 10` 的支持。

### 🤖 全自动 CI/CD 流水线
项目现已接入全自动的 GitHub Actions 发版流水线：
1. 你只需要修改 `CHANGELOG.md` 顶部的版本号（如 `## [1.1.0]`）并 Push。
2. CI 会自动提取版本号，注入到 `build.gradle` 和 App 的“关于”页面。
3. 自动解密签名文件、编译出 Release APK。
4. 自动打 Git Tag，并发布到 GitHub Release 页面供下载。

*(注：维护者需在仓库 Secrets 中配置 `NAS_KEYSTORE_BASE64` 等签名参数)*

---

## 🚧 架构局限性与风险提示（极度重要）

1. **极客实验性质**：跨网功能依赖第三方公共设施（ntfy.sh）或用户的公网配置，稳定性不保证。
2. **硬件代沟**：部分老旧设备不具备震动马达或旋转表冠；快捷磁贴仅支持 Android 7.0+，系统会自动忽略不支持的组件。
3. **TLS 代沟**：Android 2.3 原生仅支持 TLS 1.0，开启 HTTPS 会导致旧设备握手失败。
4. **手表算力瓶颈**：3D 滚轮特效与预测性返回动画对 GPU 有一定要求，极低端手表请务必开启“低性能模式”。

**总结**：WatchSend 适合极客折腾、手表文件互传或怀旧场景。日常跨平台传输，建议使用官方原版 LocalSend。

---

## 📚 关联文档

- [CHANGELOG.md](CHANGELOG.md) —— 版本更新记录
- [CONTRIBUTING.md](CONTRIBUTING.md) —— 贡献指南与提交规范
- [docs/AI_DISCLOSURE.md](docs/AI_DISCLOSURE.md) —— AI 生成声明与参与细节
- [docs/DEVELOPMENT_CONSTRAINTS.md](docs/DEVELOPMENT_CONSTRAINTS.md) —— 开发环境约束与测试覆盖说明
- [OldSend 原项目](https://github.com/styisme/oldsend) —— 协议栈与业务逻辑的来源

---

## 📜 协议

本项目使用 [MIT License](LICENSE)。

本项目完美兼容 **LocalSend 协议**。特别感谢开源项目 [LocalSend](https://github.com/localsend/localsend) 提供的优秀协议规范（Apache-2.0 授权）。OldSend 与 WatchSend 均为独立从零实现，未直接复制其源码。
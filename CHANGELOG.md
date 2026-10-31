# Changelog

本文件记录 **WatchSend** 的版本更新历史，格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

> **关于溯源**：WatchSend 是 [OldSend](https://github.com/styisme/oldsend) 的手表专属独立分支。版本号从 `v1.0.0` 重新开始计算。底层协议栈（LocalSend 兼容、跨网穿透、HTTP 服务）继承自 OldSend v2.0.0，UI 层与包名已完全重写。

## [Unreleased]

### Planned
- 基于 WebRTC 的手表端 NAT 打洞。
- 表盘 Complication（复杂功能）状态显示支持。

---

## [1.1.0] - 2026-10-31

本次更新是 WatchSend 迈向“极致原生手表体验”的里程碑。全面接入 Wear OS 硬件交互能力，清理全应用死角硬编码，并实现了全自动化发版流水线。

### Added
- **表冠滑动支持 (Rotary Input)**：完美适配 Wear OS 3+ 旋转表冠，拨动表冠即可丝滑滚动列表。
- **全局触觉反馈 (Haptic Feedback)**：对点击、成功、失败操作注入不同强度的震动反馈，提升盲操确认感。
- **传输屏幕常亮**：传输进度页强制维持屏幕常亮，防止手表息屏打断传输，结束自动恢复。
- **快捷磁贴 (Wear OS Tiles)**：支持下拉快捷面板一键唤起 App（API 24+ 生效，老设备自动忽略）。
- **低性能模式**：新增开关，一键关闭 3D 滚轮、预测性返回等复杂动画，保障老旧设备流畅度。
- **全自动 CI/CD 流水线**：集成自动提取版本号、自动注入签名、自动构建 Release 包并创建 GitHub Release 的完整发版机制。

### Fixed
- 彻底扫除了主界面、设置页、传输页及 Toast 提示中残留的中英韩混杂硬编码，实现了 10 国语言 100% 无缝覆盖。
- 修复了清理 XML 节点属性时误删宽高导致的 App 启动闪退崩溃问题。
- 修复了语言切换后主页不热更新、需重启生效的体验割裂问题。
- 修复了深色模式下部分按钮“白底白字”及进度条刺眼的问题。

### Changed
- **编译产物**：Release APK 文件名统一规范为 `WatchSend-vX.X.X.apk`。

---

## [1.0.0] - 2026-10-30

首个 WatchSend 正式版本，将 OldSend 的 UI 彻底推翻重写，100% 针对 Wear OS 圆屏、小屏幕与手势交互进行深度定制。

### Added
- **Wear OS 原生交互 UI**：彻底废弃旧版手机端布局，使用纯 Java 动态渲染“药丸卡片”（Pill Cards）与居中大标题，全面对齐系统设置页。
- **3D 滚轮特效**：列表滑动时，中间卡片保持原大，上下边缘卡片自动缩小（最小至 75%）并降低透明度，模拟真实滚轮物理感。
- **预测性返回动画**：全面接管系统返回。滑动返回时，界面自动缩小、增加超大圆角并浮起，模拟原生 Predictive Back 体验。
- **无级 UI 缩放**：在设置页引入自定义 Material 风格滑动条，支持 `0.5x` 至 `1.5x` 缩放，实时改变全 App 字体与控件大小。
- **圆屏沉浸优化**：新增开关。开启后，自动隐藏各页面顶部标题栏，增加左右边距以防圆屏切字，并启用上下半屏留白。
- **拦截系统返回开关**：防止手表系统侧滑与 App 内部滑动返回手势冲突（默认开启）。
- **应用包名更新**：从 `org.oldsend` 变更为独立的 `org.watchsend`。

### Fixed
- 彻底修复了主界面、跨网页面、传输页面在深色模式下的“白底白字”视觉灾难。
- 修复了跨网页面输入框、复选框风格与 App 整体割裂的问题，淘汰了刺眼的蓝色按钮。
- 修复了传输页面丑陋的亮黄色进度条，替换为极简极客绿进度条。
- 修复了原旧版代码中因反复注入导致的大括号丢失、类型不匹配等 Java 编译报错。
- 修复了接收文件后图标及文件大小显示异常的问题。

### Changed
- **编译产物**：GitHub Actions 构建产物名称变更为 `watchsend-debug`。
- **应用名称**：全量替换为 WatchSend。

---

## 版本对比链接

[Unreleased]: https://github.com/styisme/watchsend/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/styisme/watchsend/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/styisme/watchsend/releases/tag/v1.0.0
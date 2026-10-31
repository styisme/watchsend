# Changelog

本文件记录 **WatchSend** 的版本更新历史，格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

> **关于溯源**：WatchSend 是 [OldSend](https://github.com/styisme/oldsend) 的手表专属独立分支。版本号从 `v1.0.0` 重新开始计算。底层协议栈（LocalSend 兼容、跨网穿透、HTTP 服务）继承自 OldSend v2.0.0，UI 层与包名已完全重写。

## [Unreleased]

### Planned
- 基于 WebRTC 的手表端 NAT 打洞。
- 表冠（Rotary Input）滑动列表支持。

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

[Unreleased]: https://github.com/styisme/watchsend/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/styisme/watchsend/releases/tag/v1.0.0
# AI 生成说明

本项目（**WatchSend**）是 [OldSend](https://github.com/styisme/oldsend) 的手表专属分支。在从 OldSend 向手表端重构的整个开发过程中，大量使用了大型语言模型（LLM）辅助生成代码与文档。

本说明严格遵循 [AI 开发规范](https://github.com/styisme/ai-development-guidelines)，在此透明、公开地界定 AI 与人类在项目中的实际贡献比例与责任边界。

相关文档导航：[README](../README.md) | [CHANGELOG](../CHANGELOG.md) | [贡献指南](../CONTRIBUTING.md) | [开发约束](DEVELOPMENT_CONSTRAINTS.md)。

---

## 🤖 AI 深度参与的部分

在 v1.0.0 到 v1.1.0 的迭代中，AI 承担了极大规模的“体力活”与“高危操作”：

- **底层架构与算法重构**：
  - 彻底重写 UI，抛弃传统手机端布局，使用纯 Java 动态渲染 Wear OS 专属的“药丸卡片”（Pill Cards）。
  - 手搓 3D 滚轮特效算法（滑动时动态计算坐标，实现边缘卡片的缩小与渐隐）。
  - 实现原生预测性返回动画（Predictive Back），在滑动退出时动态绘制大圆角与立体阴影。
  - 无级 UI 缩放逻辑（在 0.5x - 1.5x 之间动态改变 DisplayMetrics）。
- **深度硬件适配**：
  - 编写 `WatchHardwareUtils`，兼容 API 10 到 Wear OS 3+ 的表冠滑动（Rotary Input）、不同类型的触觉反馈（VibrationEffect 降级），以及屏幕常亮。
  - 编写受限于 API 24+ 的快捷磁贴服务（TileService），并加入系统级隔离防止老设备崩溃。
- **全自动 CI/CD 流水线**：
  - 编写 `sync_version.py`，实现从 `CHANGELOG.md` 自动提取版本号并注入代码。
  - 重构 GitHub Actions 流水线，实现 Base64 密钥自动解密、`build.gradle` 自动改写、签名打包及自动发布 Release。
- **暴力补丁与排雷**：
  - 编写 Python 脚本，用于批量替换包名（`org.oldsend` 到 `org.watchsend`）。
  - 编写正则修复脚本，用于清理全应用残留的中文硬编码，以及清理/重写因反复注入导致的大括号错乱和不可达语句。

---

## 🧑‍💻 人类参与的部分

作为项目主导者，人类的职责是“提出痛点、把控方向、验证结果、抓虫”：

- **极端需求提出**：
  - “完全修改 UI，做一个手表版的 WatchSend。”
  - “加上表冠适配、震动、屏幕常亮和磁贴，但要兼容低版本安卓。”
  - “多语言怎么中韩英混杂？必须全部清理干净！”
  - “App 闪退了 / 圆屏优化没效果 / 网络连不上！”
- **致命 Bug 定位与拦截**：
  - 发现 AI 编写的大规模正则表达式补丁存在严重隐患（例如误删了 XML 的宽高属性导致 App 屡次停止运行，或大括号吞噬导致 Java 编译崩溃）。
  - 仔细审阅 AI 给出的补丁脚本，并在发现逻辑漏洞（如 `unreachable statement` 不可达语句）时，要求 AI 重写方法。
- **安全配置与真机验收**：
  - 使用私有仓库生成专属的 Keystore，配置 GitHub Secrets，确保发版签名安全。
  - 在真实的 Wear OS 智能手表上安装 APK，通过截图验证表冠阻尼感、震动反馈和暗黑模式，指出 UI 错位或逻辑缺陷。

---

## ⚠️ 风险提示

由于大量核心 UI 代码和 CI 脚本为 AI 辅助生成，请注意以下风险：

1. **正则补丁的脆弱性**：项目后期的许多 UI 改造是通过 Python 脚本和正则表达式直接修改 Java 源码与 XML 布局实现的。这种方式极易引发连锁语法错误（如丢失大括号、覆盖 XML 属性）。本项目完全依赖云端 CI 的编译兜底来排雷。
2. **硬件代沟的复杂性**：虽然有 API 版本隔离，但在极度特殊的定制 ROM 或老旧 Wear OS 设备上，表冠事件和磁贴拉起依然可能会被系统拦截或失效。
3. **性能瓶颈**：手表端的 3D 滚轮特效、动态圆角与阴影计算依赖 `DisplayMetrics` 和实时 View 渲染，已内置“低性能模式”用于降级，但极低端设备仍需谨慎测试。

---

## 📜 许可

本项目代码使用 [MIT 协议](../LICENSE)。

本项目完美兼容 LocalSend 协议，特别感谢开源项目 LocalSend 提供的优秀协议规范（Apache-2.0 协议授权）。WatchSend 为独立从零实现的 UI，未直接复制其源码。
# 贡献指南

感谢参与 **WatchSend** 的开发！本项目采用轻量但明确的源代码管理规范：**Conventional Commits + SemVer 2.0.0 + CHANGELOG**。

> **关于项目渊源**：WatchSend 是 [OldSend](https://github.com/styisme/oldsend) 的手表专属分支。底层协议栈（LocalSend 兼容、跨网传输）继承自 OldSend，UI 层已针对 Wear OS 完全重构。相关文档导航：[README](README.md) | [CHANGELOG](CHANGELOG.md) | [AI 披露](docs/AI_DISCLOSURE.md) | [开发约束](docs/DEVELOPMENT_CONSTRAINTS.md)。

---

## ⚠️ 关于 AI 生成代码

遵循 [AI 开发规范](https://github.com/styisme/ai-development-guidelines)，本项目的绝大多数代码由 **AI 辅助生成**（特别是 Wear OS 专属的 3D 滚轮算法、预测性返回动画、纯 Java 动态药丸卡片布局）。

提交 PR 时请务必：
- 说明改动是否由 AI 生成。
- **人工验证过功能**（必须在真实手表或模拟器上跑过一次，不能只看编译通过）。
- 如果你也是用 AI 改的，请在 PR 描述里注明使用的模型和提示词方向。

---

## 本地开发

### 环境要求

| 组件 | 版本 |
|---|---|
| JDK | 8（必须使用 JDK 8 保证 Android 2.3 旧版编译） |
| Gradle | 5.6.4 |
| Android SDK | compileSdk 28, buildTools 28.0.3 |

### 编译命令

```bash
git clone https://github.com/styisme/watchsend.git
cd watchsend
gradle assembleDebug
```

### 云端构建
项目已配置 GitHub Actions：
- Push 到 `main` 或 `master` 自动编译。
- 编译产物在 **Artifacts** 区域下载，名称为 `watchsend-debug`。

---

## 项目结构

```
watchsend/
├── app/
│   ├── src/main/java/org/watchsend/
│   │   ├── api/          # HTTP API 处理
│   │   ├── discovery/    # UDP 组播发现设备
│   │   ├── http/         # 轻量级 HTTP 客户端与服务端
│   │   ├── model/        # 数据模型与历史记录
│   │   ├── transfer/     # 文件保存与上传逻辑
│   │   ├── ui/           # 自定义控件（如 Md3Switch）与适配器
│   │   ├── util/         # 工具类（多语言引擎 I18nManager, 深色主题 ThemeUtils 等）
│   │   ├── watch/        # ⭐ 手表端专属 UI 工具
│   │   │   ├── WatchUiUtils.java       # 3D滚轮、圆屏Padding、手势拦截、动画
│   │   │   └── WatchSettingsActivity.java # 手表设置页
│   │   ├── MainActivity.java      # 主界面
│   │   └── TransferActivity.java  # 全屏传输进度 UI
│   └── src/main/res/     # 布局、图标、样式
```

---

## 提交规范

使用 [Conventional Commits](https://www.conventionalcommits.org/zh-hans/)：

```
<type>(<scope>): <subject>
```

### 允许的 type
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 仅文档变更
- `style`: 格式调整（不影响逻辑，如 UI 调整）
- `refactor`: 重构
- `perf`: 性能优化
- `build`: 构建系统变更
- `ci`: CI 配置变更
- `chore`: 杂项（版本号、.gitignore）

### 常用 scope
- `ui`: 界面、样式相关
- `watch`: 手表专属逻辑（圆屏适配、滚动特效、表冠交互）
- `lang`: 多语言相关
- `core`: 核心传输逻辑
- `http`: HTTP 服务端/客户端
- `discovery`: 组播与设备发现
- `transfer`: 文件收发

### 示例
```
feat(watch): 增加表冠滚动列表支持
fix(ui): 修复深色模式下进度条背景为亮黄色的问题
style(ui): 统一跨网页面与设置页面的药丸卡片风格
```

---

## 分支模型

| 分支 | 用途 |
|---|---|
| `main` | 稳定分支（当前默认） |
| `feat/xxx` | 功能分支 |
| `fix/xxx` | 修复分支 |
| `docs/xxx` | 文档分支 |

---

## Pull Request 流程

1. Fork 仓库（或直接编辑）
2. 新建分支：`git checkout -b feat/your-feature`
3. 修改代码并本地 / Actions 验证
4. 提交时用 Conventional Commits
5. Push 并发起 PR
6. 等 CI 通过 + Review

### PR 描述应包含

```markdown
## 变更内容
简述做了什么

## 变更类型
- [ ] feat / fix / refactor / docs / chore

## 关联 Issue
Closes #123

## 测试方式
- [ ] 本地编译通过
- [ ] 真机手表安装并测试（请注明手表型号与 Wear OS 版本）
- [ ] Actions 编译通过

## 是否使用 AI 生成
- [ ] 是（请说明使用的模型和人工验证内容）
- [ ] 否
```

---

## Issue 提交

### Bug 报告
请提供：
- **手表型号** + **Wear OS 版本**
- **对方的设备型号与 LocalSend 版本**
- **复现步骤**
- **期望行为** vs **实际行为**
- 相关日志（通过 App 主页的“日志”按钮复制）、截图

### 功能建议
- 描述使用场景（注意手表的交互限制）
- 如果可能，给出参考项目或实现思路

---

## 版本号规则

遵循 [Semantic Versioning 2.0.0](https://semver.org/lang/zh-CN/)：

| 变更 | 版本号递增 |
|---|---|
| 不兼容变更 | **MAJOR** |
| 向后兼容的新功能 | **MINOR** |
| Bug 修复 | **PATCH** |

在开发前，请更新 `CHANGELOG.md`，然后再修改 `app/build.gradle` 中的 `versionCode` 和 `versionName`。

---

## 常见问题排查

**Q: 编译报错 `tools.jar` 缺失？**
A: 必须使用 JDK 8。GitHub Actions 已经固定使用 Zulu JDK 8。

**Q: 编译报错 `error: duplicate attribute`？**
A: 布局文件（如 `activity_main.xml`）中有重复的 `android:id` 或属性，检查是否用脚本批量替换时覆盖了。

**Q: 发送端提示 `Cleartext HTTP traffic not permitted`？**
A: Android 9.0+ 默认禁止明文 HTTP。请确保 `AndroidManifest.xml` 的 `<application>` 标签中包含了 `android:usesCleartextTraffic="true"`。

**Q: 开启深色模式后，部分页面依然白底白字 / 出现刺眼颜色？**
A: WatchSend 的所有 UI 都是纯 Java 动态生成的。请检查对应页面的 `onCreate` 方法，确保调用了 `WatchUiUtils.getPillBackground(this)`，且文字颜色使用了 `isDark ? 0xFFE0E0E0 : 0xFF1C1B1F` 动态赋值。

**Q: 滑动返回手势与系统返回冲突 / 卡死？**
A: 请检查设置页的“拦截系统返回”开关是否开启。在底层，我们通过 `onBackPressed()` 拦截了系统原生返回，统一交由 `WatchUiUtils.handleSwipeBack()` 处理。

**Q: 开启圆屏优化后，顶部标题栏消失，怎么返回？**
A: 这是刻意设计的沉浸式体验。在开启圆屏优化时，依赖**滑动屏幕返回**。如果滑动失效，请检查 `WatchUiUtils` 中的 `handleSwipeBack` 事件是否被当前页面的内部控件（如横向的 ScrollView）过度消费。
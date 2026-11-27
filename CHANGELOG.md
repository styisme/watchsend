# Changelog

本文件记录版本更新历史，格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

> **关于溯源**：项目为 [OldSend](https://github.com/styisme/oldsend) 的专属进化分支。版本号从 `v1.0.0` 重新开始计算。底层协议栈（LocalSend 兼容、跨网穿透、HTTP 服务）完整继承，UI 层已完全重构。

## [Unreleased]

### Planned
- 基于 WebRTC 的手表端 NAT 打洞。
- 表盘 Complication（复杂功能）状态显示支持。

---

## [1.7.4] - 2026-11-27

### Fixed（"写着但实际不存在"的功能审计结果）
- **一键安装 / 一键打开（协议接收）**：此前用 `Uri.fromFile` 跨应用传文件，Android 7+ 会直接抛 `FileUriExposedException`，异常被 catch 后只弹一句"无法打开"——也就是说这个按钮在真机上是死的。现新增零依赖自研 `util/FileProviderEx`（ContentProvider）+ `util/SafeFileUri`（按 API 24 自动选 `content://` 或 `file://`），并在清单注册 `authorities=org.watchsend.files`，同时补上 `FLAG_GRANT_READ_URI_PERMISSION`。
- **分享文件 / 打开历史文件**：首页历史菜单里的"分享"和"打开"同样走 `Uri.fromFile`，同病同修。
- **"保存到相册"开关形同不存在**：`FileSaver.getTargetDir(name, saveAlbum)` 的 `saveAlbum` 参数根本没被读取，关掉开关照旧往公共目录写。现已真正生效。
- **Android 10+（targetSdk 30）接收文件整体失败**：分区存储下直接往 `/sdcard/Pictures/OldSend` 写会 EACCES。现改为——开关关闭时落应用私有目录（无需存储权限）；开关打开且 API ≥ 29 时落应用外部私有目录，写入完成后再用 `MediaScannerConnection` 通知媒体库索引，相册/文件管理器照样能看到。API < 29 保留原公共目录行为。
- **安全收紧**：`FileProviderEx` 只放行 `Download/Pictures/Movies/Music` 下的 `OldSend`、`WatchSend` 子目录与本应用目录，避免对外可见的 provider 变成"任意文件读取器"。

### Verified（不是假功能的部分）
- "语音输入"确认是真的：`UikitFullInputActivity` 走系统 `RecognizerIntent`（`REQUEST_VOICE`）并在 `onActivityResult` 里把识别结果回填到输入框。
- 设置页 18 个开关经脚本核对全部存在读取点，无"只写不读"。
- PIN 保护确为真校验（`/v2/prepare-upload`、网页端、下载 API 均比对 `pin`），非摆设。

## [1.7.3] - 2026-11-27

### Fixed
- 多语言词条补齐：日/韩/德/法/西/俄/葡 七种语言此前各有 **57 条**界面文案没有译文（结构上靠 `new HashMap<>(en)` 继承英文，所以不会裸出 key，但界面上是英文）。现在七种语言分别补上 57 条译文，繁体补上 `about_desc`，十种语言均做到"界面用到的 232 条文案零缺口"。
- 日期后缀按语言本地化：日文"11月27日"、韩文"11월27일"、俄文"11.27"，西方语言统一 `11/27`。

### Changed
- 新增 `docs` 之外的一次全量词条体检脚本化：扫描全部 `I18nManager.get("...")` 调用与各语言词表的差集，作为发版前的固定检查项。

## [1.7.2] - 2026-11-27

### Fixed
- 二维码页的复制按钮显示成原始词条 `copy_fingerprint`：这个 key 在词表里根本不存在（`I18nManager.get` 找不到时会原样返回 key）。已补 `copy_fingerprint`（复制地址）、`direct_send`（直连发送）、`gen_pin_send`（生成 PIN 发送）、`log`（日志）四个缺失词条，并补全繁体缺的 8 个词条。
- 顺手做了一次全量词条体检：扫描所有 `I18nManager.get("...")` 调用与词表 key 的交集，现在简体/英文均无缺口。

## [1.7.1] - 2026-11-27

### Fixed
- 二维码入口不可发现：1.7.0 只有"长按主页状态栏的 IP 那一行"才能打开二维码页，等于藏起来了。现在设置页「网络」分组里直接有一行 `▦ 网页端地址`，点进去就是二维码 + 地址文本 + 复制按钮；主页长按 IP 的老入口保留。

## [1.7.0] - 2026-11-27

把上一轮列出的"还能移植的"一次做完：三项协议语义修补 + 四项界面能力，全部按官方代码的行为重写成 Java，不引第三方依赖。

### Added
- **网页分享二维码（移植 `web_share_page.dart`，自研 `util/QrCode.java`）**：地址页现在直接给可扫二维码，电脑摄像头读一下即可打开，不用在手表上逐字抄 URL。编码器是零依赖实现（字节模式、纠错 L、版本 1–5、8 种掩码按罚分择优），**已用 OpenCV 实机解码验证版本 2–5 全部可读，并与参考实现 qrcode 8.2 做逐模块比对（8 掩码 × 4 文本，差异 0）**。
- **PIN 数字键盘（移植 `verify_page.dart`）**：3×4 大按钮 + 掩码显示，最多 6 位，满位自动提交。之前对方开了 PIN 保护时我们只能报"对方拒绝接收"，现在上传收到 401 `PIN_REQUIRED` 会自动弹键盘并带 PIN 重试。
- **已选文件确认页（移植 `selected_files_page.dart`）**：发送前列出本次文件与总大小，点一行即取消勾选，底部按钮显示"发送 N 个文件 · 12.3 MB"。之前选错文件只能等它发完。
- **指纹文字短语（移植 `fingerprint_alphabet.dart` 的比对思路）**：官方用 60 多个图标让人工比对指纹，手表上换成四段词（如`月亮-书包-灯塔-鲸鱼`），由指纹前 16 个 hex 决定，可口头核对，不占包体。
- **接收历史的时间戳与本地化格式（移植 `system_date_time_formatter.dart`）**：记录改为落库真实时间戳，列表显示"今天 14:32 / 昨天 09:10 / 11月27日 14:32"，跨年带年份；老记录没有时间戳时回退显示原时间串。

### Changed
- **取消传输会通知对端**：点取消时立即 `POST /v2/cancel` 并停止后续文件，对端不必等 10 分钟会话 TTL（对齐官方 `cancelSession`）。
- **首启设备名随机化（移植 `alias_generator.dart`）**：不再一律 `Build.MODEL`，按当前语言生成"形容词+水果"（中/繁/英三套词表，其余语言落回英文），避免同型号手表在同网段撞名。

### Fixed
- 接收端对文本消息 `preview` 缺少上限判断：超过 16KB 的预览现在按官方语义当作普通 `.txt` 文件处理，不再一次性铺满手表屏幕（`message_preview.dart` 的 `maxMessagePreviewBytes`）。

## [1.6.2] - 2026-11-27

这一版开始移植官方**安卓 UI 客户端**（`app/lib`）的界面层能力。官方界面是 Flutter，代码不能直接用，移植的是它的信息层级与交互语义。

### Added
- **设备列表头像与信息层级（对齐设备列表项）**：每台设备显示首字母彩色圆形头像（颜色由设备名哈希得出，同一台设备恒定），名称下方第二行合并显示 `IP:端口 · 设备类型 · TLS`，右侧徽标显示 `★`（已收藏）或 `🔒`（加密连接）。此前列表只有"别名 + 一坨文字"，多设备时难以区分。
- **设备详情页 `DeviceDetailActivity`（移植 `pages/device_details_page.dart`）**：长按设备直接进入详情，集中展示设备名/类型、协议与端口、版本与型号、设备指纹（点按复制）、收藏开关、**收藏备注名**（官方 FavoriteEditDialog 的 custom alias）、白名单/黑名单开关。原来长按是三选一弹窗，看不到设备身份，也无法改备注。
- **收藏备注持久化**：`FavoritesStore` 新增 `entry/remark/setRemark/displayName`，备注写入收藏条目；列表与详情页优先显示备注名。
- **传输剩余时间 ETA（移植 `pages/progress_page.dart` 的 speed + remainingTime）**：进度页在速度后追加剩余时间（`剩余 42s` / `剩余 3m20s`），按当前实时速率估算。

### Changed
- 长按设备由"收藏/白名单/黑名单"三选一菜单改为详情页（原有能力全部搬进详情页，并补上备注与指纹）。

## [1.6.1] - 2026-11-27

1.6.0 之后又对照官方源码做了一轮"漏项清扫"，补上三处官方已有、我们没移植的实现。改动集中在协议身份与落盘安全，不涉及 UI 结构。

### Added
- **每设备独立证书与身份（移植 `crypto/cert.rs`）**：首次启动生成 RSA-2048 自签名证书（CN=WatchSend User，有效期 2020–2049），存为 PKCS12 放在应用私有目录；证书 SHA-256 指纹即本机身份。此前所有设备共用一张内置预置证书，指纹全部相同——TOFU 实际上无法区分设备，收藏/白名单也认不出"是谁"。现在每台手表身份唯一，证书生成失败会自动回退旧预置证书，保证加密服务仍可启动。
- **服务端按对端证书指纹识别发送方（对齐 `server_cert_verifier.rs`）**：HTTPS 连接握手后取对端证书 SHA-256 作为 `senderFingerprint`，黑名单、收藏设备、快速保存 `paired` 三处匹配全部改用它；明文连接仍依次退到 `info.fingerprint`、IP。官方在 https 模式下会忽略 `info.fingerprint`，此前按该字段匹配等于永远匹配不上。
- **设备身份统一**：`Device.identity()` 明确"https 用证书指纹、明文用协议指纹、兜底 IP"，设备菜单、收藏、黑白名单共用同一口径。

### Changed
- **文件名清洗对齐 `util/filename.rs`**：只取最后一段路径（防 `../` 穿越）、去控制字符、按 FAT/Windows 规则替换非法字符、去掉结尾的点与空格、Windows 保留设备名（`con`/`aux`/`com1`…）加前缀、按 UTF-8 截断到 255 字节并保留扩展名；落盘前再做一次 `getCanonicalPath` 目录包含校验。此前 `".."`、`con.txt`、超长名等都能原样落盘。

### Compatibility
- 本机证书变化会让其他设备对本机的旧指纹记录失效，首次连接需要重新信任一次（`own_cert_fp` 记录旧值时会自动清空本机 `trusted_certs`，避免拿旧指纹比对）。
- 文件名规则只在接收端生效，不影响发送端给对端的原始文件名（对端仍按官方规则自行清洗）。

---

## [1.6.0] - 2026-11-27

本次迭代是**协议层对齐官方 LocalSend 2.2**：把手表端从"只能在明文局域网里收发"升级到可与官方 Android / PC / CLI 客户端双向互通，并把官方几个实用能力（收藏设备、三态快速保存、文本消息、下载 API）按手表端体积与性能预算重写落地。所有改动均为手写 Java（不引入任何 Rust/Flutter 组件），包体基本不变。

### Added
- **HTTPS 发送与证书指纹固定（TOFU）**：发送端不再拒绝 `protocol: https` 的目标。首次连接只读 `/v2/info` 取对端证书 SHA-256，之后用自定义 `X509TrustManager` 在 **TLS 握手阶段**校验指纹，指纹不符直接握手失败（请求体不会发给假冒设备）；指纹按 `ip:port` 持久化到 `trusted_certs`。官方 Android 2.x 默认开启加密，这一项是互通的前提。
- **协议字段 `protocol` / `port` / `download` 真实上报**：接收端 `/v2/info` 与组播应答不再写死 `"protocol":"http"`、`"download":false`，改为读取真实监听状态（`use_https`、自定义端口、共享下载开关）。此前写死会导致加密设备把我们当明文设备而握手错配。
- **sha256 完整性校验与 `metadata.modified`**：发送端对 ≤32MB 的文件预算 SHA-256 并随 `FileDto.sha256` 下发；接收端边写边算比对，不一致删除文件并返回 400。同时按 `metadata.modified` 还原文件修改时间。
- **文本消息（`fileType: text` + 内联 `preview`）**：单文件、`.txt/.md/.log`、≤16KB 时按官方语义发送 `preview`，接收界面直接展示原文而不是"想给你发 N 个文件"。
- **收藏设备**：长按设备菜单新增"收藏/取消收藏"，收藏列表可在设置页管理；收藏同时是快速保存 `paired` 模式的白名单。
- **快速保存三态**：`off / on / paired`（对应官方 `QuickSaveMode`），替换原来两个容易互相矛盾的开关；旧 `auto_save` / `auto_accept_whitelist` 仍被读取与回写，升级不会行为突变。
- **下载 API**：新增 `POST /v2/prepare-download` 与 `GET /v2/download`，把本机 OldSend 媒体目录作为可下载源，默认关闭（设置项"共享下载"）。`info` 里的 `download` 随开关实时上报。
- **发现回退扫描**：多播 2 秒内一无所获时，自动对所在 /24 网段并发探测 `/v2/info`（先 HTTP 再 HTTPS），并把首次拿到的证书指纹写入信任库。路由拦截组播的办公/宿舍网络下明显提升"搜不到设备"的成功率。
- **网页分享 PIN 强制**：开启 `use_pin` 后，`/web/*` 接口必须带 `pin`（查询参数或 `X-LocalSend-Pin` 头），页面本身放行并在 401 时弹输入框；此前网页端可绕过 PIN 直接读写剪贴板与文件。
- **连接限额与会话 TTL**：服务端总连接上限 16、单 IP 6（官方 64/8，手表更保守），超限直接 503 并断开且不解析请求体；上传/下载会话 10 分钟无活动自动回收，避免手表长期驻留内存。

### Fixed
- 设备菜单选"加入黑名单"时会误把设备同时加进白名单（请求码 5013 的分支未按索引区分）。
- `SocketTimeoutException` 在发现扫描里被当作异常重试，导致扫描必须等满整个超时窗口才返回。

### Compatibility
- 协议版本仍为 `2.2`，v1 `/api/localsend/v1/*` 路由保留，旧版对端与旧版 WatchSend 之间行为不变。
- 新增能力全部"检测到才启用"：对端不开加密、不提供下载、不发文本消息时，走原来的明文/文件路径。
- 与官方 1.19.0+65（协议 2.2）逐字段对照过：`FileDto`、`prepare-upload`/`upload`、`prepare-download`/`download`、`QuickSaveMode`、`message_preview` 上限一致。

---

## [1.5.1] - 2026-11-26

本次迭代把旧版非全屏弹窗入口全部收敛到 Wear-Uikit 全屏容器，并补齐圆屏适配与日志实时刷新；同时修复了 uikit 全屏页读不到设置页配置的关键断裂问题。

### Added
- **日志页实时刷新**：打开日志页后，运行中新产生的日志会实时追加并自动滚动到底部；无日志时显示占位提示，正文采用等宽字体、左对齐逐行阅读。
- **UI 缩放全屏滑块容器**：新增 `UikitFullSliderActivity`，在全屏场景下调节界面缩放比例，横向滑块与纵向滚动互不抢占手势。

### Changed
- 移除旧版非全屏弹窗及其残留资源：md3 / uikit / watch 三套对话框布局、关于 / 手动 IP / 设置旧布局，以及旧的 MD3 开关与对话框工具类；相关入口统一收敛到 uikit 全屏容器，未精简既有功能。

### Fixed
- **全屏页配置失效**：修复 uikit 组件库原先读取独立 `uikit_prefs`、与设置页配置隔离的问题。此前「强制圆屏 / 低性能模式 / 主题 / UI 大小 / 下巴避让 / 系统返回拦截」等设置在全屏页不生效；现已与设置页统一到同一份配置并对齐 key。
- **长文本页滚轮缺失**：修复长文本页（关于页 / 日志页 / URL 页）普通文本分支未挂载 3D 滚轮与回弹效果的问题；滚轮挂载目标修正为整个内容容器，解决圆屏下内容被下压、底部被裁切的问题。
- **滑块页 / 确认页圆屏适配**：为这两个页面补充纵向滚动容器、圆屏左右避让与滚轮效果，避免长内容被圆形表盘切割。
- **滚轮安装时序**：修复布局尚未测量完成时滚轮效果静默不挂载的问题——等待视口高度就绪后重试安装，并跳过尚未测量完成的子项，避免首帧整体被误缩小、变淡。

### Notes
- 本地工具链为 AGP 3.6.3 / Gradle 5.6.4 / JDK 8，`assembleDebug` 构建通过；Release 签名仍需仓库 Secrets 中的签名参数。

---

## [1.5.0] - 2026-11-18

本次更新把整套 **Wear-Uikit** 组件库引入手表端，并修复了引入过程中暴露的一连串编译崩溃，`assembleDebug` / `assembleRelease` 均已恢复可稳定出包。

### Added
- **Wear-Uikit 组件库**：新增 `uikit` 包共 44 个纯 Java 控件与工具类，涵盖按钮、图标按钮、复选框、开关、单选框、滑块、步进器、进度条/进度环/圆形计时器、加载态、骨架屏、空视图、分割线、标签徽章、底部弹窗、跑马灯文本、分页列表、列表项模型等，零第三方依赖，最低兼容 Android 2.3。
- **Uikit 全屏页面**：新增列表选择（`UikitFullListActivity`）、文本输入（`UikitFullInputActivity`）、长文本阅读（`UikitFullTextActivity`）、二次确认（`UikitFullConfirmActivity`）、滚轮选择器（`UikitPickerActivity`）与日期选择（`UikitDatePickerActivity`）六个全屏容器，并全部在 `AndroidManifest.xml` 注册。
- **Uikit 主题系统**：新增 `UikitTheme` / `UikitThemePresets` / `UikitColor` / `UikitColorMixer`，支持预设主题叠加动态取色。
- **Uikit 对话框模板**：新增 `dialog_uikit_base` / `dialog_uikit_input` / `dialog_uikit_list` 三套布局与 36 个 `ic_uikit_*` 矢量图标。

### Fixed
- **构建阻断修复**：补齐布局中引用但从未创建的 `uikit_dlg_btn_pos`、`uikit_dlg_btn_neg`、`uikit_input_bg` 三个 drawable，AAPT 不再报 `resource not found`，Debug/Release 构建重新可用。
- 修复 `MainActivity` 因缺少 `dp()` 方法导致的编译报错。
- 修复 `MainActivity` 中未闭合的废弃注释、以及历史代码注入残留引发的 Java 编译崩溃，并补齐相关导入。
- 修复深色模式下部分页面残留浅色背景的「白底白字」问题，以及 Android 16 上 MD3 动态取色失效。

### Changed
- `versionCode` / `versionName` 与 CHANGELOG 同步（`versionCode 10500` / `versionName 1.5.0`），「关于」界面版本号同步更新。

---

## [1.4.0] - 2026-11-07

本次更新彻底攻克了 Android 11+ 的系统级限制，重构了底层 UI 缩放机制，并解锁了现代 Android 的极致视觉效果。

### Added
- **深色模式三态切换**：设置中新增「跟随系统 / 浅色模式 / 深色模式」选项，完美适配系统日夜切换。
- **开发者 Debug Keys 模式**：语言列表中新增极客选项，选择后全界面文字直接显示原始 Key，极速排查多语言缺漏。
- **Web 端多语言自适应补全**：网页端补齐剩余 7 国语言字典，支持自动识别 10 种语言变体。

### Fixed
- **物理级 UI 缩放重构**：彻底废弃被 Android 11+ 拦截的 `updateConfiguration` 方法，改用 `attachBaseContext` 全局重写 Context，完美解决高版本系统（如 Android 16）缩放失效问题。
- **深色模式视觉灾难**：修复主界面、设置页部分卡片和按钮在深色模式下“白底白字”或残留浅色背景的问题。
- **Material You 取色失效**：将 Application 主题从死板的 `Theme.Light` 升级为 `Theme.DeviceDefault.NoActionBar`，配合 `targetSdk` 升级，成功解锁 Android 12+ 的动态取色。
- 修复了多语言字典中 `size_label` 和 `save_to_dir` 键值冲突、繁体中文未全量覆盖，以及德语/日语等语种核心词条缺失的问题。

### Changed
- **SDK 版本升级**：`compileSdk` 与 `targetSdk` 从 28 升级至 30，全面拥抱现代 Android 存储及组件规范（开启 `requestLegacyExternalStorage` 保证老设备兼容）。

---

## [1.3.0] - 2026-11-02

本次更新是极客功能的大爆发，全面强化了本地文件管理、Web 端多语言适配以及底层系统权限的利用。

### Added
- **极客静默安装 (Beta)**：支持检测 Root 与 Shizuku 环境，开启后接收 APK 可实现后台无感静默安装。
- **ZIP 原生解压**：内置纯 Java 实现的 ZipUtils，在接收完成页和历史记录中长按，均支持一键解压 ZIP 文件（内置 Zip Slip 漏洞防御）。
- **Web 端 10 语种自适应**：网页端彻底重构，支持中、繁、英、日、韩、德、法、西、俄、葡 10 种语言自动识别与手动切换，并附带完整前端字典。
- **历史记录快捷管理**：长按接收记录支持分享、删除记录。如果是 ZIP 文件，列表会显示 📦 图标并支持直接解压。
- **快捷查看网页地址**：主界面点击 IP 状态卡片自动复制网页链接，长按则弹出超大字号 IP 地址，方便电脑端输入。

### Fixed
- 修复了深色模式下顶部状态卡片“白底灰字”的视觉灾难。
- 修复了设置页“动态取色”开关丢失，以及部分 UI 缩放累积乘法的死循环。
- 修复了网页端 `/web/files` 等接口未注册导致的 404 问题，以及前端变量插值 `${}` 解析失败的语法灾难。
- 修复了接收完成页无法获取真实文件路径，导致解压/安装按钮不显示、总进度卡在 0% 的 Bug。
- 补齐了 10 种语言变体在极客模式、解压、动态取色等新增功能上的本地化词条。
- 修复了日志弹窗“确认”按钮点击无响应的遗留问题。

### Changed
- **文档体系升级**：全面更新中英双语 README 及 10 语种导航栏。
- **路由补全**：在 `OldSendService` 中完整注册了 WebShareHandler 的所有 RESTful API 路由。

---

## [1.2.0] - 2026-11-01

本次更新聚焦于极致的个性化与系统级深度集成，同时重构了网页端与本地编译流程。

### Added
- **动态取色 (Material You)**：Android 12+ 设备支持跟随表盘/壁纸变色。
- **息屏微光模式**：传输时如果手表息屏，UI 自动隐藏仅保留进度条；抬腕亮屏恢复。
- **语音输入**：全局输入框动态注入语音输入按钮。
- **极致 UI 缩放**：滑动条范围拉大至 `0.3x ~ 2.5x`。
- **屏幕形状强制覆盖**：设置中新增覆盖选项。
- **底部下巴避让**：针对 Moto 360 等有黑边的圆表避让。
- **表冠阻尼震动**：拨动表冠滑动时触发微弱震动反馈。
- **本地一键编译脚本**：新增 `build.py`，支持本地环境自举与签名注入。

### Fixed
- 修复了网页端硬编码在 Java 中导致的编译灾难（剥离至 `assets/index.html`）。
- 清理了大量失效的开关与死代码。

### Changed
- **Web 端 UI 重构**：采用现代卡片式设计，支持拖拽、剪贴板双向同步。
- **Debug 包独立**：Debug 版本的包名与版本号追加独立后缀，可与正式版共存。

---

## [1.1.0] - 2026-10-31

本次更新是项目迈向“极致原生手表体验”的里程碑。

### Added
- **表冠滑动支持 (Rotary Input)**：适配 Wear OS 3+ 旋转表冠。
- **全局触觉反馈 (Haptic Feedback)**。
- **快捷磁贴 (Wear OS Tiles)**。
- **全自动 CI/CD 流水线**。

### Fixed
- 彻底扫除了残留的中英韩混杂硬编码，实现了多语言覆盖。
- 修复了清理 XML 节点属性时误删宽高导致的启动闪退。
- 修复了深色模式下部分按钮“白底白字”及进度条刺眼的问题。

### Changed
- **编译产物**：Release APK 文件名统一规范。

---

## [1.0.0] - 2026-10-30

首个正式版本，UI 彻底推翻重写，100% 针对圆屏、小屏幕与手势交互进行深度定制。

### Added
- **原生交互 UI**：纯 Java 动态渲染“药丸卡片”与居中大标题。
- **3D 滚轮特效**：列表滑动时边缘卡片缩小并降低透明度。
- **预测性返回动画**：模拟系统级 Predictive Back 体验。
- **无级 UI 缩放 (0.5x - 1.5x)**。
- **圆屏沉浸优化**：自动隐藏标题栏，启用滑动返回。
- **应用包名更新**：独立包名。

### Fixed
- 彻底修复了深色模式下的“白底白字”视觉灾难。
- 淘汰了刺眼的蓝色按钮与亮黄色进度条，替换为极客绿。

---

## 版本对比链接

[Unreleased]: https://github.com/styisme/watchsend/compare/v1.4.0...HEAD
[1.4.0]: https://github.com/styisme/watchsend/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/styisme/watchsend/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/styisme/watchsend/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/styisme/watchsend/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/styisme/watchsend/releases/tag/v1.0.0
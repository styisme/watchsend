# Changelog

本文件记录版本更新历史，格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

> **关于溯源**：项目为 [OldSend](https://github.com/styisme/oldsend) 的专属进化分支。版本号从 `v1.0.0` 重新开始计算。底层协议栈（LocalSend 兼容、跨网穿透、HTTP 服务）完整继承，UI 层已完全重构。

## [1.11.1] - 2026-11-28

### Added
- **多语言补齐到 52 种**：1.11.0 的 8 条新词条（`auto_wifi` / `auto_wifi_desc` / `auto_wifi_list` / `battery_stop` / `battery_stop_desc` / `compress_image` / `compress_image_desc` / `send_all_devices`）此前只有 `zh / zh_TW / en` 三个包，其余 49 种会回退成英文。现在全部 52 种语言都有了，非基础语种的设置页不会再一半中文一半英文。
- 顺带补齐 `ja / ko / de / fr / es / ru / pt` 这 7 个基础语种（1.11.0 时也只补了三个包）。

### Changed
- `docs/LANGUAGES.md` 与 `docs/LANGUAGES_EN.md` 的实测统计同步为：`zh / zh_TW / en` 365 条、7 个基础语种 366 条、其余 42 种各 361 条，合计 **18,819** 条；并注明这 42 种仍缺 `resend`、`choose_device_to_send`、`idle_stop_server`、`idle_stop_server_desc` 四条 1.7.12 历史键。

### Notes
- 49 种语言的译文为机翻级（人工译文集中在 10 个基础语种），术语对齐了 LocalSend 官方用法，但细节措辞不必当成终稿；有母语使用者反馈可单独提 PR 改。
- 语言列表、RTL 镜像、兜底链（当前语言 → `en` → `zh` → key 本身）均未改动。

## [1.11.0] - 2026-11-28

### Added
- **按 WiFi 自动启停**（设置 → 网络与自动化，开关默认关）：连上已记住的网络自动开监听，离开就暂停；列表里的网络可在“已记住的 WiFi”里增删，当前连着的那个一键收录。服务被手动停过之后，回到记住的网络再打开 App 也会自动补启。SSID 由系统隐藏（未给定位权限）时读不到，此时不会误判，保持原状态。
- **低电量暂停服务**（开关默认关）：电量 ≤15% 且未充电时停止监听并弹一条通知，充电或亮屏后恢复；正在传输时不会硬掐断，等这一轮结束再说。
- **发送失败自动重试**：同一文件失败后重试两次（1s、2s 退避），进度条加了单调保护不会因为重试往回跳。手表 WiFi 抖动导致的“部分文件上传失败”大多在这一层被吃掉。
- **下载支持断点续传**：`/download` 接口支持 `Range` 请求头，对端断线后只要续传缺的那一段，返回 206 + `Content-Range`；越界区间返回 416。
- **一对多广播**：长按设备 → “发送给所有在线设备（N）”，串行发给列表里每台，总进度按“单台字节 × 台数”算，每台切换时文件卡会显示当前设备名与序号。串行而非并发是刻意的——手表网卡撑不住多路同时拉。
- **接收后本地预览**：图片直接走内置图片查看器（支持双指缩放），文本/日志/JSON/CSV 走内置全文页（只读前 64KB）。手表没装第三方看图或文本 App 时不再只弹一句 `cannot_open`。
- **发送前压缩图片**（开关默认关）：仅对图片生效，长边超过 720 才缩并转 JPEG（质量 85），压缩后反而变大或失败时一律用原图。
- **磁贴改为状态开关**：下拉控制中心就能看到 `WatchSend · ON/OFF`，点一下直接起停服务（原来只会拉起 App 去扫码）；App 内启停服务后磁贴会主动刷新状态。
- **通知栏操作**：常驻通知新增“停止”，不用再钻三层设置；发送时挂一条带“取消”的进度通知，切走 App 后仍可中止（已发完的文件不回滚）。

### Notes
- 8 条新词条首发时只补了 `zh / zh_TW / en` 三个包，其余 49 种语言回退英文，已由 1.11.1 补齐。
- 断点续传只在“对端来取文件”（download API）这一侧生效；往官方 LocalSend 发文件时协议本身不支持 Range，只能整文件重发，靠上面的重试兜底。
- 低电量停服与 WiFi 自动启停都依赖前台服务存活；服务被系统彻底回收后不再监听网络变化，需靠开机自启或打开 App 补启。

## [1.10.3] - 2026-11-28

### Fixed
- **浅色模式下整页仍是深色**：`WebShareQrActivity`（“网页端地址”页）与 `PinPadActivity`（PIN 键盘页）把根视图背景写死成 `0xFF000000`，标题、正文、按钮也都按深色写死，从白色主界面进来像换了一个 App，这是“有深有浅”最明显的两处。现在都按 `ThemeUtils.isDarkMode` 分支取背景与文字色，浅色走 `#F4F6F8` / `#1C1B1F` 一档。
- **网页端地址页的复制按钮**：固定的 `#262626` 深灰块改为动态开启时用主色容器档（浅 `accent1-100` / 深 `accent1-900`）配强调色文字，与主界面次要按钮同一层级；关闭动态色时保持原来的深灰/浅灰。
- **二维码边界**：浅色主题下白底二维码压在白背景上会看不出边界，补一圈 `#E0E0E0` 描边；深色下仍保持白块独立。
- **PIN 键盘**：⌫ 与数字键按明暗切换（深色 `#262626` / `#444444`，浅色 `#FFFFFF` / `#E0E0E0` 并加描边），确认键改为跟随动态主色、文字按底色亮度取黑/白。

## [1.10.2] - 2026-11-28

### Fixed
- **浅色模式下同一界面“有深有浅”**：
  - 主操作按钮原本是两套：启动/停止、发送已跟随动态主色，而“+ 追加”仍是写死的 `watch_btn_primary` 深绿，两者在同一张卡片里一深一浅。现在启动/停止、发送、追加统一走 `stylePrimaryPill`：主色底、按下态深一档、文字按底色亮度取黑/白；动态色关闭时各自回到原来的背景与白字。
  - 应用图标固定取 `system_accent1-200`（深色主题档），浅色模式下比主色浅两个档位。改为按当前明暗取档：浅色 `accent1-600`（与主色同档），深色 `accent1-200`。
  - 强调文字从 `accent1-800` 收到 `accent1-700`，与主色（600）只差一档，按钮、进度条、状态文字不再一个很深一个偏浅。
  - 桌面小组件的“● 运行中”文字原先固定 `#388E3C`，改为动态强调色，取不到时保持原绿。

## [1.10.1] - 2026-11-28

### Changed
- **发送按钮接入动态取色**：主界面“发送”按钮原来是写死的 `#1976D2`（按下 `#1565C0`），与设置里的动态取色无关。现在按动态主色重绘圆角底，按下态取 `system_accent1-700/300`，文字按底色亮度自动取黑/白；动态色不可用（低于 Android 12、ROM 缺资源、开关关闭）时保留原来那套蓝白，不会因为拿不到色就变成绿色。
- **已选文件确认页同步**：底部“发送 N 个文件”按钮与每行的 ✓ 勾选标记改为跟随动态主色，取不到时回退默认绿。

### Notes
- 桌面小组件的“停止服务”按钮仍使用静态 `watch_btn_send`（RemoteViews 无法设置带圆角的动态背景），与主界面配色可能不一致。

## [1.10.0] - 2026-11-28

### Added
- **应用图标支持 MD3 动态取色**：图标原来是一整块 vector（黑圆底 + 绿色外圈 + 绿色上箭头 + 白色下箭头），单层无法只染绿色。现拆成 `ic_launcher_disc`（黑底）、`ic_launcher_accent`（外圈与上箭头）、`ic_launcher_arrow`（白箭头）三层，新增 `uikit/AppIcon` 在运行时按 `SRC_IN` 把绿色层换成动态主色（固定取 `system_accent1-200`，黑底上足够亮），白箭头与黑底保持原样；取不到动态色（系统低于 Android 12、ROM 缺该档资源、或用户关掉开关）时不染色，保留图标原本的 `#81C784` 绿。
- **桌面小组件图标跟随取色**：`QuickWidgetProvider` 用 `AppIcon.toBitmap` 把动态版本塞进 `RemoteViews`（`widget_main` 新增 `widget_icon` id），主色变化后刷新即可看到；布局里的 `src` 保留静态图标作兜底。

### Notes
- 启动器桌面图标、快捷设置磁贴、通知小图标由系统绘制，第三方应用无法运行时改色，仍为静态绿；Android 13 的“主题图标”需要额外的 adaptive / monochrome 资源，本轮未启用。

## [1.9.2] - 2026-11-28

### Fixed
- **动态取色开了却不变色**：`UikitColor.getPrimaryColor` 原先先解析 `android:attr/colorAccent`，而手表主题（`Theme.DeviceDefault.*`）里的 accent 是写死的常量，动态色一开就被它短路，后面的 `system_accent1_*` 永远取不到。现在直接读 Android 12+ 的 M3 色调资源，浅色用 `system_accent1_600`、深色用 `system_accent1_200`，个别 ROM 缺这两档时退到 `500`/`100`。
- **主界面选完文件就变回绿色**：`updateSelectedFilesUI()` 的空分支里混进了一段旧版按钮配色副本，每次选/清文件都会把五个次要按钮刷回硬编码的深绿文字与浅绿底，动态色被覆盖。已删除该残留代码。
- **主按钮判断方式错误**：`MainActivity` 用 `primaryColor != 0xFF388E3C` 决定是否套用动态色，壁纸取色恰好落在绿色系时整块跳过。改为无条件套用（动态色关闭时主色本就是默认绿，效果与旧版一致），按钮文字色按亮度自动取黑/白。
- **两套取色实现分叉**：`ThemeUtils.getPrimaryColor/getAccentColor` 与 `UikitColor` 各自维护一份逻辑，主界面标题/按钮与 uikit 控件可能取到不同颜色。`ThemeUtils` 改为委托 `UikitColor`，单一数据源。
- **改完开关返回主界面不刷新**：`MainActivity.onResume` 只比较语言、`dark_mode` 布尔与缩放，动态取色与 `theme_mode` 都不在检测项里，切完开关回到主界面颜色停在旧值。现在检测 `theme_mode` 实际明暗与 `dynamic_color`，变化即重建。

### Changed
- 主界面五个次要按钮（记录/跨网/设置/扫描/加 IP）在动态取色下按 `system_accent1-100/900` 重绘圆角底色并保留按压态，文字改用 `system_accent1-800/200`。
- 传输页的接受/完成按钮、总进度条、完成状态文字、“打开文件”按钮，以及跨网页“生成密码”按钮、输入页语音按钮、设置页右侧箭头，全部改为跟随动态主色/强调色。

## [1.9.1] - 2026-11-28

### Fixed
- **快速保存设置切完又变回“每次都询问”**：`QuickSave` 与 `FavoritesStore` 原先只认 `OldSendService.getContext()`，服务尚未起来时该接口返回 null，`setMode` 直接 return、`getMode` 落回默认值，重新进入设置页就显示回旧模式。现在由入口 Activity 注入 application context 兜底，`MainActivity` 与设置页启动时都已接上；`setMode` 改为写入后立即读回校验，失败时给出提示并在日志留痕，不再静默回滚。
- **“仅收藏设备自动接收”匹配不到收藏**：接收侧 `PrepareUploadHandler` 会把发送方指纹归一化（转小写、去掉 `sha256:` 前缀与冒号），收藏侧却拿原始字符串直接相等比较；对方发来大写十六进制或带冒号分隔的指纹时必然失配，收藏过的设备照样弹窗。`FavoritesStore` 的增删查改统一按归一化口径比较，已存的旧条目无需迁移。
- **旧白名单子串误命中**：`paired` 模式兼容旧 `whitelist` 时用 `contains` 判断，`10.0.14.5` 会被 `10.0.14.55` 误放行；改为按逗号切分后逐项精确比对（比较前同样归一化）。

### Changed
- 快速保存的三种模式选择列表给当前生效项打勾，选之前就能看出处于哪种模式。

## [1.9.0] - 2026-11-28

### Added
- **从右到左（RTL）语言的界面镜像**：语言包里的 العربية（ar）、فارسی（fa）、עברית（he）、اردو（ur）此前只能按从左到右排布——列表项从左边起、文字靠左、右侧箭头指右，阿拉伯语用户看到的是“反过来”的界面。新增 `uikit/RtlSupport`：按当前选中语言判断书写方向，把根视图设成 RTL，让列表顺序、START/END 对齐、`marginStart`/`paddingStart` 一起镜像。4 个语种各 353 条词条均在位，无缺键。
- **方向相关的细节同步**：设置页右侧箭头在 RTL 下改指左；传输进度条改从右往左推进；文本输入框与副标题改为起始边对齐；`item_device`、`activity_main`、`activity_select_list`、`activity_transfer`、`activity_watch_settings` 里 7 处不对称的 `layout_marginLeft` / `paddingLeft` 补上对应的 `marginStart` / `paddingStart`，旧版本仍读原属性。
- **滑动返回天然双向**：手势本来就接受左右两侧边缘起手、正负方向都能退页，RTL 下无需另设规则，从另一侧边缘向内滑即可。

### Changed
- `AndroidManifest` 打开 `android:supportsRtl="true"`；17 个 Activity 在 `setContentView` 后统一调用 `RtlSupport.apply(this)`，切换语言后随页面重建生效。低于 Android 4.2 的设备保持 LTR，避免 `Gravity.START` 不可用导致错位。

### Docs
- 10 份 README 徽章与“当前版本”行、10 份用户指南的适用版本行同步到 1.9.0（versionCode 10900）。
- 中、繁、英三份指南在界面章节补充 RTL 说明：选这四种语言后界面镜像、箭头改指左、滑动返回从另一侧边缘起手。

### Verified
- `gradle assembleDebug` 通过（JDK8 / build-tools 30.0.3）。
- 语言包解析：`ar` / `fa` / `he` / `ur` 各 353 条，与其余非基础语种一致。
- 未在真机验证（云主机无 KVM、无 X11），RTL 镜像后的实际排版、阿拉伯数字与混排文本的观感以装机为准；已知未覆盖项：进度环上的百分比文本仍按 LTR 绘制，视觉可读但方向未镜像。

## [1.8.2] - 2026-11-28

### Fixed
- **长语言把设置页撑爆**：开关项的说明文字在德语、俄语能到 67–78em（约六七行），中文只要一两行，同一行卡片高度能差三倍，一屏只放得下一项。现在说明限三行、超出用省略号，点一下说明区域可打开全文页（`UikitFullTextActivity`），短语言不受影响。
- **标题与列表条目无限换行**：`UikitFullListActivity` 的页标题、条目主标题与副标题，`UikitFullConfirmActivity` 的标题与提示，`UikitFullText`/`UikitFullInput` 的页标题，以及 `TransferActivity` 的主标题，此前都没限行。现在统一走 `TextFit.wrap()`：标题最多两行、提示最多六行，超出省略。
- **按钮文字被裁**：带文字的按钮（接受、拒绝、清空全部、返回上一页、STUN 探测、编辑等）之前固定字号、单行、无省略，德语 “Alle Dateien löschen” 之类会被切掉尾字。现在统一走 `TextFit.fitSingleLine()`：Android 8.0+ 交给系统 autosize 在 16→11sp 之间自适应，更低版本按视觉宽度估算降字号（中日韩字符算 1em、拉丁按 0.55em），降到下限仍放不下才省略。
- **设备名与详情值一刀切单行**：设备列表的名称、设备详情里的值（路径、SSID、型号）此前都是单行尾部省略，长语言与长路径只剩开头几个字；现在放宽到两行。
- **德语复合词被整段挤出行外**：可换行的文本统一打开 `breakStrategy=high_quality` 与普通断词，长单词必要时从词中断开，不再把整行顶出去。

### Added
- **`uikit/TextFit`**：把“多语言文本适配”收口成一个工具，`wrap(tv, maxLines)` 负责可换行文本（限行、省略、断词），`fitSingleLine(tv, maxSp, minSp)` 负责按钮类不可换行控件（autosize + 兜底估算）。以后新增页面只调这两个方法，不用每处自己写 `setMaxLines`。

### Docs
- 10 份 README 徽章与“当前版本”行、10 份用户指南的适用版本行同步到 1.8.2（versionCode 10802）。
- 中、繁、英三份指南在设置章节补充：长语言的开关说明显示三行后以省略号收尾，点说明区域可看全文。

### Verified
- `gradle assembleDebug` 通过（JDK8 / build-tools 30.0.3）。
- 用语言包实测最长条目做了阈值核算：德语 `idle_stop_server_desc` 75.3em、俄语 78.6em、英语 55.5em，三行上限约 33em，故保留“点开看全文”这条出口，而不是硬压成两行。
- 未在真机验证（云主机无 KVM、无 X11），三行上限与按钮降字号的观感以装机为准。

## [1.8.1] - 2026-11-28

### Fixed
- **软件内返回此前只覆盖 5 个页面**：17 个 Activity 里只有 MainActivity、TransferActivity、CrossNetActivity、SelectListActivity、WatchSettingsActivity 拦了返回键，uikit 的 8 个全屏页与设备详情、PIN 键盘、已选文件、网页二维码共 12 个页面在开关开启时按返回仍会直接退出，和设置项“开启后禁用系统返回”的说明不符。新增 `uikit/BackGate`，全部 17 个页面统一调用 `BackGate.handleBack()`：开启时吞掉返回并提示横滑，关闭时原样交回系统。4 个此前连滑动都没挂的页面（设备详情、PIN 键盘、已选文件、网页二维码）一并补上 `dispatchTouchEvent` 手势。
- **两页提示写死中文**：`CrossNetActivity` 与 `SelectListActivity` 曾硬编码“请滑动屏幕返回”，日、韩、俄等语言界面下会突然蹦中文。现统一走 `swipe_back_toast` 词条（52 个语言包齐备），并加 800ms 节流，连按返回不会把 Toast 刷成一串。
- **潜在空指针**：`UikitUi.handleSwipeBack()` 里 `a == null` 判断排在读配置之后，传入空 Activity 会先崩在 `isSwipeBackEnabled(a)`；判断已提到最前。
- **低性能判定两套标准**：`WatchUiUtils.isLowPerformance()` 直接读 `low_perf_mode` 原始值、缺省 false，没走 `UikitConfig.isLowPerf()` 按内存与核心数的自动判定，导致自动判为低性能的设备上仍走 250ms 跟手动画。现两处同源。

### Changed
- **滑动返回实现合并为一份**：`WatchUiUtils` 与 `UikitUi` 各有一套手势代码，圆角、投影、复位只有前一套有，同一手势在两个页面族观感不一样。现统一到 `UikitUi.handleSwipeBack()`（圆角 + 投影 + 结束后复位），`WatchUiUtils.handleSwipeBack()` 只做转发。
- **起手点限定在边缘**：必须在左右边缘 16dp 内起手才认作返回手势，列表里横向滑动不再误退页；低性能快退路径同样受此限制。
- **删除反义旧名**：移除 `isSystemBackBlocked()` 的三个副本与 5 处调用点，`setSystemBackBlocked()`（无调用死代码）一并删除。旧键 `block_system_back` 仍随写入回写取反值，降级到 1.7.12 行为不变。

### Docs
- 10 份 README 徽章与“当前版本”行、10 份用户指南的适用版本行同步到 1.8.1（versionCode 10801）。
- 中、繁、英三份指南的滑动返回段改写：说明起手点在边缘 16dp 内，并明确“全部页面统一遵循”，不再需要按页记忆哪页能用返回键。

### Verified
- `gradle assembleDebug` 通过（JDK8 / build-tools 30.0.3）。
- `javap` 逐个核对 17 个 Activity 类，全部同时具备 `onBackPressed()` 与 `dispatchTouchEvent()`，无遗漏；`请滑动屏幕返回` 硬编码残留 0 处（仅剩词条定义）。
- 未在真机验证（云主机无 KVM、无 X11），边缘起手阈值与跟手手感以装机为准。

## [1.8.0] - 2026-11-28

### Added
- **10 种语言的使用指南（docs/USER_GUIDE_\*.md）**：简体中文、繁體中文、English、日本語、한국어、Deutsch、Français、Español、Русский、Português，与 10 份 README 的语言集合一致。每份讲安装与首次启动、界面与基本操作、发送、接收、网页端与跨网、历史记录、设置项逐条说明、常见问题，正文用语与该语种 App 内实际显示一致。文件首行是同组语言导航，可横向切换；10 份 README 的语言导航下方各挂了本语言的指南入口，`docs/` 下的指南之间也互相链接。

### Changed
- **设置页顺序与分组整理**：通用段改为“外观 → 交互 → 省电”的次序（主题、语言、界面缩放、屏幕形状、底部避让、动态取色、启用软件内返回、低性能模式、熄屏空闲暂停）；网络段固定为“HTTPS → 端口 → 网页端 → 网页二维码 → 共享下载 → 开机自启”，网页二维码紧跟网页端；**PIN 两项（启用、修改）从“传输”移回“设备与安全”**，与设备名相邻。分组仍是原有的 6 段，未新增词条，键名、默认值与交互逻辑一律未动，老配置升级后完全沿用。
- **设置项改为“标题 + 当前值”两行显示**：入口行以前把当前值拼进标题（`拦截危险后缀: exe,bat,cmd,vbs,sh`），320px 宽的圆表上长值会折行并把箭头挤出可见区。现在与开关行一致——16sp 标题在上、12sp 灰色当前值在下，超长值最多占两行；收藏设备只把数量放到副行。

### Docs
- CHANGELOG 顶部新增本版本；10 份 README 的版本徽章与“当前版本”行同步到 1.8.0（历史叙述中的 1.7.13 保留原样）；10 份用户指南的“适用版本”行同步为 1.8.0（versionCode 10800），设置项表按新的分组与顺序重排。
- 版本号规则自此明确执行语义化版本：修 bug 只升修订号（PATCH），新增功能或文档能力升次版本号（MINOR），破坏兼容性变更才升主版本号（MAJOR）；`versionCode` 按 `主*10000 + 次*100 + 修订` 单调递增（本次 `1.8.0 → 10800`）。

### Verified
- `gradle assembleDebug` 通过（JDK8 / build-tools 30.0.3），无新增依赖。
- 10 份指南与仓库版逐份比对一致（除交付版文件名的链接前缀），设置项表 27 行 × 10 份均已重排。
- 设置页仅编译级验证（云主机无 KVM、无 X11），两行布局在真机圆表上的实际换行效果以装机为准。

## [1.7.13] - 2026-11-28

### Changed
- **"拦截系统返回"改为正向语义"启用软件内返回"**：设置页的开关现在直接回答"要不要用 App 内的返回方式"。开启 = 禁用系统返回，返回动作由界面内滑动手势接管；关闭 = 系统返回照常工作，同时滑动返回不再接管触摸。原来的双重否定（"拦截系统返回"开/关）容易让人反着理解，尤其和"滑动返回"摆在同一页时。
- **滑动返回补上开关闸门**：`WatchUiUtils.handleSwipeBack()` 与 `UikitUi.handleSwipeBack()` 入口新增 `isSwipeBackEnabled()` 判断，关闭后直接 `return false`，触摸事件交还给控件与系统，不再出现"已经关掉却还能滑走"的情况。
- **配置键迁移**：新键 `swipe_back_enabled`（`UikitConfig.KEY_SWIPE_BACK`），首次读取时若不存在则读取旧键 `block_system_back` 的布尔值并写回，老用户升级后行为不变（原来开拦截的仍是滑动返回，原来关拦截的仍是系统返回）。写入时同时回写旧键的取反值，方便回退到 1.7.12 时行为一致。`isSystemBackBlocked()` 保留为兼容别名，四个 Activity 的 `onBackPressed()` 调用点无需改动。
- **52 个语种全部补齐新文案**：`swipe_back` / `swipe_back_desc` 在 52 个语言包里都写了本地译文（不是只翻 10 个基础语种再回退英文），并删除了旧的 `block_system_back` / `block_system_back_desc` 两条反义词条，词条总量维持 18,403 条不变。

### Docs
- 10 份 README 的版本徽章与版本说明同步到 1.7.13；语言统计从"每种 353 条、共 18,363 条"更正为实测值：10 个基础语种 357–358 条（多出 1.7.12 只翻了基础语种的 4 条）、其余 42 个语种 353 条，合计 18,403 条。
- `docs/LANGUAGES.md` / `docs/LANGUAGES_EN.md` 更新同一组数字，并在语言清单表后加了"条目数为何不同"的脚注。
- `CONTRIBUTING.md` / `CONTRIBUTING_EN.md` 的返回键 FAQ 改写为新语义，说明迁移逻辑。

### Verified
- `gradle assembleDebug` 通过（JDK8 / build-tools 30.0.3，无第三方依赖变化）。
- 桌面 JVM 反射校验：`langCount=53`、`dict=52`、全量加载 18,403 条；52 个包均含 `swipe_back` / `swipe_back_desc`，`block_system_back` 残留 0 处。
- 未在真机/模拟器验证（云主机无 KVM、无 X11），手势行为仅到静态与编译级确认。

---

## [Unreleased]

### Planned
- 基于 WebRTC 的手表端 NAT 打洞。
- 表盘 Complication（复杂功能）状态显示支持。

---

## [1.7.12] - 2026-11-28

### Added
- **历史记录一键重发**：历史条目的操作菜单首位新增"重新发送"，选中后把原文件放回待发送列表并退回设备列表，点设备即可发送（原来要重新走"选文件→找目录→选中"）。
- **传输限速（省电）**：新增 `PowerGuard` / `TransferThrottle`，上传与接收的字节流按上限节流。低性能模式 2 MB/s；电量低于 15% 且未充电时 512 KB/s（两者同时命中取更小）。不限速时该对象是空操作。
- **熄屏空闲暂停服务**：设置 → 网络与自动化新增独立开关（**默认关闭**）。开启后熄屏 3 分钟暂停 HTTP/组播监听以省电，亮屏（或 `USER_PRESENT`）自动原地恢复；停服前会检查是否仍有传输在进行，有则每 60s 顺延，避免掐断传到一半的文件。

### Changed
- 新增词条 `resend` / `choose_device_to_send` / `idle_stop_server` / `idle_stop_server_desc`，已补 zh、zh_TW、en、ja、ko、de、fr、es、ru、pt 十种基础包；其余语种按既有"未覆盖词条回退英文"策略显示英文。

### Verified
- `gradle assembleDebug` 通过。
- 桌面 JVM 校验新词条：zh / zh_TW / en 为人工译文，ar / vi / fr 正确回退英文，且不影响其余 18,363 条既有译文（懒加载后仍 52 包）。
- 静态断言：熄屏监听注册、停服前传输检查与 3 分钟阈值、默认关闭读 prefs、亮屏恢复、`beginTransfer/endTransfer` 成对、发送与接收两侧均接入限速、历史菜单首位重发与索引重排（zip 3 项→4 项）、设置页开关、限速阈值常量。

### Docs
- 审查并重写全部 10 份 README（zh / zh_TW / en / ja / ko / de / fr / es / ru / pt），统一版本号、特性列表、语言数字与关联文档，补齐此前缺失的语言导航与风险提示。
- 新增 [docs/LANGUAGES.md](docs/LANGUAGES.md) 与 [docs/LANGUAGES_EN.md](docs/LANGUAGES_EN.md)：52 种语言清单、切换方式、回退规则、懒加载机制、新增语言/词条的流程。
- 修正事实错误：旧 README 宣称"网页端支持 52 语种"，而 `assets/index.html` 实际只有 10 种语言字典；现按实现分别写明 App 52 / Web 10。
- 更新 CONTRIBUTING（中英）：补 `PowerGuard` / `TransferThrottle` 与新词条登记流程；更新 `docs/DEVELOPMENT_CONSTRAINTS.md` 的 1.7.10—1.7.12 验证范围；`docs/AI_DISCLOSURE.md` 增补 1.7.8—1.7.12 的 AI 参与记录。

### Notes
- 接收端断点续传与"文本自动进剪贴板"**此前已实现**（`FileSaver.createResumeTarget` + `UploadHandler` 追加写入与续算摘要；`UploadHandler` 文本落盘后写剪贴板），本次未重复开发。
- 发送端仍为整包重发、由接收端跳过已落盘前缀，因此续传省的是"重复落盘与校验"，不是带宽；真正的按偏移续发需要私有协议扩展，官方 LocalSend 客户端不认该参数，故未做。
- WebRTC 打洞与表盘 Complication 未实现：前者需要引入 libwebrtc 原生库（数十 MB），与"零第三方依赖 + 600KB 级包体"的工程约束直接冲突；后者依赖 Wearable Support Library，同样属于新增依赖。愿以现有 STUN + 手动公网地址/FRP 方案与快捷方式入口替代。

---

## [1.7.11] - 2026-11-28

### Changed
- **语言包改为按需加载**：52 种全量译文共 18,363 条，原来 `static` 块启动即全部建成 HashMap（约 1MB 堆 + 数十毫秒冷启动）。现只常驻 10 个基础包（en / zh 兜底及主要语种），其余 42 包在当前语言确定后由 `ensureLoaded()` 构建一次（幂等、线程安全）。`get()` 增加懒加载兜底，`normalizeCode()` 不再依赖字典是否已装。
- **实时日志合批刷新**：低性能模式下日志页不再每条 `appendLive` 都 `setText` 全量文本重排 + 滚动，改为 500ms 合批一次，只滚动一次。
- **传输进度节流**：低性能模式下进度回调只在百分比进位或距上次刷新超过 700ms 时才刷 UI（原来每几十毫秒全量重绘，传输页自己会拖慢传输）。
- **网段探测降并发**：低性能模式回退扫描线程 16→6、单机探测超时 400→700ms、总等待 9→15s，避免 254 个探测把弱 CPU 打满。
- **滚动边缘特效关闭**：低性能模式对整页递归 `OVER_SCROLL_NEVER`，去掉边缘拉伸/发光的额外绘制；`ScrollView` 滚轮路径同样处理。
- **骨架屏去掉渐变**：低性能模式不建 `LinearGradient` shader，改纯色灰块，省掉逐像素插值与每帧矩阵运算。
- **低性能模式支持自动判定**：用户从未手动设置过时，按设备能力自动开启（应用可用堆 ≤256MB / `isLowRamDevice` / ≤2 核）；一旦用户手动拨动开关，就完全以用户设置为准。设置页开关现在显示"实际生效值"，不再出现"开关关着但动画已关"。

### Verified
- `gradle assembleDebug` 通过。
- 桌面 JVM 反射校验语言包：静态初始化后仅 10 个包；对 `CODES` 全量调用 `ensureLoaded` 后 52 包、词条总量 18,363（其中 ja/ko/de/fr/es/ru/pt 为历史上的 354 条，含 1 条 debug 键）；重复调用幂等，包数不变。
- 静态断言：`ensureLoaded` 42 个分支、`static` 块 10 次 `dict.put`、日志 500ms 合批、进度 1%/700ms 节流、扫描并发生效、`OVER_SCROLL_NEVER` 递归、骨架纯色路径、设置页读 `isLowPerf`。

### Notes
- 懒加载省的是**运行时堆与冷启动**，不是安装包体积（字符串常量仍在 dex 中）。
- 无 KVM/真机，以上为编译级 + 桌面 JVM 级 + 静态断言级验证，真机帧率与内存实测未做。

---

## [1.7.10] - 2026-11-28

### Changed
- **低性能模式关掉更多动画**：此前只关闭滚轮缩放、进场缩放和滑动返回跟手；现在额外关闭——
  - 按钮 `UikitButton` / `UikitIconButton` 的按压缩放与透明度过渡；
  - `UikitLoadingBar` 的无限扫描动画（改为静止进度块）；
  - `UikitLoadingView` 的三点呼吸动画（改为静止三点）；
  - `UikitSkeleton` 的微光扫动（只保留静态灰块）；
  - `UikitSwitch` 的拨动过渡（直接跳到目标状态）；
  - `UikitBottomSheet` 下拉未过阈值时的 200ms 回弹（直接归位）。
- 新增 `UikitConfig.animDuration(ctx, ms)`：低性能模式统一返回 0，动画时长集中收口，不再各处散写判断。

### Verified
- `gradle assembleDebug` 通过；静态检查确认 9 处动画入口均有 `isLowPerf` 闸门（`UikitButton`、`UikitIconButton`、`UikitLoadingBar`、`UikitLoadingView`、`UikitSkeleton`、`UikitSwitch`、`UikitBottomSheet`、`UikitUi`、`WatchUiUtils`），单 dex 包内 36 种语言译文仍在。

---

## [1.7.9] - 2026-11-27

### Added
- **剩余 36 种语言全部补齐为全量 353 条**：العربية / Azərbaycan / Български / বাংলা / Bosanski / Català / Čeština / Dansk / Ελληνικά / Eesti / Euskara / فارسی / Suomi / Filipino / עברית / हिन्दी / Hrvatski / Magyar / Íslenska / Lietuvių / Latviešu / Македонски / Bahasa Melayu / Norsk bokmål / नेपाली / Română / Slovenčina / Slovenščina / Shqip / Српски / Svenska / Kiswahili / ไทย / Українська / اردو / Oʻzbekcha。
- 词条总量 18,363 条（52 种 × 353 条），语言包内不再有"核心 22 条 + 英文兜底"的分层结构。

### Changed
- 36 种语言的译文来源分三层：官方 LocalSend 人工译文（27 种可用，覆盖设置/发送/接收等高频词）→ 既有核心文案 → 离线 NLLB-200 模型机翻，再对约 140 处明显错译做人工修正。
- 每种语言独立成 `installFullXx()` 方法，避免单个方法体超过 JVM 64KB 上限。
- 修正一处合并错误：克罗地亚语曾误用塞尔维亚语（西里尔）官方译文，现已回到拉丁字母的克罗地亚语；塞尔维亚语统一为西里尔字母。

### Verified
- 桌面 JVM 反射：52 个语言包全部 353 条、零缺失，`langCount=53`，静态初始化 40ms。

---

## [1.7.8] - 2026-11-27

### Added
- **意/荷/波/土/印尼/越 6 种语言升级为全量词条**：此前这 6 种只有 22 条核心文案、长尾回退英文，现在每种 353 条全部为本地译文，界面不再出现中英混排。
- **新增意大利语**（`it` / Italiano），语言总数 51 → 52，语言选择页自动变为 53 项（含"跟随系统"）。
- 六种全量译文单独抽成 `installFullIt/Nl/Pl/Tr/Id/Vi()` 方法，避免静态初始化块超过 JVM 64KB 方法体上限。

### Changed
- `I18nManager` 中这 6 种语言由 `new HashMap<>(en) + 22 条覆盖` 改为独立 `HashMap` 全量 353 条，查找时不再依赖英文回退。

### Verified
- 桌面 JVM 反射校验：`langCount=53`、`dict=52`，六种语言各 353 条、缺失 0；与英文取值完全相同的仅 9–19 条，且均为 `/`、`OK`、`PIN:`、`IP `、`PC`、`Web` 这类本就通用的短串。

---

## [1.7.7] - 2026-11-27

### Added
- **语言从 10 种扩到 51 种**：新增 العربية / Azərbaycan / Български / বাংলা / Bosanski / Català / Čeština / Dansk / Ελληνικά / Eesti / Euskara / فارسی / Suomi / Filipino / עברית / हिन्दी / Hrvatski / Magyar / Bahasa Indonesia / Íslenska / Lietuvių / Latviešu / Македонски / Bahasa Melayu / Norsk bokmål / नेपाली / Nederlands / Polski / Română / Slovenčina / Slovenščina / Shqip / Српски / Svenska / Kiswahili / ไทย / Türkçe / Українська / اردو / Oʻzbekcha / Tiếng Việt 共 41 种。
- 每种新语言继承英文全量词条并覆盖 22 条最常用界面文案（取消/确定/关闭/设置/发送/接收/文件/应用/文本/启动/停止/运行中/已停止/暂无设备/语言/跟随系统/网络/关于/日志/暂无日志/已完成/接受），未覆盖的长尾词条自动回退英文——**任何语言都不会出现裸 key**。
- 系统语言自动识别：`iw→he`、`in→id`、`tl→fil`、`no→nb` 等历史 Locale 代码已做归一化，系统语言为 51 种之一时自动切换。
- 语言选择页改为从 `I18nManager` 动态取表，新增 `langCount()/codeAt()/labelAt()`，不再在设置页写死语言数组；列表本身支持圆屏滚轮滚动（51 项）。

### Changed（繁体中文本地化重做）
- 繁体中文由"简体直转"改为**全量台湾用语重写**（353 条全覆盖，不再靠 `new HashMap<>(zh)` 继承简体）：设定 / 檔案 / 資料夾 / 網路 / 裝置 / 連線 / 應用程式 / 介面 / 搜尋 / 儲存 / 預設 / 連接埠 / 逾時 / 傳送 / 協定 / 位址 / 相簿 / 剪貼簿 / 通知列 / 還原 等术语统一，去除"設置、文件、文件夹、網絡、設備、端口、發送、默認、界面、當前、超時、恢復"等大陆用法。
- 修复旧实现里 zh_TW 词条重复定义（同一 key 两处 `put`，后写覆盖前写导致"改过的新译被旧译文盖回"）的问题。

### Verified
- 桌面 JVM + 反射校验：`langCount=52`（含"跟随系统"）、`dict=51` 语言包、52 个 code 与 52 个 native label 一一对齐、零缺失；22 条核心词条在 51 种语言里 100% 覆盖；繁体 `port=連接埠`、`send=傳送`、`no_device=暫無裝置，請點選掃描`。

## [1.7.6] - 2026-11-27

### Fixed（官方客户端上传全部 400 的根因）
- **`Transfer-Encoding: chunked` 请求体完全没解码**：官方 LocalSend 2.x 客户端上传文件用的是 `reqwest::Body::wrap_stream(...)`（`packages/core/src/http/client/mod.rs: upload_body`），流式 body 没有 `Content-Length`，走的是 chunked 编码。我们此前把 `req.body` 当原始文件字节直接写盘，写进去的"文件"里混着 `1a\r\n`、`\r\n` 这类分块标记——1.7.5 之前表现为"传输成功但文件打不开"，1.7.5 加了长度校验后就直接 `HTTP 400`（截图里"正在发送文件"三个文件全红就是这个）。
- 新增零依赖 `http/ChunkedInputStream`，在 `HttpRequest.parse` 里统一解码分块（支持分块扩展段 `;ext=1`、结尾 trailer、空 body），上层 Handler 拿到的就是纯数据；`contentLength` 置 -1 表示长度未知。
- `RegisterHandler.readAll` 同步改为"读到流末尾"，避免对已解码的流再做一次分块解析（否则 prepare-upload 的 JSON 会解析失败）。
- 桌面 JVM 实测：100KB 随机数据按 1/1000/4096/12345/70000 不规则分块 + 带扩展段 + trailer 发送，解码后逐字节一致、SHA-256 一致、`entry.size` 与实际长度相等（不再触发 400）；空分块 body 立即返回 -1。

## [1.7.5] - 2026-11-27

### Fixed（本轮 Bug 审计，全部有源码级证据）
- **分享文件点了没反应/对方打不开**：ACTION_SEND 只塞了 `EXTRA_STREAM`，没设 `ClipData`。系统只按 data/ClipData 授予临时读权限，接收方拿到 Uri 再读会抛 `SecurityException`。已补 `setClipData(ClipData.newRawUri(...))`。
- **重传会生成 "文件 (1).ext"、续传代码是死代码**：`createTarget` 遇到同名文件一律改名，导致 `UploadHandler` 里 `existingSize > 0` 的续传分支永远进不去，中断后重传只会多出一份副本。新增 `createResumeTarget(name, saveAlbum, expectedSize)`：同名文件比预期小就复用续写，否则才去重改名。
- **半截文件被当成"接收成功"**：长度不够时旧代码只打一行日志，随后照旧写历史、发 `TRANSFER_DONE ok=true` 广播。现在直接返回 400 并保留残文件等续传，sha256 校验也挪到长度校验之后（否则残文件会被摘要不匹配删掉，续传彻底没戏）。
- **网页 `/web/download?name=` 路径穿越（安全问题）**：`new File(dir, name)` 直接拼用户输入，`name=../../../../data/data/...` 可在只凭 PIN 的情况下读取任意文件。现已 `sanitize` + `getCanonicalPath` 双重校验。
- **网页文件列表 / 下载 API 永远为空**：1.7.4 把落盘目录改成应用私有目录后，这两处仍写死公共 `OldSend` 目录。新增 `FileSaver.receivedDirs()` 统一目录来源，`DownloadShare` 与网页端共用。
- **默认 PIN 自相矛盾**：`getPin()` 默认 `1234`，设置页展示与编辑默认 `0000`。用户开启 PIN 后照界面输 0000 必然 401。已统一为 `OldSendService.DEFAULT_PIN`。
- **改端口 / 切 HTTPS 等于没改**：两处开关只写 SharedPreferences，运行中的 `HttpServer` 不会换监听端口/协议，于是二维码和地址显示新端口、实际仍在旧端口。新增 `ACTION_RESTART` 在同一个 Service 实例内拆旧监听再重建（避免 stop/start 竞态命中"服务已在运行中"提前 return）。
- **连接超限时的 503 会把服务卡死（DoS）**：`reject()` 在未握手的 `SSLSocket` 上写明文会触发阻塞式握手，而 accept 线程没有超时——一个 IP 发起 >6 条不握手的 TLS 连接就能让整个服务不再接受新连接。现在先设 `setSoTimeout(3000)`，TLS 连接只关不写。

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
package org.watchsend.util;

import android.content.Context;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class I18nManager {
    private static final Map<String, Map<String, String>> dict = new HashMap<>();
    private static String currentLang = "en";

    static {
        // ============ 简体中文 (zh) ============
        Map<String, String> zh = new HashMap<>();
        zh.put("running", "运行中"); zh.put("stopped", "已停止"); zh.put("start", "启动"); zh.put("stop", "停止");
        zh.put("log", "日志"); zh.put("scan", "扫描"); zh.put("select_content", "选择内容"); zh.put("nearby_devices", "附近的设备");
        zh.put("no_device", "暂无设备，请点击扫描"); zh.put("receive_history", "接收记录"); zh.put("settings", "设置");
        zh.put("send", "发送"); zh.put("receive", "接收"); zh.put("file", "文件"); zh.put("media", "媒体");
        zh.put("folder", "文件夹"); zh.put("app", "应用"); zh.put("text", "文本"); zh.put("clipboard", "剪贴板");
        zh.put("not_selected", "未选择文件"); zh.put("auto_save", "自动保存"); zh.put("save_album", "保存到相册");
        zh.put("save_history", "保存到历史记录"); zh.put("use_pin", "启用 PIN 密码"); zh.put("pin_off", "当前 PIN: 未启用");
        zh.put("pin_on", "当前 PIN 码: "); zh.put("network", "网络与自动化"); zh.put("web_enable", "启用网页传输");
        zh.put("auto_start", "开机自动启动"); zh.put("receive_notif", "通过通知栏接收"); zh.put("port", "端口");
        zh.put("timeout", "搜索设备超时"); zh.put("other", "其他"); zh.put("anim", "界面切换动画");
        zh.put("set_name", "修改设备名称"); zh.put("backup", "备份 / 恢复设置"); zh.put("about", "关于 WatchSend");
        zh.put("language", "语言 (Language)"); zh.put("follow_system", "跟随系统"); zh.put("cancel", "取消");
        zh.put("confirm", "确定"); zh.put("close", "关闭"); zh.put("clear", "清空"); zh.put("input_ip", "手动输入 IP");
        zh.put("input_text", "发送文本"); zh.put("select_lang", "选择语言"); zh.put("restart_tip", "切换语言成功");
        zh.put("backup_title", "备份与恢复"); zh.put("backup_export", "导出设置到剪贴板"); zh.put("backup_import", "从剪贴板导入设置");
        zh.put("dark_mode", "深色模式"); zh.put("https_enable", "开启 HTTPS 加密");
        zh.put("about_desc", "WatchSend 是一款打破常规的极客级文件传输工具。\n1. 完美兼容 LocalSend 局域网协议，最低支持 Android 2.3。\n2. 独创 6 位密码跨网传输，突破局域网限制。\n3. 支持公网 P2P 极速直连，速度与安全尽在掌控。\n无需服务器，全平台通用。\n\nAI 辅助生成，开源协议: MIT License。");
        zh.put("about_credits", "作者\nstyisme\n\n贡献者\nAI 助手 (大语言模型)");
        zh.put("device_mobile", "手机"); zh.put("device_desktop", "电脑"); zh.put("device_web", "网页");
        zh.put("device_headless", "服务"); zh.put("device_server", "服务端");
        zh.put("sending", "正在发送文件"); zh.put("send_failed", "发送失败"); zh.put("send_success", "发送完成");
        zh.put("sending_title", "正在发送文件"); zh.put("receiving_title", "正在接收文件"); zh.put("total_progress", "总进度");
        zh.put("size", "大小"); zh.put("speed", "速度"); zh.put("received", "已接收");
        zh.put("completed", "已完成"); zh.put("failed", "已失败"); zh.put("save_to_dir", "已保存到相册/下载目录");
        zh.put("enter_pin", "请输入接收方 PIN 码"); zh.put("pin_incorrect", "PIN 码错误，请重试");
        zh.put("modify_pin", "修改 PIN 码"); zh.put("pin_code", "PIN 码");
        
        zh.put("add_ip", "加IP"); zh.put("cross_net", "跨网"); zh.put("auto_whitelist", "自动接收白名单文件");
        zh.put("general", "通用"); zh.put("save_dir_prefix", "保存目录: ");
        zh.put("recv_completed", "接收完成"); zh.put("send_completed", "发送完成");
        zh.put("interrupted", "已中断"); zh.put("unknown_device", "未知设备");
        zh.put("receive_request", "接收文件请求"); zh.put("reject", "拒绝");
        zh.put("filter_ext_title", "拦截危险后缀"); zh.put("manage_acl", "管理白/黑名单");
        zh.put("cross_net_title", "跨网传输"); zh.put("cross_net_desc", "无需连接同一Wi-Fi，极客通道直连。");
        zh.put("upnp_punch", "一键自动穿透"); zh.put("direct_mode", "启用极速直连");
        zh.put("send_file_title", "我要发送文件"); zh.put("recv_file_title", "我要接收文件");
        zh.put("gen_pin_btn", "选择文件并生成6位密码"); zh.put("input_pin_hint", "输入6位密码");
        zh.put("connect_recv_btn", "连接并接收"); zh.put("author", "作者"); zh.put("contributors", "贡献者");
        
        zh.put("ipv6_local", "本机公网 IPv6: [%s]:53317 (可用直连)"); zh.put("ipv6_none", "本机 IPv6: 未获取到公网 IPv6，仅限内网或穿透");
        zh.put("stun_wait", "STUN 探测中..."); zh.put("stun_fail", "NAT 穿透失败 (对称型NAT)");
        zh.put("stun_ok", "NAT公网地址: "); zh.put("stun_probe", "STUN探测");
        zh.put("stun_tip", "NAT公网地址: 点击右侧探测"); zh.put("advanced_tip", "高级玩法：可配合 FRP/Ngrok 或 ZeroTier 组网后，将生成的公网地址填入下方极速直连。");
        
        zh.put("file_count", "文件: %d"); zh.put("size_label", "大小: %s"); zh.put("unknown_size", "未知大小");
        zh.put("manage", "管理"); zh.put("clear_all", "清空所有名单");
        zh.put("view_whitelist", "查看/移除 白名单"); zh.put("view_blacklist", "查看/移除 黑名单");
        zh.put("all_deleted", "已全部删除");
        zh.put("export_log", "导出日志"); zh.put("input_address", "输入地址"); zh.put("label", "标签"); zh.put("ip_address", "IP 地址");
                // ---- P1 新增 ----
        zh.put("general", "通用"); zh.put("save_dir_prefix", "保存目录: ");
        zh.put("recv_completed", "接收完成"); zh.put("send_completed", "发送完成");
        zh.put("interrupted", "已中断"); zh.put("unknown_device", "未知设备");
        zh.put("receive_request", "接收文件请求"); zh.put("reject", "拒绝"); zh.put("accept", "接受");
        zh.put("manage_list", "名单管理中心"); zh.put("list_empty", "当前名单为空");
        zh.put("remove_device_fmt", "点击移除设备 (%d个)"); zh.put("removed", "已移除");
        zh.put("added_to_list", "已加入名单"); zh.put("removed_from_list", "已移出名单");
        zh.put("add_files", "加入文件"); zh.put("manual_device", "手动设备");
        zh.put("share_log_missing", "日志文件不存在"); zh.put("share_failed", "分享失败");
        zh.put("clipboard_empty", "剪贴板为空");
        zh.put("filter_ext_hint", "拦截后缀 (逗号分隔, 留空为不拦截)");
        zh.put("filter_updated", "拦截规则已更新");
        zh.put("select_title", "选择"); zh.put("delete_all", "全部删除");
        zh.put("unknown_file", "未知文件");
        zh.put("service_running", "WatchSend 正在运行");
        zh.put("service_ready", "局域网文件传输已就绪，点击返回");
        zh.put("wants_to_send", "想要发送给你文件");
        zh.put("select_app", "选择应用 (APK)");
        zh.put("send_folder", ">> [发送此文件夹] <<");
        zh.put("folder_prefix", "[文件夹] "); zh.put("empty_folder", "空文件夹");
        zh.put("cn_status_packing", "正在打包文件...");
        zh.put("cn_status_packaged", "打包完成，正在建立极客通道...");
        zh.put("cn_status_sending_fmt", "正在发送... 已发送 %s KB");
        zh.put("cn_status_send_done", "发送完成！等待对方接收完毕。");
        zh.put("cn_status_send_failed_fmt", "发送失败: %s");
        zh.put("cn_status_connecting", "正在建立连接...");
        zh.put("cn_status_receiving_fmt", "接收中... 已接收 %s KB");
        zh.put("cn_status_recv_unzip", "接收完成，正在解压...");
        zh.put("cn_status_recv_done", "接收完成！已解压到 Download 目录");
        zh.put("cn_status_recv_failed_fmt", "接收失败: %s");
        zh.put("cn_status_upnp_req", "正在向路由器申请 UPnP 端口映射...");
        zh.put("cn_status_direct_read", "正在读取文件并准备直连...");
        zh.put("cn_status_prepare_failed", "准备失败");
        zh.put("cn_status_direct_send", "正在通过公网直连发送...");
        zh.put("cn_status_direct_pct_fmt", "极速发送中... %d%% (%s/%s KB)");
        zh.put("cn_status_direct_done", "公网极速直连发送完成！");
        zh.put("cn_status_direct_failed_fmt", "直连失败: %s");
        zh.put("cn_status_self", "直连失败: 无法通过公网IP直连本机，请使用另一台设备。");
        zh.put("cn_pin_prefix_fmt", "密码: %s");
        zh.put("cn_pin_invalid", "请输入6位纯数字密码");
        zh.put("cn_direct_invalid", "请输入对方公网IP和端口");
        zh.put("cn_selected_fmt", "已选择 %d 个文件, 总计 %s");
        zh.put("cn_read_failed", "读取文件失败");
        zh.put("cn_gen_pin_send", "生成密码并开始发送");
        zh.put("cn_direct_send", "直连发送已选文件");
        zh.put("cn_upnp_ok_fmt", "穿透成功！你的公网地址是: %s\n请让对方填入此地址。");
        zh.put("cn_upnp_off", "获取失败：路由器可能未开启 UPnP。");
        zh.put("cn_upnp_notfound", "获取失败：未找到支持 UPnP 的路由器。");
        zh.put("cn_upnp_api_err", "映射失败：路由器 UPnP 接口异常。");
        zh.put("cn_upnp_rejected_fmt", "穿透失败：路由器拒绝映射请求(%d)。");
        zh.put("cn_upnp_exception_fmt", "穿透异常: %s");
        zh.put("edit_btn", "编辑"); zh.put("add_btn", "+ 添加"); zh.put("manage_arrow", "管理 >");
        zh.put("select_mode", "选择传输模式"); zh.put("nat_public_untested", "NAT公网地址: 未探测");
        zh.put("ipv6_format_direct", "本机公网 IPv6: [%s]:53317 (可用直连)");
        zh.put("stun_probe_btn", "STUN探测");
        zh.put("advanced_tip", "高级玩法：可配合 FRP/Ngrok 或 ZeroTier 组网后，将生成的公网地址填入下方极速直连。");

        zh.put("ui_size", "UI 大小"); zh.put("ui_size_small", "小"); zh.put("ui_size_medium", "标准"); zh.put("ui_size_large", "大");
        zh.put("update_success", "更新成功"); zh.put("export_ok", "导出成功"); zh.put("import_ok", "导入成功");
        zh.put("view_whitelist", "查看白名单"); zh.put("view_blacklist", "查看黑名单"); zh.put("clear_all", "清空所有");
        zh.put("list_empty", "名单为空");

        zh.put("recv_request", "接收请求"); zh.put("from_device", "来自设备"); zh.put("wants_to_send_you", "想要发送文件给你");
        zh.put("action", "操作"); zh.put("current_file", "当前文件"); zh.put("preparing", "准备中...");
        zh.put("transferring", "传输中..."); zh.put("calculating", "计算中..."); zh.put("saved_to_local", "已保存到本地");
        zh.put("transfer_success", "传输成功"); zh.put("local_ipv6", "本机 IPv6"); zh.put("getting", "正在获取...");
        zh.put("not_probed", "未探测"); zh.put("probing", "探测中..."); zh.put("nat_addr", "NAT 公网地址");
        zh.put("input_direct_ip", "对方公网 IP:端口"); zh.put("selected_prefix", "已选 "); zh.put("files_suffix", " 个文件\n");
        zh.put("select_file_gen_pin", "选择文件并生成密码"); zh.put("pin_prefix", "密码: "); zh.put("toast_input_ip", "请输入对方公网IP和端口");
        zh.put("toast_input_pin", "请输入6位纯数字密码"); zh.put("ipv6_not_found", "未获取到公网 IPv6"); zh.put("packing", "正在打包文件...");
        zh.put("packed_connecting", "打包完成，正在建立通道..."); zh.put("sending_prefix", "发送中... "); zh.put("send_done", "发送完成！");
        zh.put("failed_prefix", "失败: "); zh.put("direct_self", "直连失败: 无法通过公网IP直连本机。"); zh.put("preparing_direct", "准备直连...");
        zh.put("read_failed", "读取失败"); zh.put("prepare_failed", "准备失败"); zh.put("direct_sending", "正在直连发送...");
        zh.put("sending_percent", "发送中 "); zh.put("direct_send_done", "直连发送完成！"); zh.put("direct_failed", "直连失败: ");
        zh.put("connecting", "正在连接..."); zh.put("receiving_prefix", "接收中... "); zh.put("recv_unzipping", "接收完成，解压中...");
        zh.put("recv_done", "接收完成！已保存到下载。"); zh.put("recv_failed", "接收失败: ");


        zh.put("round_screen_opt", "圆屏优化"); zh.put("round_screen_opt_desc", "防止内容被圆形边缘遮挡");
        zh.put("block_system_back", "拦截系统返回"); zh.put("block_system_back_desc", "防止与滑动返回冲突");
        zh.put("dark_mode_desc", "黑白主题切换"); zh.put("transfer_settings", "传输");
        zh.put("auto_save_desc", "免确认直接接收"); zh.put("auto_whitelist_desc", "仅限白名单设备");
        zh.put("save_album_desc", "图片与视频放入图库"); zh.put("save_history_desc", "记录收发日志");
        zh.put("use_pin_desc", "增强接收安全性"); zh.put("https_enable_desc", "可能影响旧设备兼容性");
        zh.put("web_enable_desc", "浏览器互传"); zh.put("auto_start_desc", "后台常驻");
        zh.put("receive_notif_desc", "需系统权限"); zh.put("port_settings", "端口设置");
        zh.put("timeout_settings", "搜索超时"); zh.put("device_security", "设备与安全");
        zh.put("manage_acl_desc", "管理已信任设备"); zh.put("ui_scale_dialog_title", "无级缩放 UI");
        zh.put("about_title", "关于"); zh.put("pin_title_dialog", "修改 PIN");
        zh.put("port_dialog_title", "端口"); zh.put("timeout_dialog_title", "搜索超时(ms)");
        zh.put("filter_dialog_title", "拦截后缀"); zh.put("device_name_title", "设备名称");
        zh.put("lang_title", "语言设置"); zh.put("acl_dialog_title", "黑白名单");
        zh.put("whitelist_title", "白名单"); zh.put("blacklist_title", "黑名单");
        zh.put("about_desc_watchsend", "作者: styisme\nAI 辅助生成\nMIT License\n\n兼容 LocalSend v2.2");
    

        zh.put("start_service", "启动"); zh.put("stop_service", "停止"); zh.put("devices_nearby", "附近的设备");
        zh.put("no_device_scan", "暂无设备，请点击扫描"); zh.put("send_file", "发送文件"); zh.put("select_title_desc", "选择内容");
        zh.put("device", "设备"); zh.put("file", "文件"); zh.put("media", "媒体"); zh.put("app", "应用");
        zh.put("folder", "文件夹"); zh.put("text", "文本"); zh.put("clipboard", "剪贴板"); zh.put("record", "接收记录");
        zh.put("return_to_devices", "↩ 返回设备"); zh.put("no_record", "暂无记录"); zh.put("file_not_found", "文件不存在");
        zh.put("cannot_open", "无法打开"); zh.put("no_app", "无应用"); zh.put("empty_folder", "空文件夹");
        zh.put("add_files", "追加文件"); zh.put("select_folder_send", ">> [发送此文件夹] <<"); zh.put("unknown_file", "未知文件");
        zh.put("files_count_prefix", "文件: "); zh.put("size_label", "大小: "); zh.put("clear_all_files", "全部删除");
        zh.put("back_prev", "返回上一页"); zh.put("save_to_dir", "保存目录: "); zh.put("ip_prefix", "IP ");
    
dict.put("zh", zh);

        // ============ 繁体中文 (zh_TW) ============
        Map<String, String> zhTW = new HashMap<>(zh);
        zhTW.put("running", "運行中"); zhTW.put("stopped", "已停止"); zhTW.put("start", "啟動"); zhTW.put("stop", "停止");
        zhTW.put("log", "日誌"); zhTW.put("scan", "掃描"); zhTW.put("select_content", "選擇內容"); zhTW.put("nearby_devices", "附近的設備");
        zhTW.put("no_device", "暫無設備，請點擊掃描"); zhTW.put("receive_history", "接收記錄"); zhTW.put("settings", "設定");
        zhTW.put("send", "發送"); zhTW.put("receive", "接收"); zhTW.put("file", "檔案"); zhTW.put("media", "媒體");
        zhTW.put("folder", "資料夾"); zhTW.put("app", "應用"); zhTW.put("text", "文字"); zhTW.put("clipboard", "剪貼簿");
        zhTW.put("not_selected", "未選擇檔案"); zhTW.put("auto_save", "自動儲存"); zhTW.put("save_album", "儲存到相簿");
        zhTW.put("save_history", "儲存到歷史記錄"); zhTW.put("use_pin", "啟用 PIN 密碼"); zhTW.put("pin_off", "當前 PIN: 未啟用");
        zhTW.put("pin_on", "當前 PIN 碼: "); zhTW.put("network", "網路與自動化"); zhTW.put("web_enable", "啟用網頁傳輸");
        zhTW.put("auto_start", "開機自動啟動"); zhTW.put("receive_notif", "透過通知欄接收"); zhTW.put("port", "連接埠");
        zhTW.put("timeout", "搜尋設備逾時"); zhTW.put("other", "其他"); zhTW.put("anim", "介面切換動畫");
        zhTW.put("set_name", "修改設備名稱"); zhTW.put("backup", "備份 / 恢復設定"); zhTW.put("about", "關於 WatchSend");
        zhTW.put("language", "語言 (Language)"); zhTW.put("follow_system", "跟隨系統"); zhTW.put("cancel", "取消");
        zhTW.put("confirm", "確定"); zhTW.put("close", "關閉"); zhTW.put("clear", "清空"); zhTW.put("input_ip", "手動輸入 IP");
        zhTW.put("input_text", "發送文字"); zhTW.put("select_lang", "選擇語言"); zhTW.put("restart_tip", "切換語言成功");
        zhTW.put("add_ip", "加IP"); zhTW.put("cross_net", "跨網"); zhTW.put("auto_whitelist", "自動接收白名單檔案");
        zhTW.put("filter_ext_title", "攔截危險副檔名"); zhTW.put("manage_acl", "管理白/黑名單");
        zhTW.put("cross_net_title", "跨網傳輸"); zhTW.put("cross_net_desc", "無需連接同一Wi-Fi，極客通道直連。");
        zhTW.put("upnp_punch", "一鍵自動穿透"); zhTW.put("direct_mode", "啟用極速直連");
        zhTW.put("send_file_title", "我要傳送檔案"); zhTW.put("recv_file_title", "我要接收檔案");
        zhTW.put("gen_pin_btn", "選擇檔案並生成6位密碼"); zhTW.put("input_pin_hint", "輸入6位密碼");
        zhTW.put("connect_recv_btn", "連接並接收"); zhTW.put("author", "作者"); zhTW.put("contributors", "貢獻者");
        zhTW.put("ipv6_local", "本機公網 IPv6: [%s]:53317 (可用直連)"); zhTW.put("ipv6_none", "本機 IPv6: 未獲取到公網 IPv6，僅限內網或穿透");
        zhTW.put("stun_wait", "STUN 探測中..."); zhTW.put("stun_fail", "NAT 穿透失敗 (對稱型NAT)");
        zhTW.put("stun_ok", "NAT公網位址: "); zhTW.put("stun_probe", "STUN探測");
        zhTW.put("stun_tip", "NAT公網位址: 點擊右側探測"); zhTW.put("advanced_tip", "高級玩法：可配合 FRP/Ngrok 或 ZeroTier 組網後，將生成的公網位址填入下方極速直連。");
        zhTW.put("file_count", "檔案: %d"); zhTW.put("size_label", "大小: %s"); zhTW.put("unknown_size", "未知大小");
        zhTW.put("manage", "管理"); zhTW.put("clear_all", "清空所有名單");
        zhTW.put("view_whitelist", "查看/移除 白名單"); zhTW.put("view_blacklist", "查看/移除 黑名單");
        zhTW.put("all_deleted", "已全部刪除"); zhTW.put("export_log", "匯出日誌");
        zhTW.put("input_address", "輸入地址"); zhTW.put("label", "標籤"); zhTW.put("ip_address", "IP 位址");
                // ---- P1 繁体覆盖 ----
        zhTW.put("general", "一般"); zhTW.put("accept", "接受"); zhTW.put("reject", "拒絕");
        zhTW.put("recv_completed", "接收完成"); zhTW.put("send_completed", "發送完成"); zhTW.put("interrupted", "已中斷");
        zhTW.put("unknown_device", "未知設備"); zhTW.put("unknown_file", "未知檔案");
        zhTW.put("save_dir_prefix", "儲存目錄: "); zhTW.put("wants_to_send", "想要傳送檔案給你");
        zhTW.put("receive_request", "接收檔案請求"); zhTW.put("service_running", "WatchSend 正在執行");
        zhTW.put("cn_gen_pin_send", "產生密碼並開始傳送"); zhTW.put("cn_direct_send", "直連傳送已選檔案");
        zhTW.put("cn_status_packing", "正在打包檔案..."); zhTW.put("cn_status_connecting", "正在建立連線...");
        zhTW.put("cn_status_send_done", "傳送完成！等待對方接收完畢。");
        zhTW.put("cn_status_recv_done", "接收完成！已解壓到 Download 目錄");
        zhTW.put("cn_pin_invalid", "請輸入6位純數字密碼");
        zhTW.put("cn_direct_invalid", "請輸入對方公網IP和連接埠");
        zhTW.put("manage_list", "名單管理中心"); zhTW.put("add_files", "加入檔案");
        zhTW.put("delete_all", "全部刪除"); zhTW.put("select_title", "選擇");
        zhTW.put("select_app", "選擇應用 (APK)");
        zhTW.put("removed", "已移除"); zhTW.put("added_to_list", "已加入名單"); zhTW.put("removed_from_list", "已移出名單");
        zhTW.put("share_log_missing", "日誌檔案不存在"); zhTW.put("share_failed", "分享失敗");
        zhTW.put("clipboard_empty", "剪貼簿為空");
        zhTW.put("folder_prefix", "[資料夾] "); zhTW.put("empty_folder", "空資料夾");
        zhTW.put("send_folder", ">> [傳送此資料夾] <<"); zhTW.put("manual_device", "手動設備");

        zhTW.put("recv_request", "接收請求"); zhTW.put("from_device", "來自設備"); zhTW.put("wants_to_send_you", "想要傳送檔案給你");
        zhTW.put("action", "操作"); zhTW.put("current_file", "當前檔案"); zhTW.put("preparing", "準備中...");
        zhTW.put("transferring", "傳輸中..."); zhTW.put("calculating", "計算中..."); zhTW.put("saved_to_local", "已儲存到本機");
        zhTW.put("transfer_success", "傳輸成功"); zhTW.put("local_ipv6", "本機 IPv6"); zhTW.put("getting", "正在取得...");
        zhTW.put("not_probed", "未探測"); zhTW.put("probing", "探測中..."); zhTW.put("nat_addr", "NAT 公網位址");
        zhTW.put("input_direct_ip", "對方公網 IP:連接埠"); zhTW.put("selected_prefix", "已選 "); zhTW.put("files_suffix", "個檔案\n");
        zhTW.put("select_file_gen_pin", "選擇檔案並產生密碼"); zhTW.put("pin_prefix", "密碼: "); zhTW.put("toast_input_ip", "請輸入對方公網IP和連接埠");
        zhTW.put("toast_input_pin", "請輸入6位純數字密碼"); zhTW.put("ipv6_not_found", "未取得公網 IPv6"); zhTW.put("packing", "正在打包檔案...");
        zhTW.put("packed_connecting", "打包完成，正在建立通道..."); zhTW.put("sending_prefix", "傳送中... "); zhTW.put("send_done", "傳送完成！");
        zhTW.put("failed_prefix", "失敗: "); zhTW.put("direct_self", "直連失敗: 無法透過公網IP直連本機。"); zhTW.put("preparing_direct", "準備直連...");
        zhTW.put("read_failed", "讀取失敗"); zhTW.put("prepare_failed", "準備失敗"); zhTW.put("direct_sending", "正在直連傳送...");
        zhTW.put("sending_percent", "傳送中 "); zhTW.put("direct_send_done", "直連傳送完成！"); zhTW.put("direct_failed", "直連失敗: ");
        zhTW.put("connecting", "正在連線..."); zhTW.put("receiving_prefix", "接收中... "); zhTW.put("recv_unzipping", "接收完成，解壓中...");
        zhTW.put("recv_done", "接收完成！已儲存到下載。"); zhTW.put("recv_failed", "接收失敗: ");
    

        zhTW.put("round_screen_opt", "圓屏優化"); zhTW.put("round_screen_opt_desc", "防止內容被圓形邊緣遮擋");
        zhTW.put("block_system_back", "攔截系統返回"); zhTW.put("block_system_back_desc", "防止與滑動返回衝突");
        zhTW.put("dark_mode_desc", "黑白主題切換"); zhTW.put("transfer_settings", "傳輸");
        zhTW.put("auto_save_desc", "免確認直接接收"); zhTW.put("auto_whitelist_desc", "僅限白名單設備");
        zhTW.put("save_album_desc", "圖片與影片放入圖庫"); zhTW.put("save_history_desc", "記錄收發日誌");
        zhTW.put("use_pin_desc", "增強接收安全性"); zhTW.put("https_enable_desc", "可能影響舊設備相容性");
        zhTW.put("web_enable_desc", "瀏覽器互傳"); zhTW.put("auto_start_desc", "後台常駐");
        zhTW.put("receive_notif_desc", "需系統權限"); zhTW.put("port_settings", "連接埠設定");
        zhTW.put("timeout_settings", "搜尋逾時"); zhTW.put("device_security", "設備與安全");
        zhTW.put("manage_acl_desc", "管理已信任設備"); zhTW.put("ui_scale_dialog_title", "無級縮放 UI");
        zhTW.put("about_title", "關於"); zhTW.put("pin_title_dialog", "修改 PIN");
        zhTW.put("port_dialog_title", "連接埠"); zhTW.put("timeout_dialog_title", "搜尋逾時(ms)");
        zhTW.put("filter_dialog_title", "攔截副檔名"); zhTW.put("device_name_title", "設備名稱");
        zhTW.put("lang_title", "語言設定"); zhTW.put("acl_dialog_title", "黑白名單");
        zhTW.put("whitelist_title", "白名單"); zhTW.put("blacklist_title", "黑名單");
        zhTW.put("about_desc_watchsend", "作者: styisme\nAI 輔助生成\nMIT License\n\n相容 LocalSend v2.2");
    

        zhTW.put("start_service", "啟動"); zhTW.put("stop_service", "停止"); zhTW.put("devices_nearby", "附近的設備");
        zhTW.put("no_device_scan", "暫無設備，請點擊掃描"); zhTW.put("send_file", "傳送檔案"); zhTW.put("select_title_desc", "選擇內容");
        zhTW.put("device", "設備"); zhTW.put("file", "檔案"); zhTW.put("media", "媒體"); zhTW.put("app", "應用");
        zhTW.put("folder", "資料夾"); zhTW.put("text", "文字"); zhTW.put("clipboard", "剪貼簿"); zhTW.put("record", "接收記錄");
        zhTW.put("return_to_devices", "↩ 返回設備"); zhTW.put("no_record", "暫無記錄"); zhTW.put("file_not_found", "檔案不存在");
        zhTW.put("cannot_open", "無法開啟"); zhTW.put("no_app", "無應用"); zhTW.put("empty_folder", "空資料夾");
        zhTW.put("add_files", "追加檔案"); zhTW.put("select_folder_send", ">> [傳送此資料夾] <<"); zhTW.put("unknown_file", "未知檔案");
        zhTW.put("files_count_prefix", "檔案: "); zhTW.put("size_label", "大小: "); zhTW.put("clear_all_files", "全部刪除");
        zhTW.put("back_prev", "返回上一頁"); zhTW.put("save_to_dir", "儲存目錄: "); zhTW.put("ip_prefix", "IP ");
    
dict.put("zh_TW", zhTW);

        // ============ 英语 (en) ============
        Map<String, String> en = new HashMap<>();
        en.put("running", "Running"); en.put("stopped", "Stopped"); en.put("start", "Start"); en.put("stop", "Stop");
        en.put("log", "Log"); en.put("scan", "Scan"); en.put("select_content", "Select Content"); en.put("nearby_devices", "Nearby Devices");
        en.put("no_device", "No devices, tap scan"); en.put("receive_history", "Receive History"); en.put("settings", "Settings");
        en.put("send", "Send"); en.put("receive", "Receive"); en.put("file", "File"); en.put("media", "Media");
        en.put("folder", "Folder"); en.put("app", "App"); en.put("text", "Text"); en.put("clipboard", "Clipboard");
        en.put("not_selected", "No file selected"); en.put("auto_save", "Auto Save"); en.put("save_album", "Save to Album");
        en.put("save_history", "Save to History"); en.put("use_pin", "Enable PIN"); en.put("pin_off", "PIN: Disabled");
        en.put("pin_on", "Current PIN: "); en.put("network", "Network & Automation"); en.put("web_enable", "Enable Web Share");
        en.put("auto_start", "Auto Start on Boot"); en.put("receive_notif", "Receive via Notification"); en.put("port", "Port");
        en.put("timeout", "Discovery Timeout"); en.put("other", "Other"); en.put("anim", "UI Animation");
        en.put("set_name", "Device Name"); en.put("backup", "Backup / Restore"); en.put("about", "About WatchSend");
        en.put("language", "Language"); en.put("follow_system", "Follow System"); en.put("cancel", "Cancel");
        en.put("confirm", "OK"); en.put("close", "Close"); en.put("clear", "Clear"); en.put("input_ip", "Manual IP");
        en.put("input_text", "Send Text"); en.put("select_lang", "Select Language"); en.put("restart_tip", "Language changed");
        en.put("backup_title", "Backup & Restore"); en.put("backup_export", "Export to Clipboard"); en.put("backup_import", "Import from Clipboard");
        en.put("dark_mode", "Dark Mode"); en.put("https_enable", "Enable HTTPS");
        en.put("about_desc", "WatchSend is a geek-level file transfer tool that breaks conventions.\n1. Fully compatible with LocalSend LAN protocol, supporting Android 2.3+.\n2. Original 6-digit password cross-network transfer, breaking LAN limits.\n3. Supports public P2P direct connection, speed and security in your hands.\nNo server needed, cross-platform.\n\nAI-assisted generation, open source: MIT License.");
        en.put("about_credits", "Author\nstyisme\n\nContributors\nAI Assistant (LLM)");
        en.put("device_mobile", "Mobile"); en.put("device_desktop", "PC"); en.put("device_web", "Web");
        en.put("device_headless", "Service"); en.put("device_server", "Server");
        en.put("sending", "Sending..."); en.put("send_failed", "Send Failed"); en.put("send_success", "Sent");
        en.put("sending_title", "Sending File"); en.put("receiving_title", "Receiving File"); en.put("total_progress", "Progress");
        en.put("size", "Size"); en.put("speed", "Speed"); en.put("received", "Received");
        en.put("completed", "Completed"); en.put("failed", "Failed"); en.put("save_to_dir", "Saved to Album/Downloads");
        en.put("enter_pin", "Enter Receiver PIN"); en.put("pin_incorrect", "Incorrect PIN, try again");
        en.put("modify_pin", "Modify PIN"); en.put("pin_code", "PIN Code");
        
        en.put("add_ip", "Add IP"); en.put("cross_net", "Cross-net"); en.put("auto_whitelist", "Auto save favorites");
        en.put("general", "General"); en.put("save_dir_prefix", "Save to: ");
        en.put("recv_completed", "Received"); en.put("send_completed", "Sent");
        en.put("interrupted", "Interrupted"); en.put("unknown_device", "Unknown Device");
        en.put("receive_request", "Incoming File Request"); en.put("reject", "Reject");
        en.put("filter_ext_title", "Block dangerous exts"); en.put("manage_acl", "Manage White/Blacklist");
        en.put("cross_net_title", "Cross-net Transfer"); en.put("cross_net_desc", "No need for same Wi-Fi, geek channel direct connect.");
        en.put("upnp_punch", "Auto UPnP"); en.put("direct_mode", "Direct P2P Mode");
        en.put("send_file_title", "Send File"); en.put("recv_file_title", "Receive File");
        en.put("gen_pin_btn", "Select File & Gen PIN"); en.put("input_pin_hint", "Enter 6-digit PIN");
        en.put("connect_recv_btn", "Connect & Receive"); en.put("author", "Author"); en.put("contributors", "Contributors");
        
        en.put("ipv6_local", "Local IPv6: [%s]:53317 (Direct connect)"); en.put("ipv6_none", "Local IPv6: Not found. LAN or tunnel only");
        en.put("stun_wait", "STUN Probing..."); en.put("stun_fail", "STUN Failed (Symmetric NAT)");
        en.put("stun_ok", "NAT Public Addr: "); en.put("stun_probe", "STUN Probe");
        en.put("stun_tip", "NAT Addr: Click probe on the right"); en.put("advanced_tip", "Pro Tip: Combine with FRP/Ngrok or ZeroTier, and fill the generated public address into Direct Connect.");
        
        en.put("file_count", "Files: %d"); en.put("size_label", "Size: %s"); en.put("unknown_size", "Unknown Size");
        en.put("manage", "Manage"); en.put("clear_all", "Clear all lists");
        en.put("view_whitelist", "View/Remove Whitelist"); en.put("view_blacklist", "View/Remove Blacklist");
        en.put("all_deleted", "All cleared");
        en.put("export_log", "Export Log"); en.put("input_address", "Input Address"); en.put("label", "Label"); en.put("ip_address", "IP Address");
                // ---- P1 新增 ----
        en.put("general", "General"); en.put("save_dir_prefix", "Save to: ");
        en.put("recv_completed", "Received"); en.put("send_completed", "Sent");
        en.put("interrupted", "Interrupted"); en.put("unknown_device", "Unknown Device");
        en.put("receive_request", "Incoming File Request"); en.put("reject", "Reject"); en.put("accept", "Accept");
        en.put("manage_list", "List Manager"); en.put("list_empty", "List is empty");
        en.put("remove_device_fmt", "Tap to remove (%d)"); en.put("removed", "Removed");
        en.put("added_to_list", "Added to list"); en.put("removed_from_list", "Removed from list");
        en.put("add_files", "Add Files"); en.put("manual_device", "Manual Device");
        en.put("share_log_missing", "Log file not found"); en.put("share_failed", "Share failed");
        en.put("clipboard_empty", "Clipboard is empty");
        en.put("filter_ext_hint", "Block extensions (comma separated, empty to disable)");
        en.put("filter_updated", "Filter updated");
        en.put("select_title", "Select"); en.put("delete_all", "Delete All");
        en.put("unknown_file", "Unknown");
        en.put("service_running", "WatchSend Running");
        en.put("service_ready", "LAN file transfer ready, tap to return");
        en.put("wants_to_send", "Wants to send you files");
        en.put("select_app", "Select App (APK)");
        en.put("send_folder", ">> [Send this folder] <<");
        en.put("folder_prefix", "[Folder] "); en.put("empty_folder", "Empty folder");
        en.put("cn_status_packing", "Packing files...");
        en.put("cn_status_packaged", "Packed, establishing channel...");
        en.put("cn_status_sending_fmt", "Sending... %s KB sent");
        en.put("cn_status_send_done", "Sent! Waiting for peer.");
        en.put("cn_status_send_failed_fmt", "Send failed: %s");
        en.put("cn_status_connecting", "Connecting...");
        en.put("cn_status_receiving_fmt", "Receiving... %s KB");
        en.put("cn_status_recv_unzip", "Received, extracting...");
        en.put("cn_status_recv_done", "Received! Extracted to Download");
        en.put("cn_status_recv_failed_fmt", "Receive failed: %s");
        en.put("cn_status_upnp_req", "Requesting UPnP port mapping...");
        en.put("cn_status_direct_read", "Reading files for direct send...");
        en.put("cn_status_prepare_failed", "Prepare failed");
        en.put("cn_status_direct_send", "Sending via public direct...");
        en.put("cn_status_direct_pct_fmt", "Direct sending... %d%% (%s/%s KB)");
        en.put("cn_status_direct_done", "Direct send complete!");
        en.put("cn_status_direct_failed_fmt", "Direct send failed: %s");
        en.put("cn_status_self", "Cannot direct-connect to self, use another device.");
        en.put("cn_pin_prefix_fmt", "PIN: %s");
        en.put("cn_pin_invalid", "Enter 6-digit PIN");
        en.put("cn_direct_invalid", "Enter peer public IP and port");
        en.put("cn_selected_fmt", "%d files selected, total %s");
        en.put("cn_read_failed", "Read files failed");
        en.put("cn_gen_pin_send", "Generate PIN and Send");
        en.put("cn_direct_send", "Direct Send Selected");
        en.put("cn_upnp_ok_fmt", "Punched! Your public addr: %s\nLet peer fill this in.");
        en.put("cn_upnp_off", "Failed: router may have UPnP disabled.");
        en.put("cn_upnp_notfound", "Failed: no UPnP router found.");
        en.put("cn_upnp_api_err", "Failed: router UPnP API error.");
        en.put("cn_upnp_rejected_fmt", "Failed: router rejected mapping (%d).");
        en.put("cn_upnp_exception_fmt", "Error: %s");
        en.put("edit_btn", "Edit"); en.put("add_btn", "+ Add"); en.put("manage_arrow", "Manage >");
        en.put("select_mode", "Select Transfer Mode"); en.put("nat_public_untested", "NAT Public Addr: Untested");
        en.put("ipv6_format_direct", "Local IPv6: [%s]:53317 (Direct connect)");
        en.put("stun_probe_btn", "STUN Probe");
        en.put("advanced_tip", "Pro Tip: Combine with FRP/Ngrok or ZeroTier, and fill the generated public address into Direct Connect.");

        en.put("ui_size", "UI Size"); en.put("ui_size_small", "Small"); en.put("ui_size_medium", "Medium"); en.put("ui_size_large", "Large");
        en.put("update_success", "Updated"); en.put("export_ok", "Exported"); en.put("import_ok", "Imported");
        en.put("view_whitelist", "View Whitelist"); en.put("view_blacklist", "View Blacklist"); en.put("clear_all", "Clear all");
        en.put("list_empty", "List is empty");

        en.put("recv_request", "Receive Request"); en.put("from_device", "From Device"); en.put("wants_to_send_you", "Wants to send you files");
        en.put("action", "Action"); en.put("current_file", "Current File"); en.put("preparing", "Preparing...");
        en.put("transferring", "Transferring..."); en.put("calculating", "Calculating..."); en.put("saved_to_local", "Saved locally");
        en.put("transfer_success", "Success"); en.put("local_ipv6", "Local IPv6"); en.put("getting", "Getting...");
        en.put("not_probed", "Not probed"); en.put("probing", "Probing..."); en.put("nat_addr", "NAT Addr");
        en.put("input_direct_ip", "Peer IP:Port"); en.put("selected_prefix", "Selected "); en.put("files_suffix", " files\n");
        en.put("select_file_gen_pin", "Select File & Gen PIN"); en.put("pin_prefix", "PIN: "); en.put("toast_input_ip", "Please enter peer IP and port");
        en.put("toast_input_pin", "Please enter 6-digit PIN"); en.put("ipv6_not_found", "Public IPv6 not found"); en.put("packing", "Packing files...");
        en.put("packed_connecting", "Packed, connecting..."); en.put("sending_prefix", "Sending... "); en.put("send_done", "Sent!");
        en.put("failed_prefix", "Failed: "); en.put("direct_self", "Cannot connect to self."); en.put("preparing_direct", "Preparing direct...");
        en.put("read_failed", "Read failed"); en.put("prepare_failed", "Prepare failed"); en.put("direct_sending", "Direct sending...");
        en.put("sending_percent", "Sending "); en.put("direct_send_done", "Direct send complete!"); en.put("direct_failed", "Direct failed: ");
        en.put("connecting", "Connecting..."); en.put("receiving_prefix", "Receiving... "); en.put("recv_unzipping", "Received, extracting...");
        en.put("recv_done", "Received! Saved to Downloads."); en.put("recv_failed", "Receive failed: ");


        en.put("round_screen_opt", "Round Screen Opt"); en.put("round_screen_opt_desc", "Prevent edge cutoff");
        en.put("block_system_back", "Block System Back"); en.put("block_system_back_desc", "Prevent gesture conflict");
        en.put("dark_mode_desc", "Black/White theme"); en.put("transfer_settings", "Transfer");
        en.put("auto_save_desc", "Auto accept files"); en.put("auto_whitelist_desc", "Only whitelisted devices");
        en.put("save_album_desc", "Save pics/vids to gallery"); en.put("save_history_desc", "Record transfer logs");
        en.put("use_pin_desc", "Enhance receive security"); en.put("https_enable_desc", "Might affect old devices");
        en.put("web_enable_desc", "Browser transfer"); en.put("auto_start_desc", "Run in background");
        en.put("receive_notif_desc", "Requires system permission"); en.put("port_settings", "Port Settings");
        en.put("timeout_settings", "Search Timeout"); en.put("device_security", "Device & Security");
        en.put("manage_acl_desc", "Manage trusted devices"); en.put("ui_scale_dialog_title", "Scale UI");
        en.put("about_title", "About"); en.put("pin_title_dialog", "Modify PIN");
        en.put("port_dialog_title", "Port"); en.put("timeout_dialog_title", "Timeout(ms)");
        en.put("filter_dialog_title", "Block Exts"); en.put("device_name_title", "Device Name");
        en.put("lang_title", "Language Settings"); en.put("acl_dialog_title", "Whitelist/Blacklist");
        en.put("whitelist_title", "Whitelist"); en.put("blacklist_title", "Blacklist");
        en.put("about_desc_watchsend", "Author: styisme\nAI Assisted\nMIT License\n\nCompatible with LocalSend v2.2");
    

        en.put("start_service", "Start"); en.put("stop_service", "Stop"); en.put("devices_nearby", "Nearby Devices");
        en.put("no_device_scan", "No devices, tap to scan"); en.put("send_file", "Send File"); en.put("select_title_desc", "Select Content");
        en.put("device", "Device"); en.put("file", "File"); en.put("media", "Media"); en.put("app", "App");
        en.put("folder", "Folder"); en.put("text", "Text"); en.put("clipboard", "Clipboard"); en.put("record", "Receive History");
        en.put("return_to_devices", "↩ Back to Devices"); en.put("no_record", "No records"); en.put("file_not_found", "File not found");
        en.put("cannot_open", "Cannot open"); en.put("no_app", "No apps"); en.put("empty_folder", "Empty folder");
        en.put("add_files", "Append Files"); en.put("select_folder_send", ">> [Send this folder] <<"); en.put("unknown_file", "Unknown file");
        en.put("files_count_prefix", "Files: "); en.put("size_label", "Size: "); en.put("clear_all_files", "Delete All");
        en.put("back_prev", "Go Back"); en.put("save_to_dir", "Save to: "); en.put("ip_prefix", "IP ");
    
dict.put("en", en);

        // ============ 日语 (ja) ============
        Map<String, String> ja = new HashMap<>(en);
        ja.put("running", "実行中"); ja.put("stopped", "停止"); ja.put("start", "開始"); ja.put("stop", "停止");
        ja.put("log", "ログ"); ja.put("scan", "スキャン"); ja.put("select_content", "コンテンツを選択"); ja.put("nearby_devices", "近くのデバイス");
        ja.put("no_device", "デバイスなし、スキャンしてください"); ja.put("receive_history", "受信履歴"); ja.put("settings", "設定");
        ja.put("send", "送信"); ja.put("receive", "受信"); ja.put("file", "ファイル"); ja.put("media", "メディア");
        ja.put("folder", "フォルダ"); ja.put("app", "アプリ"); ja.put("text", "テキスト"); ja.put("clipboard", "クリップボード");
        ja.put("not_selected", "ファイル未選択"); ja.put("auto_save", "自動保存"); ja.put("save_album", "アルバムに保存");
        ja.put("save_history", "履歴に保存"); ja.put("use_pin", "PINを有効化"); ja.put("pin_off", "PIN: 無効");
        ja.put("pin_on", "現在のPIN: "); ja.put("network", "ネットワークと自動化"); ja.put("web_enable", "Web共有を有効化");
        ja.put("auto_start", "起動時に自動開始"); ja.put("receive_notif", "通知から受信"); ja.put("port", "ポート");
        ja.put("timeout", "検索タイムアウト"); ja.put("other", "その他"); ja.put("anim", "UIアニメーション");
        ja.put("set_name", "デバイス名"); ja.put("backup", "バックアップ / 復元"); ja.put("about", "WatchSendについて");
        ja.put("language", "言語"); ja.put("follow_system", "システムに従う"); ja.put("cancel", "キャンセル");
        ja.put("confirm", "OK"); ja.put("close", "閉じる"); ja.put("clear", "クリア"); ja.put("input_ip", "手動IP入力");
        ja.put("input_text", "テキスト送信"); ja.put("select_lang", "言語を選択"); ja.put("restart_tip", "言語を変更しました");
        ja.put("add_ip", "IP追加"); ja.put("cross_net", "クロスネット"); ja.put("auto_whitelist", "お気に入りからの自動保存");
        ja.put("filter_ext_title", "危険な拡張子をブロック"); ja.put("manage_acl", "ホワイト/ブラックリスト管理");
        ja.put("cross_net_title", "クロスネット転送"); ja.put("cross_net_desc", "同じWi-Fiに接続する必要はありません。ギークチャネル直接接続。");
        ja.put("upnp_punch", "自動UPnP"); ja.put("direct_mode", "直接P2Pモード");
        ja.put("send_file_title", "ファイルを送信"); ja.put("recv_file_title", "ファイルを受信");
        ja.put("gen_pin_btn", "ファイルを選択して6桁のPINを生成"); ja.put("input_pin_hint", "6桁のPINを入力");
        ja.put("connect_recv_btn", "接続して受信");
        ja.put("ipv6_local", "ローカル IPv6: [%s]:53317 (直接接続可能)"); ja.put("ipv6_none", "ローカル IPv6: 見つかりません。LAN またはトンネルのみ");
        ja.put("stun_wait", "STUN プローブ中..."); ja.put("stun_fail", "STUN 失敗 (対称型 NAT)");
        ja.put("stun_ok", "NAT パブリックアドレス: "); ja.put("stun_probe", "STUN プローブ");
        ja.put("stun_tip", "NAT アドレス: 右側のプローブをクリック"); ja.put("advanced_tip", "プロのヒント: FRP/Ngrok または ZeroTier と組み合わせ、生成されたパブリックアドレスを直接接続に入力してください。");
        ja.put("file_count", "ファイル: %d"); ja.put("size_label", "サイズ: %s"); ja.put("unknown_size", "不明なサイズ");
        ja.put("manage", "管理"); ja.put("clear_all", "すべてのリストをクリア");
        ja.put("view_whitelist", "ホワイトリストを表示/削除"); ja.put("view_blacklist", "ブラックリストを表示/削除");
        ja.put("all_deleted", "すべてクリア"); ja.put("export_log", "ログをエクスポート");
        ja.put("input_address", "アドレスを入力"); ja.put("label", "ラベル"); ja.put("ip_address", "IPアドレス");
                // ---- P1 高频词条 ----
        ja.put("general", "一般"); ja.put("accept", "受け入れる"); ja.put("reject", "拒否");
        ja.put("recv_completed", "受信完了"); ja.put("send_completed", "送信完了"); ja.put("interrupted", "中断");
        ja.put("unknown_device", "不明なデバイス"); ja.put("unknown_file", "不明なファイル");
        ja.put("save_dir_prefix", "保存先: "); ja.put("wants_to_send", "ファイルを送信したい");
        ja.put("receive_request", "ファイル受信リクエスト"); ja.put("service_running", "WatchSend 実行中");
        ja.put("cn_gen_pin_send", "パスワード生成して送信"); ja.put("cn_direct_send", "直接送信 (選択済み)");
        ja.put("cn_status_packing", "パッキング中..."); ja.put("cn_status_connecting", "接続中...");
        ja.put("cn_status_send_done", "送信完了！相手の受信を待っています。");
        ja.put("cn_status_recv_done", "受信完了！Downloadに解凍しました");
        ja.put("cn_pin_invalid", "6桁の数字パスワードを入力してください");
        ja.put("cn_direct_invalid", "相手の公衆IPとポートを入力してください");
        ja.put("manage_list", "リスト管理"); ja.put("add_files", "ファイル追加");
        ja.put("delete_all", "すべて削除"); ja.put("select_title", "選択");
        ja.put("select_app", "アプリ選択 (APK)");
        ja.put("dark_mode", "ダークモード"); ja.put("modify_pin", "PINを変更");
        ja.put("https_enable", "HTTPSを有効化"); ja.put("auto_save", "自動保存");
        ja.put("save_album", "アルバムに保存"); ja.put("save_history", "履歴に保存");
        ja.put("use_pin", "PINを有効化"); ja.put("web_enable", "Web共有を有効化");
        ja.put("auto_start", "起動時に自動開始"); ja.put("receive_notif", "通知から受信");
        ja.put("port", "ポート"); ja.put("timeout", "検索タイムアウト");
        ja.put("language", "言語"); ja.put("anim", "UIアニメーション");
        ja.put("set_name", "デバイス名"); ja.put("backup", "バックアップ / 復元");
        ja.put("about", "WatchSendについて"); ja.put("manage_acl", "ホワイト/ブラックリスト管理");

        ja.put("recv_request", "受信リクエスト"); ja.put("from_device", "送信元デバイス"); ja.put("wants_to_send_you", "ファイルを送信したい");
        ja.put("action", "操作"); ja.put("current_file", "現在のファイル"); ja.put("preparing", "準備中...");
        ja.put("transferring", "転送中..."); ja.put("calculating", "計算中..."); ja.put("saved_to_local", "ローカルに保存されました");
        ja.put("transfer_success", "転送成功"); ja.put("local_ipv6", "ローカル IPv6"); ja.put("getting", "取得中...");
        ja.put("not_probed", "未プローブ"); ja.put("probing", "プローブ中..."); ja.put("nat_addr", "NAT パブリックアドレス");
        ja.put("input_direct_ip", "相手のIP:ポート"); ja.put("selected_prefix", "選択済み "); ja.put("files_suffix", "個のファイル\n");
        ja.put("select_file_gen_pin", "ファイルを選択してPINを生成"); ja.put("pin_prefix", "PIN: "); ja.put("toast_input_ip", "相手のIPとポートを入力してください");
        ja.put("toast_input_pin", "6桁の数字PINを入力してください"); ja.put("ipv6_not_found", "パブリックIPv6が見つかりません"); ja.put("packing", "パッキング中...");
        ja.put("packed_connecting", "パッキング完了、接続中..."); ja.put("sending_prefix", "送信中... "); ja.put("send_done", "送信完了！");
        ja.put("failed_prefix", "失敗: "); ja.put("direct_self", "直接接続失敗: 自分自身には接続できません。"); ja.put("preparing_direct", "直接接続の準備中...");
        ja.put("read_failed", "読み取り失敗"); ja.put("prepare_failed", "準備失敗"); ja.put("direct_sending", "直接送信中...");
        ja.put("sending_percent", "送信中 "); ja.put("direct_send_done", "直接送信完了！"); ja.put("direct_failed", "直接送信失敗: ");
        ja.put("connecting", "接続中..."); ja.put("receiving_prefix", "受信中... "); ja.put("recv_unzipping", "受信完了、解凍中...");
        ja.put("recv_done", "受信完了！ダウンロードに保存しました。"); ja.put("recv_failed", "受信失敗: ");
    

        ja.put("round_screen_opt", "丸型画面最適化"); ja.put("round_screen_opt_desc", "端の見切れを防ぐ");
        ja.put("block_system_back", "システム戻るをブロック"); ja.put("block_system_back_desc", "スワイプとの競合を防ぐ");
        ja.put("dark_mode_desc", "白黒テーマ切替"); ja.put("transfer_settings", "転送");
        ja.put("auto_save_desc", "自動的に受け入れる"); ja.put("auto_whitelist_desc", "ホワイトリストのみ");
        ja.put("save_album_desc", "ギャラリーに保存"); ja.put("save_history_desc", "履歴を記録する");
        ja.put("use_pin_desc", "セキュリティを強化"); ja.put("https_enable_desc", "古い端末に影響する可能性");
        ja.put("web_enable_desc", "ブラウザ転送"); ja.put("auto_start_desc", "バックグラウンド実行");
        ja.put("receive_notif_desc", "システム権限が必要"); ja.put("port_settings", "ポート設定");
        ja.put("timeout_settings", "検索タイムアウト"); ja.put("device_security", "デバイスとセキュリティ");
        ja.put("manage_acl_desc", "信頼済みデバイスの管理"); ja.put("ui_scale_dialog_title", "UIスケール");
        ja.put("about_title", "情報"); ja.put("pin_title_dialog", "PIN変更");
        ja.put("port_dialog_title", "ポート"); ja.put("timeout_dialog_title", "タイムアウト(ms)");
        ja.put("filter_dialog_title", "拡張子ブロック"); ja.put("device_name_title", "デバイス名");
        ja.put("lang_title", "言語設定"); ja.put("acl_dialog_title", "ホワイト/ブラックリスト");
        ja.put("whitelist_title", "ホワイトリスト"); ja.put("blacklist_title", "ブラックリスト");
        ja.put("about_desc_watchsend", "作者: styisme\nAI アシスト\nMIT License\n\nLocalSend v2.2 互換");
    

        ja.put("start_service", "開始"); ja.put("stop_service", "停止"); ja.put("devices_nearby", "近くのデバイス");
        ja.put("no_device_scan", "デバイスなし、スキャンしてください"); ja.put("send_file", "ファイル送信"); ja.put("select_title_desc", "コンテンツを選択");
        ja.put("device", "デバイス"); ja.put("file", "ファイル"); ja.put("media", "メディア"); ja.put("app", "アプリ");
        ja.put("folder", "フォルダ"); ja.put("text", "テキスト"); ja.put("clipboard", "クリップボード"); ja.put("record", "受信履歴");
        ja.put("return_to_devices", "↩ デバイスに戻る"); ja.put("no_record", "履歴なし"); ja.put("file_not_found", "ファイルが見つかりません");
        ja.put("cannot_open", "開けません"); ja.put("no_app", "アプリなし"); ja.put("empty_folder", "空のフォルダ");
        ja.put("add_files", "ファイルを追加"); ja.put("select_folder_send", ">> [このフォルダを送信] <<"); ja.put("unknown_file", "不明なファイル");
        ja.put("files_count_prefix", "ファイル: "); ja.put("size_label", "サイズ: "); ja.put("clear_all_files", "すべて削除");
        ja.put("back_prev", "戻る"); ja.put("save_to_dir", "保存先: "); ja.put("ip_prefix", "IP ");
    
dict.put("ja", ja);

        // ============ 韩语 (ko) ============
        Map<String, String> ko = new HashMap<>(en);
        ko.put("running", "실행 중"); ko.put("stopped", "중지됨"); ko.put("start", "시작"); ko.put("stop", "중지");
        ko.put("log", "로그"); ko.put("scan", "스캔"); ko.put("select_content", "콘텐츠 선택"); ko.put("nearby_devices", "주변 기기");
        ko.put("no_device", "기기가 없습니다. 스캔하세요"); ko.put("receive_history", "수신 기록"); ko.put("settings", "설정");
        ko.put("send", "보내기"); ko.put("receive", "받기"); ko.put("file", "파일"); ko.put("media", "미디어");
        ko.put("folder", "폴더"); ko.put("app", "앱"); ko.put("text", "텍스트"); ko.put("clipboard", "클립보드");
        ko.put("not_selected", "파일 선택 안 됨"); ko.put("auto_save", "자동 저장"); ko.put("save_album", "앨범에 저장");
        ko.put("save_history", "기록에 저장"); ko.put("use_pin", "PIN 활성화"); ko.put("pin_off", "PIN: 비활성화");
        ko.put("pin_on", "현재 PIN: "); ko.put("network", "네트워크 및 자동화"); ko.put("web_enable", "웹 공유 활성화");
        ko.put("auto_start", "부팅 시 자동 시작"); ko.put("receive_notif", "알림으로 받기"); ko.put("port", "포트");
        ko.put("timeout", "검색 시간 초과"); ko.put("other", "기타"); ko.put("anim", "UI 애니메이션");
        ko.put("set_name", "기기 이름"); ko.put("backup", "백업 / 복원"); ko.put("about", "WatchSend 정보");
        ko.put("language", "언어"); ko.put("follow_system", "시스템 설정 따름"); ko.put("cancel", "취소");
        ko.put("confirm", "확인"); ko.put("close", "닫기"); ko.put("clear", "지우기"); ko.put("input_ip", "수동 IP 입력");
        ko.put("input_text", "텍스트 보내기"); ko.put("select_lang", "언어 선택"); ko.put("restart_tip", "언어가 변경되었습니다");
        ko.put("add_ip", "IP 추가"); ko.put("cross_net", "크로스넷"); ko.put("auto_whitelist", "즐겨찾기에서 자동 저장");
        ko.put("filter_ext_title", "위험한 확장자 차단"); ko.put("manage_acl", "화이트/블랙리스트 관리");
        ko.put("cross_net_title", "크로스넷 전송"); ko.put("cross_net_desc", "동일한 Wi-Fi에 연결할 필요 없습니다. 긱 채널 직접 연결.");
        ko.put("upnp_punch", "자동 UPnP"); ko.put("direct_mode", "직접 P2P 모드");
        ko.put("send_file_title", "파일 보내기"); ko.put("recv_file_title", "파일 받기");
        ko.put("gen_pin_btn", "파일 선택 및 6자리 PIN 생성"); ko.put("input_pin_hint", "6자리 PIN 입력");
        ko.put("connect_recv_btn", "연결 및 받기");
        ko.put("ipv6_local", "로컬 IPv6: [%s]:53317 (직접 연결 가능)"); ko.put("ipv6_none", "로컬 IPv6: 찾을 수 없음. LAN 또는 터널만 가능");
        ko.put("stun_wait", "STUN 프로빙 중..."); ko.put("stun_fail", "STUN 실패 (대칭형 NAT)");
        ko.put("stun_ok", "NAT 공인 주소: "); ko.put("stun_probe", "STUN 프로브");
        ko.put("stun_tip", "NAT 주소: 오른쪽 프로브 클릭"); ko.put("advanced_tip", "고급 팁: FRP/Ngrok 또는 ZeroTier와 결합하고 생성된 공인 주소를 직접 연결에 입력하세요.");
        ko.put("file_count", "파일: %d"); ko.put("size_label", "크기: %s"); ko.put("unknown_size", "알 수 없는 크기");
        ko.put("manage", "관리"); ko.put("clear_all", "모든 목록 지우기");
        ko.put("view_whitelist", "화이트리스트 보기/제거"); ko.put("view_blacklist", "블랙리스트 보기/제거");
        ko.put("all_deleted", "모두 지워짐"); ko.put("export_log", "로그 내보내기");
        ko.put("input_address", "주소 입력"); ko.put("label", "라벨"); ko.put("ip_address", "IP 주소");
                // ---- P1 高频词条 ----
        ko.put("general", "일반"); ko.put("accept", "수락"); ko.put("reject", "거절");
        ko.put("recv_completed", "수신 완료"); ko.put("send_completed", "전송 완료"); ko.put("interrupted", "중단됨");
        ko.put("unknown_device", "알 수 없는 기기"); ko.put("unknown_file", "알 수 없는 파일");
        ko.put("save_dir_prefix", "저장 위치: "); ko.put("wants_to_send", "파일을 보내려 합니다");
        ko.put("receive_request", "파일 수신 요청"); ko.put("service_running", "WatchSend 실행 중");
        ko.put("cn_gen_pin_send", "비밀번호 생성 후 전송"); ko.put("cn_direct_send", "선택 항목 직접 전송");
        ko.put("cn_status_packing", "파일 패킹 중..."); ko.put("cn_status_connecting", "연결 중...");
        ko.put("cn_status_send_done", "전송 완료! 상대방 수신 대기 중.");
        ko.put("cn_status_recv_done", "수신 완료! Download에 압축 해제됨");
        ko.put("cn_pin_invalid", "6자리 숫자 비밀번호를 입력하세요");
        ko.put("cn_direct_invalid", "상대방 공인 IP와 포트를 입력하세요");
        ko.put("manage_list", "목록 관리"); ko.put("add_files", "파일 추가");
        ko.put("delete_all", "전체 삭제"); ko.put("select_title", "선택");
        ko.put("select_app", "앱 선택 (APK)");
        ko.put("dark_mode", "다크 모드"); ko.put("modify_pin", "PIN 변경");
        ko.put("https_enable", "HTTPS 활성화"); ko.put("auto_save", "자동 저장");
        ko.put("save_album", "앨범에 저장"); ko.put("save_history", "기록에 저장");
        ko.put("use_pin", "PIN 활성화"); ko.put("web_enable", "웹 공유 활성화");
        ko.put("auto_start", "부팅 시 자동 시작"); ko.put("receive_notif", "알림으로 받기");
        ko.put("port", "포트"); ko.put("timeout", "검색 시간 초과");
        ko.put("language", "언어"); ko.put("anim", "UI 애니메이션");
        ko.put("set_name", "기기 이름"); ko.put("backup", "백업 / 복원");
        ko.put("about", "WatchSend 정보"); ko.put("manage_acl", "화이트/블랙리스트 관리");

        ko.put("recv_request", "수신 요청"); ko.put("from_device", "보낸 기기"); ko.put("wants_to_send_you", "파일을 보내려 합니다");
        ko.put("action", "작업"); ko.put("current_file", "현재 파일"); ko.put("preparing", "준비 중...");
        ko.put("transferring", "전송 중..."); ko.put("calculating", "계산 중..."); ko.put("saved_to_local", "로컬에 저장됨");
        ko.put("transfer_success", "전송 성공"); ko.put("local_ipv6", "로컬 IPv6"); ko.put("getting", "가져오는 중...");
        ko.put("not_probed", "프로브 안 됨"); ko.put("probing", "프로브 중..."); ko.put("nat_addr", "NAT 공인 주소");
        ko.put("input_direct_ip", "상대 IP:포트"); ko.put("selected_prefix", "선택됨 "); ko.put("files_suffix", "개 파일\n");
        ko.put("select_file_gen_pin", "파일 선택 및 PIN 생성"); ko.put("pin_prefix", "PIN: "); ko.put("toast_input_ip", "상대 IP와 포트를 입력하세요");
        ko.put("toast_input_pin", "6자리 숫자 PIN을 입력하세요"); ko.put("ipv6_not_found", "공인 IPv6를 찾을 수 없음"); ko.put("packing", "파일 패킹 중...");
        ko.put("packed_connecting", "패킹 완료, 연결 중..."); ko.put("sending_prefix", "전송 중... "); ko.put("send_done", "전송 완료!");
        ko.put("failed_prefix", "실패: "); ko.put("direct_self", "직접 연결 실패: 자신에게 연결할 수 없습니다."); ko.put("preparing_direct", "직접 연결 준비 중...");
        ko.put("read_failed", "읽기 실패"); ko.put("prepare_failed", "준비 실패"); ko.put("direct_sending", "직접 전송 중...");
        ko.put("sending_percent", "전송 중 "); ko.put("direct_send_done", "직접 전송 완료!"); ko.put("direct_failed", "직접 전송 실패: ");
        ko.put("connecting", "연결 중..."); ko.put("receiving_prefix", "수신 중... "); ko.put("recv_unzipping", "수신 완료, 압축 해제 중...");
        ko.put("recv_done", "수신 완료! 다운로드에 저장됨."); ko.put("recv_failed", "수신 실패: ");
    

        ko.put("round_screen_opt", "원형 화면 최적화"); ko.put("round_screen_opt_desc", "가장자리 잘림 방지");
        ko.put("block_system_back", "시스템 뒤로 가기 차단"); ko.put("block_system_back_desc", "스와이프 충돌 방지");
        ko.put("dark_mode_desc", "흑백 테마 전환"); ko.put("transfer_settings", "전송");
        ko.put("auto_save_desc", "자동으로 받기"); ko.put("auto_whitelist_desc", "화이트리스트만");
        ko.put("save_album_desc", "갤러리에 저장"); ko.put("save_history_desc", "기록 저장");
        ko.put("use_pin_desc", "보안 강화"); ko.put("https_enable_desc", "구형 기기에 영향 줄 수 있음");
        ko.put("web_enable_desc", "브라우저 전송"); ko.put("auto_start_desc", "백그라운드 실행");
        ko.put("receive_notif_desc", "시스템 권한 필요"); ko.put("port_settings", "포트 설정");
        ko.put("timeout_settings", "검색 시간 초과"); ko.put("device_security", "기기 및 보안");
        ko.put("manage_acl_desc", "신뢰할 수 있는 기기 관리"); ko.put("ui_scale_dialog_title", "UI 크기 조절");
        ko.put("about_title", "정보"); ko.put("pin_title_dialog", "PIN 변경");
        ko.put("port_dialog_title", "포트"); ko.put("timeout_dialog_title", "시간 초과(ms)");
        ko.put("filter_dialog_title", "확장자 차단"); ko.put("device_name_title", "기기 이름");
        ko.put("lang_title", "언어 설정"); ko.put("acl_dialog_title", "화이트/블랙리스트");
        ko.put("whitelist_title", "화이트리스트"); ko.put("blacklist_title", "블랙리스트");
        ko.put("about_desc_watchsend", "제작: styisme\nAI 지원\nMIT License\n\nLocalSend v2.2 호환");
    

        ko.put("start_service", "시작"); ko.put("stop_service", "중지"); ko.put("devices_nearby", "주변 기기");
        ko.put("no_device_scan", "기기가 없습니다. 스캔하세요"); ko.put("send_file", "파일 보내기"); ko.put("select_title_desc", "콘텐츠 선택");
        ko.put("device", "기기"); ko.put("file", "파일"); ko.put("media", "미디어"); ko.put("app", "앱");
        ko.put("folder", "폴더"); ko.put("text", "텍스트"); ko.put("clipboard", "클립보드"); ko.put("record", "수신 기록");
        ko.put("return_to_devices", "↩ 기기로 돌아가기"); ko.put("no_record", "기록 없음"); ko.put("file_not_found", "파일을 찾을 수 없음");
        ko.put("cannot_open", "열 수 없음"); ko.put("no_app", "앱 없음"); ko.put("empty_folder", "빈 폴더");
        ko.put("add_files", "파일 추가"); ko.put("select_folder_send", ">> [이 폴더 보내기] <<"); ko.put("unknown_file", "알 수 없는 파일");
        ko.put("files_count_prefix", "파일: "); ko.put("size_label", "크기: "); ko.put("clear_all_files", "전체 삭제");
        ko.put("back_prev", "뒤로 가기"); ko.put("save_to_dir", "저장 위치: "); ko.put("ip_prefix", "IP ");
    
dict.put("ko", ko);

        // ============ 德语 (de) ============
        Map<String, String> de = new HashMap<>(en);
        de.put("running", "Läuft"); de.put("stopped", "Gestoppt"); de.put("start", "Start"); de.put("stop", "Stopp");
        de.put("log", "Protokoll"); de.put("scan", "Scannen"); de.put("select_content", "Inhalt wählen"); de.put("nearby_devices", "Geräte in der Nähe");
        de.put("no_device", "Keine Geräte, scannen"); de.put("receive_history", "Empfangsverlauf"); de.put("settings", "Einstellungen");
        de.put("send", "Senden"); de.put("receive", "Empfangen"); de.put("file", "Datei"); de.put("media", "Medien");
        de.put("folder", "Ordner"); de.put("app", "App"); de.put("text", "Text"); de.put("clipboard", "Zwischenablage");
        de.put("not_selected", "Keine Datei gewählt"); de.put("auto_save", "Automatisch speichern"); de.put("save_album", "Im Album speichern");
        de.put("save_history", "Im Verlauf speichern"); de.put("use_pin", "PIN aktivieren"); de.put("pin_off", "PIN: Inaktiv");
        de.put("pin_on", "Aktuelle PIN: "); de.put("network", "Netzwerk & Automatisierung"); de.put("web_enable", "Web-Freigabe");
        de.put("auto_start", "Autostart"); de.put("receive_notif", "Über Benachrichtigung"); de.put("port", "Port");
        de.put("timeout", "Such-Timeout"); de.put("other", "Andere"); de.put("anim", "UI-Animation");
        de.put("set_name", "Gerätename"); de.put("backup", "Sicherung / Wiederherstellung"); de.put("about", "Über WatchSend");
        de.put("language", "Sprache"); de.put("follow_system", "Systemsprache"); de.put("cancel", "Abbrechen");
        de.put("confirm", "OK"); de.put("close", "Schließen"); de.put("clear", "Löschen"); de.put("input_ip", "Manuelle IP");
        de.put("input_text", "Text senden"); de.put("select_lang", "Sprache wählen"); de.put("restart_tip", "Sprache geändert");
        de.put("add_ip", "IP hinzufügen"); de.put("cross_net", "Cross-Netz"); de.put("auto_whitelist", "Automatisch von Favoriten speichern");
        de.put("filter_ext_title", "Gefährliche Endungen blockieren"); de.put("manage_acl", "Weiß-/Schwarzliste verwalten");
        de.put("cross_net_title", "Cross-Netz-Übertragung"); de.put("cross_net_desc", "Kein gleiches Wi-Fi erforderlich, Geek-Kanal direkt verbinden.");
        de.put("upnp_punch", "Auto UPnP"); de.put("direct_mode", "Direkter P2P-Modus");
        de.put("send_file_title", "Datei senden"); de.put("recv_file_title", "Datei empfangen");
        de.put("gen_pin_btn", "Datei auswählen & PIN generieren"); de.put("input_pin_hint", "6-stellige PIN eingeben");
        de.put("connect_recv_btn", "Verbinden & Empfangen");
        de.put("ipv6_local", "Lokale IPv6: [%s]:53317 (Direktverbindung)"); de.put("ipv6_none", "Lokale IPv6: Nicht gefunden. Nur LAN oder Tunnel");
        de.put("stun_wait", "STUN-Sonde läuft..."); de.put("stun_fail", "STUN fehlgeschlagen (Symmetrisches NAT)");
        de.put("stun_ok", "NAT öffentliche Adresse: "); de.put("stun_probe", "STUN-Sonde");
        de.put("stun_tip", "NAT-Adresse: Klicken Sie rechts auf Sonde"); de.put("advanced_tip", "Profi-Tipp: Kombinieren Sie mit FRP/Ngrok oder ZeroTier und geben Sie die generierte öffentliche Adresse in Direktverbindung ein.");
        de.put("file_count", "Dateien: %d"); de.put("size_label", "Größe: %s"); de.put("unknown_size", "Unbekannte Größe");
        de.put("manage", "Verwalten"); de.put("clear_all", "Alle Listen löschen");
        de.put("view_whitelist", "Whitelist anzeigen/entfernen"); de.put("view_blacklist", "Blacklist anzeigen/entfernen");
        de.put("all_deleted", "Alle gelöscht"); de.put("export_log", "Protokoll exportieren");
        de.put("input_address", "Adresse eingeben"); de.put("label", "Etikett"); de.put("ip_address", "IP-Adresse");
                // ---- P1 高频词条 ----
        de.put("general", "Allgemein"); de.put("accept", "Annehmen"); de.put("reject", "Ablehnen");
        de.put("recv_completed", "Empfangen"); de.put("send_completed", "Gesendet"); de.put("interrupted", "Unterbrochen");
        de.put("unknown_device", "Unbekanntes Gerät"); de.put("unknown_file", "Unbekannte Datei");
        de.put("save_dir_prefix", "Speichern unter: "); de.put("wants_to_send", "Möchte dir Dateien senden");
        de.put("receive_request", "Dateiempfangsanfrage"); de.put("service_running", "WatchSend läuft");
        de.put("cn_gen_pin_send", "PIN erzeugen & senden"); de.put("cn_direct_send", "Auswahl direkt senden");
        de.put("cn_status_packing", "Dateien werden gepackt..."); de.put("cn_status_connecting", "Verbinden...");
        de.put("cn_status_send_done", "Gesendet! Warte auf Peer.");
        de.put("cn_status_recv_done", "Empfangen! In Download entpackt");
        de.put("cn_pin_invalid", "6-stellige PIN eingeben");
        de.put("cn_direct_invalid", "Peer Public-IP und Port eingeben");
        de.put("manage_list", "Listenverwaltung"); de.put("add_files", "Dateien hinzufügen");
        de.put("delete_all", "Alle löschen"); de.put("select_title", "Auswählen");
        de.put("select_app", "App auswählen (APK)");
        de.put("dark_mode", "Dunkelmodus"); de.put("modify_pin", "PIN ändern");
        de.put("https_enable", "HTTPS aktivieren"); de.put("auto_save", "Automatisch speichern");
        de.put("save_album", "Im Album speichern"); de.put("save_history", "Im Verlauf speichern");
        de.put("use_pin", "PIN aktivieren"); de.put("web_enable", "Web-Freigabe");
        de.put("auto_start", "Autostart"); de.put("receive_notif", "Über Benachrichtigung");
        de.put("port", "Port"); de.put("timeout", "Such-Timeout");
        de.put("language", "Sprache"); de.put("anim", "UI-Animation");
        de.put("set_name", "Gerätename"); de.put("backup", "Sicherung / Wiederherstellung");
        de.put("about", "Über WatchSend"); de.put("manage_acl", "Weiß-/Schwarzliste verwalten");

        de.put("recv_request", "Empfangsanfrage"); de.put("from_device", "Von Gerät"); de.put("wants_to_send_you", "Möchte dir Dateien senden");
        de.put("action", "Aktion"); de.put("current_file", "Aktuelle Datei"); de.put("preparing", "Vorbereiten...");
        de.put("transferring", "Übertragen..."); de.put("calculating", "Berechnen..."); de.put("saved_to_local", "Lokal gespeichert");
        de.put("transfer_success", "Erfolg"); de.put("local_ipv6", "Lokale IPv6"); de.put("getting", "Abrufen...");
        de.put("not_probed", "Nicht geprüft"); de.put("probing", "Prüfen..."); de.put("nat_addr", "NAT-Adresse");
        de.put("input_direct_ip", "Peer IP:Port"); de.put("selected_prefix", "Ausgewählt "); de.put("files_suffix", "Dateien\n");
        de.put("select_file_gen_pin", "Datei wählen & PIN generieren"); de.put("pin_prefix", "PIN: "); de.put("toast_input_ip", "Bitte Peer-IP und Port eingeben");
        de.put("toast_input_pin", "Bitte 6-stellige PIN eingeben"); de.put("ipv6_not_found", "Öffentliche IPv6 nicht gefunden"); de.put("packing", "Dateien packen...");
        de.put("packed_connecting", "Gepackt, verbinden..."); de.put("sending_prefix", "Senden... "); de.put("send_done", "Gesendet!");
        de.put("failed_prefix", "Fehlgeschlagen: "); de.put("direct_self", "Direktverbindung fehlgeschlagen: Nicht zu sich selbst."); de.put("preparing_direct", "Direktverbindung vorbereiten...");
        de.put("read_failed", "Lesen fehlgeschlagen"); de.put("prepare_failed", "Vorbereiten fehlgeschlagen"); de.put("direct_sending", "Direkt senden...");
        de.put("sending_percent", "Senden "); de.put("direct_send_done", "Direkt gesendet!"); de.put("direct_failed", "Direkt fehlgeschlagen: ");
        de.put("connecting", "Verbinden..."); de.put("receiving_prefix", "Empfangen... "); de.put("recv_unzipping", "Empfangen, entpacken...");
        de.put("recv_done", "Empfangen! In Downloads gespeichert."); de.put("recv_failed", "Empfangen fehlgeschlagen: ");
    

        de.put("round_screen_opt", "Rundbildschirm-Opt"); de.put("round_screen_opt_desc", "Randabschnitt verhindern");
        de.put("block_system_back", "System-Zurück blockieren"); de.put("block_system_back_desc", "Gestenkonflikt verhindern");
        de.put("dark_mode_desc", "Schwarz/Weiß-Thema"); de.put("transfer_settings", "Übertragung");
        de.put("auto_save_desc", "Automatisch annehmen"); de.put("auto_whitelist_desc", "Nur Whitelist-Geräte");
        de.put("save_album_desc", "In Galerie speichern"); de.put("save_history_desc", "Verlauf aufzeichnen");
        de.put("use_pin_desc", "Sicherheit erhöhen"); de.put("https_enable_desc", "Kann alte Geräte beeinträchtigen");
        de.put("web_enable_desc", "Browser-Übertragung"); de.put("auto_start_desc", "Im Hintergrund ausführen");
        de.put("receive_notif_desc", "Systemberechtigung erforderlich"); de.put("port_settings", "Port-Einstellungen");
        de.put("timeout_settings", "Such-Timeout"); de.put("device_security", "Gerät & Sicherheit");
        de.put("manage_acl_desc", "Vertrauenswürdige Geräte verwalten"); de.put("ui_scale_dialog_title", "UI skalieren");
        de.put("about_title", "Über"); de.put("pin_title_dialog", "PIN ändern");
        de.put("port_dialog_title", "Port"); de.put("timeout_dialog_title", "Timeout(ms)");
        de.put("filter_dialog_title", "Endungen blockieren"); de.put("device_name_title", "Gerätename");
        de.put("lang_title", "Spracheinstellungen"); de.put("acl_dialog_title", "Weiß-/Schwarzliste");
        de.put("whitelist_title", "Whitelist"); de.put("blacklist_title", "Blacklist");
        de.put("about_desc_watchsend", "Autor: styisme\nKI-unterstützt\nMIT License\n\nKompatibel mit LocalSend v2.2");
    

        de.put("start_service", "Start"); de.put("stop_service", "Stopp"); de.put("devices_nearby", "Geräte in der Nähe");
        de.put("no_device_scan", "Keine Geräte, scannen"); de.put("send_file", "Datei senden"); de.put("select_title_desc", "Inhalt wählen");
        de.put("device", "Gerät"); de.put("file", "Datei"); de.put("media", "Medien"); de.put("app", "App");
        de.put("folder", "Ordner"); de.put("text", "Text"); de.put("clipboard", "Zwischenablage"); de.put("record", "Empfangsverlauf");
        de.put("return_to_devices", "↩ Zurück zu Geräten"); de.put("no_record", "Keine Einträge"); de.put("file_not_found", "Datei nicht gefunden");
        de.put("cannot_open", "Kann nicht öffnen"); de.put("no_app", "Keine Apps"); de.put("empty_folder", "Leerer Ordner");
        de.put("add_files", "Dateien anfügen"); de.put("select_folder_send", ">> [Diesen Ordner senden] <<"); de.put("unknown_file", "Unbekannte Datei");
        de.put("files_count_prefix", "Dateien: "); de.put("size_label", "Größe: "); de.put("clear_all_files", "Alle löschen");
        de.put("back_prev", "Zurück"); de.put("save_to_dir", "Speichern unter: "); de.put("ip_prefix", "IP ");
    
dict.put("de", de);

        // ============ 法语 (fr) ============
        Map<String, String> fr = new HashMap<>(en);
        fr.put("running", "En cours"); fr.put("stopped", "Arrêté"); fr.put("start", "Démarrer"); fr.put("stop", "Arrêter");
        fr.put("log", "Journal"); fr.put("scan", "Scanner"); fr.put("select_content", "Sélectionner"); fr.put("nearby_devices", "Appareils à proximité");
        fr.put("no_device", "Aucun appareil, scannez"); fr.put("receive_history", "Historique"); fr.put("settings", "Paramètres");
        fr.put("send", "Envoyer"); fr.put("receive", "Recevoir"); fr.put("file", "Fichier"); fr.put("media", "Média");
        fr.put("folder", "Dossier"); fr.put("app", "Appli"); fr.put("text", "Texte"); fr.put("clipboard", "Presse-papiers");
        fr.put("not_selected", "Aucun fichier"); fr.put("auto_save", "Sauvegarde auto"); fr.put("save_album", "Sauver dans l'album");
        fr.put("save_history", "Sauver dans l'historique"); fr.put("use_pin", "Activer le PIN"); fr.put("pin_off", "PIN : Désactivé");
        fr.put("pin_on", "PIN actuel : "); fr.put("network", "Réseau & Automatisation"); fr.put("web_enable", "Partage Web");
        fr.put("auto_start", "Démarrage auto"); fr.put("receive_notif", "Via notification"); fr.put("port", "Port");
        fr.put("timeout", "Délai de recherche"); fr.put("other", "Autre"); fr.put("anim", "Animations");
        fr.put("set_name", "Nom de l'appareil"); fr.put("backup", "Sauvegarde / Restauration"); fr.put("about", "À propos d'WatchSend");
        fr.put("language", "Langue"); fr.put("follow_system", "Système"); fr.put("cancel", "Annuler");
        fr.put("confirm", "OK"); fr.put("close", "Fermer"); fr.put("clear", "Effacer"); fr.put("input_ip", "IP manuelle");
        fr.put("input_text", "Envoyer texte"); fr.put("select_lang", "Choisir la langue"); fr.put("restart_tip", "Langue modifiée");
        fr.put("add_ip", "Ajouter IP"); fr.put("cross_net", "Réseau croisé"); fr.put("auto_whitelist", "Sauvegarde auto depuis les favoris");
        fr.put("filter_ext_title", "Bloquer les extensions dangereuses"); fr.put("manage_acl", "Gérer liste blanche/noire");
        fr.put("cross_net_title", "Transfert réseau croisé"); fr.put("cross_net_desc", "Pas besoin du même Wi-Fi, connexion directe du canal geek.");
        fr.put("upnp_punch", "Auto UPnP"); fr.put("direct_mode", "Mode P2P direct");
        fr.put("send_file_title", "Envoyer un fichier"); fr.put("recv_file_title", "Recevoir un fichier");
        fr.put("gen_pin_btn", "Sélectionner le fichier et générer un code PIN"); fr.put("input_pin_hint", "Entrez le code PIN à 6 chiffres");
        fr.put("connect_recv_btn", "Connecter et recevoir");
        fr.put("ipv6_local", "IPv6 locale : [%s]:53317 (Connexion directe)"); fr.put("ipv6_none", "IPv6 locale : Non trouvée. LAN ou tunnel uniquement");
        fr.put("stun_wait", "Sonde STUN en cours..."); fr.put("stun_fail", "Échec STUN (NAT symétrique)");
        fr.put("stun_ok", "Adresse publique NAT : "); fr.put("stun_probe", "Sonde STUN");
        fr.put("stun_tip", "Adresse NAT : Cliquez sur la sonde à droite"); fr.put("advanced_tip", "Astuce pro : Combinez avec FRP/Ngrok ou ZeroTier et saisissez l'adresse publique générée dans Connexion directe.");
        fr.put("file_count", "Fichiers : %d"); fr.put("size_label", "Taille : %s"); fr.put("unknown_size", "Taille inconnue");
        fr.put("manage", "Gérer"); fr.put("clear_all", "Effacer toutes les listes");
        fr.put("view_whitelist", "Voir/Supprimer la liste blanche"); fr.put("view_blacklist", "Voir/Supprimer la liste noire");
        fr.put("all_deleted", "Tout effacé"); fr.put("export_log", "Exporter le journal");
        fr.put("input_address", "Entrez l'adresse"); fr.put("label", "Étiquette"); fr.put("ip_address", "Adresse IP");
                // ---- P1 高频词条 ----
        fr.put("general", "Général"); fr.put("accept", "Accepter"); fr.put("reject", "Refuser");
        fr.put("recv_completed", "Reçu"); fr.put("send_completed", "Envoyé"); fr.put("interrupted", "Interrompu");
        fr.put("unknown_device", "Appareil inconnu"); fr.put("unknown_file", "Fichier inconnu");
        fr.put("save_dir_prefix", "Enregistrer dans : "); fr.put("wants_to_send", "Veut t'envoyer des fichiers");
        fr.put("receive_request", "Demande de réception"); fr.put("service_running", "WatchSend en cours");
        fr.put("cn_gen_pin_send", "Générer PIN et envoyer"); fr.put("cn_direct_send", "Envoi direct sélection");
        fr.put("cn_status_packing", "Empaquetage..."); fr.put("cn_status_connecting", "Connexion...");
        fr.put("cn_status_send_done", "Envoyé ! En attente du pair.");
        fr.put("cn_status_recv_done", "Reçu ! Extrait dans Download");
        fr.put("cn_pin_invalid", "Entrez le PIN à 6 chiffres");
        fr.put("cn_direct_invalid", "Entrez l'IP publique et le port");
        fr.put("manage_list", "Gestion des listes"); fr.put("add_files", "Ajouter fichiers");
        fr.put("delete_all", "Tout supprimer"); fr.put("select_title", "Sélectionner");
        fr.put("select_app", "Choisir l'appli (APK)");
        fr.put("dark_mode", "Mode sombre"); fr.put("modify_pin", "Modifier le PIN");
        fr.put("https_enable", "Activer HTTPS"); fr.put("auto_save", "Sauvegarde auto");
        fr.put("save_album", "Sauver dans l'album"); fr.put("save_history", "Sauver dans l'historique");
        fr.put("use_pin", "Activer le PIN"); fr.put("web_enable", "Partage Web");
        fr.put("auto_start", "Démarrage auto"); fr.put("receive_notif", "Via notification");
        fr.put("port", "Port"); fr.put("timeout", "Délai de recherche");
        fr.put("language", "Langue"); fr.put("anim", "Animations");
        fr.put("set_name", "Nom de l'appareil"); fr.put("backup", "Sauvegarde / Restauration");
        fr.put("about", "À propos d'WatchSend"); fr.put("manage_acl", "Gérer liste blanche/noire");

        fr.put("recv_request", "Demande de réception"); fr.put("from_device", "De l'appareil"); fr.put("wants_to_send_you", "Veut t'envoyer des fichiers");
        fr.put("action", "Action"); fr.put("current_file", "Fichier actuel"); fr.put("preparing", "Préparation...");
        fr.put("transferring", "Transfert..."); fr.put("calculating", "Calcul..."); fr.put("saved_to_local", "Sauvegardé localement");
        fr.put("transfer_success", "Succès"); fr.put("local_ipv6", "IPv6 locale"); fr.put("getting", "Obtention...");
        fr.put("not_probed", "Non sondé"); fr.put("probing", "Sondage..."); fr.put("nat_addr", "Adresse NAT");
        fr.put("input_direct_ip", "IP:Port du pair"); fr.put("selected_prefix", "Sélectionné "); fr.put("files_suffix", "fichiers\n");
        fr.put("select_file_gen_pin", "Choisir fichier & générer PIN"); fr.put("pin_prefix", "PIN : "); fr.put("toast_input_ip", "Entrez l'IP et le port du pair");
        fr.put("toast_input_pin", "Entrez le PIN à 6 chiffres"); fr.put("ipv6_not_found", "IPv6 publique introuvable"); fr.put("packing", "Empaquetage...");
        fr.put("packed_connecting", "Empaqueté, connexion..."); fr.put("sending_prefix", "Envoi... "); fr.put("send_done", "Envoyé !");
        fr.put("failed_prefix", "Échec : "); fr.put("direct_self", "Échec direct : Impossible de se connecter à soi-même."); fr.put("preparing_direct", "Préparation directe...");
        fr.put("read_failed", "Échec de lecture"); fr.put("prepare_failed", "Échec de préparation"); fr.put("direct_sending", "Envoi direct...");
        fr.put("sending_percent", "Envoi "); fr.put("direct_send_done", "Envoi direct terminé !"); fr.put("direct_failed", "Échec direct : ");
        fr.put("connecting", "Connexion..."); fr.put("receiving_prefix", "Réception... "); fr.put("recv_unzipping", "Reçu, extraction...");
        fr.put("recv_done", "Reçu ! Sauvegardé dans Téléchargements."); fr.put("recv_failed", "Échec de réception : ");
    

        fr.put("round_screen_opt", "Opt. écran rond"); fr.put("round_screen_opt_desc", "Éviter la coupe des bords");
        fr.put("block_system_back", "Bloquer retour système"); fr.put("block_system_back_desc", "Éviter les conflits de gestes");
        fr.put("dark_mode_desc", "Thème noir/blanc"); fr.put("transfer_settings", "Transfert");
        fr.put("auto_save_desc", "Accepter automatiquement"); fr.put("auto_whitelist_desc", "Appareils en liste blanche uniquement");
        fr.put("save_album_desc", "Enregistrer dans la galerie"); fr.put("save_history_desc", "Enregistrer l'historique");
        fr.put("use_pin_desc", "Améliorer la sécurité"); fr.put("https_enable_desc", "Peut affecter les anciens appareils");
        fr.put("web_enable_desc", "Transfert par navigateur"); fr.put("auto_start_desc", "Exécuter en arrière-plan");
        fr.put("receive_notif_desc", "Nécessite une autorisation système"); fr.put("port_settings", "Paramètres du port");
        fr.put("timeout_settings", "Délai de recherche"); fr.put("device_security", "Appareil & Sécurité");
        fr.put("manage_acl_desc", "Gérer les appareils de confiance"); fr.put("ui_scale_dialog_title", "Échelle de l'UI");
        fr.put("about_title", "À propos"); fr.put("pin_title_dialog", "Modifier le PIN");
        fr.put("port_dialog_title", "Port"); fr.put("timeout_dialog_title", "Délai(ms)");
        fr.put("filter_dialog_title", "Bloquer les extensions"); fr.put("device_name_title", "Nom de l'appareil");
        fr.put("lang_title", "Paramètres de langue"); fr.put("acl_dialog_title", "Liste blanche/noire");
        fr.put("whitelist_title", "Liste blanche"); fr.put("blacklist_title", "Liste noire");
        fr.put("about_desc_watchsend", "Auteur : styisme\nAssisté par IA\nMIT License\n\nCompatible avec LocalSend v2.2");
    

        fr.put("start_service", "Démarrer"); fr.put("stop_service", "Arrêter"); fr.put("devices_nearby", "Appareils à proximité");
        fr.put("no_device_scan", "Aucun appareil, scannez"); fr.put("send_file", "Envoyer fichier"); fr.put("select_title_desc", "Sélectionner");
        fr.put("device", "Appareil"); fr.put("file", "Fichier"); fr.put("media", "Média"); fr.put("app", "Appli");
        fr.put("folder", "Dossier"); fr.put("text", "Texte"); fr.put("clipboard", "Presse-papiers"); fr.put("record", "Historique");
        fr.put("return_to_devices", "↩ Retour aux appareils"); fr.put("no_record", "Aucun historique"); fr.put("file_not_found", "Fichier introuvable");
        fr.put("cannot_open", "Impossible d'ouvrir"); fr.put("no_app", "Aucune appli"); fr.put("empty_folder", "Dossier vide");
        fr.put("add_files", "Ajouter des fichiers"); fr.put("select_folder_send", ">> [Envoyer ce dossier] <<"); fr.put("unknown_file", "Fichier inconnu");
        fr.put("files_count_prefix", "Fichiers : "); fr.put("size_label", "Taille : "); fr.put("clear_all_files", "Tout supprimer");
        fr.put("back_prev", "Retour"); fr.put("save_to_dir", "Enregistrer dans : "); fr.put("ip_prefix", "IP ");
    
dict.put("fr", fr);

        // ============ 西班牙语 (es) ============
        Map<String, String> es = new HashMap<>(en);
        es.put("running", "Ejecutando"); es.put("stopped", "Detenido"); es.put("start", "Iniciar"); es.put("stop", "Detener");
        es.put("log", "Registro"); es.put("scan", "Escanear"); es.put("select_content", "Seleccionar contenido"); es.put("nearby_devices", "Dispositivos cercanos");
        es.put("no_device", "Sin dispositivos, escanea"); es.put("receive_history", "Historial"); es.put("settings", "Ajustes");
        es.put("send", "Enviar"); es.put("receive", "Recibir"); es.put("file", "Archivo"); es.put("media", "Multimedia");
        es.put("folder", "Carpeta"); es.put("app", "App"); es.put("text", "Texto"); es.put("clipboard", "Portapapeles");
        es.put("not_selected", "Sin archivo"); es.put("auto_save", "Guardar auto"); es.put("save_album", "Guardar en álbum");
        es.put("save_history", "Guardar en historial"); es.put("use_pin", "Activar PIN"); es.put("pin_off", "PIN: Inactivo");
        es.put("pin_on", "PIN actual: "); es.put("network", "Red y Automatización"); es.put("web_enable", "Compartir Web");
        es.put("auto_start", "Inicio automático"); es.put("receive_notif", "Vía notificación"); es.put("port", "Puerto");
        es.put("timeout", "Tiempo de búsqueda"); es.put("other", "Otros"); es.put("anim", "Animaciones");
        es.put("set_name", "Nombre del dispositivo"); es.put("backup", "Copia / Restaurar"); es.put("about", "Acerca de WatchSend");
        es.put("language", "Idioma"); es.put("follow_system", "Sistema"); es.put("cancel", "Cancelar");
        es.put("confirm", "Aceptar"); es.put("close", "Cerrar"); es.put("clear", "Limpiar"); es.put("input_ip", "IP manual");
        es.put("input_text", "Enviar texto"); es.put("select_lang", "Seleccionar idioma"); es.put("restart_tip", "Idioma cambiado");
        es.put("add_ip", "Añadir IP"); es.put("cross_net", "Red cruzada"); es.put("auto_whitelist", "Guardar auto desde favoritos");
        es.put("filter_ext_title", "Bloquear extensiones peligrosas"); es.put("manage_acl", "Gestionar lista blanca/negra");
        es.put("cross_net_title", "Transferencia red cruzada"); es.put("cross_net_desc", "No se necesita la misma Wi-Fi, conexión directa del canal geek.");
        es.put("upnp_punch", "Auto UPnP"); es.put("direct_mode", "Modo P2P directo");
        es.put("send_file_title", "Enviar archivo"); es.put("recv_file_title", "Recibir archivo");
        es.put("gen_pin_btn", "Seleccionar archivo y generar PIN"); es.put("input_pin_hint", "Ingrese el PIN de 6 dígitos");
        es.put("connect_recv_btn", "Conectar y recibir");
        es.put("ipv6_local", "IPv6 local: [%s]:53317 (Conexión directa)"); es.put("ipv6_none", "IPv6 local: No encontrada. Solo LAN o túnel");
        es.put("stun_wait", "Sondeo STUN..."); es.put("stun_fail", "Error de STUN (NAT simétrico)");
        es.put("stun_ok", "Dirección pública NAT: "); es.put("stun_probe", "Sonda STUN");
        es.put("stun_tip", "Dirección NAT: Haz clic en la sonda a la derecha"); es.put("advanced_tip", "Consejo pro: Combina con FRP/Ngrok o ZeroTier y rellena la dirección pública generada en Conexión directa.");
        es.put("file_count", "Archivos: %d"); es.put("size_label", "Tamaño: %s"); es.put("unknown_size", "Tamaño desconocido");
        es.put("manage", "Gestionar"); es.put("clear_all", "Borrar todas las listas");
        es.put("view_whitelist", "Ver/Eliminar lista blanca"); es.put("view_blacklist", "Ver/Eliminar lista negra");
        es.put("all_deleted", "Todo borrado"); es.put("export_log", "Exportar registro");
        es.put("input_address", "Ingresar dirección"); es.put("label", "Etiqueta"); es.put("ip_address", "Dirección IP");
                // ---- P1 高频词条 ----
        es.put("general", "General"); es.put("accept", "Aceptar"); es.put("reject", "Rechazar");
        es.put("recv_completed", "Recibido"); es.put("send_completed", "Enviado"); es.put("interrupted", "Interrumpido");
        es.put("unknown_device", "Dispositivo desconocido"); es.put("unknown_file", "Archivo desconocido");
        es.put("save_dir_prefix", "Guardar en: "); es.put("wants_to_send", "Quiere enviarte archivos");
        es.put("receive_request", "Solicitud de recepción"); es.put("service_running", "WatchSend en ejecución");
        es.put("cn_gen_pin_send", "Generar PIN y enviar"); es.put("cn_direct_send", "Envío directo selección");
        es.put("cn_status_packing", "Empaquetando..."); es.put("cn_status_connecting", "Conectando...");
        es.put("cn_status_send_done", "¡Enviado! Esperando al par.");
        es.put("cn_status_recv_done", "¡Recibido! Extraído en Download");
        es.put("cn_pin_invalid", "Ingrese el PIN de 6 dígitos");
        es.put("cn_direct_invalid", "Ingrese IP pública y puerto");
        es.put("manage_list", "Gestión de listas"); es.put("add_files", "Añadir archivos");
        es.put("delete_all", "Eliminar todo"); es.put("select_title", "Seleccionar");
        es.put("select_app", "Elegir app (APK)");
        es.put("dark_mode", "Modo oscuro"); es.put("modify_pin", "Modificar PIN");
        es.put("https_enable", "Activar HTTPS"); es.put("auto_save", "Guardar auto");
        es.put("save_album", "Guardar en álbum"); es.put("save_history", "Guardar en historial");
        es.put("use_pin", "Activar PIN"); es.put("web_enable", "Compartir Web");
        es.put("auto_start", "Inicio automático"); es.put("receive_notif", "Vía notificación");
        es.put("port", "Puerto"); es.put("timeout", "Tiempo de búsqueda");
        es.put("language", "Idioma"); es.put("anim", "Animaciones");
        es.put("set_name", "Nombre del dispositivo"); es.put("backup", "Copia / Restaurar");
        es.put("about", "Acerca de WatchSend"); es.put("manage_acl", "Gestionar lista blanca/negra");

        es.put("recv_request", "Solicitud de recepción"); es.put("from_device", "Del dispositivo"); es.put("wants_to_send_you", "Quiere enviarte archivos");
        es.put("action", "Acción"); es.put("current_file", "Archivo actual"); es.put("preparing", "Preparando...");
        es.put("transferring", "Transfiriendo..."); es.put("calculating", "Calculando..."); es.put("saved_to_local", "Guardado localmente");
        es.put("transfer_success", "Éxito"); es.put("local_ipv6", "IPv6 local"); es.put("getting", "Obteniendo...");
        es.put("not_probed", "No probado"); es.put("probing", "Probando..."); es.put("nat_addr", "Dirección NAT");
        es.put("input_direct_ip", "IP:Puerto del par"); es.put("selected_prefix", "Seleccionado "); es.put("files_suffix", "archivos\n");
        es.put("select_file_gen_pin", "Elegir archivo y gen. PIN"); es.put("pin_prefix", "PIN: "); es.put("toast_input_ip", "Ingrese IP y puerto del par");
        es.put("toast_input_pin", "Ingrese PIN de 6 dígitos"); es.put("ipv6_not_found", "IPv6 pública no encontrada"); es.put("packing", "Empaquetando...");
        es.put("packed_connecting", "Empaquetado, conectando..."); es.put("sending_prefix", "Enviando... "); es.put("send_done", "¡Enviado!");
        es.put("failed_prefix", "Falló: "); es.put("direct_self", "Falló directo: No se puede conectar a sí mismo."); es.put("preparing_direct", "Preparando directo...");
        es.put("read_failed", "Falló lectura"); es.put("prepare_failed", "Falló preparación"); es.put("direct_sending", "Enviando directo...");
        es.put("sending_percent", "Enviando "); es.put("direct_send_done", "¡Envío directo completado!"); es.put("direct_failed", "Falló directo: ");
        es.put("connecting", "Conectando..."); es.put("receiving_prefix", "Recibiendo... "); es.put("recv_unzipping", "Recibido, extrayendo...");
        es.put("recv_done", "¡Recibido! Guardado en Descargas."); es.put("recv_failed", "Falló recepción: ");
    

        es.put("round_screen_opt", "Opt. pantalla redonda"); es.put("round_screen_opt_desc", "Evitar corte de bordes");
        es.put("block_system_back", "Bloquear retroceso"); es.put("block_system_back_desc", "Evitar conflicto de gestos");
        es.put("dark_mode_desc", "Tema blanco/negro"); es.put("transfer_settings", "Transferencia");
        es.put("auto_save_desc", "Aceptar archivos automáticamente"); es.put("auto_whitelist_desc", "Solo dispositivos de lista blanca");
        es.put("save_album_desc", "Guardar en galería"); es.put("save_history_desc", "Guardar historial");
        es.put("use_pin_desc", "Mejorar seguridad"); es.put("https_enable_desc", "Puede afectar a dispositivos antiguos");
        es.put("web_enable_desc", "Transferencia por navegador"); es.put("auto_start_desc", "Ejecutar en segundo plano");
        es.put("receive_notif_desc", "Requiere permiso del sistema"); es.put("port_settings", "Ajustes de puerto");
        es.put("timeout_settings", "Tiempo de búsqueda"); es.put("device_security", "Dispositivo y Seguridad");
        es.put("manage_acl_desc", "Gestionar dispositivos de confianza"); es.put("ui_scale_dialog_title", "Escala de IU");
        es.put("about_title", "Acerca de"); es.put("pin_title_dialog", "Modificar PIN");
        es.put("port_dialog_title", "Puerto"); es.put("timeout_dialog_title", "Tiempo(ms)");
        es.put("filter_dialog_title", "Bloquear extensiones"); es.put("device_name_title", "Nombre del dispositivo");
        es.put("lang_title", "Ajustes de idioma"); es.put("acl_dialog_title", "Lista blanca/negra");
        es.put("whitelist_title", "Lista blanca"); es.put("blacklist_title", "Lista negra");
        es.put("about_desc_watchsend", "Autor: styisme\nAsistido por IA\nMIT License\n\nCompatible con LocalSend v2.2");
    

        es.put("start_service", "Iniciar"); es.put("stop_service", "Detener"); es.put("devices_nearby", "Dispositivos cercanos");
        es.put("no_device_scan", "Sin dispositivos, escanea"); es.put("send_file", "Enviar archivo"); es.put("select_title_desc", "Seleccionar");
        es.put("device", "Dispositivo"); es.put("file", "Archivo"); es.put("media", "Multimedia"); es.put("app", "App");
        es.put("folder", "Carpeta"); es.put("text", "Texto"); es.put("clipboard", "Portapapeles"); es.put("record", "Historial");
        es.put("return_to_devices", "↩ Volver a disp."); es.put("no_record", "Sin registros"); es.put("file_not_found", "Archivo no encontrado");
        es.put("cannot_open", "No se puede abrir"); es.put("no_app", "Sin apps"); es.put("empty_folder", "Carpeta vacía");
        es.put("add_files", "Añadir archivos"); es.put("select_folder_send", ">> [Enviar esta carpeta] <<"); es.put("unknown_file", "Archivo desconocido");
        es.put("files_count_prefix", "Archivos: "); es.put("size_label", "Tamaño: "); es.put("clear_all_files", "Borrar todo");
        es.put("back_prev", "Volver"); es.put("save_to_dir", "Guardar en: "); es.put("ip_prefix", "IP ");
    
dict.put("es", es);

        // ============ 俄语 (ru) ============
        Map<String, String> ru = new HashMap<>(en);
        ru.put("running", "Работает"); ru.put("stopped", "Остановлен"); ru.put("start", "Старт"); ru.put("stop", "Стоп");
        ru.put("log", "Лог"); ru.put("scan", "Сканировать"); ru.put("select_content", "Выбрать контент"); ru.put("nearby_devices", "Устройства рядом");
        ru.put("no_device", "Нет устройств, сканируйте"); ru.put("receive_history", "История"); ru.put("settings", "Настройки");
        ru.put("send", "Отправить"); ru.put("receive", "Получить"); ru.put("file", "Файл"); ru.put("media", "Медиа");
        ru.put("folder", "Папка"); ru.put("app", "Прил."); ru.put("text", "Текст"); ru.put("clipboard", "Буфер");
        ru.put("not_selected", "Файл не выбран"); ru.put("auto_save", "Автосохранение"); ru.put("save_album", "В альбом");
        ru.put("save_history", "В историю"); ru.put("use_pin", "Включить PIN"); ru.put("pin_off", "PIN: Выкл");
        ru.put("pin_on", "Текущий PIN: "); ru.put("network", "Сеть и Автоматизация"); ru.put("web_enable", "Веб-обмен");
        ru.put("auto_start", "Автозапуск"); ru.put("receive_notif", "Через уведомление"); ru.put("port", "Порт");
        ru.put("timeout", "Таймаут поиска"); ru.put("other", "Другое"); ru.put("anim", "Анимации");
        ru.put("set_name", "Имя устройства"); ru.put("backup", "Резервная копия"); ru.put("about", "О WatchSend");
        ru.put("language", "Язык"); ru.put("follow_system", "Системный"); ru.put("cancel", "Отмена");
        ru.put("confirm", "ОК"); ru.put("close", "Закрыть"); ru.put("clear", "Очистить"); ru.put("input_ip", "Ввести IP");
        ru.put("input_text", "Отправить текст"); ru.put("select_lang", "Выбрать язык"); ru.put("restart_tip", "Язык изменен");
        ru.put("add_ip", "Добавить IP"); ru.put("cross_net", "Кросс-сеть"); ru.put("auto_whitelist", "Автосохранение из избранного");
        ru.put("filter_ext_title", "Блокировать опасные расширения"); ru.put("manage_acl", "Управление белым/черным списком");
        ru.put("cross_net_title", "Кросс-сетевая передача"); ru.put("cross_net_desc", "Не нужно подключаться к тому же Wi-Fi, прямое подключение гик-канала.");
        ru.put("upnp_punch", "Авто UPnP"); ru.put("direct_mode", "Прямой режим P2P");
        ru.put("send_file_title", "Отправить файл"); ru.put("recv_file_title", "Получить файл");
        ru.put("gen_pin_btn", "Выбрать файл и сгенерировать PIN"); ru.put("input_pin_hint", "Введите 6-значный PIN");
        ru.put("connect_recv_btn", "Подключиться и получить");
        ru.put("ipv6_local", "Локальный IPv6: [%s]:53317 (Прямое подключение)"); ru.put("ipv6_none", "Локальный IPv6: Не найден. Только LAN или туннель");
        ru.put("stun_wait", "STUN-зондирование..."); ru.put("stun_fail", "Ошибка STUN (Симметричный NAT)");
        ru.put("stun_ok", "Публичный адрес NAT: "); ru.put("stun_probe", "STUN-зонд");
        ru.put("stun_tip", "Адрес NAT: Нажмите на зонд справа"); ru.put("advanced_tip", "Совет профи: Объедините с FRP/Ngrok или ZeroTier и введите сгенерированный публичный адрес в Прямое подключение.");
        ru.put("file_count", "Файлы: %d"); ru.put("size_label", "Размер: %s"); ru.put("unknown_size", "Неизвестный размер");
        ru.put("manage", "Управление"); ru.put("clear_all", "Очистить все списки");
        ru.put("view_whitelist", "Просмотр/Удаление белого списка"); ru.put("view_blacklist", "Просмотр/Удаление черного списка");
        ru.put("all_deleted", "Все очищено"); ru.put("export_log", "Экспорт журнала");
        ru.put("input_address", "Введите адрес"); ru.put("label", "Метка"); ru.put("ip_address", "IP-адрес");
                // ---- P1 高频词条 ----
        ru.put("general", "Общие"); ru.put("accept", "Принять"); ru.put("reject", "Отклонить");
        ru.put("recv_completed", "Получено"); ru.put("send_completed", "Отправлено"); ru.put("interrupted", "Прервано");
        ru.put("unknown_device", "Неизвестное устройство"); ru.put("unknown_file", "Неизвестный файл");
        ru.put("save_dir_prefix", "Сохранить в: "); ru.put("wants_to_send", "Хочет отправить тебе файлы");
        ru.put("receive_request", "Запрос на получение файла"); ru.put("service_running", "WatchSend работает");
        ru.put("cn_gen_pin_send", "Создать PIN и отправить"); ru.put("cn_direct_send", "Прямая отправка выбранного");
        ru.put("cn_status_packing", "Упаковка файлов..."); ru.put("cn_status_connecting", "Подключение...");
        ru.put("cn_status_send_done", "Отправлено! Ждём получателя.");
        ru.put("cn_status_recv_done", "Получено! Распаковано в Download");
        ru.put("cn_pin_invalid", "Введите 6-значный PIN");
        ru.put("cn_direct_invalid", "Введите публичный IP и порт");
        ru.put("manage_list", "Управление списками"); ru.put("add_files", "Добавить файлы");
        ru.put("delete_all", "Удалить всё"); ru.put("select_title", "Выбрать");
        ru.put("select_app", "Выбрать приложение (APK)");
        ru.put("dark_mode", "Темная тема"); ru.put("modify_pin", "Изменить PIN");
        ru.put("https_enable", "Включить HTTPS"); ru.put("auto_save", "Автосохранение");
        ru.put("save_album", "В альбом"); ru.put("save_history", "В историю");
        ru.put("use_pin", "Включить PIN"); ru.put("web_enable", "Веб-обмен");
        ru.put("auto_start", "Автозапуск"); ru.put("receive_notif", "Через уведомление");
        ru.put("port", "Порт"); ru.put("timeout", "Таймаут поиска");
        ru.put("language", "Язык"); ru.put("anim", "Анимации");
        ru.put("set_name", "Имя устройства"); ru.put("backup", "Резервная копия");
        ru.put("about", "О WatchSend"); ru.put("manage_acl", "Управление белым/черным списком");

        ru.put("recv_request", "Запрос на получение"); ru.put("from_device", "От устройства"); ru.put("wants_to_send_you", "Хочет отправить тебе файлы");
        ru.put("action", "Действие"); ru.put("current_file", "Текущий файл"); ru.put("preparing", "Подготовка...");
        ru.put("transferring", "Передача..."); ru.put("calculating", "Вычисление..."); ru.put("saved_to_local", "Сохранено локально");
        ru.put("transfer_success", "Успех"); ru.put("local_ipv6", "Локальный IPv6"); ru.put("getting", "Получение...");
        ru.put("not_probed", "Не проверено"); ru.put("probing", "Проверка..."); ru.put("nat_addr", "Адрес NAT");
        ru.put("input_direct_ip", "IP:Порт пира"); ru.put("selected_prefix", "Выбрано "); ru.put("files_suffix", "файлов\n");
        ru.put("select_file_gen_pin", "Выбрать файл и создать PIN"); ru.put("pin_prefix", "PIN: "); ru.put("toast_input_ip", "Введите IP и порт пира");
        ru.put("toast_input_pin", "Введите 6-значный PIN"); ru.put("ipv6_not_found", "Публичный IPv6 не найден"); ru.put("packing", "Упаковка файлов...");
        ru.put("packed_connecting", "Упаковано, подключение..."); ru.put("sending_prefix", "Отправка... "); ru.put("send_done", "Отправлено!");
        ru.put("failed_prefix", "Ошибка: "); ru.put("direct_self", "Ошибка: Нельзя подключиться к себе."); ru.put("preparing_direct", "Подготовка прямого подключения...");
        ru.put("read_failed", "Ошибка чтения"); ru.put("prepare_failed", "Ошибка подготовки"); ru.put("direct_sending", "Прямая отправка...");
        ru.put("sending_percent", "Отправка "); ru.put("direct_send_done", "Прямая отправка завершена!"); ru.put("direct_failed", "Ошибка прямого подключения: ");
        ru.put("connecting", "Подключение..."); ru.put("receiving_prefix", "Получение... "); ru.put("recv_unzipping", "Получено, распаковка...");
        ru.put("recv_done", "Получено! Сохранено в Загрузки."); ru.put("recv_failed", "Ошибка получения: ");
    

        ru.put("round_screen_opt", "Опт. круглого экрана"); ru.put("round_screen_opt_desc", "Предотвратить обрезку краев");
        ru.put("block_system_back", "Блокировать системный возврат"); ru.put("block_system_back_desc", "Предотвратить конфликт жестов");
        ru.put("dark_mode_desc", "Черно-белая тема"); ru.put("transfer_settings", "Передача");
        ru.put("auto_save_desc", "Автоматически принимать"); ru.put("auto_whitelist_desc", "Только из белого списка");
        ru.put("save_album_desc", "Сохранить в галерею"); ru.put("save_history_desc", "Записывать историю");
        ru.put("use_pin_desc", "Повысить безопасность"); ru.put("https_enable_desc", "Может повлиять на старые устройства");
        ru.put("web_enable_desc", "Передача через браузер"); ru.put("auto_start_desc", "Работать в фоне");
        ru.put("receive_notif_desc", "Требуется разрешение"); ru.put("port_settings", "Настройки порта");
        ru.put("timeout_settings", "Таймаут поиска"); ru.put("device_security", "Устройство и безопасность");
        ru.put("manage_acl_desc", "Управление доверенными устройствами"); ru.put("ui_scale_dialog_title", "Масштаб UI");
        ru.put("about_title", "О программе"); ru.put("pin_title_dialog", "Изменить PIN");
        ru.put("port_dialog_title", "Порт"); ru.put("timeout_dialog_title", "Таймаут(мс)");
        ru.put("filter_dialog_title", "Блокировать расширения"); ru.put("device_name_title", "Имя устройства");
        ru.put("lang_title", "Настройки языка"); ru.put("acl_dialog_title", "Белый/Черный список");
        ru.put("whitelist_title", "Белый список"); ru.put("blacklist_title", "Черный список");
        ru.put("about_desc_watchsend", "Автор: styisme\nС поддержкой ИИ\nMIT License\n\nСовместимо с LocalSend v2.2");
    

        ru.put("start_service", "Старт"); ru.put("stop_service", "Стоп"); ru.put("devices_nearby", "Устройства рядом");
        ru.put("no_device_scan", "Нет устройств, сканируйте"); ru.put("send_file", "Отправить файл"); ru.put("select_title_desc", "Выбрать контент");
        ru.put("device", "Устройство"); ru.put("file", "Файл"); ru.put("media", "Медиа"); ru.put("app", "Прил.");
        ru.put("folder", "Папка"); ru.put("text", "Текст"); ru.put("clipboard", "Буфер"); ru.put("record", "История");
        ru.put("return_to_devices", "↩ К устройствам"); ru.put("no_record", "Нет записей"); ru.put("file_not_found", "Файл не найден");
        ru.put("cannot_open", "Не удается открыть"); ru.put("no_app", "Нет приложений"); ru.put("empty_folder", "Пустая папка");
        ru.put("add_files", "Добавить файлы"); ru.put("select_folder_send", ">> [Отправить эту папку] <<"); ru.put("unknown_file", "Неизвестный файл");
        ru.put("files_count_prefix", "Файлы: "); ru.put("size_label", "Размер: "); ru.put("clear_all_files", "Удалить всё");
        ru.put("back_prev", "Назад"); ru.put("save_to_dir", "Сохранить в: "); ru.put("ip_prefix", "IP ");
    
dict.put("ru", ru);

        // ============ 葡萄牙语 (pt) ============
        Map<String, String> pt = new HashMap<>(en);
        pt.put("running", "Rodando"); pt.put("stopped", "Parado"); pt.put("start", "Iniciar"); pt.put("stop", "Parar");
        pt.put("log", "Log"); pt.put("scan", "Escanear"); pt.put("select_content", "Selecionar conteúdo"); pt.put("nearby_devices", "Dispositivos próximos");
        pt.put("no_device", "Nenhum dispositivo, escaneie"); pt.put("receive_history", "Histórico"); pt.put("settings", "Configurações");
        pt.put("send", "Enviar"); pt.put("receive", "Receber"); pt.put("file", "Arquivo"); pt.put("media", "Mídia");
        pt.put("folder", "Pasta"); pt.put("app", "App"); pt.put("text", "Texto"); pt.put("clipboard", "Área de transferência");
        pt.put("not_selected", "Nenhum arquivo"); pt.put("auto_save", "Salvar auto"); pt.put("save_album", "Salvar no álbum");
        pt.put("save_history", "Salvar no histórico"); pt.put("use_pin", "Ativar PIN"); pt.put("pin_off", "PIN: Inativo");
        pt.put("pin_on", "PIN atual: "); pt.put("network", "Rede e Automação"); pt.put("web_enable", "Compartilhar Web");
        pt.put("auto_start", "Início automático"); pt.put("receive_notif", "Via notificação"); pt.put("port", "Porta");
        pt.put("timeout", "Tempo de busca"); pt.put("other", "Outros"); pt.put("anim", "Animações");
        pt.put("set_name", "Nome do dispositivo"); pt.put("backup", "Backup / Restaurar"); pt.put("about", "Sobre o WatchSend");
        pt.put("language", "Idioma"); pt.put("follow_system", "Sistema"); pt.put("cancel", "Cancelar");
        pt.put("confirm", "OK"); pt.put("close", "Fechar"); pt.put("clear", "Limpar"); pt.put("input_ip", "IP manual");
        pt.put("input_text", "Enviar texto"); pt.put("select_lang", "Selecionar idioma"); pt.put("restart_tip", "Idioma alterado");
        pt.put("add_ip", "Adicionar IP"); pt.put("cross_net", "Rede cruzada"); pt.put("auto_whitelist", "Salvar auto dos favoritos");
        pt.put("filter_ext_title", "Bloquear extensões perigosas"); pt.put("manage_acl", "Gerenciar lista branca/negra");
        pt.put("cross_net_title", "Transferência de rede cruzada"); pt.put("cross_net_desc", "Não precisa do mesmo Wi-Fi, conexão direta do canal geek.");
        pt.put("upnp_punch", "Auto UPnP"); pt.put("direct_mode", "Modo P2P direto");
        pt.put("send_file_title", "Enviar arquivo"); pt.put("recv_file_title", "Receber arquivo");
        pt.put("gen_pin_btn", "Selecionar arquivo e gerar PIN"); pt.put("input_pin_hint", "Digite o PIN de 6 dígitos");
        pt.put("connect_recv_btn", "Conectar e receber");
        pt.put("ipv6_local", "IPv6 local: [%s]:53317 (Conexão direta)"); pt.put("ipv6_none", "IPv6 local: Não encontrado. Apenas LAN ou túnel");
        pt.put("stun_wait", "Sondagem STUN..."); pt.put("stun_fail", "Falha STUN (NAT simétrico)");
        pt.put("stun_ok", "Endereço público NAT: "); pt.put("stun_probe", "Sonda STUN");
        pt.put("stun_tip", "Endereço NAT: Clique na sonda à direita"); pt.put("advanced_tip", "Dica pro: Combine com FRP/Ngrok ou ZeroTier e insira o endereço público gerado em Conexão direta.");
        pt.put("file_count", "Arquivos: %d"); pt.put("size_label", "Tamanho: %s"); pt.put("unknown_size", "Tamanho desconhecido");
        pt.put("manage", "Gerenciar"); pt.put("clear_all", "Limpar todas as listas");
        pt.put("view_whitelist", "Ver/Remover lista branca"); pt.put("view_blacklist", "Ver/Remover lista negra");
        pt.put("all_deleted", "Tudo limpo"); pt.put("export_log", "Exportar log");
        pt.put("input_address", "Digite o endereço"); pt.put("label", "Rótulo"); pt.put("ip_address", "Endereço IP");
                // ---- P1 高频词条 ----
        pt.put("general", "Geral"); pt.put("accept", "Aceitar"); pt.put("reject", "Rejeitar");
        pt.put("recv_completed", "Recebido"); pt.put("send_completed", "Enviado"); pt.put("interrupted", "Interrompido");
        pt.put("unknown_device", "Dispositivo desconhecido"); pt.put("unknown_file", "Arquivo desconhecido");
        pt.put("save_dir_prefix", "Salvar em: "); pt.put("wants_to_send", "Quer te enviar arquivos");
        pt.put("receive_request", "Pedido de recebimento"); pt.put("service_running", "WatchSend em execução");
        pt.put("cn_gen_pin_send", "Gerar PIN e enviar"); pt.put("cn_direct_send", "Envio direto seleção");
        pt.put("cn_status_packing", "Empacotando..."); pt.put("cn_status_connecting", "Conectando...");
        pt.put("cn_status_send_done", "Enviado! Aguardando o par.");
        pt.put("cn_status_recv_done", "Recebido! Extraído em Download");
        pt.put("cn_pin_invalid", "Digite o PIN de 6 dígitos");
        pt.put("cn_direct_invalid", "Digite o IP público e a porta");
        pt.put("manage_list", "Gerenciar listas"); pt.put("add_files", "Adicionar arquivos");
        pt.put("delete_all", "Excluir tudo"); pt.put("select_title", "Selecionar");
        pt.put("select_app", "Escolher app (APK)");
        pt.put("dark_mode", "Modo escuro"); pt.put("modify_pin", "Modificar PIN");
        pt.put("https_enable", "Ativar HTTPS"); pt.put("auto_save", "Salvar auto");
        pt.put("save_album", "Salvar no álbum"); pt.put("save_history", "Salvar no histórico");
        pt.put("use_pin", "Ativar PIN"); pt.put("web_enable", "Compartilhar Web");
        pt.put("auto_start", "Início automático"); pt.put("receive_notif", "Via notificação");
        pt.put("port", "Porta"); pt.put("timeout", "Tempo de busca");
        pt.put("language", "Idioma"); pt.put("anim", "Animações");
        pt.put("set_name", "Nome do dispositivo"); pt.put("backup", "Backup / Restaurar");
        pt.put("about", "Sobre o WatchSend"); pt.put("manage_acl", "Gerenciar lista branca/negra");

        pt.put("recv_request", "Pedido de recebimento"); pt.put("from_device", "Do dispositivo"); pt.put("wants_to_send_you", "Quer te enviar arquivos");
        pt.put("action", "Ação"); pt.put("current_file", "Arquivo atual"); pt.put("preparing", "Preparando...");
        pt.put("transferring", "Transferindo..."); pt.put("calculating", "Calculando..."); pt.put("saved_to_local", "Salvo localmente");
        pt.put("transfer_success", "Sucesso"); pt.put("local_ipv6", "IPv6 local"); pt.put("getting", "Obtendo...");
        pt.put("not_probed", "Não sondado"); pt.put("probing", "Sondando..."); pt.put("nat_addr", "Endereço NAT");
        pt.put("input_direct_ip", "IP:Porta do par"); pt.put("selected_prefix", "Selecionado "); pt.put("files_suffix", "arquivos\n");
        pt.put("select_file_gen_pin", "Escolher arquivo e gerar PIN"); pt.put("pin_prefix", "PIN: "); pt.put("toast_input_ip", "Digite o IP e a porta do par");
        pt.put("toast_input_pin", "Digite o PIN de 6 dígitos"); pt.put("ipv6_not_found", "IPv6 pública não encontrada"); pt.put("packing", "Empacotando...");
        pt.put("packed_connecting", "Empacotado, conectando..."); pt.put("sending_prefix", "Enviando... "); pt.put("send_done", "Enviado!");
        pt.put("failed_prefix", "Falhou: "); pt.put("direct_self", "Falha direta: Não é possível conectar a si mesmo."); pt.put("preparing_direct", "Preparando direto...");
        pt.put("read_failed", "Falha na leitura"); pt.put("prepare_failed", "Falha na preparação"); pt.put("direct_sending", "Enviando direto...");
        pt.put("sending_percent", "Enviando "); pt.put("direct_send_done", "Envio direto concluído!"); pt.put("direct_failed", "Falha direta: ");
        pt.put("connecting", "Conectando..."); pt.put("receiving_prefix", "Recebendo... "); pt.put("recv_unzipping", "Recebido, extraindo...");
        pt.put("recv_done", "Recebido! Salvo em Downloads."); pt.put("recv_failed", "Falha no recebimento: ");
    

        pt.put("round_screen_opt", "Otim. tela redonda"); pt.put("round_screen_opt_desc", "Evitar corte de bordas");
        pt.put("block_system_back", "Bloquear voltar sistema"); pt.put("block_system_back_desc", "Evitar conflito de gestos");
        pt.put("dark_mode_desc", "Tema preto/branco"); pt.put("transfer_settings", "Transferência");
        pt.put("auto_save_desc", "Aceitar automaticamente"); pt.put("auto_whitelist_desc", "Apenas dispositivos da lista branca");
        pt.put("save_album_desc", "Salvar na galeria"); pt.put("save_history_desc", "Registrar histórico");
        pt.put("use_pin_desc", "Aumentar segurança"); pt.put("https_enable_desc", "Pode afetar dispositivos antigos");
        pt.put("web_enable_desc", "Transferência via navegador"); pt.put("auto_start_desc", "Executar em segundo plano");
        pt.put("receive_notif_desc", "Requer permissão do sistema"); pt.put("port_settings", "Configurações de porta");
        pt.put("timeout_settings", "Tempo de busca"); pt.put("device_security", "Dispositivo e Segurança");
        pt.put("manage_acl_desc", "Gerenciar dispositivos confiáveis"); pt.put("ui_scale_dialog_title", "Escala da UI");
        pt.put("about_title", "Sobre"); pt.put("pin_title_dialog", "Modificar PIN");
        pt.put("port_dialog_title", "Porta"); pt.put("timeout_dialog_title", "Tempo(ms)");
        pt.put("filter_dialog_title", "Bloquear extensões"); pt.put("device_name_title", "Nome do dispositivo");
        pt.put("lang_title", "Configurações de idioma"); pt.put("acl_dialog_title", "Lista branca/negra");
        pt.put("whitelist_title", "Lista branca"); pt.put("blacklist_title", "Lista negra");
        pt.put("about_desc_watchsend", "Autor: styisme\nAssistido por IA\nMIT License\n\nCompatível com LocalSend v2.2");
    

        pt.put("start_service", "Iniciar"); pt.put("stop_service", "Parar"); pt.put("devices_nearby", "Dispositivos próximos");
        pt.put("no_device_scan", "Nenhum dispositivo, escaneie"); pt.put("send_file", "Enviar arquivo"); pt.put("select_title_desc", "Selecionar");
        pt.put("device", "Dispositivo"); pt.put("file", "Arquivo"); pt.put("media", "Mídia"); pt.put("app", "App");
        pt.put("folder", "Pasta"); pt.put("text", "Texto"); pt.put("clipboard", "Área de transf."); pt.put("record", "Histórico");
        pt.put("return_to_devices", "↩ Voltar aos disp."); pt.put("no_record", "Sem registros"); pt.put("file_not_found", "Arquivo não encontrado");
        pt.put("cannot_open", "Não é possível abrir"); pt.put("no_app", "Sem apps"); pt.put("empty_folder", "Pasta vazia");
        pt.put("add_files", "Adicionar arquivos"); pt.put("select_folder_send", ">> [Enviar esta pasta] <<"); pt.put("unknown_file", "Arquivo desconhecido");
        pt.put("files_count_prefix", "Arquivos: "); pt.put("size_label", "Tamanho: "); pt.put("clear_all_files", "Excluir tudo");
        pt.put("back_prev", "Voltar"); pt.put("save_to_dir", "Salvar em: "); pt.put("ip_prefix", "IP ");
    
dict.put("pt", pt);
    }

    public static void init(Context ctx) {
        String lang = ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE).getString("app_lang", "system");
        if ("system".equals(lang)) {
            String sysLang = Locale.getDefault().getLanguage();
            String sysCountry = Locale.getDefault().getCountry();
            if ("zh".equals(sysLang)) {
                currentLang = "TW".equals(sysCountry) || "HK".equals(sysCountry) ? "zh_TW" : "zh";
            } else if (dict.containsKey(sysLang)) {
                currentLang = sysLang;
            } else {
                currentLang = "en";
            }
        } else {
            currentLang = lang;
        }
    }

    public static String get(String key) {
        if (dict.containsKey(currentLang) && dict.get(currentLang).containsKey(key)) {
            return dict.get(currentLang).get(key);
        }
        return dict.get("en").containsKey(key) ? dict.get("en").get(key) : key;
    }
}

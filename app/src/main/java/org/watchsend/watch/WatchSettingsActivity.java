package org.watchsend.watch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.*;

import org.watchsend.MainActivity;
import org.watchsend.OldSendService;
import org.watchsend.R;
import org.watchsend.util.I18nManager;
import org.watchsend.util.QuickSave;
import org.watchsend.model.FavoritesStore;
import org.watchsend.transfer.DownloadShare;
import org.watchsend.util.ThemeUtils;
import org.watchsend.uikit.UikitFullListActivity;
import org.watchsend.uikit.UikitFullInputActivity;
import org.watchsend.uikit.UikitFullTextActivity;
import org.watchsend.uikit.UikitFullSliderActivity;
import org.watchsend.uikit.UikitCompatSwitch;
import org.watchsend.uikit.UikitConfig;

public class WatchSettingsActivity extends Activity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(org.watchsend.watch.WatchUiUtils.wrapScale(newBase));
    }


    private LinearLayout container;
    private String pendingAclKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        I18nManager.init(this);
        // 设置页可能被直接打开（服务尚未启动），快速保存/收藏都要能读到配置
        org.watchsend.util.QuickSave.init(this);
        org.watchsend.model.FavoritesStore.init(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_watch_settings);
        org.watchsend.uikit.RtlSupport.apply(this);
        ThemeUtils.applyTheme(this);
        WatchUiUtils.applyEntryAnimation(this);

        boolean isDark = ThemeUtils.isDarkMode(this);
        View root = findViewById(R.id.settings_root);
        if (root != null) {
            root.setBackgroundColor(isDark ? 0xFF000000 : 0xFFF4F6F8);
            WatchUiUtils.applyRoundPadding(this, root);
            
            // 在圆屏模式下隐藏顶部返回栏，依赖滑动返回
            View header = findViewById(R.id.settings_header);
            if (header != null && org.watchsend.watch.WatchHardwareUtils.isRoundScreen(root)) {
                header.setVisibility(View.GONE);
            }
        }

        container = (LinearLayout) findViewById(R.id.settings_container);
        ScrollView scrollView = (ScrollView) findViewById(R.id.settings_scroll);
        if (scrollView != null && container != null) {
            WatchUiUtils.setupWheelScroll(scrollView, container);
            org.watchsend.watch.WatchHardwareUtils.bindRotaryEncoder(scrollView);
        }
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });
        buildSettings();
    }

    private android.content.SharedPreferences prefs() {
        return getSharedPreferences("oldsend", MODE_PRIVATE);
    }

    private interface SwitchHandler { void onChanged(boolean v); }

    private SwitchHandler saveTo(final String key) {
        return new SwitchHandler() {
            public void onChanged(boolean v) { prefs().edit().putBoolean(key, v).commit(); }
        };
    }

    private void buildSettings() {
        container.removeAllViews();

        // ── 通用：外观 → 交互 → 性能与省电，按“最常调”在前排列 ──
        addSection(I18nManager.get("general"));

        // 主题模式 (深/浅/自动)
        addItem(I18nManager.get("theme_mode"), currentThemeLabel(), new View.OnClickListener() {
            public void onClick(View v) { pickTheme(); }
        });

        // 语言
        addItem(I18nManager.get("language"), currentLangLabel(), new View.OnClickListener() {
            public void onClick(View v) { pickLang(); }
        });

        // UI 大小：圆表觉得字挤时最先调的就是它，归到外观组
        addItem(I18nManager.get("ui_size"), getUiSizeLabel(), new View.OnClickListener() {
            public void onClick(View v) { pickUiSize(); }
        });

        // 屏幕形状选择 (自动/强制圆/强制方)
        addItem(I18nManager.get("screen_shape"), currentShapeLabel(), new View.OnClickListener() {
            public void onClick(View v) { pickShape(); }
        });

        // 底部下巴避让 (针对 Moto 360 等有黑边的圆表)
        addSwitch(I18nManager.get("chin_padding"), I18nManager.get("chin_padding_desc"), prefs().getBoolean("chin_padding", false), saveTo("chin_padding"));

        // 动态取色 (仅在 Android 12+ 显示)
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            addSwitch(I18nManager.get("dynamic_color"), I18nManager.get("dynamic_color_desc"), prefs().getBoolean("dynamic_color", true), new SwitchHandler() {
                public void onChanged(boolean v) { prefs().edit().putBoolean("dynamic_color", v).commit(); recreate(); }
            });
        }

        // 启用软件内返回：开启后页面内滑动接管，系统返回不再直接退出
        addSwitch(I18nManager.get("swipe_back"), I18nManager.get("swipe_back_desc"),
                UikitConfig.isSwipeBackEnabled(this), new SwitchHandler() {
                    public void onChanged(boolean v) { UikitConfig.setSwipeBackEnabled(WatchSettingsActivity.this, v); }
                });

        // 低性能模式
        // 开关要显示“实际生效值”：未手动设置过时由 UikitConfig 按设备能力自动判定，
        // 这里若仍读 prefs 默认 false，会出现“开关关着但动画已经关了”的割裂。
        addSwitch(I18nManager.get("low_perf_mode"), I18nManager.get("low_perf_desc"),
                UikitConfig.isLowPerf(this), saveTo("low_perf_mode"));

        // 熄屏空闲暂停服务（默认关闭：开启后会牺牲“对方随时推过来都能收到”的可靠性换续航）
        addSwitch(I18nManager.get("idle_stop_server"), I18nManager.get("idle_stop_server_desc"),
                prefs().getBoolean("idle_stop_server", false), saveTo("idle_stop_server"));

        // ── 传输：先定接收策略，再定落到哪里 ──
        addSection(I18nManager.get("transfer_settings"));
        // 三态取代原来的两个开关（旧 auto_save / auto_accept_whitelist 仍被 QuickSave 读取，升级不突变）
        addItem(I18nManager.get("quick_save"), QuickSave.label(QuickSave.getMode()), new View.OnClickListener() {
            public void onClick(View v) { pickQuickSave(); }
        });
        addItem(I18nManager.get("favorites"), String.valueOf(FavoritesStore.count()), new View.OnClickListener() {
            public void onClick(View v) { manageFavorites(); }
        });
        addSwitch(I18nManager.get("save_album"), I18nManager.get("save_album_desc"), OldSendService.getPrefBooleanGlobal("save_album", true), saveTo("save_album"));
        addSwitch(I18nManager.get("save_history"), I18nManager.get("save_history_desc"), OldSendService.getPrefBooleanGlobal("save_history", true), saveTo("save_history"));

        // ── 网络与自动化：先协议与端口，再网页端（入口紧跟二维码），最后自启 ──
        addSection(I18nManager.get("network"));
        addSwitch(I18nManager.get("https_enable"), I18nManager.get("https_enable_desc"), prefs().getBoolean("use_https", false), new SwitchHandler() {
            public void onChanged(boolean v) {
                prefs().edit().putBoolean("use_https", v).commit();
                // 只存偏好不会改已有监听，必须重建，否则开关看起来生效、实际仍是旧协议
                org.watchsend.OldSendService.restartServer(WatchSettingsActivity.this);
            }
        });
        addItem(I18nManager.get("port"), String.valueOf(prefs().getInt("port", 53317)), new View.OnClickListener() {
            public void onClick(View v) { editPort(); }
        });
        addSwitch(I18nManager.get("web_enable"), I18nManager.get("web_enable_desc"), OldSendService.getPrefBooleanGlobal("web_enable", true), saveTo("web_enable"));
        addItem("▦ " + I18nManager.get("web_qr"), new View.OnClickListener() {
            public void onClick(View v) {
                // 网页端二维码：电脑摄像头扫一下就能打开，不必在手表上抄地址
                org.watchsend.watch.WebShareQrActivity.show(WatchSettingsActivity.this,
                        "http://" + OldSendService.getLocalIp() + ":53317");
            }
        });
        addSwitch(I18nManager.get("download_share"), I18nManager.get("download_share_desc"), DownloadShare.isEnabled(), new SwitchHandler() {
            public void onChanged(boolean v) { DownloadShare.setEnabled(v); }
        });
        addSwitch(I18nManager.get("auto_start"), I18nManager.get("auto_start_desc"), OldSendService.getPrefBooleanGlobal("auto_start", false), saveTo("auto_start"));

        // ── 设备与安全：身份 → 口令（开关与取值相邻）→ 拦截规则 ──
        addSection(I18nManager.get("device_security"));
        addItem(I18nManager.get("set_name"), OldSendService.getAlias(this), new View.OnClickListener() {
            public void onClick(View v) { editName(); }
        });
        addSwitch(I18nManager.get("use_pin"), I18nManager.get("use_pin_desc"), OldSendService.getPrefBooleanGlobal("use_pin", false), saveTo("use_pin"));
        addItem(I18nManager.get("modify_pin"), prefs().getString("pin_code", org.watchsend.OldSendService.DEFAULT_PIN), new View.OnClickListener() {
            public void onClick(View v) { editPin(); }
        });
        addItem(I18nManager.get("filter_ext_title"), OldSendService.getPrefStringGlobal("filter_ext", "exe,bat,cmd,vbs,sh"), new View.OnClickListener() {
            public void onClick(View v) { editFilter(); }
        });
        addItem(I18nManager.get("manage_acl"), new View.OnClickListener() {
            public void onClick(View v) { manageAcl(); }
        });

        // 极客模式 (Root/Shizuku)
        if (org.watchsend.util.ShellUtils.isRootAvailable() || org.watchsend.util.ShellUtils.isShizukuAvailable()) {
            addSection(I18nManager.get("geek_mode"));
            addSwitch(I18nManager.get("silent_install") + " (Beta)", I18nManager.get("silent_install_desc"), prefs().getBoolean("silent_install", false), saveTo("silent_install"));
        }

        // ── 其他：不可逆操作与信息页 ──
        addSection(I18nManager.get("other"));
        addItem(I18nManager.get("backup"), new View.OnClickListener() {
            public void onClick(View v) { backupDialog(); }
        });
        addItem(I18nManager.get("about"), new View.OnClickListener() {
            public void onClick(View v) { aboutDialog(); }
        });
    }

    private String currentShapeLabel() {
        int shape = prefs().getInt("screen_shape", 0);
        if (shape == 1) return I18nManager.get("shape_round");
        if (shape == 2) return I18nManager.get("shape_square");
        return I18nManager.get("shape_auto");
    }

    private String getUiSizeLabel() {
        float scale = prefs().getFloat("ui_scale", 1.0f);
        if (scale <= 0.85f) return I18nManager.get("ui_size_small");
        if (scale >= 1.15f) return I18nManager.get("ui_size_large");
        return I18nManager.get("ui_size_medium");
    }


    private String currentThemeLabel() {
        String mode = prefs().getString("theme_mode", "auto");
        if ("dark".equals(mode)) return I18nManager.get("theme_dark");
        if ("light".equals(mode)) return I18nManager.get("theme_light");
        return I18nManager.get("theme_auto");
    }

    private void pickTheme() {
        final String[] items = {I18nManager.get("theme_auto"), I18nManager.get("theme_light"), I18nManager.get("theme_dark")};
        UikitFullListActivity.show(this, I18nManager.get("theme_mode"), items, 6010);
    }

    private String currentLangLabel() {
        String lang = prefs().getString("app_lang", "system");
        if ("key".equals(lang)) return "debug_keys";
        // 语言表由 I18nManager 统一维护（v1.7.7 起扩展到 50+ 种，不再在这里写死）
        for (int i = 0; i < I18nManager.langCount(); i++) {
            if (I18nManager.codeAt(i).equals(lang)) return I18nManager.labelAt(i);
        }
        return lang;
    }

    private void addSection(String title) {
        boolean isDark = ThemeUtils.isDarkMode(this);
        TextView tv = new TextView(this);
        tv.setText(title); tv.setTextSize(12);
        tv.setGravity(org.watchsend.uikit.RtlSupport.startGravity());
        tv.setTextColor(isDark ? 0xFFAAAAAA : 0xFF666666);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(dp(12), dp(10), dp(12), dp(4));
        container.addView(tv);
    }

        private void addSwitch(String label, boolean value, final SwitchHandler handler) {
        addSwitch(label, null, value, handler);
    }

private void addSwitch(String label, String subLabel, boolean value, final SwitchHandler handler) {
        boolean isDark = ThemeUtils.isDarkMode(this);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(12), dp(16), dp(12));
        row.setBackground(WatchUiUtils.getPillBackground(this));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        row.setLayoutParams(lp);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(label); tvTitle.setTextSize(16);
        tvTitle.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF1C1B1F);
        org.watchsend.uikit.TextFit.wrap(tvTitle, 2); // 德/俄长词最多两行，超出省略
        tvTitle.setGravity(org.watchsend.uikit.RtlSupport.startGravity());
        textCol.addView(tvTitle);

        if (subLabel != null) {
            TextView tvSub = new TextView(this);
            tvSub.setText(subLabel); tvSub.setTextSize(12);
            tvSub.setTextColor(isDark ? 0xFFAAAAAA : 0xFF666666);
            tvSub.setPadding(0, dp(2), 0, 0);
            // 说明文字在中日韩只要一两行，德/俄能到 70em 以上（六七行）。限三行、尾部省略，
            // 被截断的长说明改成点正文区打开全文页，短语言的正文区点了也给全文，不额外占高度。
            org.watchsend.uikit.TextFit.wrap(tvSub, 3);
            tvSub.setGravity(org.watchsend.uikit.RtlSupport.startGravity());
            final String fullDesc = subLabel;
            final String fullTitle = label;
            textCol.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    org.watchsend.uikit.UikitFullTextActivity.show(WatchSettingsActivity.this,
                            fullTitle, fullDesc, I18nManager.get("confirm"), 6090); // 6090 为本页「说明全文」专用，无结果处理分支
                }
            });
            textCol.addView(tvSub);
        }
        row.addView(textCol);

        UikitCompatSwitch cb = new UikitCompatSwitch(this);
        cb.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(26)));
        cb.setChecked(value);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton b, boolean c) { handler.onChanged(c); }
        });
        row.addView(cb);
        container.addView(row);
    }

    /** 无当前值的入口行（备份、关于、二维码等），右侧只有箭头。 */
    private void addItem(String label, View.OnClickListener click) {
        addItem(label, null, click);
    }

    /**
     * 带当前值的入口行：标题 16sp + 当前值副行 12sp。
     * 以前把值拼进标题（"设备名: 我的表"、"过滤扩展名: exe,bat,..."），
     * 圆表 320px 宽下长值会折行把箭头挤掉，改为与开关行一致的上下两行。
     */
    private void addItem(String label, String value, View.OnClickListener click) {
        boolean isDark = ThemeUtils.isDarkMode(this);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(12), dp(16), dp(12));
        row.setBackground(WatchUiUtils.getPillBackground(this));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        row.setLayoutParams(lp);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(label); tvTitle.setTextSize(16);
        tvTitle.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF1C1B1F);
        org.watchsend.uikit.TextFit.wrap(tvTitle, 2); // 德/俄长词最多两行，超出省略
        tvTitle.setGravity(org.watchsend.uikit.RtlSupport.startGravity());
        textCol.addView(tvTitle);

        if (value != null && value.length() > 0) {
            TextView tvValue = new TextView(this);
            tvValue.setText(value); tvValue.setTextSize(12);
            tvValue.setTextColor(isDark ? 0xFFAAAAAA : 0xFF666666);
            tvValue.setPadding(0, dp(2), dp(6), 0);
            org.watchsend.uikit.TextFit.wrap(tvValue, 2);
            tvValue.setGravity(org.watchsend.uikit.RtlSupport.startGravity());
            textCol.addView(tvValue);
        }
        row.addView(textCol);

        TextView arrow = new TextView(this);
        // RTL 语言的阅读方向相反，箭头也跟着指左
        arrow.setText(org.watchsend.uikit.RtlSupport.isRtl() ? "<" : ">"); arrow.setTextSize(18);
        arrow.setTextColor(isDark ? org.watchsend.uikit.UikitColor.getAccentColor(this, true) : 0xFF999999);
        row.addView(arrow);

        row.setOnClickListener(click);
        container.addView(row);
    }

    private void pickShape() {
        final String[] items = {I18nManager.get("shape_auto"), I18nManager.get("shape_round"), I18nManager.get("shape_square")};
        UikitFullListActivity.show(this, I18nManager.get("screen_shape"), items, 6011);
    }

    private void pickUiSize() {
        final float currentScale = prefs().getFloat("ui_scale", 1.0f);
        UikitFullSliderActivity.show(this, I18nManager.get("ui_size"), currentScale, 6020);
    }

    private void pickLang() {
        // 语言表来自 I18nManager（当前 50+ 种），末尾多一项开发者用的 debug_keys
        int n = I18nManager.langCount();
        String[] items = new String[n + 1];
        for (int i = 0; i < n; i++) items[i] = I18nManager.labelAt(i);
        items[n] = "debug_keys";
        UikitFullListActivity.show(this, I18nManager.get("language"), items, 6012);
    }

    private void editPin() {
        UikitFullInputActivity.show(this, I18nManager.get("modify_pin"), prefs().getString("pin_code", org.watchsend.OldSendService.DEFAULT_PIN), null, 6016);
    }

    private void editPort() {
        UikitFullInputActivity.show(this, I18nManager.get("port"), String.valueOf(prefs().getInt("port", 53317)), null, 6017);
    }

    private void editFilter() {
        UikitFullInputActivity.show(this, I18nManager.get("filter_ext_title"), OldSendService.getPrefStringGlobal("filter_ext", "exe,bat,cmd,vbs,sh"), null, 6018);
    }

    private void editName() {        UikitFullInputActivity.show(this, I18nManager.get("set_name"), OldSendService.getAlias(this), null, 6019);
    }

    private void manageAcl() {
        String[] items = {I18nManager.get("view_whitelist"), I18nManager.get("view_blacklist"), I18nManager.get("clear_all")};
        UikitFullListActivity.show(this, I18nManager.get("manage_acl"), items, 6013);
    }

    /** 快速保存三态选择（对应官方 QuickSaveMode）。当前生效项打勾，避免选完看不出结果。 */
    private void pickQuickSave() {
        String cur = QuickSave.getMode();
        java.util.ArrayList<org.watchsend.uikit.UikitListItemModel> items = new java.util.ArrayList<org.watchsend.uikit.UikitListItemModel>();
        items.add(new org.watchsend.uikit.UikitListItemModel(
                QuickSave.label(QuickSave.OFF), QuickSave.OFF.equals(cur) ? "✓" : null));
        items.add(new org.watchsend.uikit.UikitListItemModel(
                QuickSave.label(QuickSave.ON), QuickSave.ON.equals(cur) ? "✓" : null));
        items.add(new org.watchsend.uikit.UikitListItemModel(
                QuickSave.label(QuickSave.PAIRED), QuickSave.PAIRED.equals(cur) ? "✓" : null));
        UikitFullListActivity.show(this, I18nManager.get("quick_save"), items, 6030);
    }

    /** 收藏设备列表，选中即移除（收藏同时是 paired 模式的白名单）。 */
    private void manageFavorites() {
        org.json.JSONArray arr = FavoritesStore.list();
        if (arr.length() == 0) {
            Toast.makeText(this, I18nManager.get("favorites_empty"), Toast.LENGTH_SHORT).show();
            return;
        }
        String[] items = new String[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            org.json.JSONObject o = arr.optJSONObject(i);
            String alias = o == null ? "" : o.optString("alias", "");
            String ip = o == null ? "" : o.optString("ip", "");
            items[i] = alias.length() > 0 ? alias + " (" + ip + ")" : ip;
        }
        UikitFullListActivity.show(this, I18nManager.get("favorites"), items, 6031);
    }

    private void showSpecificList(final String key) {
        String s = OldSendService.getPrefStringGlobal(key, "");
        if (s.trim().isEmpty()) { Toast.makeText(this, I18nManager.get("list_empty"), Toast.LENGTH_SHORT).show(); return; }
        pendingAclKey = key;
        String[] arr = s.split(",");
        UikitFullListActivity.show(this, key.equals("whitelist") ? I18nManager.get("whitelist_title") : I18nManager.get("blacklist_title"), arr, 6014);
    }

    private void backupDialog() {
        String[] items = {I18nManager.get("backup_export"), I18nManager.get("backup_import")};
        UikitFullListActivity.show(this, I18nManager.get("backup_title"), items, 6015);
    }

    private void aboutDialog() {
        String msg = "WatchSend · Watch Edition\nv" + org.watchsend.BuildConfig.VERSION_NAME + "\n\n" + I18nManager.get("author") + ": styisme\nAI " + I18nManager.get("contributors") + "\nMIT License\n\n" + I18nManager.get("about_desc");
        UikitFullTextActivity.show(this, I18nManager.get("about"), msg, I18nManager.get("confirm"), 6021);
    }

    private int dp(int val) { return (int)(val * getResources().getDisplayMetrics().density); }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;

        // 6030 快速保存三态
        if (requestCode == 6030) {
            int pos = data.getIntExtra(UikitFullListActivity.EXTRA_RESULT_INDEX, -1);
            String mode = pos == 0 ? QuickSave.OFF
                    : pos == 1 ? QuickSave.ON
                    : pos == 2 ? QuickSave.PAIRED : null;
            if (mode != null && !QuickSave.setMode(mode)) {
                // 真的写不进去时明确提示，别再静默回滚
                Toast.makeText(this, I18nManager.get("failed_prefix") + I18nManager.get("quick_save"),
                        Toast.LENGTH_SHORT).show();
            }
            recreate();
            return;
        }
        // 6031 收藏设备（选中即移除，便于在手表上管理）
        if (requestCode == 6031) {
            int pos = data.getIntExtra(UikitFullListActivity.EXTRA_RESULT_INDEX, -1);
            org.json.JSONArray arr = FavoritesStore.list();
            if (pos >= 0 && pos < arr.length()) {
                org.json.JSONObject o = arr.optJSONObject(pos);
                if (o != null) FavoritesStore.remove(o.optString("fingerprint", ""));
                Toast.makeText(this, I18nManager.get("favorite_removed"), Toast.LENGTH_SHORT).show();
            }
            recreate();
            return;
        }
        // 6010 主题
        if (requestCode == 6010) {
            int pos = data.getIntExtra(UikitFullListActivity.EXTRA_RESULT_INDEX, -1);
            if (pos >= 0) {
                String[] modes = {"auto", "light", "dark"};
                prefs().edit().putString("theme_mode", modes[pos]).commit();
                recreate();
            }
            return;
        }
        // 6011 表盘形状
        if (requestCode == 6011) {
            int pos = data.getIntExtra(UikitFullListActivity.EXTRA_RESULT_INDEX, -1);
            if (pos >= 0) {
                prefs().edit().putInt("screen_shape", pos).commit();
                recreate();
            }
            return;
        }
        // 6012 语言
        if (requestCode == 6012) {
            int pos = data.getIntExtra(UikitFullListActivity.EXTRA_RESULT_INDEX, -1);
            if (pos >= 0) {
                // pos 末位是开发者用的 "key"（直接显示词条 key），其余从 I18nManager 取
                String chosen = (pos >= I18nManager.langCount()) ? "key" : I18nManager.codeAt(pos);
                prefs().edit().putString("app_lang", chosen).commit();
                Toast.makeText(this, I18nManager.get("lang_switched"), Toast.LENGTH_SHORT).show();
                finish();
                startActivity(new Intent(this, MainActivity.class));
            }
            return;
        }
        // 6013 ACL 菜单
        if (requestCode == 6013) {
            int pos = data.getIntExtra(UikitFullListActivity.EXTRA_RESULT_INDEX, -1);
            if (pos == 2) { prefs().edit().remove("whitelist").remove("blacklist").commit(); recreate(); }
            else if (pos == 0 || pos == 1) showSpecificList(pos == 0 ? "whitelist" : "blacklist");
            return;
        }
        // 6014 白/黑名单条目移除
        if (requestCode == 6014) {
            int pos = data.getIntExtra(UikitFullListActivity.EXTRA_RESULT_INDEX, -1);
            String cur = OldSendService.getPrefStringGlobal(pendingAclKey, "");
            String[] arr = cur.split(",");
            if (pos >= 0 && pos < arr.length) {
                cur = cur.replace(arr[pos] + ",", "").replace(arr[pos], "");
                prefs().edit().putString(pendingAclKey, cur).commit();
                showSpecificList(pendingAclKey);
            }
            return;
        }
        // 6015 备份导出 / 导入
        if (requestCode == 6015) {
            int pos = data.getIntExtra(UikitFullListActivity.EXTRA_RESULT_INDEX, -1);
            android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (pos == 0) {
                try {
                    org.json.JSONObject o = new org.json.JSONObject();
                    for (java.util.Map.Entry<String, ?> e : prefs().getAll().entrySet()) o.put(e.getKey(), e.getValue());
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("WatchSendBackup", o.toString()));
                    Toast.makeText(this, I18nManager.get("export_ok"), Toast.LENGTH_SHORT).show();
                } catch (Exception ignored) {}
            } else if (pos == 1) {
                try {
                    android.content.ClipData clip = cm.getPrimaryClip();
                    if (clip == null || clip.getItemCount() == 0) return;
                    String text = clip.getItemAt(0).coerceToText(this).toString();
                    org.json.JSONObject o = new org.json.JSONObject(text);
                    android.content.SharedPreferences.Editor ed = prefs().edit();
                    java.util.Iterator<String> keys = o.keys();
                    while (keys.hasNext()) {
                        String k = keys.next(); Object v = o.get(k);
                        if (v instanceof Boolean) ed.putBoolean(k, (Boolean) v);
                        else if (v instanceof Integer) ed.putInt(k, (Integer) v);
                        else if (v instanceof String) ed.putString(k, (String) v);
                    }
                    ed.commit();
                    Toast.makeText(this, I18nManager.get("import_ok"), Toast.LENGTH_SHORT).show();
                    recreate();
                } catch (Exception ignored) {}
            }
            return;
        }
        // 6016 PIN
        if (requestCode == 6016) {
            String text = data.getStringExtra(UikitFullInputActivity.EXTRA_RESULT_TEXT);
            if (text != null && text.length() > 0) { prefs().edit().putString("pin_code", text).commit(); recreate(); }
            return;
        }
        // 6017 端口
        if (requestCode == 6017) {
            String text = data.getStringExtra(UikitFullInputActivity.EXTRA_RESULT_TEXT);
            try {
                int p = Integer.parseInt(text.trim());
                if (p < 1 || p > 65535) throw new IllegalArgumentException("port out of range");
                prefs().edit().putInt("port", p).commit();
                // 端口改了要立刻换监听，否则对外仍监听旧端口，二维码/地址却是新端口（对不上）
                org.watchsend.OldSendService.restartServer(this);
                recreate();
            } catch (Exception ignored) {}
            return;
        }
        // 6018 文件后缀过滤
        if (requestCode == 6018) {
            String text = data.getStringExtra(UikitFullInputActivity.EXTRA_RESULT_TEXT);
            if (text != null) { prefs().edit().putString("filter_ext", text.replace(" ","").toLowerCase()).commit(); recreate(); }
            return;
        }
        // 6019 设备名称
        if (requestCode == 6019) {
            String text = data.getStringExtra(UikitFullInputActivity.EXTRA_RESULT_TEXT);
            if (text != null && text.length() > 0) { OldSendService.setAlias(this, text); recreate(); }
            return;
        }
        // 6020 字号滑块
        if (requestCode == 6020) {
            Float scale = (Float) data.getSerializableExtra(UikitFullSliderActivity.EXTRA_RESULT_SCALE);
            if (scale != null) {
                prefs().edit().putFloat("ui_scale", scale).commit();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return;
        }
        // 6021 关于页：纯展示，无需后续处理
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (WatchUiUtils.handleSwipeBack(this, ev)) return true;
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        // 开启“软件内返回”时吞掉返回键，交给界面内横滑退出；关闭时交回系统
        if (org.watchsend.uikit.BackGate.handleBack(this)) return;
        super.onBackPressed();
    }
}

package org.watchsend.watch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.watchsend.OldSendService;
import org.watchsend.R;
import org.watchsend.model.Device;
import org.watchsend.model.FavoritesStore;
import org.watchsend.uikit.UikitFullInputActivity;
import org.watchsend.util.I18nManager;
import org.watchsend.util.ThemeUtils;

/**
 * 设备详情页（对齐官方 pages/device_details_page.dart 的信息层级）：
 * 设备名 + 连接通道（IP/端口/协议/版本/类型）+ 身份指纹 + 收藏（含自定义备注）+ 白/黑名单。
 *
 * 手表上没有展开的卡片，所以用设置页同款"胶囊行 + 分区标题"的竖排结构，
 * 每行点一下就是一次动作，长按设备进入这里取代原来的三选一弹窗。
 */
public class DeviceDetailActivity extends Activity {

    public static final String EXTRA_ALIAS = "d_alias";
    public static final String EXTRA_IP = "d_ip";
    public static final String EXTRA_PORT = "d_port";
    public static final String EXTRA_HTTPS = "d_https";
    public static final String EXTRA_VERSION = "d_version";
    public static final String EXTRA_TYPE = "d_type";
    public static final String EXTRA_MODEL = "d_model";
    public static final String EXTRA_FP = "d_fp";
    public static final String EXTRA_CERT_FP = "d_cert_fp";

    private static final int REQ_REMARK = 7001;

    private LinearLayout container;
    private Device device;

    public static void show(Activity ctx, Device d, int requestCode) {
        Intent i = new Intent(ctx, DeviceDetailActivity.class);
        i.putExtra(EXTRA_ALIAS, d.alias);
        i.putExtra(EXTRA_IP, d.ip);
        i.putExtra(EXTRA_PORT, d.port);
        i.putExtra(EXTRA_HTTPS, d.https);
        i.putExtra(EXTRA_VERSION, d.version);
        i.putExtra(EXTRA_TYPE, d.deviceType);
        i.putExtra(EXTRA_MODEL, d.deviceModel);
        i.putExtra(EXTRA_FP, d.fingerprint);
        i.putExtra(EXTRA_CERT_FP, d.certFingerprint);
        ctx.startActivityForResult(i, requestCode);
    }

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(org.watchsend.watch.WatchUiUtils.wrapScale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        I18nManager.init(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_watch_settings);
        ThemeUtils.applyTheme(this);
        WatchUiUtils.applyEntryAnimation(this);

        boolean isDark = ThemeUtils.isDarkMode(this);
        View root = findViewById(R.id.settings_root);
        if (root != null) {
            root.setBackgroundColor(isDark ? 0xFF000000 : 0xFFF4F6F8);
            WatchUiUtils.applyRoundPadding(this, root);
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
        View back = findViewById(R.id.btn_back);
        if (back != null) back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });

        device = new Device();
        Intent it = getIntent();
        device.alias = it.getStringExtra(EXTRA_ALIAS);
        device.ip = it.getStringExtra(EXTRA_IP);
        device.port = it.getIntExtra(EXTRA_PORT, 53317);
        device.https = it.getBooleanExtra(EXTRA_HTTPS, false);
        device.version = it.getStringExtra(EXTRA_VERSION);
        device.deviceType = it.getStringExtra(EXTRA_TYPE);
        device.deviceModel = it.getStringExtra(EXTRA_MODEL);
        device.fingerprint = it.getStringExtra(EXTRA_FP);
        device.certFingerprint = it.getStringExtra(EXTRA_CERT_FP);

        build();
    }

    private android.content.SharedPreferences prefs() {
        return getSharedPreferences("oldsend", MODE_PRIVATE);
    }

    private void build() {
        container.removeAllViews();

        addSection(I18nManager.get("device_details"));

        addRow(device.alias == null ? "" : device.alias, typeLabel(device.deviceType), null);
        addRow(I18nManager.get("protocol_label"),
                (device.https ? "HTTPS" : "HTTP") + " · " + device.ip + ":" + device.port, null);
        addRow("Version", (device.version == null ? "" : "v" + device.version)
                + (device.deviceModel != null && device.deviceModel.length() > 0 ? " · " + device.deviceModel : ""), null);

        final String identity = device.identity();
        String fpShow = identity == null ? "" : identity;
        if (fpShow.length() > 24) fpShow = fpShow.substring(0, 12) + "…" + fpShow.substring(fpShow.length() - 8);
        addRow(I18nManager.get("fingerprint_label"), fpShow, new View.OnClickListener() {
            public void onClick(View v) { copy(identity); }
        });

        // 指纹文字短语：官方用图标字母表让人工比对，这里换成四段词（FingerprintWords）
        String phrase = org.watchsend.util.FingerprintWords.phrase(identity);
        if (phrase.length() > 0) addRow(I18nManager.get("fingerprint_phrase"), phrase, null);

        addSection(I18nManager.get("favorites"));
        boolean fav = FavoritesStore.isFavorite(identity);
        addRow(fav ? "★ " + I18nManager.get("remove_favorite") : "☆ " + I18nManager.get("add_favorite"), null,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        boolean added = FavoritesStore.toggle(identity, device.alias, device.ip, device.port);
                        toast(added ? I18nManager.get("favorite_added") : I18nManager.get("favorite_removed"));
                        setResult(RESULT_OK);
                        build();
                    }
                });

        if (fav) {
            String remark = FavoritesStore.remark(identity);
            addRow(I18nManager.get("remark_label"), remark.length() > 0 ? remark : I18nManager.get("not_set"),
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            UikitFullInputActivity.show(DeviceDetailActivity.this,
                                    I18nManager.get("remark_label"),
                                    FavoritesStore.remark(identity), null, REQ_REMARK);
                        }
                    });
        }

        addSection(I18nManager.get("manage_acl"));
        boolean inWl = contains("whitelist", identity);
        boolean inBl = contains("blacklist", identity);
        addRow((inWl ? "− " : "+ ") + I18nManager.get("view_whitelist"), inWl ? "✓" : null,
                new View.OnClickListener() {
                    public void onClick(View v) { toggleList("whitelist", identity); }
                });
        addRow((inBl ? "− " : "+ ") + I18nManager.get("view_blacklist"), inBl ? "✓" : null,
                new View.OnClickListener() {
                    public void onClick(View v) { toggleList("blacklist", identity); }
                });
    }

    private boolean contains(String key, String fp) {
        if (fp == null || fp.length() == 0) return false;
        return OldSendService.getPrefStringGlobal(key, "").contains(fp);
    }

    private void toggleList(String key, String fp) {
        String cur = OldSendService.getPrefStringGlobal(key, "");
        boolean exists = fp != null && fp.length() > 0 && cur.contains(fp);
        if (exists) cur = cur.replace(fp + ",", "").replace(fp, "");
        else cur = cur + (fp == null ? "" : fp) + ",";
        prefs().edit().putString(key, cur).commit();
        toast(exists ? I18nManager.get("removed") : I18nManager.get("added_to_list"));
        setResult(RESULT_OK);
        build();
    }

    private void copy(String text) {
        try {
            android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(android.content.ClipData.newPlainText("fingerprint", text));
            toast(I18nManager.get("copied_hint"));
        } catch (Throwable ignored) {
        }
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private String typeLabel(String type) {
        if ("desktop".equals(type)) return I18nManager.get("device_desktop");
        if ("web".equals(type)) return I18nManager.get("device_web");
        if ("headless".equals(type)) return I18nManager.get("device_headless");
        if ("server".equals(type)) return I18nManager.get("device_server");
        return I18nManager.get("device_mobile");
    }

    private void addSection(String title) {
        boolean isDark = ThemeUtils.isDarkMode(this);
        TextView tv = new TextView(this);
        tv.setText(title); tv.setTextSize(12);
        tv.setTextColor(isDark ? 0xFFAAAAAA : 0xFF666666);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setPadding(dp(12), dp(10), dp(12), dp(4));
        container.addView(tv);
    }

    private void addRow(String label, String value, View.OnClickListener click) {
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

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextSize(15);
        tvLabel.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF1C1B1F);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvLabel);

        TextView tvValue = new TextView(this);
        tvValue.setText(value == null ? "" : value);
        tvValue.setTextSize(11);
        tvValue.setTextColor(isDark ? 0xFFAAAAAA : 0xFF666666);
        org.watchsend.uikit.TextFit.wrap(tvValue, 2); // 长值（路径、SSID、型号名）允许两行
        row.addView(tvValue);

        if (click != null) {
            row.setOnClickListener(click);
            row.setClickable(true);
        }
        container.addView(row);
    }

    private int dp(int val) { return (int) (val * getResources().getDisplayMetrics().density); }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_REMARK && resultCode == RESULT_OK && data != null) {
            String remark = data.getStringExtra(UikitFullInputActivity.EXTRA_RESULT_TEXT);
            if (remark == null) remark = data.getStringExtra("result");
            FavoritesStore.setRemark(device.identity(), remark);
            setResult(RESULT_OK);
            build();
        }
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        // 横滑返回与系统返回共用同一个开关，页面级统一挂载
        if (org.watchsend.uikit.UikitUi.handleSwipeBack(this, ev)) return true;
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        // 开启“软件内返回”时吞掉返回键，交给界面内横滑退出；关闭时交回系统
        if (org.watchsend.uikit.BackGate.handleBack(this)) return;
        super.onBackPressed();
    }
}

package org.watchsend.watch;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.watchsend.R;
import org.watchsend.util.I18nManager;
import org.watchsend.util.QrCode;
import org.watchsend.util.ThemeUtils;

/**
 * 网页分享二维码（移植官方 pages/web_share_page.dart 的二维码展示）。
 *
 * 官方在这里给出一个可扫的二维码 + 地址文本；手表上逐字抄 URL 到电脑浏览器是真实痛点，
 * 所以把地址编码成 QR 直接让电脑摄像头读。二维码由 QrCode（零依赖自研）生成，
 * 已用 OpenCV 解码验证版本 2–5 全部可读。
 */
public class WebShareQrActivity extends Activity {

    public static final String EXTRA_URL = "qr_url";

    private String url;

    public static void show(Activity ctx, String url) {
        android.content.Intent i = new android.content.Intent(ctx, WebShareQrActivity.class);
        i.putExtra(EXTRA_URL, url);
        ctx.startActivity(i);
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
        org.watchsend.uikit.RtlSupport.apply(this);
        ThemeUtils.applyTheme(this);
        WatchUiUtils.applyEntryAnimation(this);

        boolean isDark = ThemeUtils.isDarkMode(this);
        View root = findViewById(R.id.settings_root);
        if (root != null) {
            // 原来固定黑底：浅色主题下这一页和主界面一黑一白，像两个应用
            root.setBackgroundColor(isDark ? 0xFF000000 : 0xFFF4F6F8);
            WatchUiUtils.applyRoundPadding(this, root);
            View header = findViewById(R.id.settings_header);
            if (header != null) header.setVisibility(View.GONE);
        }

        url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null) url = "http://" + org.watchsend.OldSendService.getLocalIp() + ":53317";

        LinearLayout container = (LinearLayout) findViewById(R.id.settings_container);
        ScrollView scroll = (ScrollView) findViewById(R.id.settings_scroll);
        if (scroll != null) {
            WatchUiUtils.setupWheelScroll(scroll, container);
            WatchHardwareUtils.bindRotaryEncoder(scroll);
        }
        container.removeAllViews();

        TextView title = new TextView(this);
        title.setText(I18nManager.get("web_qr"));
        title.setTextColor(isDark ? 0xFFFFFFFF : 0xFF1C1B1F);
        title.setTextSize(13);
        title.setGravity(Gravity.CENTER);
        container.addView(title);

        ImageView iv = new ImageView(this);
        int w = getResources().getDisplayMetrics().widthPixels;
        int h = getResources().getDisplayMetrics().heightPixels;
        int side = Math.min(w, h);
        if (side <= 0) side = 320;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(0) + side * 65 / 100, side * 65 / 100);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        lp.topMargin = dp(6);
        lp.bottomMargin = dp(6);
        iv.setLayoutParams(lp);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // 二维码本身必须是白底黑块才扫得出；浅色主题下白块压在白背景上会没有边界，补一圈描边
        GradientDrawable qrFrame = new GradientDrawable();
        qrFrame.setColor(0xFFFFFFFF);
        qrFrame.setStroke(dp(1), isDark ? 0xFF3E3E3E : 0xFFE0E0E0);
        iv.setBackground(qrFrame);
        iv.setImageBitmap(render(url, lp.width));
        container.addView(iv);

        TextView tvUrl = new TextView(this);
        tvUrl.setText(url);
        tvUrl.setTextColor(isDark ? 0xFFAAAAAA : 0xFF333333);
        tvUrl.setTextSize(10);
        tvUrl.setGravity(Gravity.CENTER);
        container.addView(tvUrl);

        TextView desc = new TextView(this);
        desc.setText(I18nManager.get("web_qr_desc"));
        desc.setTextColor(isDark ? 0xFF888888 : 0xFF666666);
        desc.setTextSize(9);
        desc.setGravity(Gravity.CENTER);
        container.addView(desc);

        Button copy = new Button(this);
        copy.setText("⧉ " + I18nManager.get("copy_fingerprint"));
        copy.setTextColor(0xFFFFFFFF);
        copy.setTextSize(11);
        copy.setAllCaps(false);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(18));
        boolean dynamic = org.watchsend.uikit.UikitColor.isDynamicColorEnabled(this);
        if (dynamic) {
            // 复制按钮用主色的容器档，和主界面次要按钮同一套层级
            bg.setColor(org.watchsend.uikit.UikitColor.getContainerColor(this, isDark));
            copy.setTextColor(org.watchsend.uikit.UikitColor.getAccentColor(this, isDark));
        } else {
            bg.setColor(isDark ? 0xFF262626 : 0xFFE0E0E0);
            copy.setTextColor(isDark ? 0xFFFFFFFF : 0xFF1C1B1F);
        }
        copy.setBackground(bg);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(dp(120), dp(34));
        clp.gravity = Gravity.CENTER_HORIZONTAL;
        clp.topMargin = dp(8);
        copy.setLayoutParams(clp);
        copy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("WatchSend", url));
                    Toast.makeText(WebShareQrActivity.this, I18nManager.get("copied_hint"), Toast.LENGTH_SHORT).show();
                } catch (Throwable ignored) {}
            }
        });
        container.addView(copy);
    }

    /** 把模块矩阵放大成位图（含 4 模块静区，扫描器才认得边界）。 */
    private Bitmap render(String text, int pixelSize) {
        QrCode.Matrix m;
        try {
            m = QrCode.encode(text);
        } catch (Throwable t) {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
        int quiet = 4;
        int modules = m.size + quiet * 2;
        int scale = Math.max(1, pixelSize / modules);
        int size = modules * scale;
        int[] pixels = new int[size * size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int mx = x / scale - quiet;
                int my = y / scale - quiet;
                boolean dark = mx >= 0 && my >= 0 && mx < m.size && my < m.size && m.modules[my][mx];
                pixels[y * size + x] = dark ? Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bmp.setPixels(pixels, 0, size, 0, 0, size, size);
        return bmp;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }

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

package org.watchsend.watch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.watchsend.R;
import org.watchsend.util.I18nManager;
import org.watchsend.util.ThemeUtils;

import java.util.ArrayList;

/**
 * 已选文件确认页（移植官方 pages/selected_files_page.dart）。
 *
 * 官方发送前会列出本次要发的文件：可逐个移除、显示总大小、再确认发送。
 * 我们原先选完文件就直接跳到传输页，选错了只能等它发完。这里补上这层确认：
 * 点一行 = 取消勾选该文件（划线置灰），底部按钮显示"发送 N 个文件"。
 */
public class SelectedFilesActivity extends Activity {

    public static final String EXTRA_NAMES = "sel_names";
    public static final String EXTRA_SIZES = "sel_sizes";
    public static final String EXTRA_RESULT_KEPT = "sel_kept";

    private final ArrayList<Boolean> kept = new ArrayList<Boolean>();
    private ArrayList<String> names = new ArrayList<String>();
    private long[] sizes;
    private LinearLayout container;
    private Button sendBtn;

    public static void show(Activity ctx, ArrayList<String> names, long[] sizes, int requestCode) {
        Intent i = new Intent(ctx, SelectedFilesActivity.class);
        i.putStringArrayListExtra(EXTRA_NAMES, names);
        i.putExtra(EXTRA_SIZES, sizes);
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
        org.watchsend.uikit.RtlSupport.apply(this);
        ThemeUtils.applyTheme(this);
        WatchUiUtils.applyEntryAnimation(this);

        boolean isDark = ThemeUtils.isDarkMode(this);
        View root = findViewById(R.id.settings_root);
        if (root != null) {
            root.setBackgroundColor(isDark ? 0xFF000000 : 0xFFF4F6F8);
            WatchUiUtils.applyRoundPadding(this, root);
            View header = findViewById(R.id.settings_header);
            if (header != null && WatchHardwareUtils.isRoundScreen(root)) header.setVisibility(View.GONE);
        }
        container = (LinearLayout) findViewById(R.id.settings_container);
        ScrollView scroll = (ScrollView) findViewById(R.id.settings_scroll);
        if (scroll != null) {
            WatchUiUtils.setupWheelScroll(scroll, container);
            WatchHardwareUtils.bindRotaryEncoder(scroll);
        }
        View back = findViewById(R.id.btn_back);
        if (back != null) back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });

        ArrayList<String> n = getIntent().getStringArrayListExtra(EXTRA_NAMES);
        if (n != null) names = n;
        sizes = getIntent().getLongArrayExtra(EXTRA_SIZES);
        if (sizes == null) sizes = new long[names.size()];
        for (int i = 0; i < names.size(); i++) kept.add(Boolean.TRUE);

        build();
    }

    private void build() {
        container.removeAllViews();

        TextView title = new TextView(this);
        title.setText(I18nManager.get("selected_files"));
        title.setTextSize(13);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(4), 0, dp(6));
        container.addView(title);

        for (int i = 0; i < names.size(); i++) container.addView(buildRow(i));

        sendBtn = new Button(this);
        sendBtn.setTextColor(Color.WHITE);
        sendBtn.setTextSize(14);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(22));
        bg.setColor(0xFF388E3C);
        sendBtn.setBackground(bg);
        sendBtn.setAllCaps(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44));
        lp.topMargin = dp(10);
        sendBtn.setLayoutParams(lp);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ArrayList<Integer> idx = new ArrayList<Integer>();
                for (int i = 0; i < kept.size(); i++) if (kept.get(i)) idx.add(i);
                if (idx.isEmpty()) { finish(); return; }
                int[] out = new int[idx.size()];
                for (int i = 0; i < out.length; i++) out[i] = idx.get(i);
                Intent data = new Intent();
                data.putExtra(EXTRA_RESULT_KEPT, out);
                setResult(RESULT_OK, data);
                finish();
            }
        });
        container.addView(sendBtn);
        refreshSendBtn();
    }

    private View buildRow(final int index) {
        boolean isDark = ThemeUtils.isDarkMode(this);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(10), dp(14), dp(10));
        row.setBackground(WatchUiUtils.getPillBackground(this));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(6);
        row.setLayoutParams(lp);

        TextView tv = new TextView(this);
        tv.setText(formatSize(sizes[index]) + "  " + names.get(index));
        tv.setTextSize(11);
        tv.setSingleLine(true);
        tv.setGravity(org.watchsend.uikit.RtlSupport.startGravity());
        tv.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);
        tv.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF1C1B1F);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tv);

        final TextView mark = new TextView(this);
        mark.setText("✓");
        mark.setTextSize(11);
        mark.setTextColor(0xFF388E3C);
        row.addView(mark);

        row.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean now = !kept.get(index);
                kept.set(index, Boolean.valueOf(now));
                mark.setText(now ? "✓" : "✕");
                mark.setTextColor(now ? 0xFF388E3C : 0xFFD32F2F);
                ((TextView) ((LinearLayout) v).getChildAt(0)).setAlpha(now ? 1f : 0.45f);
                refreshSendBtn();
            }
        });
        return row;
    }

    private void refreshSendBtn() {
        int n = 0;
        long total = 0;
        for (int i = 0; i < kept.size(); i++) {
            if (kept.get(i)) { n++; if (i < sizes.length) total += sizes[i]; }
        }
        if (sendBtn != null) sendBtn.setText(I18nManager.get("send_n_files").replace("%1", String.valueOf(n)) + " · " + formatSize(total));
    }

    private String formatSize(long bytes) {
        if (bytes >= 1024 * 1024) return String.format(java.util.Locale.US, "%.1f MB", bytes / 1024.0 / 1024.0);
        if (bytes >= 1024) return String.format(java.util.Locale.US, "%.0f KB", bytes / 1024.0);
        return bytes + " B";
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

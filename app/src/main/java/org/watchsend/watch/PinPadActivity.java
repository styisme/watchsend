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
import android.widget.TextView;

import org.watchsend.R;
import org.watchsend.util.I18nManager;
import org.watchsend.util.ThemeUtils;

/**
 * PIN 数字键盘（移植官方 pages/verify_page.dart）。
 *
 * 官方是 3×4 九宫格大按钮 + 顶部掩码显示，避免在小屏上开系统输入法（手表输入法往往不可用）。
 * 这里同样用自建按钮网格：1-9、0、退格、确认，最多 6 位（官方 PIN 也是 6 位）。
 */
public class PinPadActivity extends Activity {

    public static final String EXTRA_PIN = "pin_result";
    public static final String EXTRA_TITLE = "pin_title";

    private static final int MAX_LEN = 6;

    private final StringBuilder input = new StringBuilder();
    private TextView display;

    public static void show(Activity ctx, String title, int requestCode) {
        Intent i = new Intent(ctx, PinPadActivity.class);
        i.putExtra(EXTRA_TITLE, title);
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

        View root = findViewById(R.id.settings_root);
        if (root != null) {
            root.setBackgroundColor(0xFF000000);
            WatchUiUtils.applyRoundPadding(this, root);
            View header = findViewById(R.id.settings_header);
            if (header != null) header.setVisibility(View.GONE);
        }
        LinearLayout container = (LinearLayout) findViewById(R.id.settings_container);
        container.removeAllViews();

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (title == null || title.length() == 0) title = I18nManager.get("enter_pin");

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(0xFFFFFFFF);
        t.setTextSize(13);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(4), 0, dp(6));
        container.addView(t);

        display = new TextView(this);
        display.setTextColor(0xFFFFFFFF);
        display.setTextSize(22);
        display.setGravity(Gravity.CENTER);
        display.setPadding(0, dp(2), 0, dp(8));
        container.addView(display);
        refreshDisplay();

        container.addView(buildGrid());
    }

    private LinearLayout buildGrid() {
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        String[][] rows = {{"1", "2", "3"}, {"4", "5", "6"}, {"7", "8", "9"}, {"⌫", "0", "✓"}};
        for (int r = 0; r < rows.length; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER);
            for (int c = 0; c < rows[r].length; c++) {
                final String key = rows[r][c];
                Button b = new Button(this);
                b.setText(key);
                b.setTextSize(15);
                b.setTextColor(0xFFFFFFFF);
                b.setAllCaps(false);
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.OVAL);
                bg.setColor(key.equals("✓") ? 0xFF2E7D32 : (key.equals("⌫") ? 0xFF444444 : 0xFF262626));
                b.setBackground(bg);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(52), dp(40));
                lp.setMargins(dp(4), dp(3), dp(4), dp(3));
                b.setLayoutParams(lp);
                b.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) { onKey(key); }
                });
                row.addView(b);
            }
            grid.addView(row);
        }
        return grid;
    }

    private void onKey(String key) {
        WatchHardwareUtils.vibrate(this, WatchHardwareUtils.HAPTIC_LIGHT);
        if ("⌫".equals(key)) {
            if (input.length() > 0) input.deleteCharAt(input.length() - 1);
            refreshDisplay();
            return;
        }
        if ("✓".equals(key)) {
            String pin = input.toString();
            if (pin.length() == 0) return;
            Intent data = new Intent();
            data.putExtra(EXTRA_PIN, pin);
            setResult(RESULT_OK, data);
            finish();
            return;
        }
        if (input.length() >= MAX_LEN) return;
        input.append(key);
        refreshDisplay();
        if (input.length() == MAX_LEN) {
            // 打满 6 位直接提交，官方也是满位即校验，省一次点击
            Intent data = new Intent();
            data.putExtra(EXTRA_PIN, input.toString());
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void refreshDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) sb.append("●");
        for (int i = input.length(); i < 4; i++) sb.append("○");
        if (input.length() > 4) sb.append(" ●");
        display.setText(sb.toString());
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }
}

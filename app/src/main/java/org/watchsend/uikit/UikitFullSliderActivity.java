package org.watchsend.uikit;

import org.watchsend.R;
import org.watchsend.util.I18nManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Locale;

/**
 * 全屏滑块容器：用于在全屏场景下调节一个浮点数（如 UI 缩放比例）。
 * 替代旧版嵌在非全屏 Dialog 里的自定义 SeekBar 布局。
 */
public class UikitFullSliderActivity extends Activity {
    public static final String EXTRA_TITLE = "uikit_title";
    public static final String EXTRA_CURRENT = "uikit_current";
    public static final String EXTRA_MIN = "uikit_min";
    public static final String EXTRA_MAX = "uikit_max";
    public static final String EXTRA_RESULT_SCALE = "uikit_result_scale";

    private static final int PROGRESS_MAX = 100;

    private float minScale = 0.3f;
    private float maxScale = 2.5f;
    private float currentScale = 1.0f;

    public static void show(Activity activity, String title, float currentScale, int requestCode) {
        show(activity, title, currentScale, 0.3f, 2.5f, requestCode);
    }

    public static void show(Activity activity, String title, float currentScale,
                            float minScale, float maxScale, int requestCode) {
        Intent intent = new Intent(activity, UikitFullSliderActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_CURRENT, currentScale);
        intent.putExtra(EXTRA_MIN, minScale);
        intent.putExtra(EXTRA_MAX, maxScale);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(UikitUi.wrapScale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        UikitTheme.applyTheme(this);
        UikitUi.applyEntryAnimation(this);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        // minSdk 10 无 getFloatExtra，统一用 Serializable 装箱读取
        Float cur = (Float) getIntent().getSerializableExtra(EXTRA_CURRENT);
        Float mn = (Float) getIntent().getSerializableExtra(EXTRA_MIN);
        Float mx = (Float) getIntent().getSerializableExtra(EXTRA_MAX);
        currentScale = cur == null ? 1.0f : cur.floatValue();
        minScale = mn == null ? 0.3f : mn.floatValue();
        maxScale = mx == null ? 2.5f : mx.floatValue();

        final boolean dark = UikitTheme.isDarkMode(this);
        int density = (int) getResources().getDisplayMetrics().density;
        int bgColor = dark ? 0xFF000000 : 0xFFF4F6F8;

        getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(bgColor));

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bgColor);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bgColor);
        root.setPadding(16 * density, 16 * density, 16 * density, 16 * density);
        root.setLayoutParams(new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
        scroll.addView(root);
        setContentView(scroll);
        org.watchsend.uikit.RtlSupport.apply(this);

        UikitUi.applyRoundPadding(this, root);
        UikitUi.setupWheelScroll(scroll, root);

        // 标题
        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(20);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTextColor(UikitColor.getAccentColor(this, dark));
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleLp.bottomMargin = 16 * density;
        tvTitle.setLayoutParams(titleLp);
        root.addView(tvTitle);

        // 当前数值
        final TextView tvValue = new TextView(this);
        tvValue.setText(String.format(Locale.US, "%.2f", currentScale) + "x");
        tvValue.setTextSize(18);
        tvValue.setGravity(Gravity.CENTER);
        tvValue.setTextColor(UikitColor.getPrimaryColor(this));
        LinearLayout.LayoutParams valueLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        valueLp.bottomMargin = 16 * density;
        tvValue.setLayoutParams(valueLp);
        root.addView(tvValue);

        // 滑块
        final UikitSlider slider = new UikitSlider(this);
        slider.setMax(PROGRESS_MAX);
        int progress = (int) (((currentScale - minScale) / (maxScale - minScale)) * PROGRESS_MAX);
        slider.setProgress(Math.max(0, Math.min(PROGRESS_MAX, progress)));
        LinearLayout.LayoutParams sliderLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        slider.setLayoutParams(sliderLp);

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int p, boolean fromUser) {
                currentScale = minScale + (p / (float) PROGRESS_MAX) * (maxScale - minScale);
                tvValue.setText(String.format(Locale.US, "%.2f", currentScale) + "x");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        root.addView(slider);

        // 按钮区
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);

        TextView btnCancel = new TextView(this);
        btnCancel.setText(I18nManager.get("cancel"));
        btnCancel.setGravity(Gravity.CENTER);
        btnCancel.setTextColor(dark ? 0xFFFFFFFF : 0xFF1C1B1F);
        GradientDrawable cancelBg = new GradientDrawable();
        cancelBg.setCornerRadius(24 * density);
        cancelBg.setColor(dark ? 0xFF2C2C2E : 0xFFE0E0E0);
        btnCancel.setBackground(cancelBg);
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(0, 48 * density, 1);
        cLp.rightMargin = 8 * density;
        btnCancel.setLayoutParams(cLp);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        TextView btnOk = new TextView(this);
        btnOk.setText(I18nManager.get("confirm"));
        btnOk.setGravity(Gravity.CENTER);
        btnOk.setTextColor(UikitColor.getOnPrimaryColor(this));
        GradientDrawable okBg = new GradientDrawable();
        okBg.setCornerRadius(24 * density);
        okBg.setColor(UikitColor.getPrimaryColor(this));
        btnOk.setBackground(okBg);
        LinearLayout.LayoutParams oLp = new LinearLayout.LayoutParams(0, 48 * density, 1);
        oLp.leftMargin = 8 * density;
        btnOk.setLayoutParams(oLp);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(EXTRA_RESULT_SCALE, currentScale);
                setResult(RESULT_OK, data);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        btnRow.addView(btnCancel);
        btnRow.addView(btnOk);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = 16 * density;
        btnRow.setLayoutParams(rowLp);
        root.addView(btnRow);
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (UikitUi.handleSwipeBack(this, ev)) return true;
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        // 开启“软件内返回”时吞掉返回键，交给界面内横滑退出；关闭时交回系统
        if (org.watchsend.uikit.BackGate.handleBack(this)) return;
        super.onBackPressed();
    }
}

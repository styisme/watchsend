package org.watchsend.uikit;

import org.watchsend.R;
import org.watchsend.util.I18nManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.speech.RecognizerIntent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;

public class UikitFullInputActivity extends Activity {
    private static final int REQUEST_VOICE = 9999;
    private EditText etInput;
    public static final String EXTRA_TITLE = "uikit_title";
    public static final String EXTRA_DEFAULT_TEXT = "uikit_default_text";
    public static final String EXTRA_HINT = "uikit_hint";
    public static final String EXTRA_POSITIVE_TEXT = "uikit_positive_text";
    public static final String EXTRA_NEGATIVE_TEXT = "uikit_negative_text";
    public static final String EXTRA_RESULT_TEXT = "uikit_result_text";

    public static void show(Activity activity, String title, String defaultText, String hint, int requestCode) {
        show(activity, title, defaultText, hint, null, null, requestCode);
    }

    public static void show(Activity activity, String title, String defaultText, String hint,
                            String positiveText, String negativeText, int requestCode) {
        Intent intent = new Intent(activity, UikitFullInputActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_DEFAULT_TEXT, defaultText);
        intent.putExtra(EXTRA_HINT, hint);
        intent.putExtra(EXTRA_POSITIVE_TEXT, positiveText);
        intent.putExtra(EXTRA_NEGATIVE_TEXT, negativeText);
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
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String defaultText = getIntent().getStringExtra(EXTRA_DEFAULT_TEXT);
        String hint = getIntent().getStringExtra(EXTRA_HINT);
        String posText = getIntent().getStringExtra(EXTRA_POSITIVE_TEXT);
        String negText = getIntent().getStringExtra(EXTRA_NEGATIVE_TEXT);

        if (posText == null) posText = getString(android.R.string.ok);
        if (negText == null) negText = getString(android.R.string.cancel);

        boolean dark = UikitTheme.isDarkMode(this);
        int density = (int) getResources().getDisplayMetrics().density;
        int bgColor = dark ? 0xFF000000 : 0xFFF4F6F8;
        
        // 【修复】设置 Window 默认背景色
        getWindow().setBackgroundDrawable(new ColorDrawable(bgColor));

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bgColor); // 【修复】背景色设置在这里
        scroll.setFillViewport(true); // 【修复】让内部布局撑满屏幕

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.setPadding(16 * density, 16 * density, 16 * density, 16 * density);
        
        scroll.addView(root);
        setContentView(scroll);
        org.watchsend.uikit.RtlSupport.apply(this);

        UikitUi.applyRoundPadding(this, root);
        UikitUi.setupWheelScroll(scroll, root);

        // 1. 标题
        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(20);
        TextFit.wrap(tvTitle, 2);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTextColor(UikitColor.getAccentColor(this, dark));
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleLp.bottomMargin = 16 * density;
        tvTitle.setLayoutParams(titleLp);
        root.addView(tvTitle);

        // 2. 输入框
        etInput = new EditText(this);
        etInput.setText(defaultText);
        etInput.setHint(hint);
        etInput.setTextSize(16);
        etInput.setSingleLine(false);
        etInput.setGravity(org.watchsend.uikit.RtlSupport.startGravity() | android.view.Gravity.TOP);
        etInput.setGravity(Gravity.TOP | Gravity.START);
        etInput.setPadding(16 * density, 12 * density, 16 * density, 12 * density);
        etInput.setMinHeight(100 * density);
        
        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(dark ? 0xFF1E1E1E : 0xFFFFFFFF);
        inputBg.setCornerRadius(16 * density);
        inputBg.setStroke(1, dark ? 0xFF3E3E3E : 0xFFE0E0E0);
        etInput.setBackground(inputBg);
        etInput.setTextColor(dark ? 0xFFFFFFFF : 0xFF1C1B1F);
        etInput.setHintTextColor(dark ? 0xFF888888 : 0xFF999999);
        
        LinearLayout.LayoutParams etLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f); // 用权重 1 占据中间剩余空间
        etLp.bottomMargin = 16 * density;
        etInput.setLayoutParams(etLp);
        root.addView(etInput);

        // 2.5 语音输入入口（保留旧版语音识别能力）
        LinearLayout voiceRow = new LinearLayout(this);
        voiceRow.setOrientation(LinearLayout.HORIZONTAL);
        voiceRow.setGravity(Gravity.END);
        voiceRow.setPadding(0, 0, 0, 12 * density);
        TextView btnVoice = new TextView(this);
        btnVoice.setText(I18nManager.get("voice_input"));
        btnVoice.setTextSize(12);
        btnVoice.setTextColor(org.watchsend.uikit.UikitColor.getAccentColor(this, dark));
        int vpad = UikitScreenUtils.dp2px(this, 8);
        btnVoice.setPadding(vpad, vpad / 2, vpad, vpad / 2);
        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent voice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                try {
                    startActivityForResult(voice, REQUEST_VOICE);
                } catch (Exception e) {
                    android.widget.Toast.makeText(UikitFullInputActivity.this,
                            I18nManager.get("voice_unsupported"), android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
        voiceRow.addView(btnVoice);
        root.addView(voiceRow);

        // 3. 按钮区
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);
        
        UikitButton btnCancel = new UikitButton(this);
        btnCancel.setText(negText);
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
                finish();
                overridePendingTransition(0, 0);
            }
        });

        UikitButton btnOk = new UikitButton(this);
        btnOk.setText(posText);
        LinearLayout.LayoutParams oLp = new LinearLayout.LayoutParams(0, 48 * density, 1);
        oLp.leftMargin = 8 * density;
        btnOk.setLayoutParams(oLp);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(EXTRA_RESULT_TEXT, etInput.getText().toString());
                setResult(RESULT_OK, data);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        btnRow.addView(btnCancel);
        btnRow.addView(btnOk);
        root.addView(btnRow);

        etInput.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 语音识别结果回填到输入框
        if (requestCode == REQUEST_VOICE && resultCode == RESULT_OK && data != null && etInput != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                etInput.setText(results.get(0));
                etInput.setSelection(etInput.getText().length());
            }
        }
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

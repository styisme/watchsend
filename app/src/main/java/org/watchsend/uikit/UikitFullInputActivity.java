package org.watchsend.uikit;

import org.watchsend.R;

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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class UikitFullInputActivity extends Activity {
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

        UikitUi.applyRoundPadding(this, root);
        UikitUi.setupWheelScroll(scroll, root);

        // 1. 标题
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

        // 2. 输入框
        final EditText etInput = new EditText(this);
        etInput.setText(defaultText);
        etInput.setHint(hint);
        etInput.setTextSize(16);
        etInput.setSingleLine(false);
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
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (UikitUi.handleSwipeBack(this, ev)) return true;
        return super.dispatchTouchEvent(ev);
    }
}

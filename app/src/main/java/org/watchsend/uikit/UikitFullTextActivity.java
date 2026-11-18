package org.watchsend.uikit;

import org.watchsend.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class UikitFullTextActivity extends Activity {
    public static final String EXTRA_TITLE = "uikit_title";
    public static final String EXTRA_MESSAGE = "uikit_message";
    public static final String EXTRA_POSITIVE_TEXT = "uikit_positive_text";
    public static final String EXTRA_AUTO_SPLIT = "uikit_auto_split";

    public static void show(Activity activity, String title, String message, String positiveText, int requestCode) {
        show(activity, title, message, positiveText, false, requestCode);
    }

    public static void show(Activity activity, String title, String message, String positiveText, boolean autoSplit, int requestCode) {
        Intent intent = new Intent(activity, UikitFullTextActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtra(EXTRA_POSITIVE_TEXT, positiveText);
        intent.putExtra(EXTRA_AUTO_SPLIT, autoSplit);
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
        String message = getIntent().getStringExtra(EXTRA_MESSAGE);
        String posText = getIntent().getStringExtra(EXTRA_POSITIVE_TEXT);
        boolean autoSplit = getIntent().getBooleanExtra(EXTRA_AUTO_SPLIT, false);
        
        if (posText == null) posText = getString(android.R.string.ok);

        boolean dark = UikitTheme.isDarkMode(this);
        int density = (int) getResources().getDisplayMetrics().density;
        int bgColor = dark ? 0xFF000000 : 0xFFF4F6F8;

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bgColor);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        root.setPadding(16 * density, 16 * density, 16 * density, 16 * density);
        
        scroll.addView(root);
        setContentView(scroll);

        UikitUi.applyRoundPadding(this, root);

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

        // 2. 文本容器
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams containerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        containerLp.bottomMargin = 24 * density;
        textContainer.setLayoutParams(containerLp);
        root.addView(textContainer);

        if (autoSplit) {
            // 【核心修复】智能断句：只切分中文标点和换行。如果是英文句号，要求它前面不是数字，且后面跟着空格或大写字母
            // 注意这里的正则字符串在 Java 中的转义：\\n, \\. 和 \\s
            String[] sentences = message.split("(?<=。)|(?<=！)|(?<=？)|(?<=\\n)|(?<![0-9])\\.(?=\\s+[A-Z])");
            for (String sentence : sentences) {
                if (sentence.trim().isEmpty()) continue;
                
                TextView tvSentence = new TextView(this);
                tvSentence.setText(sentence.trim());
                tvSentence.setTextSize(16);
                tvSentence.setTextColor(dark ? 0xFFE0E0E0 : 0xFF1C1B1F);
                tvSentence.setGravity(Gravity.CENTER);
                tvSentence.setLineSpacing(0, 1.3f);
                tvSentence.setPadding(16 * density, 16 * density, 16 * density, 16 * density);
                tvSentence.setBackground(UikitUi.getPillBackground(this));
                
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.bottomMargin = 12 * density;
                tvSentence.setLayoutParams(lp);
                
                textContainer.addView(tvSentence);
            }
            UikitUi.setupWheelScroll(scroll, textContainer);
        } else {
            TextView tvAllText = new TextView(this);
            tvAllText.setText(message);
            tvAllText.setTextSize(16);
            tvAllText.setTextColor(dark ? 0xFFE0E0E0 : 0xFF1C1B1F);
            tvAllText.setGravity(Gravity.CENTER);
            tvAllText.setLineSpacing(0, 1.5f);
            tvAllText.setPadding(16 * density, 16 * density, 16 * density, 16 * density);
            tvAllText.setBackground(UikitUi.getPillBackground(this));
            
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvAllText.setLayoutParams(lp);
            textContainer.addView(tvAllText);
        }

        // 3. 底部按钮
        UikitButton btnOk = new UikitButton(this);
        btnOk.setText(posText);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 48 * density);
        btnOk.setLayoutParams(btnLp);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, 0);
            }
        });
        root.addView(btnOk);
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (UikitUi.handleSwipeBack(this, ev)) return true;
        return super.dispatchTouchEvent(ev);
    }
}

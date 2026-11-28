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
import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Looper;

public class UikitFullTextActivity extends Activity {
    public static final String EXTRA_TITLE = "uikit_title";
    public static final String EXTRA_MESSAGE = "uikit_message";
    public static final String EXTRA_POSITIVE_TEXT = "uikit_positive_text";
    public static final String EXTRA_AUTO_SPLIT = "uikit_auto_split";
    public static final String EXTRA_LIVE = "uikit_live";

    // 当前处于实时刷新模式的文本页实例（弱引用，避免 Activity 泄漏）
    private static WeakReference<UikitFullTextActivity> sLiveRef;
    private TextView tvBody;
    private ScrollView mScroll;
    private boolean liveHasContent;
    // 低性能模式的日志合批缓冲：先攒起来，500ms 落地一次，避免每条日志都触发一次全文 setText + 滚动重排
    private final StringBuilder livePending = new StringBuilder();
    private final Handler liveHandler = new Handler(Looper.getMainLooper());
    private Runnable liveFlush;

    public static void show(Activity activity, String title, String message, String positiveText, int requestCode) {
        show(activity, title, message, positiveText, false, false, requestCode);
    }

    public static void show(Activity activity, String title, String message, String positiveText, boolean autoSplit, int requestCode) {
        show(activity, title, message, positiveText, autoSplit, false, requestCode);
    }

    public static void show(Activity activity, String title, String message, String positiveText,
                             boolean autoSplit, boolean live, int requestCode) {
        Intent intent = new Intent(activity, UikitFullTextActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtra(EXTRA_POSITIVE_TEXT, positiveText);
        intent.putExtra(EXTRA_AUTO_SPLIT, autoSplit);
        intent.putExtra(EXTRA_LIVE, live);
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
        boolean live = getIntent().getBooleanExtra(EXTRA_LIVE, false);

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
        mScroll = scroll;
        if (live) sLiveRef = new WeakReference<>(this);

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
        } else {
            boolean empty = message == null || message.trim().isEmpty();
            if (empty) message = org.watchsend.util.I18nManager.get("log_empty");
            liveHasContent = !empty;

            TextView tvAllText = new TextView(this);
            tvAllText.setText(message);
            if (live) {
                // 日志页：等宽字体、左对齐、小字号，便于逐行阅读
                tvAllText.setTypeface(android.graphics.Typeface.MONOSPACE);
                tvAllText.setTextSize(12);
                tvAllText.setGravity(Gravity.START | Gravity.TOP);
                tvAllText.setTextColor(dark ? 0xFFAAAAAA : 0xFF333333);
                tvAllText.setBackgroundColor(dark ? 0xFF1E1E1E : 0xFFF5F5F5);
            } else {
                tvAllText.setTextSize(16);
                tvAllText.setTextColor(dark ? 0xFFE0E0E0 : 0xFF1C1B1F);
                tvAllText.setGravity(Gravity.CENTER);
                tvAllText.setBackground(UikitUi.getPillBackground(this));
            }
            tvAllText.setLineSpacing(0, 1.5f);
            tvAllText.setPadding(16 * density, 16 * density, 16 * density, 16 * density);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvAllText.setLayoutParams(lp);
            textContainer.addView(tvAllText);
            tvBody = tvAllText;
        }

        // 无论是否智能断句，统一挂载圆屏滚轮 / 回弹效果（作用于整个内容容器，与列表页一致）
        UikitUi.setupWheelScroll(scroll, root);

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

    /** 实时模式下追加一行文本并自动滚动到底部 */
    public void appendLive(String line) {
        if (tvBody == null || mScroll == null) return;
        if (UikitConfig.isLowPerf(this)) {
            livePending.append(line);
            if (liveFlush == null) {
                liveFlush = new Runnable() {
                    @Override public void run() {
                        liveFlush = null;
                        flushLive();
                    }
                };
                liveHandler.postDelayed(liveFlush, 500);
            }
            return;
        }
        if (!liveHasContent) { tvBody.setText(line); liveHasContent = true; }
        else tvBody.setText(tvBody.getText() + line);
        mScroll.post(new Runnable() {
            @Override public void run() { mScroll.fullScroll(ScrollView.FOCUS_DOWN); }
        });
    }

    /** 把合批缓冲一次性写进正文（低性能模式专用），并只滚动一次。 */
    private void flushLive() {
        if (tvBody == null || mScroll == null) return;
        if (livePending.length() == 0) return;
        String chunk = livePending.toString();
        livePending.setLength(0);
        if (!liveHasContent) { tvBody.setText(chunk); liveHasContent = true; }
        else tvBody.setText(tvBody.getText() + chunk);
        final ScrollView sv = mScroll;
        sv.post(new Runnable() {
            @Override public void run() { sv.fullScroll(ScrollView.FOCUS_DOWN); }
        });
    }

    /** 获取当前处于实时刷新模式的文本页实例，不存在则返回 null */
    public static UikitFullTextActivity getLiveInstance() {
        return sLiveRef == null ? null : sLiveRef.get();
    }

    @Override
    protected void onDestroy() {
        if (liveFlush != null) { liveHandler.removeCallbacks(liveFlush); liveFlush = null; }
        if (sLiveRef != null && sLiveRef.get() == this) sLiveRef.clear();
        super.onDestroy();
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

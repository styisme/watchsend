package org.watchsend;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.*;

import org.watchsend.model.Device;
import org.watchsend.transfer.Uploader;
import org.watchsend.util.I18nManager;
import org.watchsend.util.ThemeUtils;
import org.watchsend.watch.WatchUiUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransferActivity extends Activity {

    public static final String ACTION_UPDATE_PROGRESS = "org.watchsend.UPDATE_PROGRESS";
    public static final String ACTION_TRANSFER_DONE = "org.watchsend.TRANSFER_DONE";

    private int mode;
    private Device targetDevice;
    private ArrayList<String> filePaths;
    private long totalSize = 0;
    private long startTime;
    private boolean isDone = false;
    private Handler handler = new Handler();
    
    private boolean isDark;
    private int colorMain, colorSec, colorAccent;
    private LinearLayout container;

    private TextView tvFileName, tvFileIcon, tvFileStatus, tvPercent, tvSpeedSize;
    private ProgressBar pbFile, pbTotal;
    private Button btnAccept, btnReject, btnCancel, btnDone;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isDone) return;
            if (ACTION_UPDATE_PROGRESS.equals(intent.getAction())) {
                long current = intent.getLongExtra("current", 0);
                long total = intent.getLongExtra("total", 1);
                String fName = intent.getStringExtra("fileName");
                updateProgress(current, total, fName);
            } else if (ACTION_TRANSFER_DONE.equals(intent.getAction())) {
                boolean ok = intent.getBooleanExtra("ok", false);
                transferDone(ok);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WatchUiUtils.applyScale(this);
        super.onCreate(savedInstanceState);
        I18nManager.init(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_transfer);
        ThemeUtils.applyTheme(this);
        WatchUiUtils.applyEntryAnimation(this);

        isDark = ThemeUtils.isDarkMode(this);
        colorMain = isDark ? 0xFFE0E0E0 : 0xFF1C1B1F;
        colorSec = isDark ? 0xFFAAAAAA : 0xFF666666;
        colorAccent = isDark ? 0xFF81C784 : 0xFF1B5E20;

        View root = findViewById(R.id.root_transfer);
        root.setBackgroundColor(isDark ? 0xFF000000 : 0xFFF4F6F8);
        
        // 应用圆屏边距（会覆盖XML里的默认16dp）
        WatchUiUtils.applyRoundPadding(this, root);

        container = (LinearLayout) findViewById(R.id.transfer_container);
        ScrollView scrollView = (ScrollView) findViewById(R.id.transfer_scroll);
        
        // 初始化滚轮特效
        WatchUiUtils.setupWheelScroll(scrollView, container);

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        mode = intent.getIntExtra("mode", 0);
        isDone = false;
        container.removeAllViews();

        if (mode == 1) {
            buildReceiveRequestUI(intent);
        } else {
            buildTransferProgressUI(intent);
        }
    }

    // ================== UI 构建 ==================

    private void buildReceiveRequestUI(Intent intent) {
        String senderName = intent.getStringExtra("senderName");
        if (senderName == null) senderName = I18nManager.get("unknown_device");

        addMainTitle("接收请求");
        addSection("来自设备");
        
        LinearLayout card = createPillCard();
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        
        TextView tvName = new TextView(this);
        tvName.setText(senderName);
        tvName.setTextColor(colorMain);
        tvName.setTextSize(16);
        tvName.setTypeface(null, Typeface.BOLD);
        tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvName);
        
        TextView icon = new TextView(this);
        icon.setText("📱");
        icon.setTextSize(24);
        row.addView(icon);
        
        card.addView(row);
        
        TextView desc = new TextView(this);
        desc.setText("想要发送文件给你");
        desc.setTextColor(colorSec);
        desc.setTextSize(13);
        desc.setPadding(0, dp(8), 0, 0);
        card.addView(desc);
        container.addView(card);

        addSection("操作");
        
        btnAccept = createButton("✔ 接受", 0xFF388E3C, 0xFFFFFFFF);
        LinearLayout.LayoutParams accLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        accLp.bottomMargin = dp(8);
        btnAccept.setLayoutParams(accLp);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ReceiveManager.pendingCallback != null) { ReceiveManager.pendingCallback.onResult(true); ReceiveManager.pendingCallback = null; }
                Intent i = getIntent();
                i.putExtra("mode", 2);
                setIntent(i);
                handleIntent(i);
            }
        });
        container.addView(btnAccept);

        btnReject = createButton("✕ 拒绝", 0xFFD32F2F, 0xFFFFFFFF);
        LinearLayout.LayoutParams rejLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        rejLp.bottomMargin = dp(8);
        btnReject.setLayoutParams(rejLp);
        btnReject.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ReceiveManager.pendingCallback != null) { ReceiveManager.pendingCallback.onResult(false); ReceiveManager.pendingCallback = null; }
                finish();
            }
        });
        container.addView(btnReject);
    }

    private void buildTransferProgressUI(Intent intent) {
        if (mode == 2) {
            addMainTitle("正在接收");
        } else {
            addMainTitle("正在发送");
        }

        targetDevice = (Device) intent.getSerializableExtra("device");
        filePaths = intent.getStringArrayListExtra("files");

        addSection("当前文件");
        
        LinearLayout fileCard = createPillCard();
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        tvFileName = new TextView(this);
        tvFileName.setText("准备中...");
        tvFileName.setTextColor(colorMain);
        tvFileName.setTextSize(15);
        tvFileName.setSingleLine(true);
        tvFileName.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);
        tvFileName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        topRow.addView(tvFileName);

        tvFileIcon = new TextView(this);
        tvFileIcon.setText("📄");
        tvFileIcon.setTextSize(24);
        topRow.addView(tvFileIcon);
        fileCard.addView(topRow);

        tvFileStatus = new TextView(this);
        tvFileStatus.setText("传输中...");
        tvFileStatus.setTextColor(colorAccent);
        tvFileStatus.setTextSize(12);
        tvFileStatus.setPadding(0, dp(8), 0, dp(8));
        fileCard.addView(tvFileStatus);

        pbFile = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pbFile.setMax(100);
        pbFile.setProgressDrawable(createProgressDrawable());
        LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(6));
        pbLp.topMargin = dp(8);
        pbFile.setLayoutParams(pbLp);
        fileCard.addView(pbFile);
        container.addView(fileCard);

        addSection("总进度");
        
        LinearLayout totalCard = createPillCard();
        LinearLayout dataRow = new LinearLayout(this);
        dataRow.setOrientation(LinearLayout.HORIZONTAL);
        dataRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout infoCol = new LinearLayout(this);
        infoCol.setOrientation(LinearLayout.VERTICAL);
        infoCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        tvSpeedSize = new TextView(this);
        tvSpeedSize.setText("计算中...");
        tvSpeedSize.setTextColor(colorSec);
        tvSpeedSize.setTextSize(12);
        tvSpeedSize.setLineSpacing(0, 1.2f);
        infoCol.addView(tvSpeedSize);
        dataRow.addView(infoCol);

        tvPercent = new TextView(this);
        tvPercent.setText("0%");
        tvPercent.setTextColor(colorAccent);
        tvPercent.setTextSize(24);
        tvPercent.setTypeface(null, Typeface.BOLD);
        dataRow.addView(tvPercent);
        
        totalCard.addView(dataRow);

        pbTotal = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pbTotal.setMax(100);
        pbTotal.setProgressDrawable(createProgressDrawable());
        LinearLayout.LayoutParams totLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(6));
        totLp.topMargin = dp(12);
        pbTotal.setLayoutParams(totLp);
        totalCard.addView(pbTotal);
        
        container.addView(totalCard);

        addSection("操作");
        
        btnCancel = createButton("✕ 取消", isDark ? 0xFF2C2C2E : 0xFFEEEEEE, 0xFFD32F2F);
        LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        cancelLp.bottomMargin = dp(8);
        btnCancel.setLayoutParams(cancelLp);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });
        container.addView(btnCancel);

        btnDone = createButton("✔ 完成", 0xFF388E3C, 0xFFFFFFFF);
        LinearLayout.LayoutParams doneLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        doneLp.bottomMargin = dp(8);
        btnDone.setLayoutParams(doneLp);
        btnDone.setVisibility(View.GONE);
        btnDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });
        container.addView(btnDone);

        if (mode != 2 && filePaths != null && !filePaths.isEmpty()) {
            tvFileIcon.setText(getIconForFile(new File(filePaths.get(0)).getName()));
            startUpload();
        }
        startTime = System.currentTimeMillis();
    }

    // ================== 辅助方法 ==================

    private void addMainTitle(String title) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(24);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(colorMain);
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(16);
        lp.bottomMargin = dp(16);
        tv.setLayoutParams(lp);
        container.addView(tv);
    }

    private void addSection(String title) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(13);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(colorSec);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = dp(8);
        lp.topMargin = dp(12);
        lp.bottomMargin = dp(8);
        tv.setLayoutParams(lp);
        container.addView(tv);
    }

    private LinearLayout createPillCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(WatchUiUtils.getPillBackground(this));
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        card.setLayoutParams(lp);
        return card;
    }

    private Button createButton(String text, int bgColor, int textColor) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(textColor);
        btn.setTextSize(15);
        btn.setMinHeight(0);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(24));
        bg.setColor(bgColor);
        btn.setBackground(bg);
        return btn;
    }

    private Drawable createProgressDrawable() {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(3));
        bg.setColor(isDark ? 0xFF3E3E3E : 0xFFE0E0E0);
        bg.setSize(0, dp(6));

        GradientDrawable progress = new GradientDrawable();
        progress.setCornerRadius(dp(3));
        progress.setColor(0xFF388E3C);
        progress.setSize(0, dp(6));

        ClipDrawable clip = new ClipDrawable(progress, Gravity.LEFT, ClipDrawable.HORIZONTAL);
        LayerDrawable ld = new LayerDrawable(new Drawable[]{bg, clip});
        ld.setId(0, android.R.id.background);
        ld.setId(1, android.R.id.progress);
        return ld;
    }

    private int dp(float val) { return (int)(val * getResources().getDisplayMetrics().density); }

    // ================== 业务逻辑 ==================

    private String getIconForFile(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".flac")) return "🎵";
        if (lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".avi")) return "🎬";
        if (lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".jpeg")) return "🖼️";
        if (lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z")) return "📦";
        if (lower.endsWith(".apk")) return "🤖";
        if (lower.endsWith(".pdf")) return "📕";
        if (lower.endsWith(".txt") || lower.endsWith(".md")) return "📝";
        return "📄";
    }

    private void startUpload() {
        if (filePaths == null || filePaths.isEmpty()) return;
        
        final List<File> files = new ArrayList<File>();
        for (String path : filePaths) {
            files.add(new File(path));
            totalSize += new File(path).length();
        }
        
        new Thread(new Runnable() {
            public void run() {
                try {
                    Uploader.uploadFiles(targetDevice, files, new Uploader.ProgressCallback() {
                        public void onProgress(final String stage, final long sent, final long total) {
                            handler.post(new Runnable() { public void run() {
                                if (!isDone) {
                                    long accumulated = 0;
                                    String currentName = files.get(0).getName();
                                    for (File f : files) {
                                        accumulated += f.length();
                                        if (sent <= accumulated) {
                                            currentName = f.getName();
                                            break;
                                        }
                                    }
                                    updateProgress(sent, total, currentName);
                                }
                            }});
                        }
                        public void onDone(final boolean ok, final String msg) {
                            handler.post(new Runnable() { public void run() { 
                                if (!isDone) transferDone(ok); 
                            }});
                        }
                    });
                } catch (Exception e) {
                    final String errMsg = e.getMessage();
                    handler.post(new Runnable() { public void run() {
                        Toast.makeText(TransferActivity.this, "Error: " + errMsg, Toast.LENGTH_SHORT).show();
                        finish();
                    }});
                }
            }
        }).start();
    }

    private void updateProgress(long current, long total, String fileName) {
        if (isDone) return;
        long elapsed = System.currentTimeMillis() - startTime;
        double speed = current / (elapsed / 1000.0 + 0.1);
        String speedStr = speed > 1024 * 1024 ? String.format(Locale.US, "%.1f MB/s", speed / 1024 / 1024) : String.format(Locale.US, "%.1f KB/s", speed / 1024);
        
        tvFileIcon.setText(getIconForFile(fileName));
        tvFileName.setText(fileName);
        
        if (total > 0) {
            int pct = (int) (current * 100 / total);
            if (pct > 100) pct = 100;
            pbTotal.setProgress(pct);
            pbFile.setProgress(pct);
            
            tvPercent.setText(pct + "%");
            tvSpeedSize.setText("大小: " + formatSize(current) + " / " + formatSize(total) + "\n速度: " + speedStr);
        }
    }

    private void transferDone(boolean ok) {
        if (isDone) return;
        isDone = true;

        try {
            android.os.Vibrator v = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) v.vibrate(100);
        } catch (Exception ignored) {}

        btnCancel.setVisibility(View.GONE);
        btnDone.setVisibility(View.VISIBLE);
        
        if (ok) {
            addMainTitle(mode == 1 || mode == 2 ? "接收完成" : "发送完成");
            tvFileStatus.setText(mode == 1 || mode == 2 ? "已保存到本地" : "传输成功");
            tvFileStatus.setTextColor(0xFF388E3C);
            pbTotal.setProgress(100);
            pbFile.setProgress(100);
        } else {
            addMainTitle("传输失败");
            tvFileStatus.setText("已中断");
            tvFileStatus.setTextColor(0xFFD32F2F);
        }
    }

    private String formatSize(long size) {
        if (size > 1024 * 1024) return String.format(Locale.US, "%.1f MB", size / 1024.0 / 1024.0);
        return String.format(Locale.US, "%.1f KB", size / 1024.0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE_PROGRESS);
        filter.addAction(ACTION_TRANSFER_DONE);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (WatchUiUtils.handleSwipeBack(this, ev)) return true;
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (WatchUiUtils.isSystemBackBlocked(this)) {
            Toast.makeText(this, "请滑动屏幕返回", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }
}

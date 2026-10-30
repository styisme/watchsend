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
    private int colorMain, colorSec, colorAccent, bgSec;
    private int dp(float val) { return (int)(val * getResources().getDisplayMetrics().density); }

    // UI 控件
    private LinearLayout layoutConfirm, layoutProgress;
    private TextView tvSenderName, tvTitle, tvSaveDir, tvFileName, tvTotalTitle, tvSpeedInfo, tvFileIcon, tvFileStatus;
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
        bgSec = isDark ? R.drawable.watch_btn_secondary_dark : R.drawable.watch_btn_secondary;

        View root = findViewById(R.id.root_transfer);
        root.setBackgroundColor(isDark ? 0xFF000000 : 0xFFF4F6F8);
        WatchUiUtils.applyRoundPadding(this, root);

        buildUI();
        handleIntent(getIntent());
    }

    private void buildUI() {
        FrameLayout root = (FrameLayout) findViewById(R.id.root_transfer);

        // ========== 接收确认界面 ==========
        layoutConfirm = new LinearLayout(this);
        layoutConfirm.setOrientation(LinearLayout.VERTICAL);
        layoutConfirm.setGravity(Gravity.CENTER);
        layoutConfirm.setPadding(dp(16), dp(16), dp(16), dp(16));
        layoutConfirm.setVisibility(View.GONE);
        
        TextView icon = new TextView(this);
        icon.setText("📱");
        icon.setTextSize(36);
        icon.setGravity(Gravity.CENTER);
        layoutConfirm.addView(icon);

        tvSenderName = new TextView(this);
        tvSenderName.setTextColor(colorMain);
        tvSenderName.setTextSize(18);
        tvSenderName.setTypeface(null, Typeface.BOLD);
        tvSenderName.setGravity(Gravity.CENTER);
        tvSenderName.setPadding(0, dp(8), 0, 0);
        layoutConfirm.addView(tvSenderName);

        TextView tvWant = new TextView(this);
        tvWant.setText("想要发送文件给你");
        tvWant.setTextColor(colorSec);
        tvWant.setTextSize(13);
        tvWant.setGravity(Gravity.CENTER);
        tvWant.setPadding(0, dp(8), 0, dp(24));
        layoutConfirm.addView(tvWant);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);
        
        btnReject = new Button(this);
        btnReject.setText("✕ 拒绝");
        btnReject.setTextColor(0xFFFFFFFF);
        btnReject.setTextSize(14);
        btnReject.setBackground(createRoundRect(0xFFD32F2F, 24));
        btnReject.setMinHeight(0);
        LinearLayout.LayoutParams rejLp = new LinearLayout.LayoutParams(0, dp(46), 1);
        rejLp.rightMargin = dp(8);
        btnReject.setLayoutParams(rejLp);
        btnRow.addView(btnReject);

        btnAccept = new Button(this);
        btnAccept.setText("✔ 接受");
        btnAccept.setTextColor(0xFFFFFFFF);
        btnAccept.setTextSize(14);
        btnAccept.setBackground(createRoundRect(0xFF388E3C, 24));
        btnAccept.setMinHeight(0);
        LinearLayout.LayoutParams accLp = new LinearLayout.LayoutParams(0, dp(46), 1);
        accLp.leftMargin = dp(8);
        btnAccept.setLayoutParams(accLp);
        btnRow.addView(btnAccept);

        layoutConfirm.addView(btnRow);
        root.addView(layoutConfirm);

        // ========== 进度界面 ==========
        layoutProgress = new LinearLayout(this);
        layoutProgress.setOrientation(LinearLayout.VERTICAL);
        layoutProgress.setGravity(Gravity.CENTER_HORIZONTAL);
        layoutProgress.setPadding(dp(16), dp(16), dp(16), dp(16));
        layoutProgress.setVisibility(View.GONE);

        tvTitle = new TextView(this);
        tvTitle.setTextColor(colorMain);
        tvTitle.setTextSize(16);
        tvTitle.setTypeface(null, Typeface.BOLD);
        layoutProgress.addView(tvTitle);

        tvSaveDir = new TextView(this);
        tvSaveDir.setTextColor(colorSec);
        tvSaveDir.setTextSize(11);
        tvSaveDir.setPadding(0, dp(4), 0, dp(16));
        tvSaveDir.setVisibility(View.GONE);
        layoutProgress.addView(tvSaveDir);

        // 当前文件卡片
        LinearLayout fileCard = new LinearLayout(this);
        fileCard.setOrientation(LinearLayout.VERTICAL);
        fileCard.setBackground(WatchUiUtils.getPillBackground(this));
        fileCard.setPadding(dp(16), dp(16), dp(16), dp(16));
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardLp.bottomMargin = dp(12);
        fileCard.setLayoutParams(cardLp);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        tvFileIcon = new TextView(this);
        tvFileIcon.setText("📄");
        tvFileIcon.setTextSize(24);
        tvFileIcon.setPadding(0, 0, dp(12), 0);
        topRow.addView(tvFileIcon);

        tvFileName = new TextView(this);
        tvFileName.setTextColor(colorMain);
        tvFileName.setTextSize(14);
        tvFileName.setSingleLine(true);
        tvFileName.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);
        topRow.addView(tvFileName);
        
        fileCard.addView(topRow);

        tvFileStatus = new TextView(this);
        tvFileStatus.setTextColor(colorAccent);
        tvFileStatus.setTextSize(11);
        tvFileStatus.setPadding(0, dp(8), 0, dp(8));
        tvFileStatus.setVisibility(View.GONE);
        fileCard.addView(tvFileStatus);

        pbFile = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pbFile.setMax(100);
        pbFile.setProgressDrawable(createProgressDrawable());
        LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(6));
        pbLp.topMargin = dp(8);
        pbFile.setLayoutParams(pbLp);
        fileCard.addView(pbFile);

        layoutProgress.addView(fileCard);

        // 总进度卡片
        LinearLayout totalCard = new LinearLayout(this);
        totalCard.setOrientation(LinearLayout.VERTICAL);
        totalCard.setBackground(WatchUiUtils.getPillBackground(this));
        totalCard.setPadding(dp(16), dp(16), dp(16), dp(16));
        totalCard.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        tvTotalTitle = new TextView(this);
        tvTotalTitle.setTextColor(colorMain);
        tvTotalTitle.setTextSize(13);
        totalCard.addView(tvTotalTitle);

        pbTotal = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pbTotal.setMax(100);
        pbTotal.setProgressDrawable(createProgressDrawable());
        LinearLayout.LayoutParams totLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(6));
        totLp.topMargin = dp(8);
        pbTotal.setLayoutParams(totLp);
        totalCard.addView(pbTotal);

        tvSpeedInfo = new TextView(this);
        tvSpeedInfo.setTextColor(colorSec);
        tvSpeedInfo.setTextSize(11);
        tvSpeedInfo.setLineSpacing(0, 1.2f);
        tvSpeedInfo.setPadding(0, dp(8), 0, 0);
        totalCard.addView(tvSpeedInfo);

        layoutProgress.addView(totalCard);

        // 底部操作按钮
        LinearLayout btnRow2 = new LinearLayout(this);
        btnRow2.setOrientation(LinearLayout.HORIZONTAL);
        btnRow2.setGravity(Gravity.END);
        btnRow2.setPadding(0, dp(16), 0, 0);
        
        btnCancel = new Button(this);
        btnCancel.setText("✕ 取消");
        btnCancel.setTextColor(0xFFD32F2F);
        btnCancel.setTextSize(13);
        btnCancel.setBackground(createRoundRect(0xFF2C2C2E, 20));
        btnCancel.setMinHeight(0);
        btnCancel.setPadding(dp(16), 0, dp(16), 0);
        btnRow2.addView(btnCancel);

        btnDone = new Button(this);
        btnDone.setText("✔ 完成");
        btnDone.setTextColor(0xFFFFFFFF);
        btnDone.setTextSize(13);
        btnDone.setBackground(createRoundRect(0xFF388E3C, 20));
        btnDone.setMinHeight(0);
        btnDone.setVisibility(View.GONE);
        LinearLayout.LayoutParams doneLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(40));
        doneLp.leftMargin = dp(12);
        btnDone.setLayoutParams(doneLp);
        btnRow2.addView(btnDone);

        layoutProgress.addView(btnRow2);
        root.addView(layoutProgress);
    }

    private GradientDrawable createRoundRect(int color, float radiusDp) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(radiusDp));
        bg.setColor(color);
        return bg;
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        mode = intent.getIntExtra("mode", 0);
        isDone = false;

        if (mode == 1) {
            // 接收请求
            String senderName = intent.getStringExtra("senderName");
            tvSenderName.setText(senderName != null ? senderName : I18nManager.get("unknown_device"));
            layoutConfirm.setVisibility(View.VISIBLE);
            layoutProgress.setVisibility(View.GONE);
            
            btnAccept.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (ReceiveManager.pendingCallback != null) { ReceiveManager.pendingCallback.onResult(true); ReceiveManager.pendingCallback = null; }
                    layoutConfirm.setVisibility(View.GONE);
                    layoutProgress.setVisibility(View.VISIBLE);
                    tvTitle.setText("正在接收文件");
                    tvSaveDir.setVisibility(View.VISIBLE);
                    tvSaveDir.setText("保存到: Download/WatchSend");
                    startTime = System.currentTimeMillis();
                }
            });
            
            btnReject.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (ReceiveManager.pendingCallback != null) { ReceiveManager.pendingCallback.onResult(false); ReceiveManager.pendingCallback = null; }
                    finish();
                }
            });
        } else {
            // 发送请求
            layoutConfirm.setVisibility(View.GONE);
            layoutProgress.setVisibility(View.VISIBLE);
            tvTitle.setText("正在发送文件");
            
            targetDevice = (Device) intent.getSerializableExtra("device");
            filePaths = intent.getStringArrayListExtra("files");
            
            if (filePaths != null && !filePaths.isEmpty()) {
                tvFileIcon.setText(getIconForFile(new File(filePaths.get(0)).getName()));
            }
            startUpload();
        }
        
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });
        btnDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });
    }

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
        
        startTime = System.currentTimeMillis();
        
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
                                if (!isDone) {
                                    transferDone(ok); 
                                }
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
            
            long remain = (long)((total - current) / (speed + 1));
            String timeStr = String.format(Locale.US, "%02d:%02d", remain / 60, remain % 60);
            
            tvTotalTitle.setText("总进度 (" + timeStr + ")");
            tvSpeedInfo.setText("大小: " + formatSize(current) + " / " + formatSize(total) + "\n速度: " + speedStr);
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
            tvTitle.setText(mode == 1 ? "接收完成" : "发送完成");
            tvTotalTitle.setText("已完成");
            pbTotal.setProgress(100);
            pbFile.setProgress(100);
            tvFileStatus.setVisibility(View.VISIBLE);
            
            if (mode == 1) {
                tvFileStatus.setText("已保存到相册/下载目录");
                tvSaveDir.setText("保存目录: Download/WatchSend");
            } else {
                tvFileStatus.setText("传输成功");
            }
            tvFileStatus.setTextColor(0xFF388E3C);
        } else {
            tvTitle.setText("传输失败");
            tvTotalTitle.setText("已中断");
            tvFileStatus.setText("失败");
            tvFileStatus.setTextColor(0xFFD32F2F);
            tvFileStatus.setVisibility(View.VISIBLE);
        }
    }

    private String formatSize(long size) {
        if (size > 1024 * 1024) return String.format(Locale.US, "%.1f MB", size / 1024.0 / 1024.0);
        return String.format(Locale.US, "%.1f KB", size / 1024.0);
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

package org.watchsend;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.watchsend.util.ThemeUtils;
import org.watchsend.util.I18nManager;
import org.watchsend.watch.WatchUiUtils;
import java.io.File;
import java.util.List;
import java.util.Locale;

public class SelectListActivity extends Activity {
    private LinearLayout listContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WatchUiUtils.applyScale(this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_select_list);
        ThemeUtils.applyTheme(this);
        WatchUiUtils.applyEntryAnimation(this); // 入场动画

        boolean isDark = ThemeUtils.isDarkMode(this);
        boolean isRoundOpt = getSharedPreferences("oldsend", MODE_PRIVATE).getBoolean("round_screen_opt", false);
        
        View root = findViewById(R.id.root_layout);
        root.setBackgroundColor(isDark ? 0xFF000000 : 0xFFF4F6F8);
        WatchUiUtils.applyRoundPadding(this, root);

        // 隐藏顶部栏
        View topBar = findViewById(R.id.top_bar);
        if (topBar != null) {
            topBar.setVisibility(isRoundOpt ? View.GONE : View.VISIBLE);
        }

        Button btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF1C1B1F);
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });

        TextView tvSltTitle = (TextView) findViewById(R.id.tv_slt_title);
        if (tvSltTitle != null) {
            tvSltTitle.setText(I18nManager.get("select_title"));
            tvSltTitle.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF1C1B1F);
        }

        listContainer = (LinearLayout) findViewById(R.id.list_container);
        ScrollView listScroll = (ScrollView) findViewById(R.id.list_scroll);
        WatchUiUtils.setupWheelScroll(listScroll, listContainer); // 开启滚轮特效

        refreshList();
    }

    private void refreshList() {
        listContainer.removeAllViews();
        List<Uri> uris = MainActivity.pendingUris;
        
        if (uris == null || uris.isEmpty()) {
            finish();
            return;
        }

        boolean isDark = ThemeUtils.isDarkMode(this);
        int colorMain = isDark ? 0xFFE0E0E0 : 0xFF1C1B1F;
        int colorSec = isDark ? 0xFFAAAAAA : 0xFF666666;
        int colorAccent = isDark ? 0xFF81C784 : 0xFF1B5E20;

        // ===== 顶部：文件数量与总大小 =====
        LinearLayout headerCard = new LinearLayout(this);
        headerCard.setOrientation(LinearLayout.HORIZONTAL);
        headerCard.setGravity(Gravity.CENTER_VERTICAL);
        headerCard.setPadding(dp(16), dp(12), dp(16), dp(12));
        headerCard.setBackground(WatchUiUtils.getPillBackground(this));
        LinearLayout.LayoutParams headLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headLp.bottomMargin = dp(8);
        headerCard.setLayoutParams(headLp);

        TextView tvCount = new TextView(this);
        tvCount.setText(uris.size() + " 个文件");
        tvCount.setTextSize(14);
        tvCount.setTextColor(colorMain);
        tvCount.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        headerCard.addView(tvCount);

        long totalSize = 0;
        ContentResolver resolver = getContentResolver();

        // ===== 循环：文件列表 =====
        for (int i = 0; i < uris.size(); i++) {
            final Uri uri = uris.get(i);
            final int index = i;
            
            String name = I18nManager.get("unknown_file");
            long size = 0;
            
            try {
                Cursor cursor = resolver.query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        String n = cursor.getString(nameIndex);
                        if (n != null) name = n;
                    }
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex);
                    }
                    cursor.close();
                }
            } catch (Exception ignored) {}
            
            if (size <= 0 && "file".equals(uri.getScheme())) {
                String path = uri.getPath();
                if (path != null) {
                    File f = new File(path);
                    if (f.exists()) {
                        if (I18nManager.get("unknown_file").equals(name)) name = f.getName();
                        size = f.length();
                    }
                }
            }
            totalSize += (size > 0 ? size : 0);

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(16), dp(12), dp(16), dp(12));
            card.setBackground(WatchUiUtils.getPillBackground(this));
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLp.bottomMargin = dp(8);
            card.setLayoutParams(cardLp);

            TextView tvIcon = new TextView(this);
            tvIcon.setTextSize(24);
            tvIcon.setGravity(Gravity.CENTER);
            String lowerName = name.toLowerCase();
            if (lowerName.endsWith(".jpg") || lowerName.endsWith(".png") || lowerName.endsWith(".jpeg")) tvIcon.setText("🖼️");
            else if (lowerName.endsWith(".mp4") || lowerName.endsWith(".mkv")) tvIcon.setText("🎬");
            else if (lowerName.endsWith(".mp3") || lowerName.endsWith(".wav")) tvIcon.setText("🎵");
            else if (lowerName.endsWith(".zip") || lowerName.endsWith(".rar")) tvIcon.setText("📦");
            else if (lowerName.endsWith(".apk")) tvIcon.setText("🤖");
            else tvIcon.setText("📄");
            tvIcon.setPadding(0, 0, dp(12), 0);
            card.addView(tvIcon);

            LinearLayout textContainer = new LinearLayout(this);
            textContainer.setOrientation(LinearLayout.VERTICAL);
            textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView itemName = new TextView(this);
            itemName.setText(name);
            itemName.setTextSize(14);
            itemName.setTextColor(colorMain);
            itemName.setSingleLine(true);
            itemName.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);
            textContainer.addView(itemName);

            TextView itemSize = new TextView(this);
            String sizeStr = size > 1024*1024 ? String.format(Locale.US, "%.1f MB", size/1024.0/1024.0) : String.format(Locale.US, "%.1f KB", size/1024.0);
            itemSize.setText(size > 0 ? sizeStr : I18nManager.get("unknown_size"));
            itemSize.setTextSize(12);
            itemSize.setTextColor(colorSec);
            itemSize.setPadding(0, dp(2), 0, 0);
            textContainer.addView(itemSize);

            card.addView(textContainer);

            TextView btnDelete = new TextView(this);
            btnDelete.setText("🗑");
            btnDelete.setTextSize(18);
            btnDelete.setPadding(dp(12), dp(8), 0, dp(8));
            btnDelete.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    MainActivity.pendingUris.remove(index);
                    refreshList();
                }
            });
            card.addView(btnDelete);
            listContainer.addView(card);
        }

        // 填完总大小后，更新顶部卡片
        String totalSizeStr = totalSize > 1024*1024 ? String.format(Locale.US, "%.1f MB", totalSize/1024.0/1024.0) : String.format(Locale.US, "%.1f KB", totalSize/1024.0);
        TextView tvTotal = new TextView(this);
        tvTotal.setText("共 " + (totalSize > 0 ? totalSizeStr : I18nManager.get("unknown_size")));
        tvTotal.setTextSize(14);
        tvTotal.setTextColor(colorSec);
        headerCard.addView(tvTotal);
        
        listContainer.addView(headerCard, 0); // 插在最前面

        // ===== 底部：清空全部按钮 =====
        Button btnClearAll = new Button(this);
        btnClearAll.setText("清空所有文件");
        btnClearAll.setTextColor(colorAccent);
        btnClearAll.setTextSize(14);
        int bgSec = isDark ? R.drawable.watch_btn_secondary_dark : R.drawable.watch_btn_secondary;
        btnClearAll.setBackgroundResource(bgSec);
        btnClearAll.setMinHeight(0);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        btnLp.topMargin = dp(16);
        btnLp.bottomMargin = dp(16);
        btnClearAll.setLayoutParams(btnLp);
        btnClearAll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.pendingUris.clear();
                Toast.makeText(SelectListActivity.this, I18nManager.get("all_deleted"), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        listContainer.addView(btnClearAll);

        // 新增：返回按钮
        Button btnReturn = new Button(this);
        btnReturn.setText(I18nManager.get("back_prev"));
        btnReturn.setTextColor(colorAccent);
        btnReturn.setTextSize(14);
        btnReturn.setBackgroundResource(bgSec);
        btnReturn.setMinHeight(0);
        LinearLayout.LayoutParams retLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        retLp.bottomMargin = dp(16);
        btnReturn.setLayoutParams(retLp);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });
        listContainer.addView(btnReturn);
    }
    
    private int dp(int val) { return (int)(val * getResources().getDisplayMetrics().density);
    }


    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (org.watchsend.watch.WatchUiUtils.handleSwipeBack(this, ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (org.watchsend.watch.WatchUiUtils.isSystemBackBlocked(this)) {
            android.widget.Toast.makeText(this, "请滑动屏幕返回", android.widget.Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }
}

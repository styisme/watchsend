package org.watchsend;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private TextView tvFileCount, tvTotalSize;
    private Button btnDeleteAll, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WatchUiUtils.applyScale(this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_select_list);
        ThemeUtils.applyTheme(this);

        boolean isDark = ThemeUtils.isDarkMode(this);
        
        // 1. 圆屏边距优化
        View root = findViewById(R.id.root_layout);
        root.setBackgroundColor(isDark ? 0xFF000000 : 0xFFF4F6F8);
        WatchUiUtils.applyRoundPadding(this, root);

        // 2. 绑定基础控件
        listContainer = (LinearLayout) findViewById(R.id.list_container);
        tvFileCount = (TextView) findViewById(R.id.tv_file_count);
        tvTotalSize = (TextView) findViewById(R.id.tv_total_size);
        btnDeleteAll = (Button) findViewById(R.id.btn_delete_all);
        btnBack = (Button) findViewById(R.id.btn_back);
        
        int colorMain = isDark ? 0xFFE0E0E0 : 0xFF1C1B1F;
        int colorSec = isDark ? 0xFFAAAAAA : 0xFF666666;
        int colorAccent = isDark ? 0xFF81C784 : 0xFF1B5E20;

        TextView tvSltTitle = (TextView) findViewById(R.id.tv_slt_title);
        if (tvSltTitle != null) {
            tvSltTitle.setText("编辑文件");
            tvSltTitle.setTextColor(colorMain);
        }
        
        btnBack.setTextColor(colorMain);
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });

        tvFileCount.setTextColor(colorMain);
        tvTotalSize.setTextColor(colorSec);

        int bgSec = isDark ? R.drawable.watch_btn_secondary_dark : R.drawable.watch_btn_secondary;
        btnDeleteAll.setBackgroundResource(bgSec);
        btnDeleteAll.setTextColor(colorAccent);

        btnDeleteAll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.pendingUris.clear();
                refreshList();
                Toast.makeText(SelectListActivity.this, "已全部清空", Toast.LENGTH_SHORT).show();
            }
        });

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

        long totalSize = 0;
        ContentResolver resolver = getContentResolver();

        for (int i = 0; i < uris.size(); i++) {
            final Uri uri = uris.get(i);
            final int index = i;
            
            String name = "未知文件";
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
                        if ("未知文件".equals(name)) name = f.getName();
                        size = f.length();
                    }
                }
            }

            totalSize += (size > 0 ? size : 0);
            String sizeStr = size > 1024*1024 ? String.format(Locale.US, "%.1f MB", size/1024.0/1024.0) : String.format(Locale.US, "%.1f KB", size/1024.0);

            // ====== 核心：纯代码绘制药丸卡片 ======
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(16), dp(12), dp(16), dp(12));
            // 使用之前的动态背景
            card.setBackground(WatchUiUtils.getPillBackground(this));
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLp.bottomMargin = dp(8);
            card.setLayoutParams(cardLp);

            // 左侧 Emoji 图标
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

            // 中间 名字和大小
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
            itemSize.setText(size > 0 ? sizeStr : "未知大小");
            itemSize.setTextSize(12);
            itemSize.setTextColor(colorSec);
            itemSize.setPadding(0, dp(2), 0, 0);
            textContainer.addView(itemSize);

            card.addView(textContainer);

            // 右侧 删除按钮 (用垃圾桶 emoji 最省事且兼容老系统)
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

        if (tvFileCount != null) tvFileCount.setText("文件: " + uris.size());
        String totalSizeStr = totalSize > 1024*1024 ? String.format(Locale.US, "%.1f MB", totalSize/1024.0/1024.0) : String.format(Locale.US, "%.1f KB", totalSize/1024.0);
        if (tvTotalSize != null) tvTotalSize.setText("大小: " + (totalSize > 0 ? totalSizeStr : "未知"));
    }
    
    private int dp(int val) { return (int)(val * getResources().getDisplayMetrics().density); }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (WatchUiUtils.handleSwipeBack(this, ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }
}

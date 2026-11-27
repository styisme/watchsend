package org.watchsend;

import android.app.Activity;
import org.watchsend.uikit.UikitFullTextActivity;
import org.watchsend.uikit.UikitFullListActivity;
import org.watchsend.uikit.UikitFullInputActivity;
import org.watchsend.uikit.UikitFullConfirmActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.watchsend.discovery.DeviceStore;
import org.watchsend.discovery.MulticastScanner;
import org.watchsend.model.Device;
import org.watchsend.model.FavoritesStore;
import org.watchsend.model.HistoryStore;
import org.watchsend.ui.DeviceAdapter;
import org.watchsend.util.I18nManager;
import org.watchsend.util.ThemeUtils;
import org.watchsend.watch.WatchUiUtils;
import org.watchsend.watch.WatchSettingsActivity;

public class MainActivity extends Activity implements OldSendService.LogSink, DeviceStore.Listener {
    private boolean currentAppendState = false;

    // ===== uikit 全屏容器回调所需的上下文（异步 onActivityResult 恢复用）=====
    private List<ApplicationInfo> pendingApps;
    private File pendingFolderDir;
    private List<File> pendingFolders;
    private String pendingAclFp;
    private boolean pendingAclIsWl;
    private boolean pendingAclIsBl;
    private String pendingDeviceAlias;
    private String pendingDeviceIp;
    private int pendingDevicePort;
    private File pendingHistoryFile;
    private boolean pendingHistoryIsZip;
    private int pendingHistoryPos;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(org.watchsend.watch.WatchUiUtils.wrapScale(newBase));
    }

    private static final int REQ_FILE = 1001;
    private static final int REQ_MEDIA = 1002;

    public static List<Uri> pendingUris = new ArrayList<Uri>();

    private TextView status, statusIp, emptyHint;
    private LinearLayout layoutSelectedFiles;
    private TextView tvSelectedFile;
    private Button toggleBtn, btnSend, btnHistory, btnCrossNet, btnSettings, btnLog, scanBtn, manualIpBtn, btnAddFilesMore, btnEditFiles;
    private ListView deviceList;
    private DeviceAdapter adapter;
    private ArrayAdapter<String> historyAdapter;
    private final List<String> historyPaths = new ArrayList<String>();
    private Handler handler = new Handler();
    private boolean running = false;
    private boolean pendingAppend = false;
    private boolean historyMode = false;
    private StringBuilder logCache = new StringBuilder();
    
    private String lastLang;
    private boolean lastDark;
    private float lastScale;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        I18nManager.init(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ThemeUtils.applyTheme(this);
        WatchUiUtils.applyEntryAnimation(this);

        boolean isDark = ThemeUtils.isDarkMode(this);
        View root = findViewById(R.id.main_root);
        if (root != null) {
            root.setBackgroundColor(isDark ? 0xFF000000 : 0xFFF4F6F8);
            WatchUiUtils.applyRoundPadding(this, root);
        }
        
        ScrollView mainScroll = (ScrollView) findViewById(R.id.main_scroll);
        LinearLayout mainContainer = (LinearLayout) findViewById(R.id.main_container);
        if (mainScroll != null && mainContainer != null) {
            WatchUiUtils.setupWheelScroll(mainScroll, mainContainer);
            org.watchsend.watch.WatchHardwareUtils.bindRotaryEncoder(mainScroll);
        }

        View statusCard = findViewById(R.id.status_card);
        if (statusCard != null) {
            statusCard.setBackgroundResource(isDark ? R.drawable.watch_card_dark : R.drawable.watch_card);
        }

        status = (TextView) findViewById(R.id.status);
        statusIp = (TextView) findViewById(R.id.status_ip);
        
        // 强制动态颜色，防止被 ThemeUtils 的白底白字逻辑误伤
        if (status != null) status.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF1C1B1F);
        if (statusIp != null) statusIp.setTextColor(isDark ? 0xFFAAAAAA : 0xFF666666);
        statusIp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (running) {
                    String ip = OldSendService.getLocalIp();
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("WatchSend", "http://" + ip + ":53317"));
                    toast(I18nManager.get("copied_hint"));
                    org.watchsend.watch.WatchHardwareUtils.vibrate(MainActivity.this, org.watchsend.watch.WatchHardwareUtils.HAPTIC_LIGHT);
                }
            }
        });
        statusIp.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (running) showBigUrlDialog();
                return true;
            }
        });
        emptyHint = (TextView) findViewById(R.id.empty_hint);
        if (emptyHint != null) {
            // 向上找父容器 FrameLayout，强制替换背景
            android.view.View parent = (android.view.View) emptyHint.getParent();
            if (parent != null) {
                parent.setBackgroundResource(isDark ? R.drawable.watch_card_dark : R.drawable.watch_card);
            }
            emptyHint.setTextColor(isDark ? 0xFFAAAAAA : 0xFF999999);
        }
        toggleBtn = (Button) findViewById(R.id.toggle);
        btnSend = (Button) findViewById(R.id.btn_send);
        btnHistory = (Button) findViewById(R.id.btn_history);
        btnCrossNet = (Button) findViewById(R.id.btn_cross_net);
        btnSettings = (Button) findViewById(R.id.btn_settings);
        btnLog = (Button) findViewById(R.id.btn_log);
        scanBtn = (Button) findViewById(R.id.scan);
        manualIpBtn = (Button) findViewById(R.id.btn_manual_ip);
        deviceList = (ListView) findViewById(R.id.devices);
        
        layoutSelectedFiles = (LinearLayout) findViewById(R.id.layout_selected_files);
        if (layoutSelectedFiles != null && isDark) {
            layoutSelectedFiles.setBackgroundResource(R.drawable.watch_card_dark);
        }
        tvSelectedFile = (TextView) findViewById(R.id.tv_selected_file);
        if (tvSelectedFile != null && isDark) {
            tvSelectedFile.setTextColor(0xFFE0E0E0);
        }
        btnAddFilesMore = (Button) findViewById(R.id.btn_add_files_more);
        btnEditFiles = (Button) findViewById(R.id.btn_edit_files);

        if (btnEditFiles != null) {
            btnEditFiles.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { startActivity(new Intent(MainActivity.this, SelectListActivity.class)); }
            });
        }
        if (btnAddFilesMore != null) {
            btnAddFilesMore.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { showSendTypeDialog(true); }
            });
        }

        adapter = new DeviceAdapter(this);
        historyAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        deviceList.setAdapter(adapter);

        OldSendService.setSink(this);
        DeviceStore.get().setListener(this);

        toggleBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { org.watchsend.watch.WatchHardwareUtils.vibrate(MainActivity.this, org.watchsend.watch.WatchHardwareUtils.HAPTIC_LIGHT); toggleService(); }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { org.watchsend.watch.WatchHardwareUtils.vibrate(MainActivity.this, org.watchsend.watch.WatchHardwareUtils.HAPTIC_LIGHT); showSendTypeDialog(false); }
        });
        btnHistory.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { toggleHistory(); }
        });
        btnCrossNet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { org.watchsend.watch.WatchHardwareUtils.vibrate(MainActivity.this, org.watchsend.watch.WatchHardwareUtils.HAPTIC_LIGHT); startActivity(new Intent(MainActivity.this, CrossNetActivity.class)); }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { startActivity(new Intent(MainActivity.this, WatchSettingsActivity.class)); }
        });
        btnLog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { showLogDialog(); }
        });
        scanBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { doScan(); }
        });
        manualIpBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { showManualIpDialog(); }
        });

        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                if (historyMode) { openHistoryFile(pos); return; }
                Device target = adapter.getItem(pos);
                if (pendingUris.isEmpty()) { toast(I18nManager.get("not_selected")); return; }
                log("Sending to: " + target.alias);
                sendToDevice(target);
            }
        });
        deviceList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
                if (historyMode) {
                    showHistoryMenu(pos);
                    return true;
                }
                // 长按进入设备详情（对齐官方 device_details_page）：信息、收藏、备注、白/黑名单都在里面
                org.watchsend.watch.DeviceDetailActivity.show(MainActivity.this, adapter.getItem(pos), 5020);
                return true;
            }
        });


        // 强制应用 MD3 动态色到主操作按钮
        int primaryColor = ThemeUtils.getPrimaryColor(this);
        if (primaryColor != 0xFF388E3C) { // 如果取到了非默认颜色
            if (toggleBtn != null) {
                android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
                bg.setCornerRadius((int)(26 * getResources().getDisplayMetrics().density));
                bg.setColor(primaryColor);
                toggleBtn.setBackground(bg);
            }
        }

        updateStatus();
        applyI18n();
        checkAndRequestPermission();
        handleIntent(getIntent());
        recordConfigState();
        if (getIntent() != null && getIntent().getBooleanExtra("auto_scan", false)) doScan();
    }

    private void recordConfigState() {
        android.content.SharedPreferences sp = getSharedPreferences("oldsend", MODE_PRIVATE);
        lastLang = sp.getString("app_lang", "system");
        lastDark = sp.getBoolean("dark_mode", false);
        lastScale = sp.getFloat("ui_scale", 1.0f);
    }

    private void applyI18n() {
        boolean isDark = ThemeUtils.isDarkMode(this);
        if (btnSend != null) btnSend.setText(I18nManager.get("send"));
        int txtColor = isDark ? 0xFF81C784 : 0xFF1B5E20;
        int bgRes = isDark ? R.drawable.watch_btn_secondary_dark : R.drawable.watch_btn_secondary;
        
        if (btnHistory != null) { btnHistory.setTextColor(txtColor); btnHistory.setBackgroundResource(bgRes); }
        if (btnCrossNet != null) { btnCrossNet.setTextColor(txtColor); btnCrossNet.setBackgroundResource(bgRes); }
        if (btnSettings != null) { btnSettings.setTextColor(txtColor); btnSettings.setBackgroundResource(bgRes); }
        if (scanBtn != null) { scanBtn.setTextColor(txtColor); scanBtn.setBackgroundResource(bgRes); }
        if (manualIpBtn != null) { manualIpBtn.setTextColor(txtColor); manualIpBtn.setBackgroundResource(bgRes); }
        if (btnLog != null) { btnLog.setTextColor(isDark ? 0xFFAAAAAA : 0xFF666666); }
        if (btnHistory != null) btnHistory.setText(I18nManager.get("record"));
        if (btnCrossNet != null) btnCrossNet.setText(I18nManager.get("cross_net"));
        if (btnSettings != null) btnSettings.setText(I18nManager.get("settings"));
        if (btnLog != null) btnLog.setText(I18nManager.get("log"));
        if (scanBtn != null) scanBtn.setText(I18nManager.get("scan"));
        if (manualIpBtn != null) manualIpBtn.setText("+ " + I18nManager.get("add_ip"));
        if (emptyHint != null) emptyHint.setText(I18nManager.get("no_device_scan"));
        if (toggleBtn != null) toggleBtn.setText(running ? I18nManager.get("stop_service") : I18nManager.get("start_service"));
        if (btnAddFilesMore != null) btnAddFilesMore.setText(I18nManager.get("add"));
        if (btnEditFiles != null) btnEditFiles.setText(I18nManager.get("edit"));
        
        updateStatus();
    }

    private void updateStatus() {
        if (status == null) return;
        String ip = OldSendService.getLocalIp();
        if (ip != null && ip.length() > 15) ip = ip.substring(0, 12) + "...";
        status.setText(running ? I18nManager.get("running") : I18nManager.get("stopped"));
        statusIp.setText(I18nManager.get("ip_prefix") + ip + ":53317");
    }

    private void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        }
    }

    private void toggleService() {
        if (running) { stopService(new Intent(this, OldSendService.class)); running = false; }
        else { startService(new Intent(this, OldSendService.class)); running = true; }

        // 强制应用 MD3 动态色到主操作按钮
        int primaryColor = ThemeUtils.getPrimaryColor(this);
        if (primaryColor != 0xFF388E3C) { // 如果取到了非默认颜色
            if (toggleBtn != null) {
                android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
                bg.setCornerRadius((int)(26 * getResources().getDisplayMetrics().density));
                bg.setColor(primaryColor);
                toggleBtn.setBackground(bg);
            }
        }

        updateStatus();
        applyI18n();
    }

    private void showSendTypeDialog(final boolean isAppend) {
        currentAppendState = isAppend;
        final String[] items = {
            I18nManager.get("file"), I18nManager.get("media"), I18nManager.get("app"),
            I18nManager.get("folder"), I18nManager.get("text"), I18nManager.get("clipboard")
        };
        UikitFullListActivity.show(this, I18nManager.get("send_file_title"), items, 5001);
    }

    private void pickFile(boolean isMedia, boolean append) {
        pendingAppend = append;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        if (isMedia) i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*","video/*","audio/*"});
        startActivityForResult(Intent.createChooser(i, "Select"), isMedia ? REQ_MEDIA : REQ_FILE);
    }

    private void pickApp(boolean append) {
        pendingAppend = append;
        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        final List<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();
        List<String> names = new ArrayList<String>();
        for (PackageInfo pkg : packages) {
            ApplicationInfo app = pkg.applicationInfo;
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                apps.add(app); names.add(app.loadLabel(pm).toString());
            }
        }
        if (names.isEmpty()) { toast(I18nManager.get("no_app")); return; }
        pendingApps = apps;
        UikitFullListActivity.show(this, I18nManager.get("select_app"), names.toArray(new String[0]), 5010);
    }

    private void pickFolder(boolean append) {
        pendingAppend = append;
        showFolderSelector(android.os.Environment.getExternalStorageDirectory());
    }

    private void showFolderSelector(final File dir) {
        final File[] files = dir.listFiles();
        if (files == null) return;
        List<String> folderNames = new ArrayList<String>();
        final List<File> folders = new ArrayList<File>();
        folderNames.add(I18nManager.get("select_folder_send"));
        for (File f : files) {
            if (f.isDirectory() && !f.isHidden()) {
                folderNames.add(I18nManager.get("folder_prefix") + f.getName());
                folders.add(f);
            }
        }
        pendingFolderDir = dir;
        pendingFolders = folders;
        UikitFullListActivity.show(this, dir.getName().isEmpty() ? "/" : dir.getName(), folderNames.toArray(new String[0]), 5011);
    }

    private void getAllFiles(File dir, List<File> out) {
        File[] fs = dir.listFiles(); if (fs == null) return;
        for (File f : fs) { if (f.isDirectory()) getAllFiles(f, out); else out.add(f); }
    }

    private void showSendTextDialog(final boolean append) {
        pendingAppend = append;
        UikitFullInputActivity.show(this, I18nManager.get("input_text"), "", null, 5015);
    }

    private void sendClipboard(final boolean append) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = cm.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            CharSequence text = clip.getItemAt(0).coerceToText(this);
            if (text != null && text.length() > 0) {
                try {
                    File f = new File(getCacheDir(), "clip_" + System.currentTimeMillis() + ".txt");
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(text.toString().getBytes("UTF-8")); fos.close();
                    if (!append) pendingUris.clear();
                    pendingUris.add(Uri.fromFile(f));
                    onFilesPicked();
                    return;
                } catch (Exception ignored) {}
            }
        }
        toast(I18nManager.get("clipboard_empty"));
    }

    private void onFilesPicked() {
        if (historyMode) exitHistoryMode();
        updateSelectedFilesUI();
        log("Selected " + pendingUris.size() + " files");
    }

    private void updateSelectedFilesUI() {
        if (pendingUris == null || pendingUris.isEmpty()) {
            if (layoutSelectedFiles != null) layoutSelectedFiles.setVisibility(View.GONE);
            boolean isDark = ThemeUtils.isDarkMode(this);
        if (btnSend != null) btnSend.setText(I18nManager.get("send"));
        int txtColor = isDark ? 0xFF81C784 : 0xFF1B5E20;
        int bgRes = isDark ? R.drawable.watch_btn_secondary_dark : R.drawable.watch_btn_secondary;
        
        if (btnHistory != null) { btnHistory.setTextColor(txtColor); btnHistory.setBackgroundResource(bgRes); }
        if (btnCrossNet != null) { btnCrossNet.setTextColor(txtColor); btnCrossNet.setBackgroundResource(bgRes); }
        if (btnSettings != null) { btnSettings.setTextColor(txtColor); btnSettings.setBackgroundResource(bgRes); }
        if (scanBtn != null) { scanBtn.setTextColor(txtColor); scanBtn.setBackgroundResource(bgRes); }
        if (manualIpBtn != null) { manualIpBtn.setTextColor(txtColor); manualIpBtn.setBackgroundResource(bgRes); }
        if (btnLog != null) { btnLog.setTextColor(isDark ? 0xFFAAAAAA : 0xFF666666); }
        } else {
            if (layoutSelectedFiles != null) layoutSelectedFiles.setVisibility(View.VISIBLE);
            long totalSize = 0;
            for (Uri u : pendingUris) {
                try {
                    android.database.Cursor c = getContentResolver().query(u, null, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        int sizeIdx = c.getColumnIndex(android.provider.OpenableColumns.SIZE);
                        if (sizeIdx != -1) totalSize += c.getLong(sizeIdx);
                        c.close();
                    }
                } catch (Exception ignored) {}
            }
            String sizeStr = totalSize > 1024*1024 ? String.format(java.util.Locale.US, "%.1f MB", totalSize/1024.0/1024.0) : String.format(java.util.Locale.US, "%.1f KB", totalSize/1024.0);
            if (tvSelectedFile != null) {
                tvSelectedFile.setText(String.format(java.util.Locale.US, I18nManager.get("selected_files_fmt"), pendingUris.size(), (totalSize > 0 ? sizeStr : I18nManager.get("unknown_size"))));
            }
        }
    }

    

        

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 5020) {
            if (adapter != null) adapter.notifyDataSetChanged();
            return;
        }
        // 5001 主类型选择列表
        if (requestCode == 5001 && resultCode == RESULT_OK) {
            int pos = data.getIntExtra(UikitFullListActivity.EXTRA_RESULT_INDEX, -1);
            if (pos >= 0) {
                if (pos == 0) pickFile(false, currentAppendState);
                else if (pos == 1) pickFile(true, currentAppendState);
                else if (pos == 2) pickApp(currentAppendState);
                else if (pos == 3) pickFolder(currentAppendState);
                else if (pos == 4) showSendTextDialog(currentAppendState);
                else if (pos == 5) sendClipboard(currentAppendState);
            }
            return;
        }

        // 5010 选择应用
        if (requestCode == 5010 && resultCode == RESULT_OK) {
            int pos = data.getIntExtra(UikitFullListActivity.EXTRA_RESULT_INDEX, -1);
            if (pos >= 0 && pendingApps != null && pos < pendingApps.size()) {
                if (!pendingAppend) pendingUris.clear();
                pendingUris.add(Uri.fromFile(new File(pendingApps.get(pos).sourceDir)));
                onFilesPicked();
            }
            return;
        }

        // 5011 文件夹选择
        if (requestCode == 5011 && resultCode == RESULT_OK) {
            int pos = data.getIntExtra(UikitFullListActivity.EXTRA_RESULT_INDEX, -1);
            if (pos == 0) {
                List<File> all = new ArrayList<File>();
                getAllFiles(pendingFolderDir, all);
                if (all.isEmpty()) { toast(I18nManager.get("empty_folder")); return; }
                if (!pendingAppend) pendingUris.clear();
                for (File f : all) pendingUris.add(Uri.fromFile(f));
                onFilesPicked();
            } else if (pos > 0 && pendingFolders != null && pos - 1 < pendingFolders.size()) {
                showFolderSelector(pendingFolders.get(pos - 1));
            }
            return;
        }

        // 5012 手动添加 IP
        if (requestCode == 5012 && resultCode == RESULT_OK) {
            String text = data.getStringExtra(UikitFullInputActivity.EXTRA_RESULT_TEXT);
            if (text != null && text.length() > 0) {
                String ip = text.trim();
                if (!ip.matches("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")) {
                    toast(I18nManager.get("ip_format_error")); return;
                }
                Device d = new Device();
                d.alias = I18nManager.get("manual_device");
                d.ip = ip; d.port = 53317; d.https = false;
                DeviceStore.get().addDevice(d);
                log("Manual add: " + ip);
            }
            return;
        }

        // 5013 设备菜单：白名单 / 黑名单 / 收藏
        if (requestCode == 5013 && resultCode == RESULT_OK) {
            int p = data.getIntExtra(UikitFullListActivity.EXTRA_RESULT_INDEX, -1);
            if (p == 0) {
                toggleList("whitelist", pendingAclFp, pendingAclIsWl);
            } else if (p == 1) {
                toggleList("blacklist", pendingAclFp, pendingAclIsBl);
            } else if (p == 2) {
                boolean added = FavoritesStore.toggle(pendingAclFp, pendingDeviceAlias, pendingDeviceIp, pendingDevicePort);
                toast(added ? I18nManager.get("favorite_added") : I18nManager.get("favorite_removed"));
            }
            return;
        }

        // 5014 历史记录菜单
        if (requestCode == 5014 && resultCode == RESULT_OK) {
            int p = data.getIntExtra(UikitFullListActivity.EXTRA_RESULT_INDEX, -1);
            int shareIdx = pendingHistoryIsZip ? 1 : 0;
            int delIdx = pendingHistoryIsZip ? 2 : 1;
            if (pendingHistoryIsZip && p == 0) {
                toast(I18nManager.get("unzipping"));
                new Thread(new Runnable() {
                    public void run() {
                        final boolean ok = org.watchsend.util.ZipUtils.unzip(pendingHistoryFile, pendingHistoryFile.getParentFile());
                        handler.post(new Runnable() {
                            public void run() { toast(ok ? I18nManager.get("unzip_ok") : I18nManager.get("unzip_fail")); }
                        });
                    }
                }).start();
            } else if (p == shareIdx) {
                if (pendingHistoryFile.exists()) {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("*/*");
                    i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(pendingHistoryFile));
                    startActivity(Intent.createChooser(i, I18nManager.get("share_file")));
                } else toast(I18nManager.get("file_not_found"));
            } else if (p == delIdx) {
                UikitFullConfirmActivity.show(this, I18nManager.get("delete_record"), I18nManager.get("delete_confirm"), 5016);
            }
            return;
        }

        // 5015 发送文本
        if (requestCode == 5015 && resultCode == RESULT_OK) {
            String text = data.getStringExtra(UikitFullInputActivity.EXTRA_RESULT_TEXT);
            if (text == null || text.length() == 0) return;
            try {
                File f = new File(getCacheDir(), "text_" + System.currentTimeMillis() + ".txt");
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(text.getBytes("UTF-8")); fos.close();
                if (!pendingAppend) pendingUris.clear();
                pendingUris.add(Uri.fromFile(f));
                onFilesPicked();
            } catch (Exception e) { toast(I18nManager.get("save_fail")); }
            return;
        }

        // 5016 确认删除历史记录
        if (requestCode == 5016 && resultCode == RESULT_OK) {
            HistoryStore.get().remove(MainActivity.this, historyPaths.get(pendingHistoryPos));
            refreshHistory();
            return;
        }

        // 5002 日志 / 5017 URL 展示：纯展示，无需后续处理
        if (requestCode == 5002 || requestCode == 5017) return;

        // 原有系统文件选择
        if ((requestCode != REQ_FILE && requestCode != REQ_MEDIA) || resultCode != RESULT_OK || data == null) return;
        if (!pendingAppend) pendingUris.clear();
        if (data.getClipData() != null) {
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) pendingUris.add(data.getClipData().getItemAt(i).getUri());
        } else if (data.getData() != null) pendingUris.add(data.getData());
        onFilesPicked();
    }

    private void sendToDevice(final Device device) {
        new Thread(new Runnable() {
            public void run() {
                final List<File> files = new ArrayList<File>();
                for (Uri u : pendingUris) {
                    try { files.add(copyToCache(u, queryDisplayName(u))); } catch (Exception e) { log("Read failed: " + e); }
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        pendingUris.clear();
                        updateSelectedFilesUI();
                        Intent i = new Intent(MainActivity.this, TransferActivity.class);
                        i.putExtra("mode", 0);
                        i.putExtra("device", device);
                        ArrayList<String> paths = new ArrayList<String>();
                        for (File f : files) paths.add(f.getAbsolutePath());
                        i.putStringArrayListExtra("files", paths);
                        startActivity(i);
                    }
                });
            }
        }).start();
    }

    private String queryDisplayName(Uri uri) {
        if ("file".equals(uri.getScheme())) return new File(uri.getPath()).getName();
        String name = null; android.database.Cursor c = null;
        try { c = getContentResolver().query(uri, new String[]{"_display_name"}, null, null, null); if (c != null && c.moveToFirst()) name = c.getString(0); }
        catch (Exception ignored) {} finally { if (c != null) try { c.close(); } catch (Exception ignored) {} }
        if (name == null) name = uri.getLastPathSegment();
        return name == null ? "file" : name;
    }

    private File copyToCache(Uri uri, String name) throws Exception {
        if ("file".equals(uri.getScheme())) return new File(uri.getPath());
        InputStream in = getContentResolver().openInputStream(uri);
        String safe = name.replace("/", "_").replace("\\","_").replace(":","_").replace("*","_").replace("?","_").replace("\"","_").replace("<","_").replace(">","_").replace("|","_");
        File out = new File(getCacheDir(), safe);
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[65536]; int n;
        while ((n = in.read(buf)) > 0) fos.write(buf, 0, n);
        in.close(); fos.close();
        return out;
    }

    private void doScan() {
        log("Scanning..."); toast(I18nManager.get("empty_scanning"));
        MulticastScanner.scanAsync(new MulticastScanner.Callback() {
            public void onDevice(Device d) { DeviceStore.get().addDevice(d); log("Found: " + d.alias); }
            public void onFinished() { log("Scan finished"); }
        });
    }

    private void showManualIpDialog() {
        UikitFullInputActivity.show(this, I18nManager.get("input_address"), "", null, 5012);
    }

    private void showAclMenu(final Device d) {
        pendingAclFp = d.identity();
        pendingAclIsWl = OldSendService.getPrefStringGlobal("whitelist", "").contains(pendingAclFp);
        pendingAclIsBl = OldSendService.getPrefStringGlobal("blacklist", "").contains(pendingAclFp);
        pendingDeviceAlias = d.alias;
        pendingDeviceIp = d.ip;
        pendingDevicePort = d.port;
        boolean fav = FavoritesStore.isFavorite(pendingAclFp);
        String[] items = new String[]{
            pendingAclIsWl ? I18nManager.get("remove_whitelist") : I18nManager.get("add_whitelist"),
            pendingAclIsBl ? I18nManager.get("remove_blacklist") : I18nManager.get("add_blacklist"),
            fav ? I18nManager.get("remove_favorite") : I18nManager.get("add_favorite")
        };
        UikitFullListActivity.show(this, d.alias, items, 5013);
    }

    private void toggleList(String key, String fp, boolean exists) {
        String current = OldSendService.getPrefStringGlobal(key, "");
        if (exists) current = current.replace(fp + ",", "").replace(fp, "");
        else current += fp + ",";
        getSharedPreferences("oldsend", MODE_PRIVATE).edit().putString(key, current).commit();
        toast(exists ? I18nManager.get("removed") : I18nManager.get("added_to_list"));
    }

    private void toggleHistory() {
        if (historyMode) exitHistoryMode(); else enterHistoryMode();
    }

    private void enterHistoryMode() {
        historyMode = true;
        btnHistory.setText(I18nManager.get("return_to_devices"));
        deviceList.setAdapter(historyAdapter);
        refreshHistory();
        setListViewHeightBasedOnChildren(deviceList);
    }

    private void exitHistoryMode() {
        historyMode = false;
        btnHistory.setText(I18nManager.get("record"));
        deviceList.setAdapter(adapter);
        refreshDevices();
    }

    private void refreshHistory() {
        historyAdapter.clear(); historyPaths.clear();
        List<HistoryStore.Record> list = HistoryStore.get().snapshot();
        for (HistoryStore.Record r : list) {
            String sizeStr = r.size > 1024*1024 ? (r.size/1024/1024)+"MB" : (r.size/1024)+"KB";
            historyAdapter.add("[" + r.time + "] " + r.fileName + "\n" + sizeStr);
            historyPaths.add(r.path);
        }
        historyAdapter.notifyDataSetChanged();
        emptyHint.setText(historyPaths.isEmpty() ? I18nManager.get("no_record") : "");
        emptyHint.setVisibility(historyPaths.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openHistoryFile(int pos) {
        File f = new File(historyPaths.get(pos));
        if (!f.exists()) { toast(I18nManager.get("file_not_found")); return; }
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(f), "application/*");
            startActivity(i);
        } catch (Exception e) { toast(I18nManager.get("cannot_open")); }
    }


    private void showBigUrlDialog() {
        String ip = OldSendService.getLocalIp();
        String url = "http://" + ip + ":53317";
        String msg = url + "\n" + I18nManager.get("web_qr_desc");
        UikitFullTextActivity.show(this, I18nManager.get("web_qr"), msg, I18nManager.get("confirm"), 5017);
    }

    private void showHistoryMenu(final int pos) {
        final File f = new File(historyPaths.get(pos));
        final boolean isZip = f.exists() && f.getName().toLowerCase().endsWith(".zip");
        pendingHistoryFile = f;
        pendingHistoryIsZip = isZip;
        pendingHistoryPos = pos;
        String[] items = isZip ?
            new String[]{I18nManager.get("unzip"), I18nManager.get("share_file"), I18nManager.get("delete_record")} :
            new String[]{I18nManager.get("share_file"), I18nManager.get("delete_record")};
        UikitFullListActivity.show(this, historyAdapter.getItem(pos).split("\n")[0], items, 5014);
    }

    private void showLogDialog() {
        UikitFullTextActivity.show(this, I18nManager.get("log"), logCache.toString(), I18nManager.get("confirm"), false, true, 5002);
    }

    public void onChanged() { if (!historyMode) refreshDevices(); }

    private void refreshDevices() {
        if (historyMode) return;
        adapter.setData(DeviceStore.get().snapshot());
        emptyHint.setVisibility(adapter.getCount() == 0 ? View.VISIBLE : View.GONE);
        setListViewHeightBasedOnChildren(deviceList);
    }

    private void setListViewHeightBasedOnChildren(ListView lv) {
        ListAdapter la = lv.getAdapter(); if (la == null) return;
        int total = 0;
        for (int i = 0; i < la.getCount(); i++) {
            View item = la.getView(i, null, lv);
            item.measure(0, 0); total += item.getMeasuredHeight();
        }
        ViewGroup.LayoutParams lp = lv.getLayoutParams();
        lp.height = total + lv.getDividerHeight() * Math.max(0, la.getCount() - 1);
        lv.setLayoutParams(lp);
    }

    public void log(final String msg) {
        handler.post(new Runnable() {
            public void run() {
                logCache.append(msg).append("\n");
                if (logCache.length() > 4000) logCache.delete(0, logCache.length() - 4000);
                // 若日志页正处于打开的实时模式，同步追加并滚动到底部
                UikitFullTextActivity live = UikitFullTextActivity.getLiveInstance();
                if (live != null) live.appendLive(msg + "\n");
            }
        });
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); setIntent(intent); handleIntent(intent); }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) { pendingUris.clear(); pendingUris.add(uri); onFilesPicked(); }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (uris != null) { pendingUris.clear(); pendingUris.addAll(uris); onFilesPicked(); }
        }
    }

    @Override protected void onResume() {
        super.onResume();
        android.content.SharedPreferences sp = getSharedPreferences("oldsend", MODE_PRIVATE);
        String curLang = sp.getString("app_lang", "system");
        boolean curDark = sp.getBoolean("dark_mode", false);
        float curScale = sp.getFloat("ui_scale", 1.0f);
        
        if (!curLang.equals(lastLang) || curDark != lastDark || curScale != lastScale) {
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    recreate();
                }
            }, 100);
            return;
        }
        
        applyI18n();
        updateSelectedFilesUI();
        if (!historyMode) refreshDevices();
        updateStatus();
    }

    @Override protected void onDestroy() {
        OldSendService.setSink(null);
        DeviceStore.get().setListener(null);
        super.onDestroy();
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (WatchUiUtils.handleSwipeBack(this, ev)) return true;
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (WatchUiUtils.isSystemBackBlocked(this)) {
            Toast.makeText(this, I18nManager.get("swipe_back_toast"), Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    private int dp(int val) {
        return (int)(val * getResources().getDisplayMetrics().density);
    }
}


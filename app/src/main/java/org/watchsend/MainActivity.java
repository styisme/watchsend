package org.watchsend;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
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
import org.watchsend.model.HistoryStore;
import org.watchsend.ui.DeviceAdapter;
import org.watchsend.util.FileLogger;
import org.watchsend.util.I18nManager;
import org.watchsend.util.ThemeUtils;
import org.watchsend.watch.WatchSettingsActivity;
import org.watchsend.watch.WatchUiUtils;

public class MainActivity extends Activity implements OldSendService.LogSink, DeviceStore.Listener {
    private static final int REQ_FILE = 1001;
    private static final int REQ_MEDIA = 1002;

    public static List<Uri> pendingUris = new ArrayList<Uri>();

    private TextView status, statusIp, emptyHint;
    private Button toggleBtn, btnSend, btnHistory, btnCrossNet, btnSettings, btnLog, scanBtn, manualIpBtn;
    private ListView deviceList;
    private DeviceAdapter adapter;
    private ArrayAdapter<String> historyAdapter;
    private final List<String> historyPaths = new ArrayList<String>();
    private Handler handler = new Handler();
    private boolean running = false;
    private boolean pendingAppend = false;
    private boolean historyMode = false;
    private Dialog logDialog;
    private TextView dialogLogView;
    private StringBuilder logCache = new StringBuilder();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        org.watchsend.watch.WatchUiUtils.applyScale(this);
        I18nManager.init(this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ThemeUtils.applyTheme(this);
        View root = findViewById(R.id.main_root);
        root.setBackgroundColor(ThemeUtils.isDarkMode(this) ? 0xFF000000 : 0xFFF4F6F8);
        WatchUiUtils.applyRoundPadding(this, root);
        ScrollView mainScroll = (ScrollView) findViewById(R.id.main_scroll);
        LinearLayout mainContainer = (LinearLayout) findViewById(R.id.main_container);
        WatchUiUtils.setupWheelScroll(mainScroll, mainContainer);
        org.watchsend.watch.WatchUiUtils.applyRoundPadding(this, findViewById(android.R.id.content));
        FileLogger.log("WatchMain", "onCreate");

        status = (TextView) findViewById(R.id.status);
        statusIp = (TextView) findViewById(R.id.status_ip);
        emptyHint = (TextView) findViewById(R.id.empty_hint);
        toggleBtn = (Button) findViewById(R.id.toggle);
        btnSend = (Button) findViewById(R.id.btn_send);
        btnHistory = (Button) findViewById(R.id.btn_history);
        btnCrossNet = (Button) findViewById(R.id.btn_cross_net);
        btnSettings = (Button) findViewById(R.id.btn_settings);
        btnLog = (Button) findViewById(R.id.btn_log);
        scanBtn = (Button) findViewById(R.id.scan);
        manualIpBtn = (Button) findViewById(R.id.btn_manual_ip);
        deviceList = (ListView) findViewById(R.id.devices);

        adapter = new DeviceAdapter(this);
        historyAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        deviceList.setAdapter(adapter);

        OldSendService.setSink(this);
        DeviceStore.get().setListener(this);

        toggleBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { toggleService(); }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { showSendTypeDialog(); }
        });
        btnHistory.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { toggleHistory(); }
        });
        btnCrossNet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CrossNetActivity.class));
            }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, WatchSettingsActivity.class));
            }
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
                if (pendingUris.isEmpty()) { toast("请先选择文件"); return; }
                log("Sending to: " + target.alias);
                sendToDevice(target);
            }
        });
        deviceList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
                if (historyMode) return false;
                showAclMenu(adapter.getItem(pos));
                return true;
            }
        });

        updateStatus();
        applyI18n();
        checkAndRequestPermission();
        handleIntent(getIntent());
    }

    private void applyI18n() {
        btnSend.setText("📤  " + I18nManager.get("send"));
        btnHistory.setText("📥  " + I18nManager.get("receive_history"));
        btnCrossNet.setText("🌐  " + I18nManager.get("cross_net"));
        btnSettings.setText("⚙  " + I18nManager.get("settings"));
        btnLog.setText("📃  " + I18nManager.get("log"));
        scanBtn.setText(I18nManager.get("scan"));
        manualIpBtn.setText("+ " + I18nManager.get("add_ip"));
        if (emptyHint != null) emptyHint.setText(I18nManager.get("no_device"));
        toggleBtn.setText(running ? I18nManager.get("stop") : I18nManager.get("start"));
        updateStatus();
    }

    private void updateStatus() {
        if (status == null) return;
        String ip = OldSendService.getLocalIp();
        if (ip != null && ip.length() > 15) ip = ip.substring(0, 12) + "...";
        status.setText(running ? I18nManager.get("running") : I18nManager.get("stopped"));
        statusIp.setText("IP " + ip + ":53317");
    }

    private void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1001);
        }
    }

    private void toggleService() {
        if (running) { stopService(new Intent(this, OldSendService.class)); running = false; }
        else { startService(new Intent(this, OldSendService.class)); running = true; }
        updateStatus();
        toggleBtn.setText(running ? I18nManager.get("stop") : I18nManager.get("start"));
    }

    private void showSendTypeDialog() {
        final String[] items = {
            I18nManager.get("file"), I18nManager.get("media"), I18nManager.get("app"),
            I18nManager.get("folder"), I18nManager.get("text"), I18nManager.get("clipboard")
        };
        WatchUiUtils.showList(this, I18nManager.get("send"), items, new WatchUiUtils.OnItemSelectListener() {
            public void onItemSelect(int pos) {
                if (pos == 0) pickFile(false, false);
                else if (pos == 1) pickFile(true, false);
                else if (pos == 2) pickApp(false);
                else if (pos == 3) pickFolder(false);
                else if (pos == 4) showSendTextDialog();
                else if (pos == 5) sendClipboard();
            }
        }).show();
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
        if (names.isEmpty()) { toast("无应用"); return; }
        WatchUiUtils.showList(this, I18nManager.get("select_app"), names.toArray(new String[0]),
            new WatchUiUtils.OnItemSelectListener() {
                public void onItemSelect(int pos) {
                    if (!pendingAppend) pendingUris.clear();
                    pendingUris.add(Uri.fromFile(new File(apps.get(pos).sourceDir)));
                    onFilesPicked();
                }
            }).show();
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
        folderNames.add(I18nManager.get("send_folder"));
        for (File f : files) {
            if (f.isDirectory() && !f.isHidden()) {
                folderNames.add("[DIR] " + f.getName());
                folders.add(f);
            }
        }
        WatchUiUtils.showList(this, dir.getName().isEmpty() ? "/" : dir.getName(),
            folderNames.toArray(new String[0]), new WatchUiUtils.OnItemSelectListener() {
                public void onItemSelect(int pos) {
                    if (pos == 0) {
                        List<File> all = new ArrayList<File>();
                        getAllFiles(dir, all);
                        if (all.isEmpty()) { toast("空文件夹"); return; }
                        if (!pendingAppend) pendingUris.clear();
                        for (File f : all) pendingUris.add(Uri.fromFile(f));
                        onFilesPicked();
                    } else showFolderSelector(folders.get(pos - 1));
                }
            }).show();
    }

    private void getAllFiles(File dir, List<File> out) {
        File[] fs = dir.listFiles(); if (fs == null) return;
        for (File f : fs) { if (f.isDirectory()) getAllFiles(f, out); else out.add(f); }
    }

    private void showSendTextDialog() {
        WatchUiUtils.showInput(this, I18nManager.get("input_text"), "",
            new WatchUiUtils.OnInputListener() {
                public void onInput(String text) {
                    if (text == null || text.length() == 0) return;
                    try {
                        File f = new File(getCacheDir(), "text_" + System.currentTimeMillis() + ".txt");
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(text.getBytes("UTF-8")); fos.close();
                        pendingUris.clear(); pendingUris.add(Uri.fromFile(f));
                        onFilesPicked();
                    } catch (Exception e) { toast("失败"); }
                }
            }).show();
    }

    private void sendClipboard() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = cm.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            CharSequence text = clip.getItemAt(0).coerceToText(this);
            if (text != null && text.length() > 0) {
                try {
                    File f = new File(getCacheDir(), "clip_" + System.currentTimeMillis() + ".txt");
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(text.toString().getBytes("UTF-8")); fos.close();
                    pendingUris.clear(); pendingUris.add(Uri.fromFile(f));
                    onFilesPicked();
                    return;
                } catch (Exception ignored) {}
            }
        }
        toast(I18nManager.get("clipboard_empty"));
    }

    private void onFilesPicked() {
        if (historyMode) exitHistoryMode();
        toast("已选 " + pendingUris.size() + " 个文件，点设备发送");
        log("Selected " + pendingUris.size() + " files");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    try { files.add(copyToCache(u, queryDisplayName(u))); }
                    catch (Exception e) { log("Read failed: " + e); }
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        pendingUris.clear();
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
        try { c = getContentResolver().query(uri, new String[]{"_display_name"}, null, null, null);
            if (c != null && c.moveToFirst()) name = c.getString(0); }
        catch (Exception ignored) {}
        finally { if (c != null) try { c.close(); } catch (Exception ignored) {} }
        if (name == null) name = uri.getLastPathSegment();
        return name == null ? "file" : name;
    }

    private File copyToCache(Uri uri, String name) throws Exception {
        if ("file".equals(uri.getScheme())) return new File(uri.getPath());
        InputStream in = getContentResolver().openInputStream(uri);
        String safe = name.replace("/", "_").replace("\\","_").replace(":","_")
            .replace("*","_").replace("?","_").replace("\"","_").replace("<","_").replace(">","_").replace("|","_");
        File out = new File(getCacheDir(), safe);
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[65536]; int n;
        while ((n = in.read(buf)) > 0) fos.write(buf, 0, n);
        in.close(); fos.close();
        return out;
    }

    private void doScan() {
        log("Scanning..."); toast("扫描中...");
        MulticastScanner.scanAsync(new MulticastScanner.Callback() {
            public void onDevice(Device d) { DeviceStore.get().addDevice(d); log("Found: " + d.alias); }
            public void onFinished() { log("Scan finished"); }
        });
    }

    private void showManualIpDialog() {
        WatchUiUtils.showInput(this, I18nManager.get("input_address"), "",
            new WatchUiUtils.OnInputListener() {
                public void onInput(String text) {
                    if (text == null || text.length() == 0) return;
                    String ip = text.trim();
                    if (!ip.matches("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")) {
                        toast("IP 格式错误"); return;
                    }
                    Device d = new Device();
                    d.alias = I18nManager.get("manual_device");
                    d.ip = ip; d.port = 53317; d.https = false;
                    DeviceStore.get().addDevice(d);
                    log("Manual add: " + ip);
                }
            }).show();
    }

    private void showAclMenu(final Device d) {
        final String fp = (d.fingerprint != null && d.fingerprint.length() > 0) ? d.fingerprint : d.ip;
        final boolean isWl = OldSendService.getPrefStringGlobal("whitelist", "").contains(fp);
        final boolean isBl = OldSendService.getPrefStringGlobal("blacklist", "").contains(fp);
        String[] items = new String[]{
            isWl ? "移除白名单" : "⭐ 加入白名单",
            isBl ? "移除黑名单" : "🚫 加入黑名单"
        };
        WatchUiUtils.showList(this, d.alias, items, new WatchUiUtils.OnItemSelectListener() {
            public void onItemSelect(int p) {
                toggleList("whitelist", fp, isWl);
                if (p == 1) toggleList("blacklist", fp, isBl);
            }
        }).show();
    }

    private void toggleList(String key, String fp, boolean exists) {
        String current = OldSendService.getPrefStringGlobal(key, "");
        if (exists) current = current.replace(fp + ",", "").replace(fp, "");
        else current += fp + ",";
        getSharedPreferences("oldsend", MODE_PRIVATE).edit().putString(key, current).commit();
        toast(exists ? "已移出" : "已加入");
    }

    private void toggleHistory() {
        if (historyMode) exitHistoryMode(); else enterHistoryMode();
    }

    private void enterHistoryMode() {
        historyMode = true;
        btnHistory.setText("↩ 返回设备");
        deviceList.setAdapter(historyAdapter);
        refreshHistory();
        setListViewHeightBasedOnChildren(deviceList);
    }

    private void exitHistoryMode() {
        historyMode = false;
        btnHistory.setText("📥  " + I18nManager.get("receive_history"));
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
        emptyHint.setText(historyPaths.isEmpty() ? "暂无记录" : "");
        emptyHint.setVisibility(historyPaths.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openHistoryFile(int pos) {
        File f = new File(historyPaths.get(pos));
        if (!f.exists()) { toast("文件不存在"); return; }
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(f), "application/*");
            startActivity(i);
        } catch (Exception e) { toast("无法打开"); }
    }

    private void showLogDialog() {
        if (logDialog != null && logDialog.isShowing()) return;
        dialogLogView = new TextView(this);
        dialogLogView.setTextSize(10);
        dialogLogView.setTypeface(android.graphics.Typeface.MONOSPACE);
        dialogLogView.setPadding(8, 8, 8, 8);
        dialogLogView.setText(logCache.toString());
        if (ThemeUtils.isDarkMode(this)) {
            dialogLogView.setTextColor(0xFFAAAAAA);
            dialogLogView.setBackgroundColor(0xFF1E1E1E);
        } else {
            dialogLogView.setTextColor(0xFF333333);
            dialogLogView.setBackgroundColor(0xFFF5F5F5);
        }
        ScrollView sv = new ScrollView(this); sv.addView(dialogLogView);
        logDialog = WatchUiUtils.createBaseDialog(this, I18nManager.get("log"), sv,
            I18nManager.get("close"), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int w) { dialogLogView = null; }
            }, null, null);
        logDialog.show();
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
        FileLogger.log("UI", msg);
        handler.post(new Runnable() {
            public void run() {
                logCache.append(msg).append("\n");
                if (logCache.length() > 4000) logCache.delete(0, logCache.length() - 4000);
                if (dialogLogView != null) dialogLogView.setText(logCache.toString());
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
        if (org.watchsend.watch.WatchUiUtils.handleSwipeBack(this, ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

}
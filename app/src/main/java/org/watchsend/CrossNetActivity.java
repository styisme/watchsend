package org.watchsend;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.watchsend.util.I18nManager;
import org.watchsend.util.ThemeUtils;
import org.watchsend.watch.WatchUiUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import android.util.Base64;

public class CrossNetActivity extends Activity {
    private static final String NTFY_URL = "https://ntfy.sh/oldsend_cross_";
    
    private LinearLayout container;
    private TextView tvStatus, tvPinDisplay, tvFileInfo, tvIpv6Info, tvStunInfo;
    private EditText etPin, etDirectIp;
    private org.watchsend.ui.Md3Switch cbUseDirect;
    private Button btnGen, btnRecv, btnStunProbe, btnUpnpPunch;
    
    private Handler handler = new Handler();
    private boolean isTransferring = false;
    private List<Uri> pendingUris = new ArrayList<Uri>();
    private boolean pendingAppend = false;
    private String currentPin = null;
    
    private boolean isDark;
    private int colorMain, colorSec, colorAccent, bgSec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WatchUiUtils.applyScale(this);
        super.onCreate(savedInstanceState);
        I18nManager.init(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_cross_net);
        ThemeUtils.applyTheme(this);
        WatchUiUtils.applyEntryAnimation(this);

        isDark = ThemeUtils.isDarkMode(this);
        colorMain = isDark ? 0xFFE0E0E0 : 0xFF1C1B1F;
        colorSec = isDark ? 0xFFAAAAAA : 0xFF666666;
        colorAccent = isDark ? 0xFF81C784 : 0xFF1B5E20;
        bgSec = isDark ? R.drawable.watch_btn_secondary_dark : R.drawable.watch_btn_secondary;

        View rootCn = findViewById(R.id.root_cross_net);
        rootCn.setBackgroundColor(isDark ? 0xFF000000 : 0xFFF4F6F8);
        WatchUiUtils.applyRoundPadding(this, rootCn);

        container = (LinearLayout) findViewById(R.id.cross_net_container);
        ScrollView scrollView = (ScrollView) findViewById(R.id.cross_net_scroll);
        WatchUiUtils.setupWheelScroll(scrollView, container);

        buildUI();
    }

    private void buildUI() {
        container.removeAllViews();

        // 1. 居中大标题 (模仿设置页)
        TextView tvTitle = new TextView(this);
        tvTitle.setText(I18nManager.get("cross_net"));
        tvTitle.setTextSize(24);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(colorMain);
        tvTitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleLp.bottomMargin = dp(16);
        tvTitle.setLayoutParams(titleLp);
        container.addView(tvTitle);

        // 2. 副标题 (无需同一Wi-Fi)
        TextView tvDesc = new TextView(this);
        tvDesc.setText(I18nManager.get("cross_net_desc"));
        tvDesc.setTextSize(12);
        tvDesc.setTextColor(colorSec);
        tvDesc.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        descLp.bottomMargin = dp(20);
        tvDesc.setLayoutParams(descLp);
        container.addView(tvDesc);

        // ====== 构建列表项 ======

        // 卡片1：本机 IPv6
        tvIpv6Info = new TextView(this);
        tvIpv6Info.setText("IPv6: 正在获取...");
        tvIpv6Info.setTextColor(colorAccent);
        tvIpv6Info.setTextSize(12);
        addCardItem(I18nManager.get("local_ipv6"), tvIpv6Info, null);

        // 卡片2：STUN 探测
        tvStunInfo = new TextView(this);
        tvStunInfo.setText(I18nManager.get("not_probed"));
        tvStunInfo.setTextColor(colorSec);
        tvStunInfo.setTextSize(12);
        btnStunProbe = createSmallButton(I18nManager.get("stun_probe"));
        btnStunProbe.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tvStunInfo.setText(I18nManager.get("probing"));
                new Thread(new Runnable() {
                    public void run() {
                        final String result = org.watchsend.util.StunClient.getPublicAddress();
                        handler.post(new Runnable() {
                            public void run() {
                                if (result.contains("失败") || result.contains("未知")) {
                                    tvStunInfo.setText(I18nManager.get("stun_fail"));
                                    tvStunInfo.setTextColor(0xFFD32F2F);
                                } else {
                                    tvStunInfo.setText(result);
                                    tvStunInfo.setTextColor(colorAccent);
                                    etDirectIp.setText(result);
                                }
                            }
                        });
                    }
                }).start();
            }
        });
        addCardItem(I18nManager.get("nat_addr"), tvStunInfo, btnStunProbe);

        // 卡片3：极速直连
        cbUseDirect = new org.watchsend.ui.Md3Switch(this);
        cbUseDirect.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(26)));
        cbUseDirect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layoutDirectIp.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                btnGen.setText(isChecked ? I18nManager.get("direct_send") : I18nManager.get("gen_pin_send"));
            }
        });
        addCardItem(I18nManager.get("direct_mode"), null, cbUseDirect);

        // 卡片4：直连 IP 输入框 (动态显示)
        layoutDirectIp = new LinearLayout(this);
        layoutDirectIp.setOrientation(LinearLayout.VERTICAL);
        layoutDirectIp.setBackground(WatchUiUtils.getPillBackground(this));
        layoutDirectIp.setPadding(dp(16), dp(8), dp(16), dp(8));
        LinearLayout.LayoutParams ipLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ipLp.bottomMargin = dp(8);
        layoutDirectIp.setLayoutParams(ipLp);
        layoutDirectIp.setVisibility(View.GONE);

        etDirectIp = new EditText(this);
        etDirectIp.setHint(I18nManager.get("input_direct_ip"));
        etDirectIp.setTextColor(colorMain);
        etDirectIp.setHintTextColor(colorSec);
        etDirectIp.setTextSize(13);
        etDirectIp.setBackground(null);
        etDirectIp.setPadding(0, dp(8), 0, dp(8));
        layoutDirectIp.addView(etDirectIp);
        container.addView(layoutDirectIp);

        // 卡片5：已选文件管理
        layoutSelectedFiles = new LinearLayout(this);
        layoutSelectedFiles.setOrientation(LinearLayout.HORIZONTAL);
        layoutSelectedFiles.setGravity(Gravity.CENTER_VERTICAL);
        layoutSelectedFiles.setBackground(WatchUiUtils.getPillBackground(this));
        layoutSelectedFiles.setPadding(dp(16), dp(12), dp(16), dp(12));
        LinearLayout.LayoutParams fileLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        fileLp.bottomMargin = dp(8);
        layoutSelectedFiles.setLayoutParams(fileLp);
        layoutSelectedFiles.setVisibility(View.GONE);

        tvFileInfo = new TextView(this);
        tvFileInfo.setTextSize(13);
        tvFileInfo.setTextColor(colorMain);
        tvFileInfo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        layoutSelectedFiles.addView(tvFileInfo);

        TextView btnEdit = createTextButton(I18nManager.get("edit"));
        btnEdit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.pendingUris = pendingUris;
                startActivity(new Intent(CrossNetActivity.this, SelectListActivity.class));
            }
        });
        layoutSelectedFiles.addView(btnEdit);
        container.addView(layoutSelectedFiles);

        // 卡片6：操作按钮 (生成密码)
        btnGen = new Button(this);
        btnGen.setText(I18nManager.get("select_file_gen_pin"));
        btnGen.setTextColor(0xFFFFFFFF);
        btnGen.setTextSize(14);
        btnGen.setBackgroundResource(R.drawable.watch_btn_primary);
        btnGen.setMinHeight(0);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        btnLp.bottomMargin = dp(8);
        btnGen.setLayoutParams(btnLp);
        btnGen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isTransferring) return;
                if (pendingUris.isEmpty()) {
                    pendingAppend = false;
                    pickFile();
                } else {
                    if (cbUseDirect.isChecked()) {
                        String ipStr = etDirectIp.getText().toString().trim();
                        if (ipStr.isEmpty()) {
                            Toast.makeText(CrossNetActivity.this, I18nManager.get("toast_input_ip"), Toast.LENGTH_SHORT).show();
                        } else {
                            startDirectSend(ipStr);
                        }
                    } else {
                        generatePinAndSend();
                    }
                }
            }
        });
        container.addView(btnGen);

        // 显示生成的 PIN
        tvPinDisplay = new TextView(this);
        tvPinDisplay.setTextSize(20);
        tvPinDisplay.setTypeface(null, Typeface.BOLD);
        tvPinDisplay.setTextColor(colorAccent);
        tvPinDisplay.setGravity(Gravity.CENTER);
        tvPinDisplay.setVisibility(View.GONE);
        LinearLayout.LayoutParams pinDispLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pinDispLp.bottomMargin = dp(16);
        tvPinDisplay.setLayoutParams(pinDispLp);
        container.addView(tvPinDisplay);

        // 卡片7：接收密码区
        LinearLayout recvCard = new LinearLayout(this);
        recvCard.setOrientation(LinearLayout.HORIZONTAL);
        recvCard.setGravity(Gravity.CENTER_VERTICAL);
        recvCard.setBackground(WatchUiUtils.getPillBackground(this));
        recvCard.setPadding(dp(16), dp(8), dp(16), dp(8));
        LinearLayout.LayoutParams recvLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        recvLp.bottomMargin = dp(8);
        recvCard.setLayoutParams(recvLp);

        etPin = new EditText(this);
        etPin.setHint(I18nManager.get("input_pin_hint"));
        etPin.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etPin.setTextColor(colorMain);
        etPin.setHintTextColor(colorSec);
        etPin.setTextSize(15);
        etPin.setBackground(null);
        etPin.setSingleLine(true);
        etPin.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        recvCard.addView(etPin);

        btnRecv = createSmallButton(I18nManager.get("receive"));
        btnRecv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isTransferring) return;
                String pin = etPin.getText().toString().trim();
                if (pin.length() != 6) {
                    Toast.makeText(CrossNetActivity.this, I18nManager.get("toast_input_pin"), Toast.LENGTH_SHORT).show();
                } else {
                    startReceive(pin);
                }
            }
        });
        recvCard.addView(btnRecv);
        container.addView(recvCard);

        // 状态提示
        tvStatus = new TextView(this);
        tvStatus.setTextSize(12);
        tvStatus.setTextColor(colorAccent);
        tvStatus.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        statusLp.topMargin = dp(16);
        statusLp.bottomMargin = dp(16);
        tvStatus.setLayoutParams(statusLp);
        container.addView(tvStatus);
        
        // 初始化 IP 显示
        String ipv6 = OldSendService.getIpv6();
        if (ipv6 != null) tvIpv6Info.setText("[" + ipv6 + "]:53317");
        else tvIpv6Info.setText(I18nManager.get("ipv6_not_found"));
    }

    // ====== 辅助 UI 方法 ======
    
    private LinearLayout layoutDirectIp;
    private LinearLayout layoutSelectedFiles;

    private void addCardItem(String title, View subtitleView, View rightView) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(WatchUiUtils.getPillBackground(this));
        row.setPadding(dp(16), dp(12), dp(16), dp(12));
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        row.setLayoutParams(lp);

        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(15);
        tvTitle.setTextColor(colorMain);
        textContainer.addView(tvTitle);

        if (subtitleView != null) {
            LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            subLp.topMargin = dp(2);
            subtitleView.setLayoutParams(subLp);
            textContainer.addView(subtitleView);
        }

        row.addView(textContainer);

        if (rightView != null) {
            row.addView(rightView);
        }
        container.addView(row);
    }

    private Button createSmallButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextSize(11);
        btn.setTextColor(colorAccent);
        btn.setBackgroundResource(bgSec);
        btn.setMinHeight(0);
        btn.setMinWidth(0);
        btn.setPadding(dp(12), 0, dp(12), 0);
        btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(32)));
        return btn;
    }
    
    private TextView createTextButton(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(colorSec);
        tv.setPadding(dp(8), dp(8), dp(8), dp(8));
        return tv;
    }
    
    private int dp(int val) { return (int)(val * getResources().getDisplayMetrics().density); }

    // ================== 以下为业务逻辑（原样保留） ==================

    @Override
    protected void onResume() {
        super.onResume();
        if (MainActivity.pendingUris != null && MainActivity.pendingUris != pendingUris) {
            pendingUris = new ArrayList<Uri>(MainActivity.pendingUris);
        }
        updateUI();
    }

    private void pickFile() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(i, "选择文件"), 2001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2001 && resultCode == RESULT_OK && data != null) {
            if (!pendingAppend) pendingUris.clear();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) pendingUris.add(data.getClipData().getItemAt(i).getUri());
            } else if (data.getData() != null) {
                pendingUris.add(data.getData());
            }
            updateUI();
        }
    }

    private void updateUI() {
        if (pendingUris == null || pendingUris.isEmpty()) {
            layoutSelectedFiles.setVisibility(View.GONE);
            if (btnGen != null) btnGen.setText(I18nManager.get("select_file_gen_pin"));
        } else {
            layoutSelectedFiles.setVisibility(View.VISIBLE);
            long totalSize = 0;
            for (Uri u : pendingUris) {
                try {
                    Cursor cursor = getContentResolver().query(u, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                        if (sizeIndex != -1) totalSize += cursor.getLong(sizeIndex);
                        cursor.close();
                    }
                } catch (Exception ignored) {}
            }
            String sizeStr = totalSize > 1024*1024 ? String.format("%.1f MB", totalSize/1024.0/1024.0) : String.format("%.1f KB", totalSize/1024.0);
            if (tvFileInfo != null) tvFileInfo.setText(I18nManager.get("selected_prefix") + pendingUris.size() + I18nManager.get("files_suffix") + sizeStr);
            if (btnGen != null) btnGen.setText(I18nManager.get("gen_pin_send"));
        }
    }

    private void generatePinAndSend() {
        int pin = 100000 + new Random().nextInt(900000);
        currentPin = String.valueOf(pin);
        tvPinDisplay.setText(I18nManager.get("pin_prefix") + currentPin);
        tvPinDisplay.setVisibility(View.VISIBLE);
        startSend(currentPin);
    }

    private void startSend(final String pin) {
        isTransferring = true;
        tvStatus.setText(I18nManager.get("packing"));
        
        new Thread(new Runnable() {
            public void run() {
                try {
                    File tempZip = new File(getCacheDir(), "crossnet_" + pin + ".zip");
                    FileOutputStream fos = new FileOutputStream(tempZip);
                    ZipOutputStream zos = new ZipOutputStream(fos);
                    byte[] buf = new byte[8192];
                    
                    for (Uri uri : pendingUris) {
                        InputStream is = getContentResolver().openInputStream(uri);
                        String name = queryDisplayName(uri);
                        zos.putNextEntry(new ZipEntry(name));
                        int n;
                        while ((n = is.read(buf)) > 0) zos.write(buf, 0, n);
                        is.close();
                        zos.closeEntry();
                    }
                    zos.close();
                    fos.close();
                    
                    handler.post(new Runnable() { public void run() { tvStatus.setText(I18nManager.get("packed_connecting")); } });
                    
                    FileInputStream fis = new FileInputStream(tempZip);
                    byte[] chunk = new byte[3000];
                    int n;
                    long totalSent = 0;
                    
                    while ((n = fis.read(chunk)) > 0) {
                        byte[] actualChunk = new byte[n];
                        System.arraycopy(chunk, 0, actualChunk, 0, n);
                        String b64 = Base64.encodeToString(actualChunk, Base64.NO_WRAP);
                        
                        java.net.HttpURLConnection c = (java.net.HttpURLConnection) new java.net.URL(NTFY_URL + pin).openConnection();
                        c.setRequestMethod("POST");
                        c.setDoOutput(true);
                        c.setRequestProperty("Content-Type", "text/plain");
                        OutputStream os = c.getOutputStream();
                        os.write(b64.getBytes("UTF-8"));
                        os.flush();
                        os.close();
                        
                        totalSent += n;
                        final long finalSent = totalSent;
                        handler.post(new Runnable() { public void run() { tvStatus.setText(I18nManager.get("sending_prefix") + (finalSent/1024) + " KB"); } });
                        Thread.sleep(100);
                    }
                    fis.close();
                    
                    java.net.HttpURLConnection c = (java.net.HttpURLConnection) new java.net.URL(NTFY_URL + pin).openConnection();
                    c.setRequestMethod("POST");
                    c.setDoOutput(true);
                    c.getOutputStream().write("__END__".getBytes("UTF-8"));
                    
                    tempZip.delete();
                    
                    handler.post(new Runnable() {
                        public void run() { 
                            tvStatus.setText(I18nManager.get("send_done")); 
                            isTransferring = false; 
                        }
                    });
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        public void run() { tvStatus.setText(I18nManager.get("failed_prefix") + e.getMessage()); isTransferring = false; }
                    });
                }
            }
        }).start();
    }

    private void startDirectSend(String ipStr) {
        String cleanIp = ipStr.trim();
        int port = 53317;
        
        if (cleanIp.contains(OldSendService.getLocalIp())) {
            tvStatus.setText(I18nManager.get("direct_self"));
            return;
        }

        cleanIp = cleanIp.replace("[", "").replace("]", "");
        int lastColon = cleanIp.lastIndexOf(':');
        if (lastColon != -1) {
            String possiblePort = cleanIp.substring(lastColon + 1);
            if (possiblePort.matches("\\d{1,5}")) {
                try {
                    port = Integer.parseInt(possiblePort);
                    cleanIp = cleanIp.substring(0, lastColon);
                } catch (Exception ignored) {}
            }
        }
        
        final String ip = cleanIp;
        final int finalPort = port;
        
        final org.watchsend.model.Device target = new org.watchsend.model.Device();
        target.alias = "公网设备";
        target.ip = ip;
        target.port = finalPort;
        target.https = false;

        isTransferring = true;
        tvStatus.setText(I18nManager.get("preparing_direct"));

        new Thread(new Runnable() {
            public void run() {
                final List<File> files = new ArrayList<File>();
                try {
                    for (Uri uri : pendingUris) {
                        File out = new File(getCacheDir(), queryDisplayName(uri));
                        InputStream is = getContentResolver().openInputStream(uri);
                        FileOutputStream fos = new FileOutputStream(out);
                        byte[] buf = new byte[8192];
                        int n;
                        while ((n = is.read(buf)) > 0) fos.write(buf, 0, n);
                        is.close(); fos.close();
                        files.add(out);
                    }
                } catch (Exception e) {
                    handler.post(new Runnable() { public void run() {
                        Toast.makeText(CrossNetActivity.this, I18nManager.get("read_failed"), Toast.LENGTH_SHORT).show();
                        isTransferring = false;
                        tvStatus.setText(I18nManager.get("prepare_failed"));
                    }});
                    return;
                }

                handler.post(new Runnable() { public void run() {
                    tvStatus.setText(I18nManager.get("direct_sending"));
                }});

                org.watchsend.transfer.Uploader.uploadFiles(target, files, new org.watchsend.transfer.Uploader.ProgressCallback() {
                    public void onProgress(final String stage, final long sent, final long total) {
                        handler.post(new Runnable() { public void run() {
                            if (total > 0) {
                                int pct = (int)(sent * 100 / total);
                                tvStatus.setText(I18nManager.get("sending_percent") + pct + "%");
                            }
                        }});
                    }
                    public void onDone(final boolean ok, final String msg) {
                        handler.post(new Runnable() { public void run() {
                            isTransferring = false;
                            tvStatus.setText(ok ? I18nManager.get("direct_send_done") : I18nManager.get("direct_failed") + msg);
                        }});
                    }
                });
            }
        }).start();
    }

    private void startReceive(final String pin) {
        isTransferring = true;
        tvStatus.setText(I18nManager.get("connecting"));
        
        new Thread(new Runnable() {
            public void run() {
                try {
                    String pollUrl = NTFY_URL + pin + "/json?poll=1";
                    File tempZip = new File(getCacheDir(), "recv_crossnet_" + pin + ".zip");
                    FileOutputStream fos = new FileOutputStream(tempZip);
                    
                    boolean finished = false;
                    long lastTime = 0;
                    
                    while (!finished) {
                        java.net.HttpURLConnection c = (java.net.HttpURLConnection) new java.net.URL(pollUrl).openConnection();
                        c.setConnectTimeout(15000);
                        c.setReadTimeout(15000);
                        InputStream is = c.getInputStream();
                        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is, "UTF-8"));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.trim().length() > 0) {
                                org.json.JSONObject o = new org.json.JSONObject(line);
                                long time = o.optLong("time", 0);
                                if (time > lastTime) {
                                    String msg = o.optString("message", "");
                                    if ("__END__".equals(msg)) {
                                        finished = true;
                                        break;
                                    }
                                    if (!msg.isEmpty()) {
                                        byte[] data = Base64.decode(msg, Base64.NO_WRAP);
                                        fos.write(data);
                                        final long size = fos.getChannel().size();
                                        handler.post(new Runnable() { public void run() { tvStatus.setText(I18nManager.get("receiving_prefix") + (size/1024) + " KB"); } });
                                    }
                                    lastTime = time;
                                }
                            }
                        }
                        reader.close(); is.close();
                        if (!finished) Thread.sleep(1000);
                    }
                    fos.close();
                    
                    handler.post(new Runnable() { public void run() { tvStatus.setText(I18nManager.get("recv_unzipping")); } });
                    File downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
                    ZipInputStream zis = new ZipInputStream(new FileInputStream(tempZip));
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        File outFile = new File(downloadDir, entry.getName());
                        FileOutputStream out = new FileOutputStream(outFile);
                        byte[] b = new byte[8192];
                        int len;
                        while ((len = zis.read(b)) > 0) out.write(b, 0, len);
                        out.close();
                    }
                    zis.close();
                    tempZip.delete();
                    
                    handler.post(new Runnable() {
                        public void run() { 
                            tvStatus.setText(I18nManager.get("recv_done")); 
                            isTransferring = false; 
                        }
                    });
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        public void run() { tvStatus.setText(I18nManager.get("recv_failed") + e.getMessage()); isTransferring = false; }
                    });
                }
            }
        }).start();
    }

    private String queryDisplayName(Uri uri) {
        if ("file".equals(uri.getScheme())) return new File(uri.getPath()).getName();
        String name = null; Cursor c = null;
        try { c = getContentResolver().query(uri, new String[] { "_display_name" }, null, null, null); if (c != null && c.moveToFirst()) name = c.getString(0); } catch (Exception ignored) {} finally { if (c != null) try { c.close(); } catch (Exception ignored) {} }
        if (name == null) name = uri.getLastPathSegment();
        return name == null ? "file" : name;
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

package org.watchsend;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private TextView tvStatus, tvPinDisplay, tvFileInfo, tvIpv6Info, tvStunInfo;
    private EditText etPin, etDirectIp;
    private CheckBox cbUseDirect;
    private LinearLayout layoutSelectedFiles, layoutDirectIp;
    private Button btnGen, btnRecv, btnStunProbe, btnUpnpPunch;
    private Handler handler = new Handler();
    private boolean isTransferring = false;
    private List<Uri> pendingUris = new ArrayList<Uri>();
    private boolean pendingAppend = false;
    private String currentPin = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WatchUiUtils.applyScale(this);
        super.onCreate(savedInstanceState);
        I18nManager.init(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_cross_net);
        ThemeUtils.applyTheme(this);
        WatchUiUtils.applyEntryAnimation(this);

        boolean isDark = ThemeUtils.isDarkMode(this);
        int colorMain = isDark ? 0xFFE0E0E0 : 0xFF1C1B1F;
        int colorSec = isDark ? 0xFFAAAAAA : 0xFF666666;
        int colorAccent = isDark ? 0xFF81C784 : 0xFF1B5E20;
        int bgSec = isDark ? R.drawable.watch_btn_secondary_dark : R.drawable.watch_btn_secondary;
        android.graphics.drawable.GradientDrawable pillBg = WatchUiUtils.getPillBackground(this);

        View rootCn = findViewById(R.id.root_cross_net);
        rootCn.setBackgroundColor(isDark ? 0xFF000000 : 0xFFF4F6F8);
        WatchUiUtils.applyRoundPadding(this, rootCn);

        // 1. 圆屏隐藏顶栏
        boolean isRoundOpt = getSharedPreferences("oldsend", MODE_PRIVATE).getBoolean("round_screen_opt", false);
        View topBar = findViewById(R.id.cross_net_top_bar);
        if (topBar != null) topBar.setVisibility(isRoundOpt ? View.GONE : View.VISIBLE);

        // 2. 初始化 UI 控件
        tvStatus = (TextView) findViewById(R.id.tv_cn_status);
        tvPinDisplay = (TextView) findViewById(R.id.tv_cn_pin_display);
        tvFileInfo = (TextView) findViewById(R.id.tv_cn_file_info);
        tvIpv6Info = (TextView) findViewById(R.id.tv_ipv6_info);
        tvStunInfo = (TextView) findViewById(R.id.tv_stun_info);
        
        etPin = (EditText) findViewById(R.id.et_cn_pin);
        etDirectIp = (EditText) findViewById(R.id.et_direct_ip);
        
        layoutSelectedFiles = (LinearLayout) findViewById(R.id.layout_cn_selected_files);
        layoutDirectIp = (LinearLayout) findViewById(R.id.layout_direct_ip);
        
        cbUseDirect = (CheckBox) findViewById(R.id.cb_use_direct);
        btnGen = (Button) findViewById(R.id.btn_cn_generate);
        btnRecv = (Button) findViewById(R.id.btn_cn_receive);
        btnStunProbe = (Button) findViewById(R.id.btn_stun_probe);
        btnUpnpPunch = (Button) findViewById(R.id.btn_upnp_punch);

        // 3. 动态适配所有颜色与背景
        ((Button) findViewById(R.id.btn_cn_back)).setTextColor(colorMain);
        ((TextView) findViewById(R.id.tv_cross_net_title)).setTextColor(colorMain);
        tvCrossNetDescSetColor(colorSec);

        tvIpv6Info.setBackground(pillBg);
        tvIpv6Info.setTextColor(colorAccent);

        ((View) btnStunProbe.getParent()).setBackground(pillBg);
        tvStunInfo.setTextColor(colorSec);
        btnStunProbe.setBackgroundResource(bgSec);
        btnStunProbe.setTextColor(colorAccent);

        ((View) btnUpnpPunch.getParent()).setBackground(pillBg);
        cbUseDirect.setTextColor(colorMain);
        btnUpnpPunch.setBackgroundResource(bgSec);
        btnUpnpPunch.setTextColor(colorAccent);

        etDirectIp.setBackground(pillBg);
        etDirectIp.setTextColor(colorMain);
        etDirectIp.setHintTextColor(colorSec);

        layoutSelectedFiles.setBackground(pillBg);
        tvFileInfo.setTextColor(colorMain);

        // 接收区域
        ((View) etPin.getParent()).setBackground(pillBg);
        etPin.setTextColor(colorMain);
        etPin.setHintTextColor(colorSec);
        btnRecv.setBackgroundResource(bgSec);
        btnRecv.setTextColor(colorAccent);

        tvPinDisplay.setTextColor(colorAccent);
        tvStatus.setTextColor(colorAccent);
        ((TextView) findViewById(R.id.tv_advanced_tip)).setTextColor(colorSec);

        // 4. 绑定事件
        findViewById(R.id.btn_cn_back).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });

        findViewById(R.id.btn_cn_add_files).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pendingAppend = true;
                pickFile();
            }
        });

        findViewById(R.id.btn_cn_edit_files).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.pendingUris = pendingUris;
                startActivity(new Intent(CrossNetActivity.this, SelectListActivity.class));
            }
        });

        cbUseDirect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layoutDirectIp.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (isChecked) {
                    tvPinDisplay.setVisibility(View.GONE);
                    btnGen.setText("直连发送已选文件");
                } else {
                    btnGen.setText("生成密码并开始发送");
                }
            }
        });

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
                            Toast.makeText(CrossNetActivity.this, "请输入对方公网IP和端口", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        startDirectSend(ipStr);
                    } else {
                        generatePinAndSend();
                    }
                }
            }
        });

        btnRecv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isTransferring) return;
                String pin = etPin.getText().toString().trim();
                if (pin.length() != 6) {
                    Toast.makeText(CrossNetActivity.this, "请输入6位纯数字密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                startReceive(pin);
            }
        });

        btnStunProbe.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tvStunInfo.setText("STUN 探测中...");
                new Thread(new Runnable() {
                    public void run() {
                        final String result = org.watchsend.util.StunClient.getPublicAddress();
                        handler.post(new Runnable() {
                            public void run() {
                                if (result.contains("失败") || result.contains("未知")) {
                                    tvStunInfo.setText("NAT 穿透失败 (对称型NAT)");
                                    tvStunInfo.setTextColor(0xFFD32F2F);
                                } else {
                                    tvStunInfo.setText("NAT: " + result);
                                    tvStunInfo.setTextColor(0xFF388E3C);
                                    etDirectIp.setText(result);
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        btnUpnpPunch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isTransferring = true;
                tvStatus.setText("正在向路由器申请 UPnP 端口映射...");
                new Thread(new Runnable() {
                    public void run() {
                        final String result = requestUPnP();
                        handler.post(new Runnable() {
                            public void run() {
                                isTransferring = false;
                                tvStatus.setText(result);
                                if (result.contains("成功")) {
                                    cbUseDirect.setChecked(true);
                                }
                            }
                        });
                    }
                }).start();
            }
        });
    }

    private void tvCrossNetDescSetColor(int color) {
        ((TextView) findViewById(R.id.tv_cross_net_desc)).setTextColor(color);
    }

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
            btnGen.setText("选择文件并生成密码");
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
            tvFileInfo.setText("已选择 " + pendingUris.size() + " 个文件, 总计 " + sizeStr);
            btnGen.setText("生成密码并开始发送");
        }
    }

    private void generatePinAndSend() {
        int pin = 100000 + new Random().nextInt(900000);
        currentPin = String.valueOf(pin);
        tvPinDisplay.setText("密码: " + currentPin);
        tvPinDisplay.setVisibility(View.VISIBLE);
        startSend(currentPin);
    }

    private void startSend(final String pin) {
        isTransferring = true;
        tvStatus.setText("正在打包文件...");
        
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
                    
                    handler.post(new Runnable() { public void run() { tvStatus.setText("打包完成，正在建立极客通道..."); } });
                    
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
                        handler.post(new Runnable() { public void run() { tvStatus.setText("正在发送... 已发送 " + (finalSent/1024) + " KB"); } });
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
                            tvStatus.setText("发送完成！等待对方接收完毕。"); 
                            isTransferring = false; 
                        }
                    });
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        public void run() { tvStatus.setText("发送失败: " + e.getMessage()); isTransferring = false; }
                    });
                }
            }
        }).start();
    }

    private void startDirectSend(String ipStr) {
        String cleanIp = ipStr.trim();
        int port = 53317;
        
        if (cleanIp.contains(OldSendService.getLocalIp())) {
            tvStatus.setText("直连失败: 无法通过公网IP直连本机，请使用另一台设备。");
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
        tvStatus.setText("正在读取文件并准备直连...");

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
                        Toast.makeText(CrossNetActivity.this, "读取文件失败", Toast.LENGTH_SHORT).show();
                        isTransferring = false;
                        tvStatus.setText("准备失败");
                    }});
                    return;
                }

                handler.post(new Runnable() { public void run() {
                    tvStatus.setText("正在通过公网直连发送...");
                }});

                org.watchsend.transfer.Uploader.uploadFiles(target, files, new org.watchsend.transfer.Uploader.ProgressCallback() {
                    public void onProgress(final String stage, final long sent, final long total) {
                        handler.post(new Runnable() { public void run() {
                            if (total > 0) {
                                int pct = (int)(sent * 100 / total);
                                tvStatus.setText("极速发送中... " + pct + "% (" + (sent/1024) + "/" + (total/1024) + " KB)");
                            }
                        }});
                    }
                    public void onDone(final boolean ok, final String msg) {
                        handler.post(new Runnable() { public void run() {
                            isTransferring = false;
                            tvStatus.setText(ok ? "公网极速直连发送完成！" : "直连失败: " + msg);
                        }});
                    }
                });
            }
        }).start();
    }

    private void startReceive(final String pin) {
        isTransferring = true;
        tvStatus.setText("正在建立连接...");
        
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
                                        handler.post(new Runnable() { public void run() { tvStatus.setText("接收中... 已接收 " + (size/1024) + " KB"); } });
                                    }
                                    lastTime = time;
                                }
                            }
                        }
                        reader.close(); is.close();
                        if (!finished) Thread.sleep(1000);
                    }
                    fos.close();
                    
                    handler.post(new Runnable() { public void run() { tvStatus.setText("接收完成，正在解压..."); } });
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
                            tvStatus.setText("接收完成！已解压到 Download 目录"); 
                            isTransferring = false; 
                        }
                    });
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        public void run() { tvStatus.setText("接收失败: " + e.getMessage()); isTransferring = false; }
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

    private String requestUPnP() {
        try {
            java.net.DatagramSocket ssdpSock = new java.net.DatagramSocket();
            ssdpSock.setSoTimeout(2000);
            String mSearch = "M-SEARCH * HTTP/1.1\r\n" +
                    "HOST: 239.255.255.250:1900\r\n" +
                    "MAN: \"ssdp:discover\"\r\n" +
                    "MX: 3\r\n" +
                    "ST: urn:schemas-upnp-org:device:InternetGatewayDevice:1\r\n\r\n";
            byte[] data = mSearch.getBytes("UTF-8");
            java.net.DatagramPacket packet = new java.net.DatagramPacket(data, data.length,
                    java.net.InetAddress.getByName("239.255.255.250"), 1900);
            ssdpSock.send(packet);

            byte[] buf = new byte[2048];
            java.net.DatagramPacket recv = new java.net.DatagramPacket(buf, buf.length);
            String location = null;
            try {
                ssdpSock.receive(recv);
                String response = new String(recv.getData(), 0, recv.getLength(), "UTF-8");
                for (String line : response.split("\r\n")) {
                    if (line.toUpperCase().startsWith("LOCATION:")) {
                        location = line.substring(9).trim();
                        break;
                    }
                }
            } catch (Exception e) {
                return "获取失败：路由器可能未开启 UPnP。";
            }
            ssdpSock.close();

            if (location == null) return "获取失败：未找到支持 UPnP 的路由器。";

            java.net.URL url = new java.net.URL(location);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            InputStream in = conn.getInputStream();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(in));
            String line;
            String controlUrl = null;
            String serviceType = null;
            
            while ((line = reader.readLine()) != null) {
                if (line.contains("<controlURL>") && line.contains("WANIPConnection")) {
                    int start = line.indexOf("<controlURL>") + 12;
                    int end = line.indexOf("</controlURL>");
                    if (start > 12 && end > start) {
                        controlUrl = line.substring(start, end);
                    }
                }
                if (line.contains("<serviceType>") && line.contains("WANIPConnection")) {
                    int start = line.indexOf("<serviceType>") + 13;
                    int end = line.indexOf("</serviceType>");
                    if (start > 13 && end > start) {
                        serviceType = line.substring(start, end);
                    }
                }
            }
            reader.close();

            if (controlUrl == null || serviceType == null) {
                return "映射失败：路由器 UPnP 接口异常。";
            }
            
            if (!controlUrl.startsWith("http")) {
                java.net.URL base = new java.net.URL(location);
                controlUrl = base.getProtocol() + "://" + base.getHost() + (base.getPort() != -1 ? ":" + base.getPort() : "") + (controlUrl.startsWith("/") ? "" : "/") + controlUrl;
            }

            String localIp = OldSendService.getLocalIp();
            String soapBody = "<?xml version=\"1.0\"?>\n" +
                    "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                    "<s:Body>\n" +
                    "<u:AddPortMapping xmlns:u=\"" + serviceType + "\">\n" +
                    "<NewRemoteHost></NewRemoteHost>\n" +
                    "<NewExternalPort>53317</NewExternalPort>\n" +
                    "<NewProtocol>TCP</NewProtocol>\n" +
                    "<NewInternalPort>53317</NewInternalPort>\n" +
                    "<NewInternalClient>" + localIp + "</NewInternalClient>\n" +
                    "<NewEnabled>1</NewEnabled>\n" +
                    "<NewPortMappingDescription>WatchSend</NewPortMappingDescription>\n" +
                    "<NewLeaseDuration>0</NewLeaseDuration>\n" +
                    "</u:AddPortMapping>\n" +
                    "</s:Body>\n" +
                    "</s:Envelope>";

            java.net.HttpURLConnection soapConn = (java.net.HttpURLConnection) new java.net.URL(controlUrl).openConnection();
            soapConn.setRequestMethod("POST");
            soapConn.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"");
            soapConn.setRequestProperty("SOAPAction", "\"" + serviceType + "#AddPortMapping\"");
            soapConn.setDoOutput(true);
            soapConn.getOutputStream().write(soapBody.getBytes("UTF-8"));
            
            int code = soapConn.getResponseCode();
            if (code == 200) {
                String pubIp = getPublicIP();
                return "穿透成功！你的公网地址是: " + pubIp + ":53317\n请让对方填入此地址。";
            } else {
                return "穿透失败：路由器拒绝映射请求(" + code + ")。";
            }
        } catch (Exception e) {
            return "穿透异常: " + e.getMessage();
        }
    }

    private String getPublicIP() {
        try {
            java.net.URL url = new java.net.URL("https://api.ipify.org");
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
            String ip = in.readLine();
            in.close();
            return ip;
        } catch (Exception e) {
            return "未知公网IP";
        }
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

package org.watchsend;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import org.watchsend.api.*;
import org.watchsend.api.WebShareHandler;
import org.watchsend.discovery.MulticastListener;
import org.watchsend.http.HttpServer;
import org.watchsend.http.Router;
import org.watchsend.protocol.Identity;
import org.watchsend.model.HistoryStore;
import org.watchsend.util.I18nManager;

public class OldSendService extends Service {
    public static boolean isServiceRunning = false;
    public interface LogSink { void log(String msg); }
    private static LogSink sink;
    public static void setSink(LogSink s) { sink = s; }
    public static void log(String msg) { LogSink s = sink; if (s != null) s.log(msg); }

    private HttpServer httpServer;
    private MulticastListener multicastListener;

    // ===== 空闲停服（设置项 idle_stop_server，默认关闭）=====
    /** 熄屏后静默多久停掉监听。选 3 分钟：足够覆盖"看完就熄屏"的短暂离开，又不至于半夜一直挂着。 */
    private static final long IDLE_STOP_DELAY_MS = 3L * 60 * 1000;
    /** 停服前发现仍有传输在进行时，最多再往后顺延多久一次。 */
    private static final long IDLE_STOP_RECHECK_MS = 60L * 1000;
    private final Handler idleHandler = new Handler(Looper.getMainLooper());
    private Runnable idleStopTask;
    private BroadcastReceiver screenReceiver;
    private volatile boolean listeningStopped = false;
    private volatile boolean destroyed = false;
    /** 当前正在进行的接收传输数，>0 时不允许空闲停服（否则会把传到一半的文件硬掐断）。 */
    private static volatile int activeTransfers = 0;

    public static void beginTransfer() { activeTransfers++; }
    public static void endTransfer() { if (activeTransfers > 0) activeTransfers--; }
    public static boolean hasActiveTransfer() { return activeTransfers > 0; }
    public static boolean isListeningStopped() { return sInstance != null && sInstance.listeningStopped; }
    private static volatile OldSendService sInstance;
    private WifiManager.MulticastLock multicastLock;

    // ===== 网络变化自动启停（设置项 auto_wifi）=====
    /** NetworkCallback 在 API 21 才有；用 Object 持有，避免低版本类加载时解析到不存在的类型。 */
    private Object netCallback;
    // ===== 低电量停服（设置项 battery_stop）=====
    private BroadcastReceiver batteryReceiver;
    private static final int LOW_BATTERY_LIMIT = 15;
    private static final int LOW_BATTERY_NOTIFY_ID = 101;

    public static boolean getPrefBooleanGlobal(String key, boolean def) {
        Context ctx = getContext();
        if (ctx == null) return def;
        return ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE).getBoolean(key, def);
    }
    
    public static String getPrefStringGlobal(String key, String def) {
        Context ctx = getContext();
        if (ctx == null) return def;
        return ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE).getString(key, def);
    }
    
    /** 默认 PIN，必须与设置页展示/编辑用的默认值一致（此前这里是 1234、设置页显示 0000，用户照着输必然 401）。 */
    public static final String DEFAULT_PIN = "0000";

    public static String getPin() {
        Context ctx = getContext();
        if (ctx == null) return "0000";
        return ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE).getString("pin_code", DEFAULT_PIN);
    }

    private static Context appContext;
    public static Context getContext() { return appContext; }
    @Override public void onCreate() {
        super.onCreate();
        sInstance = this;
        appContext = this;
        Identity.init(getApplicationContext());
        HistoryStore.get().init(this);
        startForegroundNotification();
        setupIdleStopWatcher();
        setupNetworkWatcher();
        setupBatteryWatcher();
    }

    /** 当前连着的 WiFi 名称；未连 WiFi 或系统隐藏 SSID（无定位权限）时返回 null。 */
    public static String currentWifiSsid() {
        Context ctx = getContext();
        if (ctx == null) return null;
        try {
            WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            if (wm == null) return null;
            String ssid = wm.getConnectionInfo().getSSID();
            if (ssid == null) return null;
            if (ssid.length() > 1 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            if (ssid.length() == 0 || "<unknown ssid>".equals(ssid) || "0x".equals(ssid)) return null;
            return ssid;
        } catch (Throwable t) { return null; }
    }

    public static java.util.Set<String> autoWifiSsids(Context ctx) {
        if (ctx == null) return new java.util.HashSet<String>();
        return ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE)
                .getStringSet("auto_wifi_ssids", new java.util.HashSet<String>());
    }

    public static void setAutoWifiSsids(Context ctx, java.util.Set<String> list) {
        if (ctx == null) return;
        java.util.Set<String> copy = list == null ? new java.util.HashSet<String>() : new java.util.HashSet<String>(list);
        ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE).edit()
                .putStringSet("auto_wifi_ssids", copy).commit();
    }

    @android.annotation.TargetApi(21)
    private static class NetWatcher extends ConnectivityManager.NetworkCallback {
        private final OldSendService host;
        NetWatcher(OldSendService h) { host = h; }
        @Override public void onAvailable(android.net.Network network) { host.postNetworkCheck(); }
        @Override public void onLost(android.net.Network network) { host.postNetworkCheck(); }
    }

    private void setupNetworkWatcher() {
        if (Build.VERSION.SDK_INT < 21) return;
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return;
            NetWatcher watcher = new NetWatcher(this);
            cm.registerNetworkCallback(new NetworkRequest.Builder().build(), watcher);
            netCallback = watcher;
        } catch (Throwable t) { log("网络监听注册失败: " + t); }
    }

    private void unregisterNetworkWatcher() {
        if (Build.VERSION.SDK_INT < 21 || netCallback == null) return;
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) cm.unregisterNetworkCallback((ConnectivityManager.NetworkCallback) netCallback);
        } catch (Throwable ignored) {}
        netCallback = null;
    }

    /** 网络刚切换时 SSID 往往还没就绪，延迟一拍再判断。 */
    void postNetworkCheck() {
        idleHandler.postDelayed(new Runnable() {
            public void run() { applyAutoWifi(); }
        }, 1200);
    }

    private void applyAutoWifi() {
        if (destroyed) return;
        if (!getPrefBooleanGlobal("auto_wifi", false)) return;
        String ssid = currentWifiSsid();
        if (ssid == null || ssid.length() == 0) return;
        java.util.Set<String> list = autoWifiSsids(this);
        if (list.contains(ssid)) {
            if (httpServer == null && !hasActiveTransfer()) {
                log("已连上记住的网络「" + ssid + "」，恢复监听");
                listeningStopped = false;
                restartServer(this);
            }
        } else if (httpServer != null && !hasActiveTransfer()) {
            // 离开已记住的网络就暂停监听，等回到白名单网络再自动起
            log("已离开记住的网络（当前「" + ssid + "」），暂停监听");
            stopListeningForIdle();
        }
    }

    private void setupBatteryWatcher() {
        batteryReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                if (!getPrefBooleanGlobal("battery_stop", false)) return;
                if (destroyed || httpServer == null) return;
                int level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
                int status = intent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1);
                boolean charging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING
                        || status == android.os.BatteryManager.BATTERY_STATUS_FULL;
                if (level < 0 || scale <= 0 || charging) return;
                int pct = level * 100 / scale;
                if (pct > LOW_BATTERY_LIMIT) return;
                if (hasActiveTransfer()) { log("电量 " + pct + "%，仍有传输，暂不停服"); return; }
                stopListeningForIdle();
                log("电量 " + pct + "%，已暂停监听（充电或亮屏后恢复）");
                notifyLowBattery(pct);
            }
        };
        try {
            registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        } catch (Throwable t) { log("电量监听注册失败: " + t); }
    }

    private void notifyLowBattery(int pct) {
        try {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;
            Notification.Builder b = Build.VERSION.SDK_INT >= 26
                    ? new Notification.Builder(this, "oldsend_channel")
                    : new Notification.Builder(this);
            b.setSmallIcon(android.R.drawable.ic_dialog_alert)
             .setContentTitle(I18nManager.get("battery_stop"))
             .setContentText(pct + "%")
             .setAutoCancel(true);
            nm.notify(LOW_BATTERY_NOTIFY_ID, b.build());
        } catch (Throwable ignored) {}
    }

    /**
     * 注册熄屏/亮屏监听（必须动态注册，SCREEN_OFF 没有静态声明方式）。
     * 只在 Service 存活期间注册，Service 结束随 onDestroy 注销。
     */
    private void setupIdleStopWatcher() {
        screenReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                String action = intent == null ? null : intent.getAction();
                if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    scheduleIdleStop();
                } else if (Intent.ACTION_SCREEN_ON.equals(action) || Intent.ACTION_USER_PRESENT.equals(action)) {
                    cancelIdleStop();
                    // 熄屏时停过的监听，亮屏立刻恢复，避免用户回来发现"搜不到设备"
                    if (listeningStopped) {
                        listeningStopped = false;
                        log("亮屏，恢复监听");
                        restartServer(OldSendService.this);
                    }
                }
            }
        };
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_SCREEN_OFF);
        f.addAction(Intent.ACTION_SCREEN_ON);
        f.addAction(Intent.ACTION_USER_PRESENT);
        try { registerReceiver(screenReceiver, f); } catch (Throwable t) { log("熄屏监听注册失败: " + t); }
    }

    /** 熄屏后开始倒计时；设置项关闭时直接忽略。 */
    private void scheduleIdleStop() {
        if (!getPrefBooleanGlobal("idle_stop_server", false)) return;
        cancelIdleStop();
        idleStopTask = new Runnable() {
            @Override public void run() {
                idleStopTask = null;
                if (!getPrefBooleanGlobal("idle_stop_server", false)) return;
                if (destroyed) return;
                if (hasActiveTransfer()) {
                    log("仍有传输在进行，空闲停服顺延");
                    idleHandler.postDelayed(new Runnable() {
                        @Override public void run() {
                            if (destroyed) return;
                            if (hasActiveTransfer()) scheduleIdleStopNow(); else scheduleIdleStop();
                        }
                    }, IDLE_STOP_RECHECK_MS);
                    return;
                }
                stopListeningForIdle();
            }
        };
        idleHandler.postDelayed(idleStopTask, IDLE_STOP_DELAY_MS);
    }

    /** 顺延到点后的再次检查：仍忙就继续等，不忙就停。 */
    private void scheduleIdleStopNow() {
        if (hasActiveTransfer()) {
            idleHandler.postDelayed(new Runnable() {
                @Override public void run() { if (hasActiveTransfer()) scheduleIdleStopNow(); else stopListeningForIdle(); }
            }, IDLE_STOP_RECHECK_MS);
        } else {
            stopListeningForIdle();
        }
    }

    private void cancelIdleStop() {
        if (idleStopTask != null) { idleHandler.removeCallbacks(idleStopTask); idleStopTask = null; }
    }

    /** 真正停掉 HTTP/组播监听，但保留前台服务与通知，方便亮屏后原地恢复。 */
    private void stopListeningForIdle() {
        if (httpServer == null && multicastListener == null) return;
        try { if (httpServer != null) httpServer.stop(); } catch (Throwable ignored) {}
        try { if (multicastListener != null) multicastListener.stop(); } catch (Throwable ignored) {}
        if (multicastLock != null) { try { multicastLock.release(); } catch (Throwable ignored) {} multicastLock = null; }
        httpServer = null;
        multicastListener = null;
        listeningStopped = true;
        log("熄屏空闲，已暂停监听（亮屏或打开应用后恢复）");
    }
    
    public static void showReceiveNotification(String alias, int count) {
        Context ctx = getContext();
        if (ctx == null) return;
        android.app.NotificationManager nm = (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        String channelId = "oldsend_receive";
        if (Build.VERSION.SDK_INT >= 26) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(channelId, "接收文件", android.app.NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        Intent acceptIntent = new Intent(ctx, NotificationReceiver.class);
        acceptIntent.setAction(NotificationReceiver.ACTION_ACCEPT);
        android.app.PendingIntent acceptPi = android.app.PendingIntent.getBroadcast(ctx, 1, acceptIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT);

        Intent rejectIntent = new Intent(ctx, NotificationReceiver.class);
        rejectIntent.setAction(NotificationReceiver.ACTION_REJECT);
        android.app.PendingIntent rejectPi = android.app.PendingIntent.getBroadcast(ctx, 2, rejectIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT);

        android.app.Notification.Builder b;
        if (Build.VERSION.SDK_INT >= 26) {
            b = new android.app.Notification.Builder(ctx, channelId);
        } else {
            b = new android.app.Notification.Builder(ctx);
        }
        b.setSmallIcon(android.R.drawable.stat_sys_download)
         .setContentTitle(I18nManager.get("receive_request"))
         .setContentText("【" + alias + "】 " + count + " files")
         .setAutoCancel(true);
        if (Build.VERSION.SDK_INT >= 16) {
            b.addAction(android.R.drawable.ic_input_add, I18nManager.get("accept"), acceptPi);
            b.addAction(android.R.drawable.ic_delete, I18nManager.get("reject"), rejectPi);
        }

        nm.notify(100, b.build());
    }

    public static String getAlias(Context ctx) {
        if (ctx == null) return Build.MODEL;
        SharedPreferences sp = ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE);
        String saved = sp.getString("alias", "");
        if (saved != null && saved.length() > 0) return saved;
        // 首次启动：生成随语言变化的随机别名（官方 alias_generator 的语义），
        // 否则同型号手表在同网段里全是 Build.MODEL，列表里分不清谁是谁。
        String generated;
        try {
            generated = org.watchsend.util.AliasGenerator.generate();
        } catch (Throwable t) {
            generated = Build.MODEL;
        }
        if (generated == null || generated.length() == 0) generated = Build.MODEL;
        sp.edit().putString("alias", generated).commit();
        return generated;
    }

    public static void setAlias(Context ctx, String name) {
        SharedPreferences sp = ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE);
        sp.edit().putString("alias", name).commit();
    }

    public static String getLocalIp() {
        // 默认优先返回 IPv4 用于局域网组播
        String v4 = getIpv4();
        if (v4 != null) return v4;
        String v6 = getIpv6();
        if (v6 != null) return v6;
        return "0.0.0.0";
    }

    public static String getIpv4() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isLoopback() || !ni.isUp()) continue;
                for (InetAddress a : Collections.list(ni.getInetAddresses())) {
                    if (!a.isLoopbackAddress() && a.getHostAddress().indexOf(':') < 0) return a.getHostAddress();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static String getIpv6() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isLoopback() || !ni.isUp()) continue;
                for (InetAddress a : Collections.list(ni.getInetAddresses())) {
                    if (!a.isLoopbackAddress() && a.getHostAddress().indexOf(':') >= 0) {
                        String ip = a.getHostAddress();
                        if (!ip.startsWith("fe80")) return ip;
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override public IBinder onBind(Intent intent) { return null; }
    
    private void startForegroundNotification() {
        String channelId = "oldsend_channel";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channelId, "OldSend后台服务", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            builder = new Notification.Builder(this, channelId);
        } else {
            builder = new Notification.Builder(this);
        }
        // 常驻通知直接给"停止服务"，不必再钻三层设置页去关
        Intent stopIntent = new Intent(this, OldSendService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPi = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentTitle(I18nManager.get("service_running"))
               .setContentText(I18nManager.get("service_ready"))
               .setSmallIcon(android.R.drawable.stat_sys_download_done)
               .setContentIntent(pi);
        try {
            // addAction 的 int 图标重载是 API 16 才有，低版本直接不带按钮
            if (Build.VERSION.SDK_INT >= 16) {
                builder.addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        I18nManager.get("stop_service"), stopPi);
            }
        } catch (Throwable ignored) {}

        startForeground(1, builder.build());
    }

    /** 改端口 / 切 HTTPS 后必须让监听真正按新配置重开，否则设置里改了等于没改。 */
    public static final String ACTION_RESTART = "org.watchsend.service.RESTART";
    public static final String ACTION_STOP = "org.watchsend.service.STOP";

    /** 彻底停掉前台服务（通知栏、磁贴、设置页共用）。 */
    public static void stopServer(Context ctx) {
        if (ctx == null) return;
        try { ctx.stopService(new Intent(ctx, OldSendService.class)); } catch (Throwable ignored) {}
    }

    public static void restartServer(Context ctx) {
        if (ctx == null) return;
        Intent i = new Intent(ctx, OldSendService.class);
        i.setAction(ACTION_RESTART);
        ctx.startService(i);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            log("收到停止指令，结束服务");
            stopSelf();
            return START_NOT_STICKY;
        }
        isServiceRunning = true;
        // 先拆掉旧监听（同一个 Service 实例里重建，避免 stop/start 竞态导致"服务已在运行中"直接返回）
        if (intent != null && ACTION_RESTART.equals(intent.getAction())) {
            try { if (httpServer != null) httpServer.stop(); } catch (Exception ignored) {}
            try { if (multicastListener != null) multicastListener.stop(); } catch (Exception ignored) {}
            if (multicastLock != null) { try { multicastLock.release(); } catch (Exception ignored) {} multicastLock = null; }
            httpServer = null; multicastListener = null;
            log("配置变更，重建监听");
        }
        if (getContext() != null) {
            QuickWidgetProvider.sendUpdateBroadcast(getContext());
            org.watchsend.watch.QuickScanTileService.requestRefresh(getContext());
        }
        if (httpServer != null && multicastListener != null) {
            log("服务已在运行中");
            return START_STICKY;
        }
        try {
            if (Build.VERSION.SDK_INT >= 12) {
                try {
                    WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    multicastLock = wm.createMulticastLock("oldsend");
                    multicastLock.setReferenceCounted(false);
                    multicastLock.acquire();
                } catch (Exception e) { log("multicast lock: " + e); }
            }
            String alias = getAlias(this);
            SessionStore store = new SessionStore();
            DownloadSessionStore downloadStore = new DownloadSessionStore();
            Router router = new Router();
            // 兼容 V2
            router.register(Protocol.V2_PREFIX + "/info", new InfoHandler(alias));
            router.register(Protocol.V2_PREFIX + "/register", new RegisterHandler(alias));
            router.register(Protocol.V2_PREFIX + "/prepare-upload", new PrepareUploadHandler(store));
            router.register(Protocol.V2_PREFIX + "/upload", new UploadHandler(store));
            router.register(Protocol.V2_PREFIX + "/cancel", new CancelHandler(store));
            // 官方 v2 下载 API（对方来取文件），仅在设置里开启共享下载后生效
            router.register(Protocol.V2_PREFIX + "/prepare-download", new PrepareDownloadHandler(downloadStore));
            router.register(Protocol.V2_PREFIX + "/download", new DownloadHandler(downloadStore));
            
            // 兼容老设备 V1
            String V1_PREFIX = "/api/localsend/v1";
            router.register(V1_PREFIX + "/info", new InfoHandler(alias));
            router.register(V1_PREFIX + "/register", new RegisterHandler(alias));
            router.register(V1_PREFIX + "/prepare-upload", new PrepareUploadHandler(store));
            router.register(V1_PREFIX + "/upload", new UploadHandler(store));
            router.register(V1_PREFIX + "/cancel", new CancelHandler(store));
            WebShareHandler webHandler = new WebShareHandler();
            router.register("/", webHandler);
            router.register("/index.html", webHandler);
            router.register("/web-upload", webHandler);
            // 补齐 v1.2 网页端所有 API 路由
            router.register("/web/info", webHandler);
            router.register("/web/clipboard", webHandler);
            router.register("/web/text", webHandler);
            router.register("/web/files", webHandler);
            router.register("/web/upload", webHandler);
            router.register("/web/download", webHandler);

            boolean useHttps = getSharedPreferences("oldsend", MODE_PRIVATE).getBoolean("use_https", false);
            javax.net.ssl.SSLContext sslContext = useHttps ? TlsHelper.getServerSSLContext(this) : null;
            int port = getSharedPreferences("oldsend", MODE_PRIVATE).getInt("port", Protocol.DEFAULT_PORT);
            httpServer = new HttpServer(port, router, sslContext);
            httpServer.start();
            log((useHttps ? "HTTPS" : "HTTP") + " 服务已启动，端口: " + port);

            multicastListener = new MulticastListener(port, alias);
            multicastListener.start();
            log("组播服务已启动: " + Protocol.MULTICAST_GROUP + ":" + Protocol.DEFAULT_PORT);
            log("本机IP: " + getLocalIp());
        } catch (Exception e) { log("启动失败: " + e); }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        cancelIdleStop();
        if (screenReceiver != null) { try { unregisterReceiver(screenReceiver); } catch (Throwable ignored) {} screenReceiver = null; }
        if (batteryReceiver != null) { try { unregisterReceiver(batteryReceiver); } catch (Throwable ignored) {} batteryReceiver = null; }
        unregisterNetworkWatcher();
        sInstance = null;
        isServiceRunning = false;
        if (getContext() != null) {
            QuickWidgetProvider.sendUpdateBroadcast(getContext());
            org.watchsend.watch.QuickScanTileService.requestRefresh(getContext());
        }
        try { if (httpServer != null) httpServer.stop(); } catch (Exception ignored) {}
        try { if (multicastListener != null) multicastListener.stop(); } catch (Exception ignored) {}
        try { if (multicastLock != null) multicastLock.release(); } catch (Exception ignored) {}
        httpServer = null; multicastListener = null;
        log("服务已停止");
        super.onDestroy();
    }
}

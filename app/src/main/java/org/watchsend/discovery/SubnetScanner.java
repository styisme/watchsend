package org.watchsend.discovery;

import android.content.Context;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.watchsend.OldSendService;
import org.watchsend.Protocol;
import org.watchsend.api.InfoHandler;
import org.watchsend.http.HttpClient;
import org.watchsend.model.Device;
import org.watchsend.util.TrustStore;
import org.watchsend.uikit.UikitConfig;

/**
 * 多播被路由器拦截时的回退发现（官方 discovery 也有类似的多播失败兜底）。
 *
 * 做法：对本机所在 /24 网段逐个探测 GET /v2/info，先 HTTP 再 HTTPS。
 * 手表 Wi-Fi 省电时对多播很不友好，这一步能显著提升"搜不到设备"时的成功率。
 */
public class SubnetScanner {

    private static final int PROBE_TIMEOUT_MS = 400;
    private static final int THREADS = 16;
    private static final long TOTAL_WAIT_MS = 9000;

    // 低性能模式：并发线程减半、单机超时放宽、总等待拉长，避免 254 个探测把弱 CPU 打满
    private static final int PROBE_TIMEOUT_MS_LOW = 700;
    private static final int THREADS_LOW = 6;
    private static final long TOTAL_WAIT_MS_LOW = 15000;

    public interface Callback {
        void onDevice(Device d);
        void onFinished();
    }

    public static void scanAsync(final Callback cb) {
        new Thread(new Runnable() {
            public void run() { doScan(cb); }
        }, "subnet-scan").start();
    }

    static void doScan(final Callback cb) {
        String localIp = OldSendService.getIpv4();
        if (localIp == null) {
            if (cb != null) cb.onFinished();
            return;
        }
        int lastDot = localIp.lastIndexOf('.');
        if (lastDot < 0) {
            if (cb != null) cb.onFinished();
            return;
        }
        final String prefix = localIp.substring(0, lastDot + 1);
        final int port = InfoHandler.selfPort();

        Context ctx = OldSendService.getContext();
        boolean low = ctx != null && UikitConfig.isLowPerf(ctx);
        final int timeoutMs = low ? PROBE_TIMEOUT_MS_LOW : PROBE_TIMEOUT_MS;
        ExecutorService pool = Executors.newFixedThreadPool(low ? THREADS_LOW : THREADS);
        final CountDownLatch latch = new CountDownLatch(254);
        for (int i = 1; i <= 254; i++) {
            final String host = prefix + i;
            if (host.equals(localIp)) {
                latch.countDown();
                continue;
            }
            pool.execute(new Runnable() {
                public void run() {
                    try {
                        probe(host, port, timeoutMs, cb);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        try {
            latch.await(low ? TOTAL_WAIT_MS_LOW : TOTAL_WAIT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) {
        }
        pool.shutdownNow();
        if (cb != null) cb.onFinished();
    }

    private static void probe(String host, int port, int timeoutMs, Callback cb) {
        // 1) 明文设备
        JSONObject info = null;
        String[] fp = new String[1];
        try {
            info = HttpClient.getJson("http://" + host + ":" + port + Protocol.V2_PREFIX + "/info",
                    null, timeoutMs, fp);
        } catch (Exception ignored) {
        }
        if (info == null) {
            // 2) 开启加密的设备（首次只读 info，拿到证书指纹后记录）
            fp[0] = null;
            try {
                info = HttpClient.getJson("https://" + host + ":" + port + Protocol.V2_PREFIX + "/info",
                        null, timeoutMs, fp);
            } catch (Exception ignored) {
            }
        }
        if (info == null || info.length() == 0) return;

        Device d = Device.fromJson(info, host);
        if (d.port <= 0) d.port = port;
        else if (d.port != port) OldSendService.log("端口不一致: info=" + d.port + " probe=" + port);
        if (d.https && fp[0] != null) {
            d.certFingerprint = fp[0];
            TrustStore.put(host, d.port, fp[0]);
        }
        OldSendService.log("回退发现: " + d.alias + " @ " + host + ":" + d.port);
        if (cb != null) cb.onDevice(d);
    }
}

package org.watchsend.transfer;

import java.io.File;
import android.content.Intent;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONObject;
import org.watchsend.OldSendService;
import org.watchsend.Protocol;
import org.watchsend.api.InfoHandler;
import org.watchsend.http.HttpClient;
import org.watchsend.model.Device;
import org.watchsend.protocol.Identity;
import org.watchsend.util.CryptoUtils;
import org.watchsend.util.TrustStore;

public class Uploader {
    public interface ProgressCallback {
        void onProgress(String stage, long sent, long total);
        void onDone(boolean ok, String msg);
    }

    /** 当前正在进行的会话（用于"本地取消时通知对端"，官方 cancelSession 语义）。 */
    private static volatile Device activeTarget;
    private static volatile String activeSessionId;
    private static volatile boolean cancelRequested;

    /**
     * 用户点取消：立刻置位停止标记，并向对端 POST /v2/cancel。
     * 已发完的文件不可回滚，但对端能立即释放会话，不必等 10 分钟 TTL。
     */
    public static void cancelCurrent() {
        cancelRequested = true;
        final Device t = activeTarget;
        final String sid = activeSessionId;
        if (t == null || sid == null) return;
        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONObject body = new JSONObject();
                    body.put("sessionId", sid);
                    HttpClient.postJson(t.url(Protocol.V2_PREFIX + "/cancel"), body, t.https ? t.certFingerprint : null, 5000, null);
                    OldSendService.log("已通知对端取消会话: " + sid);
                } catch (Exception e) {
                    OldSendService.log("通知取消失败: " + e.getMessage());
                }
            }
        }).start();
    }

    /** 摘要只在不超过这个体积时才预算，避免手表为几十 MB 的文件读两遍。 */
    private static final long SHA_MAX_BYTES = 32L * 1024 * 1024;
    /** 文本消息内联上限，与官方 message_preview 保持一致。 */
    private static final long TEXT_PREVIEW_MAX = 16L * 1024;

    public static void uploadFiles(final Device target, final List<File> files, final ProgressCallback cb) {
        uploadFiles(target, files, null, cb);
    }

    public static void uploadFiles(final Device target, final List<File> files, final String pin, final ProgressCallback cb) {
        new Thread(new Runnable() {
            public void run() {
                try { doUpload(target, files, pin, cb); } catch (Exception e) { if (cb != null) cb.onDone(false, e.toString()); }
            }
        }).start();
    }

    static void doUpload(Device target, List<File> files, String pin, final ProgressCallback cb) throws Exception {
        if (files.isEmpty()) return;

        cancelRequested = false;
        activeTarget = target;
        activeSessionId = null;
        JSONObject info = buildInfo(target);
        JSONObject filesJson = new JSONObject();
        long totalSize = 0;
        String[] fileIds = new String[files.size()];
        boolean textMessage = isTextMessage(files);
        String preview = textMessage ? readTextPreview(files.get(0)) : null;

        for (int i = 0; i < files.size(); i++) {
            File f = files.get(i);
            String id = UUID.randomUUID().toString().replace("-", "");
            fileIds[i] = id;
            totalSize += f.length();
            JSONObject fileDto = new JSONObject();
            fileDto.put("id", id); fileDto.put("fileName", f.getName());
            fileDto.put("size", f.length());
            fileDto.put("fileType", textMessage ? "text" : guessMime(f.getName()));
            // 大文件不预算摘要：双方按官方语义把它当可选字段
            if (f.length() <= SHA_MAX_BYTES) {
                String sha = CryptoUtils.sha256File(f);
                if (sha != null) fileDto.put("sha256", sha);
            }
            JSONObject meta = new JSONObject();
            meta.put("modified", CryptoUtils.iso8601(f.lastModified()));
            fileDto.put("metadata", meta);
            if (preview != null) fileDto.put("preview", preview);
            filesJson.put(id, fileDto);
        }

        JSONObject prepareBody = new JSONObject();
        prepareBody.put("info", info); prepareBody.put("files", filesJson);

        cb.onProgress("准备上传", 0, totalSize);
        String prepareUrl = target.url(Protocol.V2_PREFIX + "/prepare-upload");
        if (pin != null && pin.length() > 0) prepareUrl += "?pin=" + pin;

        // HTTPS：优先用已记住的指纹做固定校验；首次（null）只读一次握手取指纹再落库（TOFU）
        String knownFp = target.certFingerprint;
        if (knownFp == null || knownFp.length() == 0) knownFp = TrustStore.get(target.ip, target.port);
        String[] fpOut = new String[1];
        JSONObject resp;
        try {
            resp = HttpClient.postJson(prepareUrl, prepareBody, knownFp, 15000, fpOut);
        } catch (Exception e) {
            // 指纹不匹配时 TLS 握手阶段就会失败，这里明确告知而不是笼统报错
            if (target.https && knownFp != null && knownFp.length() > 0) {
                cb.onDone(false, "对方证书指纹已变化，已拒绝连接（可能被冒充）");
                return;
            }
            cb.onDone(false, "连接失败: " + e.getMessage());
            return;
        }
        if (target.https && fpOut[0] != null) {
            if (knownFp == null || knownFp.length() == 0) TrustStore.put(target.ip, target.port, fpOut[0]);
            target.certFingerprint = fpOut[0];
        }
        if (resp == null) { cb.onDone(false, "对方拒绝接收"); return; }
        String sessionId = resp.optString("sessionId", null);
        activeSessionId = sessionId;
        JSONObject respFiles = resp.optJSONObject("files");
        if (sessionId == null || respFiles == null) { cb.onDone(false, "响应无效"); return; }

        final AtomicLong totalSent = new AtomicLong(0);
        ExecutorService pool = Executors.newFixedThreadPool(3);
        final long finalTotalSize = totalSize;
        final boolean[] hasError = new boolean[]{false};
        final String fpForUpload = target.https ? (target.certFingerprint != null ? target.certFingerprint : knownFp) : null;

        for (int i = 0; i < files.size(); i++) {
            final File f = files.get(i);
            final String fileId = fileIds[i];
            final String token = respFiles.optString(fileId, null);
            if (token == null) continue;
            final String uploadUrl = target.url(Protocol.V2_PREFIX + "/upload")
                    + "?sessionId=" + enc(sessionId) + "&fileId=" + enc(fileId) + "&token=" + enc(token);

            pool.execute(new Runnable() {
                public void run() {
                    if (cancelRequested) return;  // 取消后不再发后续文件
                    try {
                        boolean ok = HttpClient.uploadFile(uploadUrl, f, fpForUpload, new HttpClient.ProgressListener() {
                            public void onProgress(long sent, long fileSize) {
                                long current = totalSent.addAndGet(0);
                                // 进度以"已完成字节 + 本文件已发字节"近似统计
                                if (cb != null) cb.onProgress("发送中", current + sent, finalTotalSize);
                            }
                        });
                        if (!ok) hasError[0] = true;
                        totalSent.addAndGet(f.length());
                    } catch (Exception e) {
                        hasError[0] = true;
                        OldSendService.log("upload fail: " + f.getName() + " " + e);
                    }
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.HOURS);

        if (hasError[0]) {
            cb.onDone(false, "部分文件上传失败");
        } else {
            cb.onDone(true, "发送成功");
        }
        if (OldSendService.getContext() != null) {
            OldSendService.getContext().sendBroadcast(
                new Intent("org.watchsend.TRANSFER_DONE").putExtra("ok", true));
        }
    }

    private static boolean isTextMessage(List<File> files) {
        if (files.size() != 1) return false;
        File f = files.get(0);
        if (f.length() > TEXT_PREVIEW_MAX) return false;
        String n = f.getName().toLowerCase();
        return n.endsWith(".txt") || n.endsWith(".md") || n.endsWith(".log");
    }

    private static String readTextPreview(File f) {
        try {
            java.io.FileInputStream in = new java.io.FileInputStream(f);
            try {
                byte[] buf = new byte[(int) f.length()];
                int off = 0;
                while (off < buf.length) {
                    int n = in.read(buf, off, buf.length - off);
                    if (n <= 0) break;
                    off += n;
                }
                return new String(buf, 0, off, "UTF-8");
            } finally { in.close(); }
        } catch (Exception e) {
            return null;
        }
    }

    static boolean uploadFile(String url, File file, final long baseSent, final long finalTotalSize, final ProgressCallback cb) throws Exception {
        long size = file.length();
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(15000); c.setReadTimeout(300000);
        c.setRequestMethod("POST"); c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/octet-stream");
        c.setFixedLengthStreamingMode((int) size);
        try {
            OutputStream os = c.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] buf = new byte[65536];
            int n;
            long fileSent = 0;
            while ((n = fis.read(buf)) > 0) {
                os.write(buf, 0, n); fileSent += n;
                if (cb != null) cb.onProgress("发送中", baseSent + fileSent, finalTotalSize);
            }
            os.flush(); fis.close(); os.close();
        } catch (Exception e) { c.disconnect(); throw e; }
        try { return c.getResponseCode() >= 200 && c.getResponseCode() < 300; }
        finally { c.disconnect(); }
    }

    /**
     * 发送方的自述信息：protocol / port / download 必须与真实监听状态一致，
     * 否则对方会按明文或"不可下载"处理（官方 2.x 默认 https，写死 http 会直接不可互通）。
     */
    static JSONObject buildInfo(Device target) throws Exception {
        JSONObject o = new JSONObject();
        o.put("alias", OldSendService.getAlias(null));
        o.put("version", Protocol.VERSION);
        o.put("deviceModel", "Android " + android.os.Build.VERSION.RELEASE);
        o.put("deviceType", "mobile");
        o.put("fingerprint", Identity.getFingerprint());
        o.put("port", InfoHandler.selfPort());
        o.put("protocol", InfoHandler.selfProtocol());
        o.put("download", DownloadShare.isEnabled());
        return o;
    }

    public static String guessMime(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".mp4")) return "video/mp4";
        return "application/octet-stream";
    }

    static String enc(String s) { try { return java.net.URLEncoder.encode(s, "UTF-8"); } catch (Exception e) { return s; } }
}

package org.watchsend.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Map;

import org.json.JSONObject;
import org.watchsend.OldSendService;
import org.watchsend.http.HttpRequest;
import org.watchsend.http.HttpResponse;
import org.watchsend.http.Router;
import org.watchsend.model.HistoryStore;
import org.watchsend.transfer.FileSaver;
import org.watchsend.util.CryptoUtils;

public class UploadHandler implements Router.Handler {
    private final SessionStore store;

    public UploadHandler(SessionStore store) { this.store = store; }

    public HttpResponse handle(HttpRequest req) throws Exception {
        if (!"POST".equalsIgnoreCase(req.method)) return HttpResponse.status(405);
        Map<String, String> q = req.getQueryParams();
        String sessionId = q.get("sessionId");
        String fileId = q.get("fileId");
        String token = q.get("token");

        if (sessionId == null || fileId == null || token == null) return HttpResponse.status(400);
        UploadSession session = store.get(sessionId);
        if (session == null) return HttpResponse.status(400);
        UploadSession.FileEntry entry = session.getFile(fileId);
        if (entry == null || !entry.token.equals(token)) return HttpResponse.status(403);

        boolean saveAlbum = OldSendService.getPrefBooleanGlobal("save_album", true);
        File target = FileSaver.createTarget(entry.fileName, saveAlbum);
        OldSendService.log("上传: " + entry.fileName + " -> " + target.getAbsolutePath());

        long written = 0;
        long existingSize = 0;
        if (target.exists()) existingSize = target.length();

        MessageDigest digest = null;
        try { digest = MessageDigest.getInstance("SHA-256"); } catch (Exception ignored) {}

        // 如果存在并且大小小于总大小，则断点续传（追加模式）
        FileOutputStream fos = new FileOutputStream(target, existingSize > 0);
        try {
            long remaining = req.contentLength > 0 ? req.contentLength : entry.size;
            if (existingSize > 0 && remaining > 0) {
                OldSendService.log("断点续传追加: 已存在 " + existingSize + " 字节");
                // 追加模式下摘要要从已落盘的前缀算起，否则校验必然失败
                if (digest != null) feedDigest(digest, target, existingSize);
                written = existingSize;
                long toSkip = existingSize;
                while (toSkip > 0) {
                    long s = req.body.skip(toSkip);
                    if (s <= 0) break;
                    toSkip -= s;
                }
                remaining -= existingSize;
            }
            InputStream in = req.body;
            byte[] buf = new byte[65536];
            if (remaining <= 0) remaining = Long.MAX_VALUE;
            while (remaining > 0) {
                int max = (int) Math.min(buf.length, remaining);
                int n = in.read(buf, 0, max);
                if (n <= 0) break;
                fos.write(buf, 0, n);
                if (digest != null) digest.update(buf, 0, n);
                written += n;
                android.content.Intent intent = new android.content.Intent("org.watchsend.UPDATE_PROGRESS");
                intent.putExtra("current", written);
                intent.putExtra("total", entry.size > 0 ? entry.size : req.contentLength);
                intent.putExtra("fileName", entry.fileName);
                OldSendService.getContext().sendBroadcast(intent);
                remaining -= n;
            }
        } finally {
            try { fos.close(); } catch (Exception ignored) {}
        }

        // 完整性校验：发送方提供了 sha256 时逐字节比对（官方 crypto/hash.rs 同等语义）
        if (entry.sha256 != null && digest != null) {
            String actual = CryptoUtils.hex(digest.digest());
            if (!actual.equalsIgnoreCase(CryptoUtils.normalizeFingerprint(entry.sha256))) {
                OldSendService.log("校验失败: " + entry.fileName + " 期望 " + entry.sha256 + " 实际 " + actual);
                target.delete();
                return HttpResponse.status(400);
            }
        }
        // 还原修改时间（metadata.modified 是 RFC3339）
        if (entry.modified != null) {
            long ms = CryptoUtils.parseIso8601(entry.modified);
            if (ms > 0) target.setLastModified(ms);
        }

        if (entry.size > 0 && written != entry.size) {
            OldSendService.log("接收不完整: " + entry.fileName + " (" + written + "/" + entry.size + ")");
        }
        OldSendService.log("接收完成: " + entry.fileName + " (" + written + " 字节)");
        // Android 10+ 落在应用私有目录时，通知媒体库索引，相册/文件管理器才看得见
        FileSaver.publish(target);
        OldSendService.getContext().sendBroadcast(new android.content.Intent("org.watchsend.TRANSFER_DONE").putExtra("ok", true).putExtra("savedFile", target.getAbsolutePath()));

        // 记录到历史
        String timeStr = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date());
        boolean saveHistory = OldSendService.getPrefBooleanGlobal("save_history", true);
        if (saveHistory) {
            HistoryStore.get().add(OldSendService.getContext(), new HistoryStore.Record(entry.fileName, target.getAbsolutePath(), written, timeStr, true, System.currentTimeMillis()));
        }

        // 文本消息：优先用发送方内联的 preview，避免再从磁盘读一遍；否则按 .txt 读文件。
        // 超过 16KB 的 preview 说明对方是旧版本/异常实现，按官方语义当普通文件处理，不内联展示。
        String text = entry.preview;
        if (text != null && text.length() > 16384) {
            OldSendService.log("预览超过 16KB，按普通文件接收: " + entry.fileName);
            text = null;
        } else if (text == null && entry.fileName.toLowerCase().endsWith(".txt") && written < 10 * 1024) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(target);
                byte[] bytes = new byte[(int) written];
                int off = 0;
                while (off < bytes.length) {
                    int n = fis.read(bytes, off, bytes.length - off);
                    if (n < 0) break;
                    off += n;
                }
                text = new String(bytes, 0, off, "UTF-8");
            } catch (Exception e) {
                OldSendService.log("读取文本失败: " + e);
            } finally {
                if (fis != null) try { fis.close(); } catch (Exception ignored) {}
            }
        }
        if (text != null && text.length() > 0 && text.length() < 10 * 1024) {
            try {
                android.content.ClipboardManager cm = (android.content.ClipboardManager)
                        OldSendService.getContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(android.content.ClipData.newPlainText("OldSend", text));
                OldSendService.log("文本已自动复制到剪贴板");
            } catch (Exception e) {
                OldSendService.log("自动复制失败: " + e);
            }
        }

        session.markDone(fileId);
        if (session.allDone()) store.remove(sessionId);

        JSONObject resp = new JSONObject();
        resp.put("sessionId", sessionId);
        return HttpResponse.json(resp.toString());
    }

    /** 把已存在的文件前缀喂给摘要对象，仅在断点续传追加时需要。 */
    private static void feedDigest(MessageDigest digest, File f, long limit) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            byte[] buf = new byte[65536];
            long left = limit;
            while (left > 0) {
                int n = fis.read(buf, 0, (int) Math.min(buf.length, left));
                if (n <= 0) break;
                digest.update(buf, 0, n);
                left -= n;
            }
        } catch (Exception e) {
            OldSendService.log("摘要续算失败: " + e);
        } finally {
            if (fis != null) try { fis.close(); } catch (Exception ignored) {}
        }
    }
}

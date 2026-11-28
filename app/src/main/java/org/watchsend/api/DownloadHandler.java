package org.watchsend.api;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import org.watchsend.OldSendService;
import org.watchsend.http.HttpRequest;
import org.watchsend.http.HttpResponse;
import org.watchsend.http.Router;
import org.watchsend.transfer.DownloadShare;

/**
 * GET /api/localsend/v2/download?sessionId=&fileId=&token=
 *
 * 对端按 prepare-download 拿到的 token 取文件内容，响应为二进制流。
 */
public class DownloadHandler implements Router.Handler {

    private final DownloadSessionStore store;

    public DownloadHandler(DownloadSessionStore store) { this.store = store; }

    public HttpResponse handle(HttpRequest req) throws Exception {
        if (!"GET".equalsIgnoreCase(req.method)) return HttpResponse.status(405);
        if (!DownloadShare.isEnabled()) return HttpResponse.status(403);

        Map<String, String> q = req.getQueryParams();
        String sessionId = q.get("sessionId");
        String fileId = q.get("fileId");
        String token = q.get("token");
        if (sessionId == null || fileId == null || token == null) return HttpResponse.status(400);

        Map<String, DownloadSessionStore.Entry> session = store.get(sessionId);
        if (session == null) return HttpResponse.status(400);
        DownloadSessionStore.Entry entry = session.get(fileId);
        if (entry == null || !entry.token.equals(token)) return HttpResponse.status(403);

        File f = entry.file;
        if (f == null || !f.isFile() || f.length() <= 0) return HttpResponse.status(404);

        long total = f.length();
        long start = 0;
        long end = total - 1;
        boolean partial = false;
        // 对端断线后带 Range 重来时只补缺的那段，不必从头再拉一遍
        String range = req.headers.get("range");
        if (range != null && range.startsWith("bytes=")) {
            String spec = range.substring(6).trim();
            int comma = spec.indexOf(',');
            if (comma > 0) spec = spec.substring(0, comma);
            int dash = spec.indexOf('-');
            try {
                if (dash > 0) {
                    start = Long.parseLong(spec.substring(0, dash));
                    String tail = spec.substring(dash + 1).trim();
                    if (tail.length() > 0) end = Long.parseLong(tail);
                } else if (dash == 0 && spec.length() > 1) {
                    // bytes=-N：末尾 N 字节
                    long suffix = Long.parseLong(spec.substring(1));
                    start = Math.max(0, total - suffix);
                }
                partial = true;
            } catch (NumberFormatException e) {
                start = 0; end = total - 1; partial = false;
            }
        }
        if (start < 0) start = 0;
        if (partial && start >= total) {
            HttpResponse bad = HttpResponse.status(416);
            bad.headers.put("Content-Range", "bytes */" + total);
            return bad;
        }
        if (end >= total) end = total - 1;

        HttpResponse resp = new HttpResponse();
        resp.contentType = "application/octet-stream";
        resp.headers.put("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"");
        resp.headers.put("Accept-Ranges", "bytes");
        FileInputStream in = new FileInputStream(f);
        if (start > 0) {
            long skipped = 0;
            while (skipped < start) {
                long n = in.skip(start - skipped);
                if (n <= 0) break;
                skipped += n;
            }
        }
        resp.stream = in;
        resp.contentLength = end - start + 1;
        if (partial) {
            resp.status = 206;
            resp.headers.put("Content-Range", "bytes " + start + "-" + end + "/" + total);
            OldSendService.log("下载 API 续传: " + f.getName() + " [" + start + "-" + end + "/" + total + "]");
        } else {
            OldSendService.log("下载 API 提供: " + f.getName() + " (" + total + " 字节)");
        }
        return resp;
    }
}

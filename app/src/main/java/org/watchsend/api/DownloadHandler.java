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

        HttpResponse resp = new HttpResponse();
        resp.contentType = "application/octet-stream";
        resp.headers.put("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"");
        resp.stream = new FileInputStream(f);
        resp.contentLength = f.length();
        OldSendService.log("下载 API 提供: " + f.getName() + " (" + f.length() + " 字节)");
        return resp;
    }
}

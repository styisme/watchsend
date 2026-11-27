package org.watchsend.api;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.watchsend.OldSendService;
import org.watchsend.http.HttpRequest;
import org.watchsend.http.HttpResponse;
import org.watchsend.http.Router;
import org.watchsend.transfer.DownloadShare;
import org.watchsend.transfer.Uploader;
import org.watchsend.util.CryptoUtils;

/**
 * POST /api/localsend/v2/prepare-download
 *
 * 把本机可下载的文件清单交给对端（官方 PrepareDownloadResponseDtoV2：
 * info + sessionId + files）。未开启共享下载或 PIN 不符时拒绝。
 */
public class PrepareDownloadHandler implements Router.Handler {

    private final DownloadSessionStore store;

    public PrepareDownloadHandler(DownloadSessionStore store) { this.store = store; }

    public HttpResponse handle(HttpRequest req) throws Exception {
        if (!"POST".equalsIgnoreCase(req.method)) return HttpResponse.status(405);
        if (!DownloadShare.isEnabled()) return HttpResponse.status(403);

        if (!checkPin(req)) {
            HttpResponse res = HttpResponse.status(401);
            res.headers.put("X-LocalSend-Pin", "true");
            return res;
        }

        List<File> files = DownloadShare.listFiles();
        String sessionId = store.create(files);
        Map<String, DownloadSessionStore.Entry> session = store.get(sessionId);

        JSONObject filesJson = new JSONObject();
        Iterator<Map.Entry<String, DownloadSessionStore.Entry>> it = session.entrySet().iterator();
        while (it.hasNext()) {
            DownloadSessionStore.Entry entry = it.next().getValue();
            JSONObject f = new JSONObject();
            f.put("id", entry.fileId);
            f.put("fileName", entry.fileName);
            f.put("size", entry.size);
            f.put("fileType", Uploader.guessMime(entry.fileName));
            // 大文件不预算摘要，避免对端一请求就把手表 I/O 打满
            if (entry.size > 0 && entry.size <= 32L * 1024 * 1024) {
                String sha = CryptoUtils.sha256File(entry.file);
                if (sha != null) f.put("sha256", sha);
            }
            if (entry.file.lastModified() > 0) {
                JSONObject md = new JSONObject();
                md.put("modified", CryptoUtils.iso8601(entry.file.lastModified()));
                f.put("metadata", md);
            }
            filesJson.put(entry.fileId, f);
        }

        JSONObject resp = new JSONObject();
        resp.put("info", InfoHandler.selfInfo(OldSendService.getAlias(null)));
        resp.put("sessionId", sessionId);
        resp.put("files", filesJson);
        OldSendService.log("下载 API: 已发布 " + session.size() + " 个文件");
        return HttpResponse.json(resp.toString());
    }

    static boolean checkPin(HttpRequest req) {
        if (!OldSendService.getPrefBooleanGlobal("use_pin", false)) return true;
        Map<String, String> q = req.getQueryParams();
        String clientPin = q.get("pin");
        if (clientPin == null) clientPin = req.headers.get("x-localsend-pin");
        return clientPin != null && clientPin.equals(OldSendService.getPin());
    }
}

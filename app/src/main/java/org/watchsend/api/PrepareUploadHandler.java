package org.watchsend.api;

import java.util.Iterator;
import java.util.Map;
import org.json.JSONObject;
import org.watchsend.OldSendService;
import org.watchsend.ReceiveManager;
import org.watchsend.http.HttpRequest;
import org.watchsend.http.HttpResponse;
import org.watchsend.http.Router;

public class PrepareUploadHandler implements Router.Handler {
    private final SessionStore store;

    public PrepareUploadHandler(SessionStore store) { this.store = store; }

    public HttpResponse handle(HttpRequest req) throws Exception {
        if (!"POST".equalsIgnoreCase(req.method)) return HttpResponse.status(405);
        
        // 1. 验证 PIN
        boolean usePin = OldSendService.getPrefBooleanGlobal("use_pin", false);
        String expectedPin = OldSendService.getPin();
        if (usePin) {
            Map<String, String> q = req.getQueryParams();
            String clientPin = q.get("pin");
            if (clientPin == null || !clientPin.equals(expectedPin)) {
                HttpResponse res = HttpResponse.status(401);
                res.headers.put("X-LocalSend-Pin", "true");
                OldSendService.log("拦截：对方未提供正确的 PIN");
                return res;
            }
        }

        String body = RegisterHandler.readAll(req);
        JSONObject o = new JSONObject(body);
        JSONObject files = o.optJSONObject("files");
        if (files == null) files = new JSONObject();

        String senderAlias = org.watchsend.util.I18nManager.get("unknown_device");
        // 身份优先级与官方一致：HTTPS 用对端证书指纹；明文用 info.fingerprint；最后退回 IP
        String senderFingerprint = req.peerCertFingerprint;
        if (senderFingerprint == null || senderFingerprint.length() == 0) {
            senderFingerprint = req.remoteAddress.getHostAddress();
        }

        JSONObject info = o.optJSONObject("info");
        if (info != null) {
            senderAlias = info.optString("alias", "未知设备");
            if (req.peerCertFingerprint == null || req.peerCertFingerprint.length() == 0) {
                String f = info.optString("fingerprint", "");
                if (!f.isEmpty()) senderFingerprint = f;
            }
        }
        senderFingerprint = org.watchsend.util.CryptoUtils.normalizeFingerprint(senderFingerprint);
        
        String blacklist = OldSendService.getPrefStringGlobal("blacklist", "");
        if (blacklist.contains(senderFingerprint)) {
            OldSendService.log("黑名单拦截: " + senderAlias);
            return HttpResponse.status(403);
        }

        int fileCount = files.length();
        OldSendService.log("收到请求: " + senderAlias + " 请求发送 " + fileCount + " 个文件");

        // 2. 快速保存三态（off/on/paired），旧 auto_save / auto_accept_whitelist 由 QuickSave 兼容推导
        String previewForAsk = null;
        Iterator<?> pit = files.keys();
        while (pit.hasNext()) {
            JSONObject pf = files.optJSONObject((String) pit.next());
            if (pf == null) continue;
            if ("text".equals(pf.optString("fileType", ""))) {
                String pv = pf.optString("preview", null);
                // 官方 message_preview 规定：超过 16KB 的预览不内联，按普通 .txt 文件对待
                if (pv != null && pv.length() <= 16384) { previewForAsk = pv; break; }
            }
        }

        boolean autoSave = org.watchsend.util.QuickSave.shouldAutoAccept(senderFingerprint);
        boolean accepted = true;
        if (!autoSave) {
            accepted = ReceiveManager.askUser(senderAlias, fileCount, previewForAsk);
        } else {
            OldSendService.log("快速保存放行: " + senderAlias + " (mode=" + org.watchsend.util.QuickSave.getMode() + ")");
        }

        if (!accepted) {
            OldSendService.log("已拒绝 " + senderAlias + " 的发送请求");
            return HttpResponse.status(403);
        }

        UploadSession session = store.create(req.remoteAddress.getHostAddress());
        JSONObject respFiles = new JSONObject();
        Iterator<?> it = files.keys();
        while (it.hasNext()) {
            String fileId = (String) it.next();
            JSONObject f = files.getJSONObject(fileId);
            String fileName = f.optString("fileName", fileId);
            String filterStr = OldSendService.getPrefStringGlobal("filter_ext", "exe,bat,cmd,vbs,sh");
            if (filterStr.length() > 0) {
                String lower = fileName.toLowerCase();
                String[] exts = filterStr.split(",");
                for (String ext : exts) {
                    if (lower.endsWith("." + ext.trim())) {
                        OldSendService.log("拦截自定义后缀文件: " + fileName);
                        return HttpResponse.status(403);
                    }
                }
            }
            long size = f.optLong("size", -1);
            String token = Long.toHexString(System.nanoTime() ^ (long) (Math.random() * 1e18));
            // 官方 2.x 的可选字段：sha256 用于接收端校验，metadata.modified 用于还原时间
            String sha = f.optString("sha256", null);
            String modified = null;
            JSONObject meta = f.optJSONObject("metadata");
            if (meta != null) modified = meta.optString("modified", null);
            String preview = f.optString("preview", null);
            session.addFile(fileId, fileName, size, token, sha, modified, preview);
            respFiles.put(fileId, token);
        }

        JSONObject resp = new JSONObject();
        resp.put("sessionId", session.sessionId);
        resp.put("files", respFiles);
        return HttpResponse.json(resp.toString());
    }
}

package org.watchsend.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.watchsend.OldSendService;
import org.watchsend.http.HttpRequest;
import org.watchsend.http.HttpResponse;
import org.watchsend.http.Router;
import org.watchsend.transfer.FileSaver;
import android.os.Environment;
import android.content.ClipboardManager;

public class WebShareHandler implements Router.Handler {

    public HttpResponse handle(HttpRequest req) throws Exception {
        if (!OldSendService.getPrefBooleanGlobal("web_enable", true)) return HttpResponse.status(404);
        
        if (req.path.equals("/") || req.path.equals("/index.html")) {
            HttpResponse resp = new HttpResponse();
            resp.contentType = "text/html; charset=utf-8";
            try {
                java.io.InputStream is = OldSendService.getContext().getAssets().open("index.html");
                java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
                is.close();
                resp.body = bos.toByteArray();
            } catch (Exception e) {
                resp.body = "<h1>Error loading Web UI</h1>".getBytes("UTF-8");
            }
            return resp;
        }

        if (req.path.equals("/web/info") && req.method.equalsIgnoreCase("GET")) {
            JSONObject info = new JSONObject();
            info.put("alias", OldSendService.getAlias(null));
            return HttpResponse.json(info.toString());
        }
        
        // 剪贴板双向同步 GET
        if (req.path.equals("/web/clipboard") && req.method.equalsIgnoreCase("GET")) {
            try {
                ClipboardManager cm = (ClipboardManager) OldSendService.getContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                if (cm.hasPrimaryClip() && cm.getPrimaryClip().getItemCount() > 0) {
                    CharSequence text = cm.getPrimaryClip().getItemAt(0).coerceToText(OldSendService.getContext());
                    HttpResponse resp = new HttpResponse();
                    resp.contentType = "text/plain; charset=utf-8";
                    resp.body = (text != null ? text.toString() : "").getBytes("UTF-8");
                    return resp;
                }
            } catch (Exception e) {}
            return HttpResponse.json("null");
        }

        // 剪贴板双向同步 POST
        if (req.path.equals("/web/text") && req.method.equalsIgnoreCase("POST")) {
            String body = RegisterHandler.readAll(req);
            try {
                JSONObject obj = new JSONObject(body);
                String text = obj.optString("text", "");
                if (!text.isEmpty()) {
                    ClipboardManager cm = (ClipboardManager) OldSendService.getContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("OldSend", text));
                }
            } catch (Exception e) {}
            return HttpResponse.json("{\"status\":\"ok\"}");
        }
        
        // 文件上传接口
        if (req.path.equals("/web/upload") && req.method.equalsIgnoreCase("POST")) {
            Map<String, String> q = req.getQueryParams();
            String fileName = q.get("name");
            if (fileName == null) fileName = "web_" + System.currentTimeMillis();
            
            String filterStr = OldSendService.getPrefStringGlobal("filter_ext", "exe,bat,cmd,vbs,sh");
            if (filterStr.length() > 0) {
                String lower = fileName.toLowerCase();
                String[] exts = filterStr.split(",");
                for (String ext : exts) {
                    if (lower.endsWith("." + ext.trim())) {
                        OldSendService.log("Web拦截自定义后缀: " + fileName);
                        return HttpResponse.status(403);
                    }
                }
            }

            File target = FileSaver.createTarget(fileName, OldSendService.getPrefBooleanGlobal("save_album", true));
            FileOutputStream fos = new FileOutputStream(target);
            try {
                InputStream in = req.body;
                byte[] buf = new byte[65536];
                long remaining = req.contentLength > 0 ? req.contentLength : Long.MAX_VALUE;
                while (remaining > 0) {
                    int max = (int) Math.min(buf.length, remaining);
                    int n = in.read(buf, 0, max);
                    if (n < 0) break;
                    fos.write(buf, 0, n);
                    remaining -= n;
                }
            } finally {
                try { fos.close(); } catch (Exception ignored) {}
            }
            return HttpResponse.json("{\"status\":\"ok\"}");
        }
        
        // 反向索要文件列表
        if (req.path.equals("/web/files") && req.method.equalsIgnoreCase("GET")) {
            JSONArray arr = new JSONArray();
            try {
                File[] dirs = {
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "OldSend"),
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OldSend"),
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "OldSend"),
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "OldSend")
                };
                for (File dir : dirs) {
                    if (dir != null && dir.exists() && dir.isDirectory()) {
                        File[] list = dir.listFiles();
                        if (list != null) {
                            for (File f : list) {
                                if (f != null && f.isFile()) {
                                    JSONObject obj = new JSONObject();
                                    obj.put("name", f.getName()); 
                                    obj.put("size", f.length()); 
                                    arr.put(obj);
                                }
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                OldSendService.log("Web files error: " + t.getMessage());
            }
            return HttpResponse.json(arr.toString());
        }
        
        // 下载文件接口
        if (req.path.equals("/web/download") && req.method.equalsIgnoreCase("GET")) {
            Map<String, String> q = req.getQueryParams();
            String name = q.get("name");
            if (name != null) {
                File[] dirs = {
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "OldSend"),
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OldSend"),
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "OldSend")
                };
                for (File dir : dirs) {
                    File f = new File(dir, name);
                    if (f.exists() && f.isFile()) {
                        HttpResponse resp = new HttpResponse();
                        resp.contentType = "application/octet-stream";
                        resp.headers.put("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"");
                        resp.stream = new FileInputStream(f);
                        resp.contentLength = f.length();
                        return resp;
                    }
                }
            }
        }
        return HttpResponse.status(404);
    }
}

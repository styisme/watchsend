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

    \n        setInterval(() => {\n            fetch('/web/clipboard').then(r=>r.text()).then(txt => {\n                if (txt && txt !== \"null\" && txt !== lastClip) {\n                    lastClip = txt;\n                    document.getElementById('clipboard-display').innerText = \"手机: \" + txt;\n                }\n            }).catch(()=>{});\n        }, 3000);\n\n        function syncClipboard() {\n            if (lastClip) {\n                navigator.clipboard.writeText(lastClip).then(()=>showToast('已复制手机剪贴板内容')).catch(()=>showToast('浏览器拒绝访问剪贴板'));\n            }\n        }\n\n        function switchTab(tab) {\n            document.querySelectorAll('.tab').forEach(t=>t.classList.remove('active'));\n            document.querySelectorAll('.card').forEach(c=>c.classList.remove('active'));\n            document.querySelectorAll('.tab')[['upload','download','text'].indexOf(tab)].classList.add('active');\n            document.getElementById('tab-'+tab).classList.add('active');\n            if(tab==='download') loadFiles();\n        }\n\n        function showToast(msg) { \n            const t=document.getElementById('toast'); t.innerText=msg; t.classList.add('show'); \n            setTimeout(()=>t.classList.remove('show'),2000); \n        }\n\n        const dropZone = document.getElementById('drop-zone');\n        dropZone.addEventListener('dragover', (e) => { e.preventDefault(); dropZone.classList.add('dragover'); });\n        dropZone.addEventListener('dragleave', (e) => { e.preventDefault(); dropZone.classList.remove('dragover'); });\n        dropZone.addEventListener('drop', (e) => {\n            e.preventDefault(); dropZone.classList.remove('dragover');\n            if (e.dataTransfer.files.length > 0) { handleFiles(e.dataTransfer.files); return; }\n            const text = e.dataTransfer.getData('text/plain');\n            if (text) {\n                const file = new File([text], \"拖拽文本_\" + Date.now() + \".txt\", {type: \"text/plain\"});\n                handleFiles([file]);\n                showToast('已自动将文本转为 TXT 发送');\n            }\n        });\n\n        document.addEventListener('paste', (e) => {\n            const items = e.clipboardData.items;\n            for (let i = 0; i < items.length; i++) {\n                if (items[i].type.indexOf('image') !== -1) {\n                    const file = items[i].getAsFile();\n                    if(file) { handleFiles([file]); showToast('已粘贴图片'); }\n                }\n            }\n        });\n\n        function getIcon(name) {\n            name = name.toLowerCase();\n            if (name.match(/\\.(jpg|jpeg|png|gif|webp)$/)) return '🖼️';\n            if (name.match(/\\.(mp4|mkv|avi)$/)) return '🎬';\n            if (name.match(/\\.(mp3|wav|flac)$/)) return '🎵';\n            if (name.match(/\\.(zip|rar|7z)$/)) return '📦';\n            if (name.match(/\\.(apk)$/)) return '🤖';\n            if (name.match(/\\.(txt|md)$/)) return '📝';\n            return '📄';\n        }\n        \n        function formatSize(b) { \n            if(!b) return '0 B'; \n            let k=1024,s=['B','KB','MB','GB'],i=Math.floor(Math.log(b)/Math.log(k)); \n            return parseFloat((b/Math.pow(k,i)).toFixed(1))+' '+s[i]; \n        }\n\n        async function handleFiles(files) {\n            if(!files || files.length===0) return;\n            const list = document.getElementById('upload-list');\n            let index = 0;\n            \n            async function worker() {\n                while(index < files.length) {\n                    const i = index++;\n                    const f = files[i];\n                    const div = document.createElement('div'); div.className = 'file-item';\n                    div.innerHTML = `<div class=\"file-icon\">${getIcon(f.name)}</div>\n                        <div class=\"file-info\">\n                            <div class=\"file-name\">${f.name}</div>\n                            <div class=\"file-size\">${formatSize(f.size)}</div>\n                            <div class=\"progress-bar\"><div class=\"progress-fill\" id=\"p-${i}\"></div></div>\n                        </div>\n                        <div id=\"s-${i}\" style=\"font-size:12px; font-weight:bold; color:var(--text-sec); margin-left:10px;\">0%</div>`;\n                    list.appendChild(div);\n                    \n                    await new Promise(resolve => {\n                        const xhr = new XMLHttpRequest();\n                        xhr.open('POST', '/web/upload?name=' + encodeURIComponent(f.name), true);\n                        xhr.upload.onprogress = e => { \n                            if(e.lengthComputable) { \n                                const p = Math.round((e.loaded/e.total)*100); \n                                document.getElementById('p-'+i).style.width = p+'%'; \n                                document.getElementById('s-'+i).innerText = p+'%'; \n                            } \n                        };\n                        xhr.onload = () => { document.getElementById('s-'+i).innerText = xhr.status===200?'✅ 完成':'❌ 失败'; resolve(); };\n                        xhr.onerror = () => { document.getElementById('s-'+i).innerText = '❌ 出错'; resolve(); };\n                        xhr.send(f);\n                    });\n                }\n            }\n            const ws = []; for(let w=0;w<Math.min(3, files.length);w++) ws.push(worker());\n            await Promise.all(ws);\n            showToast('全部传输完成！');\n        }\n\n        function loadFiles() {\n            const list = document.getElementById('download-list');\n            list.innerHTML = '<p style=\"text-align:center; color:var(--text-sec); font-size:14px;\">加载中...</p>';\n            fetch('/web/files').then(r=>r.json()).then(data => {\n                if(data.length===0) { list.innerHTML = '<p style=\"text-align:center; color:var(--text-sec); font-size:14px;\">手表端暂无文件</p>'; return; }\n                list.innerHTML = '';\n                data.forEach(f => {\n                    const div = document.createElement('div'); div.className = 'file-item';\n                    div.innerHTML = `<div class=\"file-icon\">${getIcon(f.name)}</div>\n                        <div class=\"file-info\">\n                            <div class=\"file-name\">${f.name}</div>\n                            <div class=\"file-size\">${formatSize(f.size)}</div>\n                        </div>\n                        <a href=\"/web/download?name=${encodeURIComponent(f.name)}\" class=\"btn\" style=\"width:auto; padding:6px 12px; text-decoration:none; flex-shrink:0;\">下载</a>`;\n                    list.appendChild(div);\n                });\n            }).catch(()=>{ list.innerHTML = '<p style=\"text-align:center; color:red;\">加载失败</p>'; });\n        }\n\n        function sendText() {\n            const text = document.getElementById('text-input').value;\n            if(!text) return showToast('请输入文本');\n            fetch('/web/text', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ text }) })\n            .then(r => { if(r.ok) { showToast('已发送到手表剪贴板'); document.getElementById('text-input').value = ''; } else showToast('发送失败'); })\n            .catch(()=>showToast('网络错误'));\n        }\n    </script>\n</body>\n</html>";

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
            File[] dirs = {
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "OldSend"),
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OldSend"),
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "OldSend")
            };
            for (File dir : dirs) {
                if (dir.exists() && dir.isDirectory()) {
                    File[] list = dir.listFiles();
                    if (list != null) for (File f : list) if (f.isFile()) {
                        JSONObject obj = new JSONObject();
                        obj.put("name", f.getName()); obj.put("size", f.length()); arr.put(obj);
                    }
                }
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

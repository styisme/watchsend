package org.watchsend.api;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.json.JSONObject;
import org.watchsend.OldSendService;
import org.watchsend.discovery.DeviceStore;
import org.watchsend.http.HttpRequest;
import org.watchsend.http.HttpResponse;
import org.watchsend.http.Router;
import org.watchsend.model.Device;

public class RegisterHandler implements Router.Handler {
    private final String alias;

    public RegisterHandler(String alias) { this.alias = alias; }

    public HttpResponse handle(HttpRequest req) throws Exception {
        if ("POST".equalsIgnoreCase(req.method)) {
            try {
                String body = readAll(req);
                if (body.length() > 0) {
                    JSONObject o = new JSONObject(body);
                    String peerIp = req.remoteAddress.getHostAddress();
                    Device d = Device.fromJson(o, peerIp);
                    DeviceStore.get().addDevice(d);
                    OldSendService.log("register: " + d.alias + " @ " + peerIp);
                }
            } catch (Exception e) {
                OldSendService.log("register parse: " + e);
            }
        }
        return HttpResponse.json(InfoHandler.buildSelfJson(alias));
    }

    public static String readAll(HttpRequest req) throws Exception {
        // chunked 已在 HttpRequest.parse 里解码，这里只需读到流末尾
        if (req.chunked) return readUntilEof(req.body);
        if (req.contentLength <= 0) return "";
        byte[] buf = new byte[(int) req.contentLength];
        int off = 0;
        while (off < buf.length) {
            int n = req.body.read(buf, off, buf.length - off);
            if (n < 0) break;
            off += n;
        }
        return new String(buf, 0, off, "UTF-8");
    }

    static String readUntilEof(InputStream in) throws Exception {
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[16384];
        while (true) {
            int n = in.read(buf, 0, buf.length);
            if (n < 0) break;
            bo.write(buf, 0, n);
        }
        return new String(bo.toByteArray(), "UTF-8");
    }
}

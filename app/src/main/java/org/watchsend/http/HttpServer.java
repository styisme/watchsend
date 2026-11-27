package org.watchsend.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.SSLContext;

import org.watchsend.OldSendService;

/**
 * 极简 HTTP(S) 服务端。
 *
 * 对齐官方对连接的约束（packages/core/src/http/server/connection_limit.rs）：
 * 官方是总 64 / 每 IP 8，手表资源更紧张，这里取总 16 / 每 IP 6，
 * 防止单个对端（或恶意脚本）把文件描述符和线程吃光。
 */
public class HttpServer {

    private static final int MAX_CONNECTIONS = 16;
    private static final int MAX_CONNECTIONS_PER_IP = 6;

    private final int port;
    private final Router router;
    private final SSLContext sslContext;
    private ServerSocket server;
    private volatile boolean running;

    private final AtomicInteger total = new AtomicInteger(0);
    private final Map<String, Integer> perIp = new HashMap<String, Integer>();

    public HttpServer(int port, Router router, SSLContext sslContext) {
        this.port = port;
        this.router = router;
        this.sslContext = sslContext;
    }

    public void start() throws IOException {
        running = true;
        if (sslContext != null) {
            server = sslContext.getServerSocketFactory().createServerSocket();
        } else {
            server = new ServerSocket();
        }
        server.setReuseAddress(true);
        server.bind(new InetSocketAddress(port));

        Thread t = new Thread(new Runnable() {
            public void run() {
                while (running) {
                    try {
                        final Socket s = server.accept();
                        final String ip = s.getInetAddress() == null ? "" : s.getInetAddress().getHostAddress();
                        if (!acquire(ip)) {
                            reject(s);
                            continue;
                        }
                        Thread th = new Thread(new Runnable() {
                            public void run() {
                                try { handle(s); } finally { release(ip); }
                            }
                        }, "http-conn");
                        th.setDaemon(true);
                        th.start();
                    } catch (IOException e) {
                        if (running) OldSendService.log("accept: " + e.getMessage());
                    }
                }
            }
        }, "http-accept");
        t.setDaemon(true);
        t.start();
    }

    private synchronized boolean acquire(String ip) {
        if (total.get() >= MAX_CONNECTIONS) return false;
        Integer n = perIp.get(ip);
        int cur = n == null ? 0 : n.intValue();
        if (cur >= MAX_CONNECTIONS_PER_IP) return false;
        total.incrementAndGet();
        perIp.put(ip, Integer.valueOf(cur + 1));
        return true;
    }

    private synchronized void release(String ip) {
        total.decrementAndGet();
        Integer n = perIp.get(ip);
        if (n == null) return;
        int cur = n.intValue() - 1;
        if (cur <= 0) perIp.remove(ip);
        else perIp.put(ip, Integer.valueOf(cur));
    }

    /** 超限时直接回 503 并断开，不解析请求体，避免被拖住资源。 */
    private void reject(Socket s) {
        try {
            OutputStream out = s.getOutputStream();
            String body = "{\"error\":\"too many connections\"}";
            out.write(("HTTP/1.1 503 Service Unavailable\r\n"
                    + "Content-Type: application/json; charset=utf-8\r\n"
                    + "Content-Length: " + body.getBytes("UTF-8").length + "\r\n"
                    + "Connection: close\r\n\r\n" + body).getBytes("UTF-8"));
            out.flush();
        } catch (Exception ignored) {
        } finally {
            try { s.close(); } catch (Exception ignored) {}
        }
    }

    private void handle(Socket s) {
        try {
            s.setSoTimeout(300000);
            String peerFp = null;
            if (s instanceof javax.net.ssl.SSLSocket) {
                javax.net.ssl.SSLSocket ss = (javax.net.ssl.SSLSocket) s;
                try {
                    ss.startHandshake();
                    peerFp = peerFingerprint(ss);
                } catch (Exception e) {
                    OldSendService.log("TLS 握手失败: " + e);
                    return;
                }
            }
            InputStream in = new BufferedInputStream(s.getInputStream(), 8192);
            OutputStream out = new BufferedOutputStream(s.getOutputStream(), 8192);
            HttpRequest req = HttpRequest.parse(in, s.getInetAddress());
            req.peerCertFingerprint = peerFp;
            HttpResponse resp = router.dispatch(req);
            resp.writeTo(out);
            out.flush();
        } catch (Exception e) {
            OldSendService.log("conn error: " + e);
        } finally {
            try { s.close(); } catch (Exception ignored) {}
        }
    }

    /** 官方在 https 模式下用对端证书指纹代表发送方身份（server_cert_verifier.rs）。 */
    private static String peerFingerprint(javax.net.ssl.SSLSocket ss) {
        try {
            java.security.cert.Certificate[] certs = ss.getSession().getPeerCertificates();
            if (certs != null && certs.length > 0) {
                return org.watchsend.util.SelfSignedCert.fingerprintOf(certs[0].getEncoded());
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public void stop() {
        running = false;
        try { if (server != null) server.close(); } catch (Exception ignored) {}
    }
}

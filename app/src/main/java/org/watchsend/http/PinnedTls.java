package org.watchsend.http;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.watchsend.util.CryptoUtils;

/**
 * LocalSend 2.x 的对端是自签证书，没有 CA 可链，官方做法是
 * 用证书 DER 的 SHA-256 做 trust-on-first-use 固定
 * （packages/core/src/http/client/server_cert_verifier.rs）。
 *
 * 这里的实现：
 * - expectedFingerprint 为 null 时接受任意证书（仅用于首次发现，HTTPS 下只读 info）；
 * - 已知指纹时，在 TLS 握手阶段直接校验，握手失败即中止，不会把请求体发给假冒设备；
 * - 首次读取到的指纹由调用方写入 {@link org.watchsend.util.TrustStore} 持久化。
 */
public class PinnedTls {

    /** 自签证书的 CN 是 IP/主机名之外的随机串，不能做主机名校验。 */
    private static final HostnameVerifier ALLOW_ALL = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) { return true; }
    };

    private static final Map<String, SSLSocketFactory> CACHE = new HashMap<String, SSLSocketFactory>();
    private static SSLSocketFactory trustAnyFactory;

    /** 给连接装上固定的校验策略（必须是 HttpsURLConnection）。 */
    public static void prepare(HttpsURLConnection c, String expectedFingerprint) {
        String fp = CryptoUtils.normalizeFingerprint(expectedFingerprint);
        c.setSSLSocketFactory(factoryFor(fp));
        c.setHostnameVerifier(ALLOW_ALL);
    }

    public static synchronized SSLSocketFactory factoryFor(String normalizedFp) {
        if (normalizedFp == null) {
            if (trustAnyFactory == null) trustAnyFactory = build(null);
            return trustAnyFactory;
        }
        SSLSocketFactory f = CACHE.get(normalizedFp);
        if (f == null) {
            f = build(normalizedFp);
            CACHE.put(normalizedFp, f);
        }
        return f;
    }

    private static SSLSocketFactory build(final String expectedFp) {
        try {
            X509TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    // 客户端不做校验（服务端由 TlsHelper 的证书标识）
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    if (expectedFp == null) return; // TOFU 首次连接
                    if (chain == null || chain.length == 0) {
                        throw new CertificateException("empty certificate chain");
                    }
                    String actual = CryptoUtils.normalizeFingerprint(CryptoUtils.sha256Hex(chain[0].getEncoded()));
                    if (!expectedFp.equals(actual)) {
                        throw new CertificateException("certificate fingerprint mismatch");
                    }
                }

                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{tm}, new SecureRandom());
            return sc.getSocketFactory();
        } catch (Exception e) {
            // 极端情况下退回系统默认（仍然带 HostnameVerifier 放行）
            try {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init((KeyStore) null);
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, tmf.getTrustManagers(), new SecureRandom());
                return sc.getSocketFactory();
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    /** 从已握手的连接里取出对端证书指纹（小写十六进制），用于 TOFU 记录。 */
    public static String peerFingerprint(HttpsURLConnection c) {
        try {
            java.security.cert.Certificate[] chain = c.getServerCertificates();
            if (chain != null && chain.length > 0) {
                return CryptoUtils.sha256Hex(chain[0].getEncoded());
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}

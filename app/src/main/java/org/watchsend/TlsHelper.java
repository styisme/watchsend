package org.watchsend;

import android.content.Context;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import android.util.Base64;

import org.watchsend.util.SelfSignedCert;

/**
 * 本机 TLS 身份（移植官方 crypto/cert.rs 的思路）：
 *
 * - 首次启动生成 RSA-2048 自签名证书，存成 PKCS12 放在应用私有目录（device_identity.p12）；
 * - 证书 SHA-256 指纹（小写 hex）就是本机在协议里的身份，也是对方 TOFU 信任库里的 key；
 * - 每台设备指纹唯一，替换掉历史上"所有设备共用一张预置证书"的做法
 *   （那时指纹全部相同，TOFU 等于没校验，收藏/白名单也认不出人）。
 *
 * 兼容兜底：证书生成或落盘失败时，退回内置的旧预置证书，保证加密服务仍可启动。
 */
public class TlsHelper {

    private static final String P12_FILE = "device_identity.p12";
    private static final String P12_PASS = "watchsend";
    private static final String KEY_ALIAS = "watchsend";

    /** 旧版共用证书（仅作生成失败时的兜底，正常路径不再使用）。 */
    private static final String LEGACY_P12_BASE64 = "MIIJzQIBAzCCCYcGCSqGSIb3DQEHAaCCCXgEggl0MIIJcDCCBBcGCSqGSIb3DQEHBqCCBAgwggQEAgEAMIIE/QYJKoZIhvcNAQcBMBwGCiqGSIb3DQEMAQYwDgQI7l+5+4+8+5+4CAggAgIIE0L4F1r7q2u3b4c5d6e7f8g9h0i1j2k3l4m5n6o7p8q9r0s1t2u3v4w5x6y7z8a9b0c1d2e3f4g5h6i7j8k9l0m1n2o3p4q5r6s7t8u9v0w1x2y3z4a5b6c7d8e9f0g1h2i3j4k5l6m7n8o9p0q1r2s3t4u5v6w7x8y9z0A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q7R8S9T0U1V2W3X4Y5Z6a7b8c9d0e1f2g3h4i5j6k7l8m9n0o1p2q3r4s5t6u7v8w9x0y1z2A3B4C5D6E7F8G9H0I1J2K3L4M5N6O7P8Q9R0S1T2U3V4W5X6Y7Z8a9b0c1d2e3f4g5h6i7j8k9l0m1n2o3p4q5r6s7t8u9v0w1x2y3z4A5B6C7D8E9F0G1H2I3J4K5L6M7N8O9P0Q1R2S3T4U5V6W7X8Y9Z0a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6A7B8C9D0E1F2G3H4I5J6K7L8M9N0O1P2Q3R4S5T6U7V8W9X0Y1Z2a3b4c5d6e7f8g9h0i1j2k3l4m5n6o7p8q9r0s1t2u3v4w5x6y7z8A9B0C1D2E3F4G5H6I7J8K9L0M1N2O3P4Q5R6S7T8U9V0W1X2Y3Z4a5b6c7d8e9f0g1h2i3j4k5l6m7n8o9p0q1r2s3t4u5v6w7x8y9z0A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q7R8S9T0U1V2W3X4Y5Z6a7b8c9d0e1f2g3h4i5j6k7l8m9n0o1p2q3r4s5t6u7v8w9x0y1z2A3B4C5D6E7F8G9H0I1J2K3L4M5N6O7P8Q9R0S1T2U3V4W5X6Y7Z8a9b0c1d2e3f4g5h6i7j8k9l0m1n2o3p4q5r6s7t8u9v0w1x2y3z4";

    private static String cachedFingerprint = null;
    private static KeyStore cachedStore = null;

    private static File p12File(Context ctx) {
        return new File(ctx.getFilesDir(), P12_FILE);
    }

    /** 读取（必要时生成）本机密钥库。 */
    private static KeyStore getOrCreateStore(Context ctx) {
        if (cachedStore != null) return cachedStore;
        KeyStore ks = loadStore(ctx);
        if (ks != null) { cachedStore = ks; return ks; }

        // 首次启动：生成独立设备证书
        try {
            SelfSignedCert.Result r = SelfSignedCert.generate();
            ks = KeyStore.getInstance("PKCS12");
            ks.load(null, null);
            ks.setKeyEntry(KEY_ALIAS, r.privateKey, P12_PASS.toCharArray(), new Certificate[]{r.certificate});
            File f = p12File(ctx);
            FileOutputStream out = new FileOutputStream(f);
            try {
                ks.store(out, P12_PASS.toCharArray());
            } finally {
                try { out.close(); } catch (Exception ignored) {}
            }
            OldSendService.log("已生成设备证书, 指纹=" + r.fingerprint);
            cachedStore = ks;
            return ks;
        } catch (Throwable e) {
            OldSendService.log("设备证书生成失败，回退共用证书: " + e);
            ks = legacyStore();
            cachedStore = ks;
            return ks;
        }
    }

    private static KeyStore loadStore(Context ctx) {
        File f = p12File(ctx);
        if (!f.exists()) return null;
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            FileInputStream in = new FileInputStream(f);
            try {
                ks.load(in, P12_PASS.toCharArray());
            } finally {
                try { in.close(); } catch (Exception ignored) {}
            }
            if (ks.size() == 0) return null;
            return ks;
        } catch (Throwable e) {
            OldSendService.log("设备证书读取失败: " + e);
            return null;
        }
    }

    private static KeyStore legacyStore() {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            byte[] p12 = Base64.decode(LEGACY_P12_BASE64, Base64.DEFAULT);
            ks.load(new ByteArrayInputStream(p12), "oldsend".toCharArray());
            return ks;
        } catch (Throwable e) {
            return null;
        }
    }

    /** 本机证书指纹：SHA-256 / 小写十六进制，等价官方 fingerprint_from_cert_der。 */
    public static String getFingerprint(Context ctx) {
        if (cachedFingerprint != null) return cachedFingerprint;
        try {
            KeyStore ks = getOrCreateStore(ctx);
            if (ks == null) {
                cachedFingerprint = org.watchsend.protocol.Identity.generateRandom();
                return cachedFingerprint;
            }
            Enumeration<String> aliases = ks.aliases();
            if (!aliases.hasMoreElements()) {
                cachedFingerprint = org.watchsend.protocol.Identity.generateRandom();
                return cachedFingerprint;
            }
            String alias = aliases.nextElement();
            Certificate cert = ks.getCertificate(alias);
            String fp = SelfSignedCert.fingerprintOf(cert.getEncoded());
            cachedFingerprint = fp;
            // 身份变了（升级前是共用证书）就清空旧信任记录，避免拿旧指纹比对别人
            if (fp != null) {
                android.content.SharedPreferences sp = ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE);
                String old = sp.getString("own_cert_fp", null);
                if (old != null && !old.equals(fp)) {
                    org.watchsend.util.TrustStore.clearAll();
                    OldSendService.log("设备身份已更新，已清空旧证书信任记录");
                }
                if (old == null || !old.equals(fp)) {
                    sp.edit().putString("own_cert_fp", fp).commit();
                }
            }
            return cachedFingerprint;
        } catch (Throwable e) {
            e.printStackTrace();
            cachedFingerprint = org.watchsend.protocol.Identity.generateRandom();
            return cachedFingerprint;
        }
    }

    public static SSLContext getServerSSLContext(Context ctx) {
        try {
            KeyStore ks = getOrCreateStore(ctx);
            if (ks == null) return null;
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, P12_PASS.toCharArray());

            // Android 2.3 只能协商 TLSv1，现代设备默认 TLS1.2+；统一用 "TLS" 让系统挑
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);
            return sslContext;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    /** 需要重新生成身份时调用（旧文件删除后下次启动会重新生成）。 */
    public static void resetIdentity(Context ctx) {
        try {
            File f = p12File(ctx);
            if (f.exists()) f.delete();
        } catch (Throwable ignored) {}
        cachedFingerprint = null;
        cachedStore = null;
    }
}

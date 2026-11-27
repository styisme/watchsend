package org.watchsend.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * 移植官方 packages/core/src/crypto/cert.rs：为每台设备生成独立的 RSA-2048 自签名证书。
 *
 * 官方用 rcgen 生成，Android 上没有等价 API，这里手写最小 X.509 DER 编码：
 * - 版本 v3、序列号随机、issuer=subject=CN=WatchSend User
 * - 有效期 2020-01-01 ~ 2049-01-01（永不因时间失效，和官方 1975~4096 的意图一致）
 * - 签名算法 sha1WithRSA（API 10 起可用；自签名证书只给 TLS 用，对端按指纹固定，不校验证书链）
 *
 * 证书指纹（SHA-256，小写十六进制）即本机身份，写入协议的 fingerprint 字段，
 * 也是 TOFU 信任库里标识对方的那把 key。
 */
public class SelfSignedCert {

    public static class Result {
        public PrivateKey privateKey;
        public PublicKey publicKey;
        public X509Certificate certificate;
        public byte[] certDer;
        public String fingerprint;
    }

    private static final String CN = "WatchSend User";
    private static final String OID_SHA1_RSA = "1.2.840.113549.1.1.5";
    private static final String OID_CN = "2.5.4.3";
    private static final String NOT_BEFORE = "200101000000Z";
    private static final String NOT_AFTER = "490101000000Z";

    public static Result generate() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        byte[] serial = new BigInteger(64, new SecureRandom()).setBit(62).toByteArray();
        byte[] tbs = tbsCertificate(kp.getPublic(), serial);

        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initSign(kp.getPrivate());
        sig.update(tbs);
        byte[] signature = sig.sign();

        byte[] der = seq(tbs, algorithmIdentifier(), bitString(signature));

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(der));

        Result r = new Result();
        r.privateKey = kp.getPrivate();
        r.publicKey = kp.getPublic();
        r.certificate = cert;
        r.certDer = der;
        r.fingerprint = sha256Hex(der);
        return r;
    }

    /** 与官方 fingerprint_from_cert_der 一致：对 DER 取 SHA-256，输出小写十六进制。 */
    public static String fingerprintOf(byte[] certDer) {
        return sha256Hex(certDer);
    }

    public static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(data);
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (int i = 0; i < d.length; i++) {
                int b = d[i] & 0xFF;
                sb.append(Character.forDigit(b >> 4, 16));
                sb.append(Character.forDigit(b & 0x0F, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    // ---------------- 最小 DER 编码 ----------------

    private static byte[] tbsCertificate(PublicKey pub, byte[] serial) throws Exception {
        byte[] issuer = name();
        byte[] spki = pub.getEncoded(); // 已是完整 SubjectPublicKeyInfo DER
        return seq(
                explicit0(integer(new byte[]{2})),   // version v3
                integer(serial),
                algorithmIdentifier(),
                issuer,
                seq(utcTime(NOT_BEFORE), utcTime(NOT_AFTER)),
                issuer,                              // subject == issuer
                spki);
    }

    private static byte[] algorithmIdentifier() {
        return seq(oid(OID_SHA1_RSA), new byte[]{0x05, 0x00});
    }

    private static byte[] name() {
        return seq(setOf(seq(oid(OID_CN), utf8String(CN))));
    }

    private static byte[] seq(byte[]... parts) {
        return tlv(0x30, concat(parts));
    }

    private static byte[] setOf(byte[]... parts) {
        return tlv(0x31, concat(parts));
    }

    private static byte[] explicit0(byte[] content) {
        return tlv(0xA0, content);
    }

    private static byte[] integer(byte[] raw) {
        // 去掉多余的前导 0，并保证最高位为 0（正数）
        int start = 0;
        while (start < raw.length - 1 && raw[start] == 0) start++;
        byte[] v = new byte[raw.length - start];
        System.arraycopy(raw, start, v, 0, v.length);
        if ((v[0] & 0x80) != 0) {
            byte[] padded = new byte[v.length + 1];
            padded[0] = 0;
            System.arraycopy(v, 0, padded, 1, v.length);
            v = padded;
        }
        return tlv(0x02, v);
    }

    private static byte[] utcTime(String s) {
        try {
            return tlv(0x17, s.getBytes("US-ASCII"));
        } catch (Exception e) {
            return tlv(0x17, s.getBytes());
        }
    }

    private static byte[] utf8String(String s) {
        try {
            return tlv(0x0C, s.getBytes("UTF-8"));
        } catch (Exception e) {
            return tlv(0x0C, s.getBytes());
        }
    }

    private static byte[] bitString(byte[] content) {
        byte[] zero = new byte[]{0};   // 无未用位
        return tlv(0x03, concat(new byte[][]{zero, content}));
    }

    private static byte[] oid(String dotted) {
        String[] parts = dotted.split("\\.");
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        int first = Integer.parseInt(parts[0]) * 40 + Integer.parseInt(parts[1]);
        body.write(first);
        for (int i = 2; i < parts.length; i++) {
            long v = Long.parseLong(parts[i]);
            byte[] tmp = new byte[8];
            int n = 0;
            tmp[n++] = (byte) (v & 0x7F);
            v >>= 7;
            while (v > 0) {
                tmp[n++] = (byte) ((v & 0x7F) | 0x80);
                v >>= 7;
            }
            for (int k = n - 1; k >= 0; k--) body.write(tmp[k]);
        }
        return tlv(0x06, body.toByteArray());
    }

    private static byte[] tlv(int tag, byte[] content) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(content.length + 8);
        out.write(tag);
        int len = content.length;
        if (len < 0x80) {
            out.write(len);
        } else {
            byte[] b = new byte[]{(byte) ((len >> 16) & 0xFF), (byte) ((len >> 8) & 0xFF), (byte) (len & 0xFF)};
            if (len <= 0xFF) {
                out.write(0x81); out.write(len);
            } else if (len <= 0xFFFF) {
                out.write(0x82); out.write(b[1]); out.write(b[2]);
            } else {
                out.write(0x83); out.write(b[0]); out.write(b[1]); out.write(b[2]);
            }
        }
        out.write(content, 0, content.length);
        return out.toByteArray();
    }

    private static byte[] concat(byte[]... parts) {
        int total = 0;
        for (int i = 0; i < parts.length; i++) total += parts[i].length;
        byte[] out = new byte[total];
        int off = 0;
        for (int i = 0; i < parts.length; i++) {
            System.arraycopy(parts[i], 0, out, off, parts[i].length);
            off += parts[i].length;
        }
        return out;
    }
}

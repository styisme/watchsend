package org.watchsend.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 与 LocalSend 协议相关的密码学/时间工具。
 *
 * <p>官方实现（packages/core/src/crypto/hash.rs、model/transfer.rs）约定：
 * <ul>
 *   <li>设备身份 = 对端证书 DER 的 SHA-256，大写十六进制，可能带 "sha256:" 前缀；</li>
 *   <li>文件时间戳 = RFC3339（如 2026-08-02T10:20:30.456Z）。</li>
 * </ul>
 * 本类只做这两件事的 Java 侧等价实现，且全部 API 在 Android 2.3 上可用。
 */
public class CryptoUtils {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    /** 字节数组的 SHA-256，小写十六进制。 */
    public static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return hex(md.digest(data));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 流式计算文件摘要，避免大文件一次性读入内存。
     * 调用方负责关闭输入流。
     */
    public static String sha256Hex(InputStream in) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) > 0) {
                md.update(buf, 0, n);
            }
            return hex(md.digest());
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }

    /** 文件的 SHA-256；文件不存在或读失败返回 null。 */
    public static String sha256File(File f) {
        if (f == null || !f.isFile()) return null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            return sha256Hex(fis);
        } catch (Exception e) {
            return null;
        } finally {
            if (fis != null) try { fis.close(); } catch (Exception ignored) {}
        }
    }

    public static String hex(byte[] data) {
        if (data == null) return null;
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            int b = data[i] & 0xFF;
            sb.append(HEX[b >> 4]);
            sb.append(HEX[b & 0x0F]);
        }
        return sb.toString();
    }

    /**
     * 归一化证书指纹：去空格、转小写、去掉 "sha256:" 前缀与冒号分隔。
     * 官方用大写十六进制，部分实现会写成 "AA:BB:..."，这里统一后再比较。
     */
    public static String normalizeFingerprint(String fp) {
        if (fp == null) return null;
        String s = fp.trim().toLowerCase(Locale.US);
        if (s.startsWith("sha256:")) s = s.substring(7).trim();
        s = s.replace(":", "").replace(" ", "");
        return s.length() == 0 ? null : s;
    }

    /** 毫秒时间戳 → RFC3339（UTC，带毫秒）。协议要求该格式。 */
    public static String iso8601(long millis) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("GMT"));
        return f.format(new Date(millis));
    }

    /** RFC3339 → 毫秒；解析失败返回 -1。兼容少量非标准写法。 */
    public static long parseIso8601(String value) {
        if (value == null || value.length() == 0) return -1;
        String[] patterns = new String[]{
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ssZ"
        };
        String v = value.trim();
        for (int i = 0; i < patterns.length; i++) {
            try {
                SimpleDateFormat f = new SimpleDateFormat(patterns[i], Locale.US);
                if (patterns[i].endsWith("'Z'")) {
                    f.setTimeZone(TimeZone.getTimeZone("GMT"));
                }
                return f.parse(v).getTime();
            } catch (Exception ignored) {
            }
        }
        return -1;
    }
}

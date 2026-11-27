package org.watchsend.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;
import org.watchsend.OldSendService;

/**
 * 已信任对端证书指纹的本地缓存（trust-on-first-use）。
 *
 * key = "ip:port"，value = 证书 DER 的 SHA-256（小写十六进制）。
 * 首次连上某台 HTTPS 设备时记录，之后每次连接都要求指纹一致；
 * 指纹变化说明设备换了证书（或有人在中间人攻击），此时拒绝连接并提示用户。
 */
public class TrustStore {

    private static final String PREF = "oldsend";
    private static final String KEY = "trusted_certs";
    private static final Object LOCK = new Object();

    private static SharedPreferences prefs() {
        Context ctx = OldSendService.getContext();
        if (ctx == null) return null;
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private static String storeKey(String ip, int port) {
        return (ip == null ? "" : ip) + ":" + port;
    }

    public static String get(String ip, int port) {
        SharedPreferences sp = prefs();
        if (sp == null) return null;
        synchronized (LOCK) {
            String raw = sp.getString(KEY, "");
            if (raw == null || raw.length() == 0) return null;
            try {
                JSONObject o = new JSONObject(raw);
                return CryptoUtils.normalizeFingerprint(o.optString(storeKey(ip, port), null));
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static void put(String ip, int port, String fingerprint) {
        String fp = CryptoUtils.normalizeFingerprint(fingerprint);
        if (fp == null) return;
        SharedPreferences sp = prefs();
        if (sp == null) return;
        synchronized (LOCK) {
            try {
                String raw = sp.getString(KEY, "");
                JSONObject o = (raw != null && raw.length() > 0) ? new JSONObject(raw) : new JSONObject();
                o.put(storeKey(ip, port), fp);
                sp.edit().putString(KEY, o.toString()).commit();
            } catch (Exception ignored) {
            }
        }
    }

    public static void remove(String ip, int port) {
        SharedPreferences sp = prefs();
        if (sp == null) return;
        synchronized (LOCK) {
            try {
                String raw = sp.getString(KEY, "");
                if (raw == null || raw.length() == 0) return;
                JSONObject o = new JSONObject(raw);
                o.remove(storeKey(ip, port));
                sp.edit().putString(KEY, o.toString()).commit();
            } catch (Exception ignored) {
            }
        }
    }
}

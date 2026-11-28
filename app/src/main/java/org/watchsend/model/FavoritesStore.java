package org.watchsend.model;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;
import org.watchsend.OldSendService;

/**
 * 收藏设备（对应官方 favorite_device.dart 的最小实现）。
 *
 * 存在 olds end 的 "favorites" 里，按指纹识别；HTTPS 设备没有可用指纹时退回 IP。
 * 收藏既是"常用设备"列表的来源，也是快速保存 paired 模式的白名单。
 */
public class FavoritesStore {

    private static final String PREF = "oldsend";
    private static final String KEY = "favorites";
    private static final Object LOCK = new Object();

    /** 服务未启动时读不到配置，收藏页会显示 0 台、点了也没反应，用注入的 application context 兜底。 */
    private static Context appCtx;

    public static void init(Context c) {
        if (c != null) appCtx = c.getApplicationContext();
    }

    private static Context context() {
        Context c = OldSendService.getContext();
        return c != null ? c : appCtx;
    }

    private static SharedPreferences prefs() {
        Context ctx = context();
        if (ctx == null) return null;
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    /**
     * 指纹比较口径。收藏写入时可能拿到对方原始形态（大写十六进制、"sha256:" 前缀、
     * "AA:BB:.." 冒号分隔），而接收侧 PrepareUploadHandler 已经归一化，直接字符串相等
     * 会让 paired 模式匹配不到收藏设备。这里统一按归一化后的形式比较，并兼容旧条目。
     */
    private static String keyOf(String fp) {
        if (fp == null) return "";
        String norm = org.watchsend.util.CryptoUtils.normalizeFingerprint(fp);
        return norm != null ? norm : fp.trim();
    }

    private static boolean same(String stored, String query) {
        if (stored == null || query == null) return false;
        if (stored.equals(query)) return true;
        return keyOf(stored).equals(keyOf(query));
    }

    public static JSONArray list() {
        SharedPreferences sp = prefs();
        if (sp == null) return new JSONArray();
        synchronized (LOCK) {
            String raw = sp.getString(KEY, "");
            if (raw == null || raw.length() == 0) return new JSONArray();
            try {
                return new JSONArray(raw);
            } catch (Exception e) {
                return new JSONArray();
            }
        }
    }

    public static boolean isFavorite(String fingerprint) {
        if (fingerprint == null || fingerprint.length() == 0) return false;
        JSONArray arr = list();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o != null && same(o.optString("fingerprint", ""), fingerprint)) return true;
        }
        return false;
    }

    /** 返回 true 表示加入收藏，false 表示移出收藏。 */
    public static boolean toggle(String fingerprint, String alias, String ip, int port) {
        if (fingerprint == null || fingerprint.length() == 0) fingerprint = ip;
        SharedPreferences sp = prefs();
        if (sp == null) return false;
        synchronized (LOCK) {
            JSONArray arr = list();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null && same(o.optString("fingerprint", ""), fingerprint)) {
                    arr.remove(i);
                    sp.edit().putString(KEY, arr.toString()).commit();
                    return false;
                }
            }
            JSONObject o = new JSONObject();
            try {
                o.put("fingerprint", keyOf(fingerprint));
                o.put("alias", alias == null ? "" : alias);
                o.put("ip", ip == null ? "" : ip);
                o.put("port", port);
            } catch (Exception ignored) {}
            arr.put(o);
            sp.edit().putString(KEY, arr.toString()).commit();
            return true;
        }
    }

    public static void remove(String fingerprint) {
        SharedPreferences sp = prefs();
        if (sp == null) return;
        synchronized (LOCK) {
            JSONArray arr = list();
            for (int i = arr.length() - 1; i >= 0; i--) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null && fingerprint != null && fingerprint.equals(o.optString("fingerprint", ""))) {
                    arr.remove(i);
                }
            }
            sp.edit().putString(KEY, arr.toString()).commit();
        }
    }

    /** 取收藏条目，没有则返回 null。 */
    public static JSONObject entry(String fingerprint) {
        if (fingerprint == null || fingerprint.length() == 0) return null;
        JSONArray arr = list();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o != null && same(o.optString("fingerprint", ""), fingerprint)) return o;
        }
        return null;
    }

    /** 官方 FavoriteEditDialog 的自定义备注名。 */
    public static String remark(String fingerprint) {
        JSONObject o = entry(fingerprint);
        return o == null ? "" : o.optString("remark", "");
    }

    public static void setRemark(String fingerprint, String remark) {
        SharedPreferences sp = prefs();
        if (sp == null || fingerprint == null) return;
        synchronized (LOCK) {
            JSONArray arr = list();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null && same(o.optString("fingerprint", ""), fingerprint)) {
                    try {
                        o.put("remark", remark == null ? "" : remark);
                    } catch (Exception ignored) {}
                    sp.edit().putString(KEY, arr.toString()).commit();
                    return;
                }
            }
        }
    }

    /** 列表里显示的名字：收藏且有备注时用备注，否则用设备别名。 */
    public static String displayName(org.watchsend.model.Device d) {
        if (d == null) return "";
        String remark = remark(d.identity());
        if (remark != null && remark.length() > 0) return remark;
        return d.alias == null ? "" : d.alias;
    }

    public static int count() {
        return list().length();
    }
}

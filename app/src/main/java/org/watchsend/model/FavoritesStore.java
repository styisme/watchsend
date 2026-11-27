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

    private static SharedPreferences prefs() {
        Context ctx = OldSendService.getContext();
        if (ctx == null) return null;
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
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
            if (o != null && fingerprint.equals(o.optString("fingerprint", ""))) return true;
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
                if (o != null && fingerprint.equals(o.optString("fingerprint", ""))) {
                    arr.remove(i);
                    sp.edit().putString(KEY, arr.toString()).commit();
                    return false;
                }
            }
            JSONObject o = new JSONObject();
            try {
                o.put("fingerprint", fingerprint);
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

    public static int count() {
        return list().length();
    }
}

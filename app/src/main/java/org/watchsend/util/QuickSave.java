package org.watchsend.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.watchsend.OldSendService;
import org.watchsend.model.FavoritesStore;

/**
 * 快速保存三态，对齐官方 QuickSaveMode（app/lib/model/persistence/quick_save_mode.dart）：
 * - off     ：每次都询问
 * - on      ：全部自动接收
 * - paired  ：只自动接收收藏设备
 *
 * 兼容旧版本：老配置里只有 auto_save（布尔）和 auto_accept_whitelist（布尔），
 * 未显式设置 quick_save_mode 时按这两个键推导，避免升级后行为突变。
 */
public class QuickSave {

    public static final String OFF = "off";
    public static final String ON = "on";
    public static final String PAIRED = "paired";

    private static final String PREF = "oldsend";
    private static final String KEY = "quick_save_mode";

    private static SharedPreferences prefs() {
        Context ctx = OldSendService.getContext();
        if (ctx == null) return null;
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static String getMode() {
        SharedPreferences sp = prefs();
        if (sp == null) return OFF;
        String mode = sp.getString(KEY, null);
        if (mode != null && mode.length() > 0) {
            if (ON.equals(mode) || PAIRED.equals(mode) || OFF.equals(mode)) return mode;
        }
        // 旧配置推导
        boolean autoSave = sp.getBoolean("auto_save", false);
        boolean autoWl = sp.getBoolean("auto_accept_whitelist", false);
        if (autoSave) return ON;
        if (autoWl) return PAIRED;
        return OFF;
    }

    public static void setMode(String mode) {
        SharedPreferences sp = prefs();
        if (sp == null) return;
        if (mode == null) mode = OFF;
        boolean autoSave = ON.equals(mode);
        boolean autoWl = PAIRED.equals(mode);
        sp.edit()
                .putString(KEY, mode)
                .putBoolean("auto_save", autoSave)
                .putBoolean("auto_accept_whitelist", autoWl)
                .commit();
    }

    public static String label(String mode) {
        if (ON.equals(mode)) return I18nManager.get("quick_save_on");
        if (PAIRED.equals(mode)) return I18nManager.get("quick_save_paired");
        return I18nManager.get("quick_save_off");
    }

    /** 是否对该发送方自动接收（不弹窗）。 */
    public static boolean shouldAutoAccept(String fingerprint) {
        String mode = getMode();
        if (ON.equals(mode)) return true;
        if (PAIRED.equals(mode)) {
            if (FavoritesStore.isFavorite(fingerprint)) return true;
            // 兼容旧白名单
            String wl = OldSendService.getPrefStringGlobal("whitelist", "");
            return fingerprint != null && fingerprint.length() > 0 && wl.contains(fingerprint);
        }
        return false;
    }
}

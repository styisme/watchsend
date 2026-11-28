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

    /**
     * 服务未启动时 OldSendService.appContext 还是 null，此时读写都会静默失败：
     * 设置页点了三态、界面重建后又显示回"每次都询问"。用 Activity 注入的 application
     * context 兜底，保证任何入口都能读到同一份 olds end 配置。
     */
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

    /** @return 写入是否真正落到磁盘并读回一致；设置页据此提示，避免"切了又变回去"无声发生。 */
    public static boolean setMode(String mode) {
        if (mode == null) mode = OFF;
        SharedPreferences sp = prefs();
        if (sp == null) {
            OldSendService.log("快速保存: 配置上下文不可用，模式未保存 (" + mode + ")");
            return false;
        }
        boolean autoSave = ON.equals(mode);
        boolean autoWl = PAIRED.equals(mode);
        boolean committed = sp.edit()
                .putString(KEY, mode)
                .putBoolean("auto_save", autoSave)
                .putBoolean("auto_accept_whitelist", autoWl)
                .commit();
        // commit 返回 true 只说明没抛异常，仍读回确认，防止被别的实例/进程覆盖
        String readBack = sp.getString(KEY, null);
        boolean ok = committed && readBack != null && readBack.equals(mode);
        OldSendService.log("快速保存模式 -> " + mode + (ok ? "（已保存）" : "（保存失败，读回=" + readBack + "）"));
        return ok;
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
            // 兼容旧白名单：旧实现是整串 contains，IP 10.0.14.5 会被 10.0.14.55 误命中，
            // 这里按逗号切分后逐项精确比对（比较前统一做指纹归一化）
            String wl = OldSendService.getPrefStringGlobal("whitelist", "");
            if (wl == null || wl.length() == 0) return false;
            String norm = org.watchsend.util.CryptoUtils.normalizeFingerprint(fingerprint);
            String raw = fingerprint == null ? "" : fingerprint.trim();
            for (String item : wl.split(",")) {
                String t = item.trim();
                if (t.length() == 0) continue;
                if (t.equals(raw)) return true;
                if (norm != null && norm.equals(org.watchsend.util.CryptoUtils.normalizeFingerprint(t))) return true;
            }
            return false;
        }
        return false;
    }
}

package org.watchsend.uikit;

import org.watchsend.R;

import android.content.Context;
import android.content.SharedPreferences;

public final class UikitConfig {
    // 与设置页 WatchSettingsActivity 共用同一份配置（oldsend），key 保持一致，
    // 否则 uikit 全屏页读不到"强制圆屏 / 低性能 / 主题 / UI 大小"等用户设置。
    public static final String KEY_THEME_MODE        = "theme_mode";
    public static final String KEY_UI_SCALE          = "ui_scale";
    public static final String KEY_DYNAMIC_COLOR     = "dynamic_color";
    public static final String KEY_LOW_PERF          = "low_perf_mode";
    /** 软件内（滑动）返回：开关语义从"拦截系统返回"翻转而来，存独立的键，老用户的值自动迁移。 */
    public static final String KEY_SWIPE_BACK         = "swipe_back_enabled";
    /** 旧键，仅用于把历史设置迁移到 KEY_SWIPE_BACK；新代码不要再读写它。 */
    public static final String KEY_BLOCK_SYSTEM_BACK = "block_system_back";
    public static final String KEY_SCREEN_SHAPE      = "screen_shape";
    public static final String KEY_CHIN_PADDING      = "chin_padding";
    // 新增：自定义颜色
    public static final String KEY_CUSTOM_PRIMARY    = "uikit_custom_primary";
    public static final String KEY_CUSTOM_ACCENT     = "uikit_custom_accent";

    public static final String THEME_AUTO  = "auto";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK  = "dark";

    public static final int SHAPE_AUTO   = 0;
    public static final int SHAPE_ROUND  = 1;
    public static final int SHAPE_SQUARE = 2;


    public static final String KEY_APP_LANG = "app_lang";
    public static String getAppLang(Context ctx) {
        return prefs(ctx).getString(KEY_APP_LANG, "system");
    }
    public static void setAppLang(Context ctx, String lang) {
        prefs(ctx).edit().putString(KEY_APP_LANG, lang).commit();
    }

    private static String sPrefsName = "oldsend";
    private UikitConfig() {}

    public static void init(String prefsName) {
        if (prefsName != null && prefsName.length() > 0) sPrefsName = prefsName;
    }
    public static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(sPrefsName, Context.MODE_PRIVATE);
    }
    public static String getThemeMode(Context ctx) {
        return prefs(ctx).getString(KEY_THEME_MODE, THEME_AUTO);
    }
    public static void setThemeMode(Context ctx, String mode) {
        prefs(ctx).edit().putString(KEY_THEME_MODE, mode).commit();
    }
    public static float getUiScale(Context ctx) {
        return prefs(ctx).getFloat(KEY_UI_SCALE, 1.0f);
    }
    public static void setUiScale(Context ctx, float scale) {
        if (scale < 0.3f) scale = 0.3f;
        if (scale > 2.5f) scale = 2.5f;
        prefs(ctx).edit().putFloat(KEY_UI_SCALE, scale).commit();
    }
    public static boolean isDynamicColorEnabled(Context ctx) {
        return prefs(ctx).getBoolean(KEY_DYNAMIC_COLOR, true);
    }
    public static void setDynamicColorEnabled(Context ctx, boolean enabled) {
        prefs(ctx).edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).commit();
    }
    /** 自动判定结果缓存（每进程算一次，避免每次读都由系统调用重算）。 */
    private static Boolean sAutoLowPerf;

    /**
     * 低性能模式开关：
     * 1) 用户在设置页显式开/关过 → 完全听用户的；
     * 2) 从未设置过 → 按设备能力自动判定（低内存设备 / 低 RAM 标记 / ≤2 核），
     *    老手表不必让用户自己猜该不该开。
     */
    public static boolean isLowPerf(Context ctx) {
        SharedPreferences p = prefs(ctx);
        if (p.contains(KEY_LOW_PERF)) return p.getBoolean(KEY_LOW_PERF, false);
        return autoLowPerf(ctx);
    }

    private static boolean autoLowPerf(Context ctx) {
        if (sAutoLowPerf != null) return sAutoLowPerf;
        boolean low = false;
        try {
            // 应用可用堆 ≤ 256MB：这是手表与入门机的典型区间（手机通常 ≥256MB，手表多为 48~192MB）
            if (Runtime.getRuntime().maxMemory() / (1024L * 1024L) <= 256L) low = true;
            // 系统自报低内存设备
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                android.app.ActivityManager am =
                        (android.app.ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null && am.isLowRamDevice()) low = true;
            }
            // 双核及以下
            if (Runtime.getRuntime().availableProcessors() <= 2) low = true;
        } catch (Throwable ignored) {
        }
        sAutoLowPerf = low;
        return low;
    }

    /** 用户是否显式选择过低性能模式（false 表示当前值来自自动判定）。 */
    public static boolean isLowPerfExplicit(Context ctx) {
        return prefs(ctx).contains(KEY_LOW_PERF);
    }

    /**
     * 动画时长闸门：低性能模式统一返回 0，调用方不必各写一遍 if。
     * 普通模式下原样返回，便于集中调节。
     */
    public static int animDuration(Context ctx, int ms) {
        return isLowPerf(ctx) ? 0 : ms;
    }
    public static void setLowPerf(Context ctx, boolean low) {
        prefs(ctx).edit().putBoolean(KEY_LOW_PERF, low).commit();
    }
    /**
     * 是否启用"软件内返回"（界面内滑动退出）。
     * true：禁用系统返回，统一由滑动返回处理；false：系统返回照常，滑动返回不接管。
     * 老版本只有反义的 block_system_back，首次读取时迁移一次并落到新键。
     */
    public static boolean isSwipeBackEnabled(Context ctx) {
        SharedPreferences p = prefs(ctx);
        if (p.contains(KEY_SWIPE_BACK)) return p.getBoolean(KEY_SWIPE_BACK, true);
        boolean enabled = p.getBoolean(KEY_BLOCK_SYSTEM_BACK, true);
        p.edit().putBoolean(KEY_SWIPE_BACK, enabled).apply();
        return enabled;
    }

    public static void setSwipeBackEnabled(Context ctx, boolean enabled) {
        prefs(ctx).edit()
                .putBoolean(KEY_SWIPE_BACK, enabled)
                // 同时写回旧键：若用户降级回 1.7.12，行为与这里保持一致
                .putBoolean(KEY_BLOCK_SYSTEM_BACK, !enabled)
                .commit();
    }

    /** 兼容旧调用点：拦截系统返回 ⇔ 启用软件内返回。 */
    public static boolean isSystemBackBlocked(Context ctx) {
        return isSwipeBackEnabled(ctx);
    }
    public static void setSystemBackBlocked(Context ctx, boolean blocked) {
        setSwipeBackEnabled(ctx, blocked);
    }
    public static int getScreenShape(Context ctx) {
        return prefs(ctx).getInt(KEY_SCREEN_SHAPE, SHAPE_AUTO);
    }
    public static void setScreenShape(Context ctx, int shape) {
        prefs(ctx).edit().putInt(KEY_SCREEN_SHAPE, shape).commit();
    }
    public static boolean isChinPaddingEnabled(Context ctx) {
        return prefs(ctx).getBoolean(KEY_CHIN_PADDING, false);
    }
    public static void setChinPaddingEnabled(Context ctx, boolean enabled) {
        prefs(ctx).edit().putBoolean(KEY_CHIN_PADDING, enabled).commit();
    }

    // ====== 新增：自定义颜色 API ======
    public static int getCustomPrimaryColor(Context ctx) {
        return prefs(ctx).getInt(KEY_CUSTOM_PRIMARY, 0);
    }
    public static void setCustomPrimaryColor(Context ctx, int color) {
        prefs(ctx).edit().putInt(KEY_CUSTOM_PRIMARY, color).commit();
    }
    public static int getCustomAccentColor(Context ctx) {
        return prefs(ctx).getInt(KEY_CUSTOM_ACCENT, 0);
    }
    public static void setCustomAccentColor(Context ctx, int color) {
        prefs(ctx).edit().putInt(KEY_CUSTOM_ACCENT, color).commit();
    }
    public static void clearCustomColors(Context ctx) {
        prefs(ctx).edit().remove(KEY_CUSTOM_PRIMARY).remove(KEY_CUSTOM_ACCENT).commit();
    }
}

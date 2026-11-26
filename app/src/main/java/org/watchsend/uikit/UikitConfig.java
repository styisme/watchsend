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
    public static boolean isLowPerf(Context ctx) {
        return prefs(ctx).getBoolean(KEY_LOW_PERF, false);
    }
    public static void setLowPerf(Context ctx, boolean low) {
        prefs(ctx).edit().putBoolean(KEY_LOW_PERF, low).commit();
    }
    public static boolean isSystemBackBlocked(Context ctx) {
        return prefs(ctx).getBoolean(KEY_BLOCK_SYSTEM_BACK, true);
    }
    public static void setSystemBackBlocked(Context ctx, boolean blocked) {
        prefs(ctx).edit().putBoolean(KEY_BLOCK_SYSTEM_BACK, blocked).commit();
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

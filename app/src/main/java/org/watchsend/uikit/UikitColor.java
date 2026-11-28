package org.watchsend.uikit;

import org.watchsend.R;

import android.content.Context;
import android.os.Build;

public final class UikitColor {
    public static final int FALLBACK_PRIMARY      = 0xFF388E3C;
    public static final int FALLBACK_ACCENT_LIGHT = 0xFF1B5E20;
    public static final int FALLBACK_ACCENT_DARK  = 0xFF81C784;

    private UikitColor() {}

    public static boolean isDynamicColorEnabled(Context ctx) {
        return Build.VERSION.SDK_INT >= 31 && UikitConfig.isDynamicColorEnabled(ctx);
    }
    public static int getSystemColor(Context ctx, String resName, int fallback) {
        try {
            int colorId = ctx.getResources().getIdentifier(resName, "color", "android");
            if (colorId != 0) {
                if (Build.VERSION.SDK_INT >= 23) return ctx.getColor(colorId);
                return ctx.getResources().getColor(colorId);
            }
        } catch (Exception ignored) {}
        return fallback;
    }

    public static int getPrimaryColor(Context ctx) {
        // 1. 优先使用宿主自定义的主色
        int custom = UikitConfig.getCustomPrimaryColor(ctx);
        if (custom != 0) return custom;

        // 2. 动态取色：直接用 framework 的 M3 色调资源。
        //    旧实现先解析 android:attr/colorAccent，但手表主题（Theme.DeviceDefault.*）里的
        //    accent 是写死的常量，动态色一开就被它短路，看起来"开关没反应"，因此去掉这一层。
        //    浅色用 accent1-600（白底上足够深），深色用 accent1-200（黑底上足够亮）。
        if (isDynamicColorEnabled(ctx)) {
            boolean dark = UikitTheme.isDarkMode(ctx);
            int c = getSystemColor(ctx, dark ? "system_accent1_200" : "system_accent1_600", 0);
            if (c == 0) {
                // 个别 ROM 缺失 600/200 档位，退一档取近似色，而不是直接掉回默认绿
                c = getSystemColor(ctx, dark ? "system_accent1_100" : "system_accent1_500", 0);
            }
            if (c != 0) return c;
        }
        // 3. 兜底
        return FALLBACK_PRIMARY;
    }

    public static int getAccentColor(Context ctx, boolean isDark) {
        // 1. 自定义强调色
        int custom = UikitConfig.getCustomAccentColor(ctx);
        if (custom != 0) return custom;

        // 2. 动态强调色
        if (isDynamicColorEnabled(ctx)) {
            return getSystemColor(ctx,
                    isDark ? "system_accent1_200" : "system_accent1_800",
                    isDark ? FALLBACK_ACCENT_DARK : FALLBACK_ACCENT_LIGHT);
        }
        // 3. 兜底
        return isDark ? FALLBACK_ACCENT_DARK : FALLBACK_ACCENT_LIGHT;
    }

    public static int getOnPrimaryColor(Context ctx) {
        int p = getPrimaryColor(ctx);
        int r = (p >> 16) & 0xFF, g = (p >> 8) & 0xFF, b = p & 0xFF;
        int y = (r * 299 + g * 587 + b * 114) / 1000;
        return y > 160 ? 0xFF1C1B1F : 0xFFFFFFFF;
    }

    /**
     * 取某一档 M3 色调（system_accent1_xxx），动态色关闭或系统缺该档时回退 fallback。
     * 供需要"主色容器/按下态"的界面复用，避免各处再硬编码绿色。
     */
    public static int getToneColor(Context ctx, boolean dark, String lightTone, String darkTone, int fallback) {
        if (isDynamicColorEnabled(ctx)) {
            int c = getSystemColor(ctx, dark ? darkTone : lightTone, 0);
            if (c != 0) return c;
        }
        return fallback;
    }

    /** 次要按钮/标签的底色：浅色用 accent1-100，深色用 accent1-900。 */
    public static int getContainerColor(Context ctx, boolean dark) {
        return getToneColor(ctx, dark, "system_accent1_100", "system_accent1_900",
                dark ? 0xFF1E2E22 : 0xFFC8E6C9);
    }

    /** 容器色按下态：比常态深/浅一档，保留原来的按压反馈。 */
    public static int getContainerPressedColor(Context ctx, boolean dark) {
        return getToneColor(ctx, dark, "system_accent1_200", "system_accent1_800",
                dark ? 0xFF2E4F33 : 0xFFA5D6A7);
    }
    public static int getCardBgColor(Context ctx) {
        return UikitTheme.isDarkMode(ctx) ? 0xFF1E1E1E : 0xFFFFFFFF;
    }
    public static int getCardStrokeColor(Context ctx) {
        return UikitTheme.isDarkMode(ctx) ? 0xFF3E3E3E : 0xFFE0E0E0;
    }
}

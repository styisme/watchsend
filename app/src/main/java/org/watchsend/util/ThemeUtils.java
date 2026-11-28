package org.watchsend.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.watchsend.R;

public class ThemeUtils {
    public static boolean isDynamicColorEnabled(Context ctx) {
        // 仅在 Android 12 (API 31) 及以上开启
        return android.os.Build.VERSION.SDK_INT >= 31 &&
               ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE).getBoolean("dynamic_color", true);
    }

    public static int getSystemColor(Context ctx, String resName, int fallback) {
        try {
            int colorId = ctx.getResources().getIdentifier(resName, "color", "android");
            if (colorId != 0) return ctx.getResources().getColor(colorId);
        } catch (Exception ignored) {}
        return fallback;
    }

    /**
     * 主色/强调色统一委托给 UikitColor，避免两份实现分叉：
     * 之前这里也有一份"先解析 colorAccent"的逻辑，与 UikitColor 各自为政，
     * 主界面（MainActivity 标题、按钮）与 uikit 控件会取到不同颜色。
     */
    public static int getPrimaryColor(Context ctx) {
        return org.watchsend.uikit.UikitColor.getPrimaryColor(ctx);
    }

    public static int getAccentColor(Context ctx) {
        return org.watchsend.uikit.UikitColor.getAccentColor(ctx, isDarkMode(ctx));
    }

    public static boolean isDarkMode(Context ctx) {
        if (ctx == null) return false;
        android.content.SharedPreferences sp = ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE);
        
        // 兼容旧数据：如果存了 dark_mode = true 并且没有 theme_mode，默认转为深色
        if (!sp.contains("theme_mode") && sp.getBoolean("dark_mode", false)) {
            sp.edit().putString("theme_mode", "dark").apply();
        }
        
        String mode = sp.getString("theme_mode", "auto");
        if ("dark".equals(mode)) return true;
        if ("light".equals(mode)) return false;
        
        // auto 模式：检查系统深色模式
        int uiMode = ctx.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    public static void applyTheme(Activity activity) {
        boolean isDark = isDarkMode(activity);
        if (!isDark) return;

        // 窗口底色变黑
        activity.getWindow().getDecorView().setBackgroundColor(Color.parseColor("#121212"));

        View root = activity.findViewById(android.R.id.content);
        if (root != null) applyView(root, isDark);
    }

    private static void applyView(View view, boolean isDark) {
        // 替换背景色
        if (view.getTag() != null && "card".equals(view.getTag())) {
            view.setBackgroundResource(R.drawable.bg_md3_card_dark);
        } else if (view.getTag() != null && "bottom_bar".equals(view.getTag())) {
            view.setBackgroundColor(Color.parseColor("#1E1E1E"));
        } else if (view.getBackground() instanceof ColorDrawable) {
            int color = ((ColorDrawable) view.getBackground()).getColor();
            if (color == Color.parseColor("#F4F6F8") || color == Color.parseColor("#FFFFFF")) {
                view.setBackgroundColor(Color.parseColor("#1E1E1E"));
            }
        }

        // 替换文字颜色
        if (view instanceof TextView) {
            int textColor = ((TextView) view).getCurrentTextColor();
            // 把深色文字替换为亮色
            if (textColor == Color.parseColor("#1C1B1F") || textColor == Color.parseColor("#333333") || textColor == Color.parseColor("#222222") || textColor == Color.BLACK) {
                ((TextView) view).setTextColor(Color.parseColor("#E0E0E0"));
            } else if (textColor == Color.parseColor("#666666") || textColor == Color.parseColor("#49454F") || textColor == Color.parseColor("#888888")) {
                ((TextView) view).setTextColor(Color.parseColor("#AAAAAA"));
            }
        }

        // 递归子 View
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyView(group.getChildAt(i), isDark);
            }
        }
    }
}

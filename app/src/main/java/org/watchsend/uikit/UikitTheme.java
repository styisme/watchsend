package org.watchsend.uikit;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public final class UikitTheme {
    private UikitTheme() {}

    public static boolean isDarkMode(Context ctx) {
        if (ctx == null) return false;
        String mode = UikitConfig.getThemeMode(ctx);
        if (UikitConfig.THEME_DARK.equals(mode)) return true;
        if (UikitConfig.THEME_LIGHT.equals(mode)) return false;
        int uiMode = ctx.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return uiMode == Configuration.UI_MODE_NIGHT_YES;
    }
    public static void applyTheme(Activity activity) {
        if (activity == null || !isDarkMode(activity)) return;
        try { activity.getWindow().getDecorView().setBackgroundColor(0xFF121212); } catch (Exception ignored) {}
        View root = activity.findViewById(android.R.id.content);
        if (root != null) applyView(root);
    }
    private static void applyView(View view) {
        if (view.getBackground() instanceof ColorDrawable) {
            int c = ((ColorDrawable) view.getBackground()).getColor();
            if (c == 0xFFF4F6F8 || c == 0xFFFFFFFF) view.setBackgroundColor(0xFF1E1E1E);
        }
        if (view instanceof TextView) {
            int tc = ((TextView) view).getCurrentTextColor();
            if (tc == 0xFF1C1B1F || tc == 0xFF333333 || tc == Color.BLACK) ((TextView) view).setTextColor(0xFFE0E0E0);
            else if (tc == 0xFF666666 || tc == 0xFF49454F) ((TextView) view).setTextColor(0xFFAAAAAA);
        }
        if (view instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) view;
            for (int i = 0; i < g.getChildCount(); i++) applyView(g.getChildAt(i));
        }
    }
}

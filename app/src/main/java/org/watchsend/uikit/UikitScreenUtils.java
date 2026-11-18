package org.watchsend.uikit;

import org.watchsend.R;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
public final class UikitScreenUtils {
    private UikitScreenUtils() {}
    public static int getScreenWidth(Context ctx) {
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }
    public static boolean isSquareScreen(View root) {
        if (root == null) return false;
        DisplayMetrics dm = root.getContext().getResources().getDisplayMetrics();
        float ratio = (float) dm.widthPixels / dm.heightPixels;
        return ratio >= 0.95f && ratio <= 1.05f;
    }
    public static int dp2px(Context ctx, float dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density);
    }
}

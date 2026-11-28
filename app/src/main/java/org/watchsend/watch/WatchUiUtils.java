package org.watchsend.watch;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import org.watchsend.util.ThemeUtils;

public class WatchUiUtils {

    public static boolean isDark(Context ctx) { return ThemeUtils.isDarkMode(ctx); }

    public static void applyScale(android.content.Context context) {
        // Deprecated: 缩放逻辑已迁移至 attachBaseContext
    }

    public static android.content.Context wrapScale(android.content.Context base) {
        android.content.SharedPreferences sp = base.getSharedPreferences("oldsend", android.content.Context.MODE_PRIVATE);
        float scale = sp.getFloat("ui_scale", 1.0f);
        if (scale == 1.0f) return base;

        android.content.res.Configuration config = new android.content.res.Configuration();
        config.updateFrom(base.getResources().getConfiguration());

        int sysDpi = android.content.res.Resources.getSystem().getDisplayMetrics().densityDpi;
        int targetDpi = (int)(sysDpi * scale);
        if (targetDpi < 80) targetDpi = 80;
        if (targetDpi > 960) targetDpi = 960;

        config.densityDpi = targetDpi;
        return base.createConfigurationContext(config);
    }

    // ⭐ 修复：正确传入 root 进行形状判断
    public static void applyRoundPadding(Context ctx, View root) {
        boolean isRoundOpt = WatchHardwareUtils.isRoundScreen(root);
        boolean isChinOpt = WatchHardwareUtils.needChinPadding(ctx);
        float density = ctx.getResources().getDisplayMetrics().density;
        int pLeft = isRoundOpt ? (int)(24 * density) : (int)(12 * density);
        int pRight = isRoundOpt ? (int)(24 * density) : (int)(12 * density);
        int pBottom = isChinOpt ? (int)(32 * density) : 0;
        root.setPadding(pLeft, 0, pRight, pBottom);
    }

    public static GradientDrawable getPillBackground(Context ctx) {
        boolean isDark = isDark(ctx);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(32 * ctx.getResources().getDisplayMetrics().density);
        bg.setColor(isDark ? 0xFF1E1E1E : 0xFFFFFFFF);
        if (!isDark) {
            bg.setStroke((int)(1 * ctx.getResources().getDisplayMetrics().density), 0xFFE0E0E0);
        }
        return bg;
    }

    // ⭐ 修复：正确传入 scrollView 进行形状判断
    public static void setupWheelScroll(final ScrollView scrollView, final LinearLayout container) {
        final Context ctx = scrollView.getContext();
        if (isLowPerformance(ctx)) return;

        boolean isRoundOpt = WatchHardwareUtils.isRoundScreen(scrollView);
        scrollView.setClipToPadding(false);
        scrollView.setVerticalScrollBarEnabled(false);

        if (!isRoundOpt) {
            container.setPadding(container.getPaddingLeft(), 0, container.getPaddingRight(), 0);
            for (int i = 0; i < container.getChildCount(); i++) {
                View child = container.getChildAt(i);
                child.setScaleX(1.0f); child.setScaleY(1.0f); child.setAlpha(1.0f);
            }
            return;
        }
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                int screenHeight = scrollView.getHeight();
                if (screenHeight > 0) {
                    int padding = screenHeight / 2 - dp(40, ctx);
                    container.setPadding(container.getPaddingLeft(), padding, container.getPaddingRight(), padding);
                    scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                        @Override
                        public void onScrollChanged() { applyWheelEffect(scrollView, container); }
                    });
                    applyWheelEffect(scrollView, container);
                }
            }
        });
    }

    private static void applyWheelEffect(ScrollView scrollView, LinearLayout container) {
        int centerY = scrollView.getHeight() / 2;
        int scrollY = scrollView.getScrollY();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) continue;
            int childCenterY = child.getTop() + (child.getHeight() / 2) - scrollY;
            int distance = Math.abs(centerY - childCenterY);
            float maxDistance = centerY;
            float fraction = distance / maxDistance;
            if (fraction > 1.0f) fraction = 1.0f;
            float scale = 1.0f - (0.25f * fraction);
            float alpha = 1.0f - (0.7f * fraction);
            child.setScaleX(scale); child.setScaleY(scale); child.setAlpha(alpha);
        }
    }

    private static int dp(int val, Context ctx) { return (int)(val * ctx.getResources().getDisplayMetrics().density); }

    /** 与设置页显示的值同源：未手设时由 UikitConfig 按设备能力自动判定。 */
    public static boolean isLowPerformance(Context ctx) {
        return org.watchsend.uikit.UikitConfig.isLowPerf(ctx);
    }

    public static void applyEntryAnimation(Activity activity) {
        if (isLowPerformance(activity)) return;
        activity.overridePendingTransition(0, 0);
        final View root = activity.findViewById(android.R.id.content);
        if (root == null) return;
        root.setScaleX(0.6f); root.setScaleY(0.6f); root.setAlpha(0f);
        root.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(250).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
    }

    // ====== 滑动返回（实现只有一份，在 uikit 里，这里只做包内转发） ======
    public static boolean handleSwipeBack(Activity activity, android.view.MotionEvent ev) {
        return org.watchsend.uikit.UikitUi.handleSwipeBack(activity, ev);
    }

    /** 是否启用软件内返回（粗判用；页面内请直接问 BackGate）。 */
    public static boolean isSwipeBackEnabled(Context ctx) {
        return org.watchsend.uikit.UikitConfig.isSwipeBackEnabled(ctx);
    }
}

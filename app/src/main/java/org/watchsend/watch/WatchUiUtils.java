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

    public static boolean isLowPerformance(Context ctx) {
        return ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE).getBoolean("low_perf_mode", false);
    }

    public static void applyEntryAnimation(Activity activity) {
        if (isLowPerformance(activity)) return;
        activity.overridePendingTransition(0, 0);
        final View root = activity.findViewById(android.R.id.content);
        if (root == null) return;
        root.setScaleX(0.6f); root.setScaleY(0.6f); root.setAlpha(0f);
        root.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(250).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
    }

    // ====== 滑动返回 ======
    private static float startX = 0;
    private static float startY = 0;
    private static boolean isSwiping = false;

    public static boolean handleSwipeBack(final Activity activity, android.view.MotionEvent ev) {
        // 关闭"软件内返回"时，滑动不该被我们消费，交给系统手势/控件自己处理
        if (!isSwipeBackEnabled(activity)) return false;
        final View root = activity.findViewById(android.R.id.content);
        final int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;

        switch (ev.getAction()) {
            case android.view.MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                isSwiping = false;
                break;

            case android.view.MotionEvent.ACTION_MOVE:
                float deltaX = ev.getX() - startX;
                float deltaY = ev.getY() - startY;

                if (isLowPerformance(activity)) {
                    if (Math.abs(deltaX) > screenWidth / 4 && Math.abs(deltaX) > Math.abs(deltaY) * 1.5) {
                        activity.finish();
                        return true;
                    }
                    break;
                }

                if (!isSwiping) {
                    if (Math.abs(deltaX) > 40 && Math.abs(deltaX) > Math.abs(deltaY) * 1.5) {
                        isSwiping = true;
                        if (android.os.Build.VERSION.SDK_INT >= 21) {
                            root.setClipToOutline(true);
                            root.setOutlineProvider(new android.view.ViewOutlineProvider() {
                                @Override
                                public void getOutline(android.view.View view, android.graphics.Outline outline) {
                                    float radius = 40f * view.getContext().getResources().getDisplayMetrics().density;
                                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                                }
                            });
                            root.setElevation(20f * activity.getResources().getDisplayMetrics().density);
                        }
                    }
                }

                if (isSwiping) {
                    root.setTranslationX(deltaX);
                    float scale = 1.0f - (Math.abs(deltaX) / (float) screenWidth) * 0.25f;
                    root.setScaleX(scale);
                    root.setScaleY(scale);
                    float alpha = 1.0f - (Math.abs(deltaX) / (float) screenWidth) * 0.5f;
                    root.setAlpha(alpha);
                    return true;
                }
                break;

            case android.view.MotionEvent.ACTION_UP:
            case android.view.MotionEvent.ACTION_CANCEL:
                if (isSwiping) {
                    isSwiping = false;
                    float finalDeltaX = ev.getX() - startX;

                    if (Math.abs(finalDeltaX) > screenWidth / 4) {
                        float targetX = finalDeltaX > 0 ? screenWidth : -screenWidth;
                        root.animate().translationX(targetX).scaleX(0.5f).scaleY(0.5f).alpha(0f).setDuration(200).withEndAction(new Runnable() {
                            public void run() {
                                activity.finish();
                                activity.overridePendingTransition(0, 0);
                                root.setTranslationX(0);
                                root.setScaleX(1f);
                                root.setScaleY(1f);
                                root.setAlpha(1f);
                            }
                        }).start();
                    } else {
                        root.animate().translationX(0).scaleX(1f).scaleY(1f).alpha(1f).setDuration(200).start();
                    }
                    return true;
                }
                break;
        }
        return false;
    }

    /** 是否启用软件内返回（读取的是 UikitConfig 的新键，含老值迁移）。 */
    public static boolean isSwipeBackEnabled(Context ctx) {
        return org.watchsend.uikit.UikitConfig.isSwipeBackEnabled(ctx);
    }

    /** 兼容旧调用点：拦截系统返回 ⇔ 启用软件内返回。 */
    public static boolean isSystemBackBlocked(Context ctx) {
        return isSwipeBackEnabled(ctx);
    }
}

package org.watchsend.uikit;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public final class UikitUi {
    private UikitUi() {}

    public static Context wrapScale(Context base) {
        float scale = UikitConfig.getUiScale(base);
        if (scale == 1.0f) return base;
        if (Build.VERSION.SDK_INT < 17) return base;
        try {
            Configuration config = new Configuration();
            config.updateFrom(base.getResources().getConfiguration());
            int sysDpi = Resources.getSystem().getDisplayMetrics().densityDpi;
            int targetDpi = (int)(sysDpi * scale);
            if (targetDpi < 80) targetDpi = 80;
            if (targetDpi > 960) targetDpi = 960;
            config.densityDpi = targetDpi;
            return base.createConfigurationContext(config);
        } catch (Exception e) { return base; }
    }
    public static GradientDrawable getPillBackground(Context ctx) {
        boolean isDark = UikitTheme.isDarkMode(ctx);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(32 * ctx.getResources().getDisplayMetrics().density);
        bg.setColor(isDark ? 0xFF1E1E1E : 0xFFFFFFFF);
        if (!isDark) bg.setStroke((int)(1 * ctx.getResources().getDisplayMetrics().density), 0xFFE0E0E0);
        return bg;
    }
    /** 递归关掉整页的 overscroll 边缘特效（低性能模式用）。 */
    public static void disableOverScroll(View root) {
        if (root == null) return;
        root.setOverScrollMode(View.OVER_SCROLL_NEVER);
        if (root instanceof android.view.ViewGroup) {
            android.view.ViewGroup g = (android.view.ViewGroup) root;
            for (int i = 0; i < g.getChildCount(); i++) disableOverScroll(g.getChildAt(i));
        }
    }

    public static void applyRoundPadding(Context ctx, View root) {
        if (root == null) return;
        if (UikitConfig.isLowPerf(ctx)) disableOverScroll(root);
        boolean isRound = WearHardwareUtils.isRoundScreen(root);
        boolean isChin  = WearHardwareUtils.needChinPadding(ctx);
        float d = ctx.getResources().getDisplayMetrics().density;
        int pL = isRound ? (int)(24*d) : (int)(12*d);
        int pR = isRound ? (int)(24*d) : (int)(12*d);
        int pB = isChin ? (int)(32*d) : 0;
        root.setPadding(pL, 0, pR, pB);
    }
    public static void setupWheelScroll(final ScrollView sv, final LinearLayout c) {
        if (sv == null || c == null) return;
        if (UikitConfig.isLowPerf(sv.getContext())) {
            // 低性能模式：滚轮缩放/回弹整体跳过，同时关掉边缘 overscroll 特效（边缘拉伸需要额外交付绘制）
            sv.setOverScrollMode(View.OVER_SCROLL_NEVER);
            return;
        }
        boolean isRound = WearHardwareUtils.isRoundScreen(sv);
        sv.setClipToPadding(false);
        sv.setVerticalScrollBarEnabled(false);
        // 统一挂载回弹特效
        setupBounceEffect(sv, c);

        if (!isRound) {
            for (int i = 0; i < c.getChildCount(); i++) {
                View ch = c.getChildAt(i);
                ch.setScaleX(1f); ch.setScaleY(1f); ch.setAlpha(1f);
            }
            return;
        }
        // 圆屏：等布局真正有高度后再安装滚轮，避免 onCreate 阶段 getHeight()==0 导致效果静默失效
        sv.post(new Runnable() {
            @Override public void run() { installWheel(sv, c, 0); }
        });
    }

    /** 布局未完成时最多重试 6 次（约 300ms），保证滚轮效果一定会挂上 */
    private static void installWheel(final ScrollView sv, final LinearLayout c, final int attempt) {
        int h = sv.getHeight();
        if (h <= 0) {
            if (attempt < 6) {
                sv.postDelayed(new Runnable() {
                    @Override public void run() { installWheel(sv, c, attempt + 1); }
                }, 50);
            }
            return;
        }
        int pad = h / 2 - dp(40, sv.getContext());
        if (pad < 0) pad = 0;
        c.setPadding(c.getPaddingLeft(), pad, c.getPaddingRight(), pad);
        sv.getViewTreeObserver().addOnScrollChangedListener(
            new ViewTreeObserver.OnScrollChangedListener() {
                @Override public void onScrollChanged() { applyWheelEffect(sv, c); }
            });
        applyWheelEffect(sv, c);
    }
    private static void applyWheelEffect(ScrollView sv, LinearLayout c) {
        int centerY = sv.getHeight() / 2;
        if (centerY <= 0) return;
        int scrollY = sv.getScrollY();
        for (int i = 0; i < c.getChildCount(); i++) {
            View ch = c.getChildAt(i);
            if (ch.getVisibility() != View.VISIBLE) continue;
            // 尚未测量完成的子 View 不参与缩放，否则会被误判为"远离中心"而整体变暗缩小
            if (ch.getHeight() <= 0) continue;
            int cc = ch.getTop() + ch.getHeight()/2 - scrollY;
            float frac = Math.abs(centerY - cc) / (float) centerY;
            if (frac > 1f) frac = 1f;
            ch.setScaleX(1f - 0.25f*frac);
            ch.setScaleY(1f - 0.25f*frac);
            ch.setAlpha(1f - 0.7f*frac);
        }
    }
    public static void applyEntryAnimation(Activity a) {
        if (a == null || Build.VERSION.SDK_INT < 12) return;
        if (UikitConfig.isLowPerf(a)) return;
        a.overridePendingTransition(0, 0);
        final View root = a.findViewById(android.R.id.content);
        if (root == null) return;
        root.setScaleX(0.6f); root.setScaleY(0.6f); root.setAlpha(0f);
        root.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(250)
            .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
    }

    private static float sStartX = 0, sStartY = 0;
    private static boolean sIsSwiping = false;
    /** 起手点必须靠近左右边缘才算返回手势，避免列表里横向滑动误退页。 */
    private static final int EDGE_DP = 16;

    /**
     * 界面内横滑返回：从左右边缘横向拖动超过约四分之一屏宽时退出当前页。
     * 软件内返回关闭时直接返回 false，触摸事件交还控件与系统，不做任何消费。
     */
    public static boolean handleSwipeBack(final Activity a, android.view.MotionEvent ev) {
        if (a == null) return false;
        if (!UikitConfig.isSwipeBackEnabled(a)) return false;
        final View root = a.findViewById(android.R.id.content);
        if (root == null) return false;
        final int w = a.getResources().getDisplayMetrics().widthPixels;
        final int edge = dp(EDGE_DP, a);

        switch (ev.getAction()) {
            case android.view.MotionEvent.ACTION_DOWN:
                sStartX = ev.getX(); sStartY = ev.getY(); sIsSwiping = false;
                break;

            case android.view.MotionEvent.ACTION_MOVE:
                if (sStartX > edge && sStartX < w - edge) break; // 非边缘起手，不认作返回手势
                float dx = ev.getX() - sStartX;
                float dy = ev.getY() - sStartY;

                if (UikitConfig.isLowPerf(a)) {
                    // 低性能模式不做跟手动画，够距离直接退，省掉一帧帧重绘
                    if (Math.abs(dx) > w / 4 && Math.abs(dx) > Math.abs(dy) * 1.5) { a.finish(); return true; }
                    break;
                }

                if (!sIsSwiping && Math.abs(dx) > 40 && Math.abs(dx) > Math.abs(dy) * 1.5) {
                    sIsSwiping = true;
                    applySwipeOutline(root);
                }
                if (sIsSwiping) {
                    root.setTranslationX(dx);
                    float s = 1f - (Math.abs(dx) / (float) w) * 0.25f;
                    root.setScaleX(s); root.setScaleY(s);
                    root.setAlpha(1f - (Math.abs(dx) / (float) w) * 0.5f);
                    return true;
                }
                break;

            case android.view.MotionEvent.ACTION_UP:
            case android.view.MotionEvent.ACTION_CANCEL:
                if (sIsSwiping) {
                    sIsSwiping = false;
                    float fx = ev.getX() - sStartX;
                    if (Math.abs(fx) > w / 4) {
                        float tx = fx > 0 ? w : -w;
                        root.animate().translationX(tx).scaleX(0.5f).scaleY(0.5f).alpha(0f)
                            .setDuration(200).withEndAction(new Runnable() {
                                @Override public void run() {
                                    a.finish();
                                    a.overridePendingTransition(0, 0);
                                    resetSwipe(root); // 页面可能被复用（singleTop / 返回栈），复位后再交还
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

    /** 拖动中的卡片外观：圆角 + 抬高投影，和主界面原来的观感保持一致。 */
    private static void applySwipeOutline(final View root) {
        if (Build.VERSION.SDK_INT < 21) return;
        root.setClipToOutline(true);
        root.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override public void getOutline(android.view.View view, android.graphics.Outline outline) {
                float radius = 40f * view.getContext().getResources().getDisplayMetrics().density;
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        root.setElevation(20f * root.getContext().getResources().getDisplayMetrics().density);
    }

    private static void resetSwipe(final View root) {
        root.setTranslationX(0);
        root.setScaleX(1f); root.setScaleY(1f); root.setAlpha(1f);
    }

    public static boolean isSwipeBackEnabled(Context ctx) {
        return UikitConfig.isSwipeBackEnabled(ctx);
    }
    private static int dp(int v, Context ctx) {
        return (int)(v * ctx.getResources().getDisplayMetrics().density);
    }

    private static void setupBounceEffect(final ScrollView sv, final View content) {
        sv.setOverScrollMode(View.OVER_SCROLL_NEVER); // 关闭系统自带发光
        sv.setClipChildren(false); // 允许越界绘制

        sv.setOnTouchListener(new View.OnTouchListener() {
            float startY = 0;
            boolean isDragging = false;

            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        isDragging = false;
                        break;
                    case android.view.MotionEvent.ACTION_MOVE:
                        float deltaY = event.getY() - startY;
                        int scrollY = sv.getScrollY();
                        int maxScrollY = Math.max(0, content.getHeight() - sv.getHeight());

                        // 阻尼系数 0.35，最大拉伸 180dp
                        float maxDrag = UikitScreenUtils.dp2px(sv.getContext(), 180);

                        if (scrollY == 0 && deltaY > 0) {
                            // 顶部下拉
                            float translation = Math.min(deltaY * 0.35f, maxDrag);
                            content.setTranslationY(translation);
                            isDragging = true;
                        } else if (scrollY == maxScrollY && deltaY < 0) {
                            // 底部上推
                            float translation = Math.max(deltaY * 0.35f, -maxDrag);
                            content.setTranslationY(translation);
                            isDragging = true;
                        } else if (isDragging) {
                            // 如果滑回了有效区域，立刻复位
                            content.setTranslationY(0);
                            isDragging = false;
                        }
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        if (isDragging) {
                            // 松手后使用弹性插值器弹回
                            content.animate().translationY(0).setDuration(400)
                                .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
                                .start();
                            isDragging = false;
                        }
                        break;
                }
                return false; // 返回 false 保证事件继续传递给子 View 和原生滑动
            }
        });
    }

}

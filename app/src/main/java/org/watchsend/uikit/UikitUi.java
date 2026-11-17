package org.watchsend.uikit;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class UikitUi {
    public interface OnItemSelectListener { void onItemSelect(int position); }
    public interface OnInputListener { void onInput(String text); }
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
    public static void applyRoundPadding(Context ctx, View root) {
        if (root == null) return;
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
        if (UikitConfig.isLowPerf(sv.getContext())) return;
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
        sv.post(new Runnable() {
            @Override public void run() {
                int h = sv.getHeight();
                if (h > 0) {
                    int pad = h/2 - dp(40, sv.getContext());
                    c.setPadding(c.getPaddingLeft(), pad, c.getPaddingRight(), pad);
                    sv.getViewTreeObserver().addOnScrollChangedListener(
                        new ViewTreeObserver.OnScrollChangedListener() {
                            @Override public void onScrollChanged() { applyWheelEffect(sv, c); }
                        });
                    applyWheelEffect(sv, c);
                }
            }
        });
    }
    private static void applyWheelEffect(ScrollView sv, LinearLayout c) {
        int centerY = sv.getHeight()/2;
        int scrollY = sv.getScrollY();
        for (int i = 0; i < c.getChildCount(); i++) {
            View ch = c.getChildAt(i);
            if (ch.getVisibility() != View.VISIBLE) continue;
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
    
    private static void setupWindow(Context ctx, Dialog d) {
        Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(w.getAttributes());
            
            // 判断是否圆屏
            android.util.DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
            float ratio = (float) dm.widthPixels / dm.heightPixels;
            boolean isRound = (ratio >= 0.95f && ratio <= 1.05f);
            
            if (isRound) {
                // 圆屏：宽度缩窄到 75%，避免左右切边
                lp.width = (int)(dm.widthPixels * 0.75);
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            } else {
                // 方屏：保持 90%
                lp.width = (int)(dm.widthPixels * 0.9);
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            }
            w.setAttributes(lp);
        }
    }

    // ====== 修复核心：全局弹窗样式动态化 ======
    private static void applyDialogStyle(Context ctx, Dialog d) {
        boolean dark = UikitTheme.isDarkMode(ctx);
        int density = (int) ctx.getResources().getDisplayMetrics().density;
        
        android.util.DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        float ratio = (float) dm.widthPixels / dm.heightPixels;
        boolean isRound = (ratio >= 0.95f && ratio <= 1.05f);
        
        // 1. 背景
        View root = d.findViewById(R.id.dlg_root);
        if (root != null) {
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(dark ? 0xFF1A1A1A : 0xFFFFFFFF);
            // 圆屏缩小圆角，避免视觉上太圆
            bg.setCornerRadius((isRound ? 24 : 36) * density);
            root.setBackground(bg);
            
            // 圆屏增加上下 padding 避让
            if (isRound) {
                root.setPadding(dp(16, ctx), dp(24, ctx), dp(16, ctx), dp(24, ctx));
            }
        }
        
        // 2. 限制 ScrollView 最大高度 (防止圆屏底部按钮被顶出屏幕)
        try {
            ScrollView scroll = (ScrollView) d.findViewById(R.id.dlg_root).findViewById(
                    ctx.getResources().getIdentifier("dlg_scroll", "id", ctx.getPackageName()));
            // 由于我们无法直接通过 ID 找到没有命名的 ScrollView，我们遍历 root
            if (root instanceof ViewGroup) {
                findAndLimitScrollView((ViewGroup) root, isRound ? 90 : 160, ctx);
            }
        } catch (Exception ignored) {}
        
        // 3. 标题
        TextView title = (TextView) d.findViewById(R.id.dlg_title);
        if (title != null) title.setTextColor(dark ? 0xFFFFFFFF : 0xFF1C1B1F);
        
        // 4. 输入框
        EditText et = (EditText) d.findViewById(R.id.dlg_input);
        if (et != null) {
            GradientDrawable inputBg = new GradientDrawable();
            inputBg.setCornerRadius(16 * density);
            inputBg.setColor(dark ? 0xFF2C2C2E : 0xFFF0F0F0);
            et.setBackground(inputBg);
            et.setTextColor(dark ? 0xFFFFFFFF : 0xFF000000);
        }
        
        // 5. 按钮排版 (圆屏改竖向)
        View btnPos = d.findViewById(R.id.dlg_btn_positive);
        View btnNeg = d.findViewById(R.id.dlg_btn_negative);
        
        if (btnPos != null && btnNeg != null) {
            View parent = (View) btnPos.getParent();
            if (parent instanceof LinearLayout) {
                LinearLayout btnRow = (LinearLayout) parent;
                if (isRound) {
                    // 圆屏：竖向排列，按钮居中
                    btnRow.setOrientation(LinearLayout.VERTICAL);
                    btnRow.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams lpPos = (LinearLayout.LayoutParams) btnPos.getLayoutParams();
                    lpPos.topMargin = dp(16, ctx);
                    btnPos.setLayoutParams(lpPos);
                } else {
                    // 方屏：横向排列
                    btnRow.setOrientation(LinearLayout.HORIZONTAL);
                    btnRow.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams lpPos = (LinearLayout.LayoutParams) btnPos.getLayoutParams();
                    lpPos.topMargin = 0;
                    btnPos.setLayoutParams(lpPos);
                }
            }
        }

        if (btnPos != null) {
            GradientDrawable posBg = new GradientDrawable();
            posBg.setShape(GradientDrawable.OVAL);
            posBg.setColor(UikitColor.getPrimaryColor(ctx));
            btnPos.setBackground(posBg);
            if (btnPos instanceof TextView) ((TextView) btnPos).setTextColor(UikitColor.getOnPrimaryColor(ctx));
        }
        
        if (btnNeg != null) {
            GradientDrawable negBg = new GradientDrawable();
            negBg.setShape(GradientDrawable.OVAL);
            negBg.setColor(dark ? 0xFF2C2C2E : 0xFFE0E0E0);
            btnNeg.setBackground(negBg);
            if (btnNeg instanceof TextView) ((TextView) btnNeg).setTextColor(dark ? 0xFFFFFFFF : 0xFF666666);
        }
    }
    
    private static void findAndLimitScrollView(ViewGroup group, int maxHeightDp, Context ctx) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof ScrollView) {
                child.getLayoutParams().height = dp(maxHeightDp, ctx);
                child.requestLayout();
            } else if (child instanceof ViewGroup) {
                findAndLimitScrollView((ViewGroup) child, maxHeightDp, ctx);
            }
        }
    }

    public static Dialog createBaseDialog(Context ctx, String title, View content,
                                          String positive, final DialogInterface.OnClickListener posL,
                                          String negative, final DialogInterface.OnClickListener negL) {
        final Dialog d = new Dialog(ctx);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_uikit_base);
        setupWindow(ctx, d);
        applyDialogStyle(ctx, d);

        ((TextView) d.findViewById(R.id.dlg_title)).setText(title);
        LinearLayout c = (LinearLayout) d.findViewById(R.id.dlg_content_container);
        if (content != null) c.addView(content);
        
        View bp = d.findViewById(R.id.dlg_btn_positive);
        View bn = d.findViewById(R.id.dlg_btn_negative);
        
        if (positive != null) {
            bp.setVisibility(View.VISIBLE);
            bp.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (posL != null) posL.onClick(d, 0); else d.dismiss();
                }
            });
        } else bp.setVisibility(View.GONE);
        
        if (negative != null) {
            bn.setVisibility(View.VISIBLE);
            bn.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (negL != null) negL.onClick(d, 0); else d.dismiss();
                }
            });
        } else bn.setVisibility(View.GONE);
        
        return d;
    }

    /**
     * @deprecated This non-fullscreen dialog is deprecated and will be removed in future versions.
     * Please migrate to {@link UikitFullListActivity#show(Activity, String, java.util.ArrayList, int)} for better
     * round-screen adaptation and 3D wheel scrolling support.
     */
    @Deprecated
    public static Dialog showList(Context ctx, String title, String[] items, final OnItemSelectListener l) {
        final Dialog d = new Dialog(ctx);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_uikit_list);
        setupWindow(ctx, d);
        applyDialogStyle(ctx, d);
        
        boolean isDark = UikitTheme.isDarkMode(ctx);
        ((TextView) d.findViewById(R.id.dlg_title)).setText(title);
        LinearLayout c = (LinearLayout) d.findViewById(R.id.dlg_list_container);
        c.removeAllViews();
        
        for (int i = 0; i < items.length; i++) {
            final int idx = i;
            TextView tv = new TextView(ctx);
            tv.setText(items[i]);
            tv.setTextSize(16);
            tv.setPadding(16, 16, 16, 16);
            tv.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF333333);
            tv.setGravity(Gravity.CENTER);
            android.util.TypedValue t = new android.util.TypedValue();
            ctx.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, t, true);
            if (t.resourceId != 0) tv.setBackgroundResource(t.resourceId);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    d.dismiss();
                    if (l != null) l.onItemSelect(idx);
                }
            });
            c.addView(tv);
        }
        d.findViewById(R.id.dlg_btn_negative).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { d.dismiss(); }
        });
        return d;
    }

    /**
     * @deprecated This non-fullscreen dialog is deprecated and will be removed in future versions.
     * Please migrate to {@link UikitFullInputActivity#show(Activity, String, String, String, int)} for a better
     * full-screen typing experience and keyboard adaptation.
     */
    @Deprecated
    public static Dialog showInput(Context ctx, String title, String def, final OnInputListener l) {
        final Dialog d = new Dialog(ctx);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_uikit_input);
        setupWindow(ctx, d);
        applyDialogStyle(ctx, d); // 应用动态样式
        
        ((TextView) d.findViewById(R.id.dlg_title)).setText(title);
        final EditText et = (EditText) d.findViewById(R.id.dlg_input);
        et.setText(def);
        
        d.findViewById(R.id.dlg_btn_negative).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { d.dismiss(); }
        });
        d.findViewById(R.id.dlg_btn_positive).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String s = et.getText().toString();
                d.dismiss();
                if (l != null) l.onInput(s);
            }
        });
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface x) {
                et.requestFocus();
                if (d.getWindow() != null)
                    d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });
        return d;
    }

    /**
     * @deprecated This non-fullscreen dialog is deprecated and will be removed in future versions.
     * Please migrate to {@link UikitFullTextActivity#show(Activity, String, String, String, int)} for long texts
     * with 3D wheel scrolling support.
     */
    @Deprecated
    public static Dialog showConfirm(Context ctx, String title, String msg, final DialogInterface.OnClickListener pos) {
        TextView tv = new TextView(ctx);
        tv.setText(msg);
        tv.setTextSize(15);
        tv.setTextColor(UikitTheme.isDarkMode(ctx) ? 0xFFE0E0E0 : 0xFF49454F);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(16, 8, 16, 8);
        return createBaseDialog(ctx, title, tv,
                ctx.getString(R.string.uikit_ok), pos,
                ctx.getString(R.string.uikit_cancel), null);
    }

    private static float sStartX = 0, sStartY = 0;
    private static boolean sIsSwiping = false;
    public static boolean handleSwipeBack(final Activity a, android.view.MotionEvent ev) {
        if (a == null) return false;
        final View root = a.findViewById(android.R.id.content);
        final int w = a.getResources().getDisplayMetrics().widthPixels;
        if (root == null) return false;
        switch (ev.getAction()) {
            case android.view.MotionEvent.ACTION_DOWN:
                sStartX = ev.getX(); sStartY = ev.getY(); sIsSwiping = false; break;
            case android.view.MotionEvent.ACTION_MOVE:
                float dx = ev.getX() - sStartX;
                float dy = ev.getY() - sStartY;
                if (UikitConfig.isLowPerf(a)) {
                    if (Math.abs(dx) > w/4 && Math.abs(dx) > Math.abs(dy)*1.5) { a.finish(); return true; }
                    break;
                }
                if (!sIsSwiping && Math.abs(dx) > 40 && Math.abs(dx) > Math.abs(dy)*1.5) sIsSwiping = true;
                if (sIsSwiping) {
                    root.setTranslationX(dx);
                    float s = 1f - (Math.abs(dx)/(float)w)*0.25f;
                    root.setScaleX(s); root.setScaleY(s);
                    root.setAlpha(1f - (Math.abs(dx)/(float)w)*0.5f);
                    return true;
                }
                break;
            case android.view.MotionEvent.ACTION_UP:
            case android.view.MotionEvent.ACTION_CANCEL:
                if (sIsSwiping) {
                    sIsSwiping = false;
                    float fx = ev.getX() - sStartX;
                    if (Math.abs(fx) > w/4) {
                        if (Build.VERSION.SDK_INT >= 16) {
                            float tx = fx > 0 ? w : -w;
                            root.animate().translationX(tx).scaleX(0.5f).scaleY(0.5f).alpha(0f)
                                .setDuration(200).withEndAction(new Runnable() {
                                    @Override public void run() {
                                        a.finish();
                                        a.overridePendingTransition(0, 0);
                                    }
                                }).start();
                        } else { a.finish(); }
                    } else {
                        root.animate().translationX(0).scaleX(1f).scaleY(1f).alpha(1f).setDuration(200).start();
                    }
                    return true;
                }
                break;
        }
        return false;
    }
    public static boolean isSystemBackBlocked(Context ctx) {
        return UikitConfig.isSystemBackBlocked(ctx);
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

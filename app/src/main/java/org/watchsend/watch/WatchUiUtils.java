package org.watchsend.watch;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import org.watchsend.R;
import org.watchsend.util.I18nManager;
import org.watchsend.util.ThemeUtils;

public class WatchUiUtils {

    public interface OnItemSelectListener { void onItemSelect(int position); }
    public interface OnInputListener { void onInput(String text); }

    public static boolean isDark(Context ctx) { return ThemeUtils.isDarkMode(ctx); }

    public static void applyScale(Context context) {
        float scale = context.getSharedPreferences("oldsend", Context.MODE_PRIVATE).getFloat("ui_scale", 1.0f);
        if (scale != 1.0f) {
            Configuration config = new Configuration();
            config.updateFrom(context.getResources().getConfiguration());
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            if (metrics.densityDpi > 0 && metrics.densityDpi < 1000) {
                metrics.density *= scale;
                metrics.scaledDensity *= scale;
                metrics.densityDpi = (int)(metrics.densityDpi * scale);
                context.getResources().updateConfiguration(config, metrics);
            }
        }
    }

    public static void applyRoundPadding(Context ctx, View root) {
        boolean isRoundOpt = ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE).getBoolean("round_screen_opt", false);
        float density = ctx.getResources().getDisplayMetrics().density;
        int pLeft = isRoundOpt ? (int)(24 * density) : (int)(12 * density);
        int pRight = isRoundOpt ? (int)(24 * density) : (int)(12 * density);
        root.setPadding(pLeft, 0, pRight, 0);
    }

    // 主界面卡片背景 (保留原来的样式，但如果是深色则偏黑灰)
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

    public static void setupWheelScroll(final ScrollView scrollView, final LinearLayout container) {
        final Context ctx = scrollView.getContext();
        boolean isRoundOpt = ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE).getBoolean("round_screen_opt", false);
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

    // ================= Dialog 核心重写 =================
    
    private static void setupWindow(Context ctx, Dialog d) {
        Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(w.getAttributes());
            lp.width = (int) (ctx.getResources().getDisplayMetrics().widthPixels * 0.9);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            w.setAttributes(lp);
        }
    }

    private static void applyWearStyle(Context ctx, Dialog d, View root) {
        // 强制纯黑圆角风格
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF1A1A1A); // 纯黑略带一点点灰，质感更好
        bg.setCornerRadius(36 * ctx.getResources().getDisplayMetrics().density); // 大圆角
        if (root != null) root.setBackground(bg);
        
        TextView title = (TextView) d.findViewById(R.id.dlg_title);
        if (title != null) title.setTextColor(Color.WHITE);
    }

    public static Dialog createBaseDialog(Context ctx, String title, View contentView,
                                          String positive, final DialogInterface.OnClickListener posListener,
                                          String negative, final DialogInterface.OnClickListener negListener) {
        final Dialog d = new Dialog(ctx);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_watch_base);
        setupWindow(ctx, d);
        applyWearStyle(ctx, d, d.findViewById(R.id.dlg_root));

        ((TextView) d.findViewById(R.id.dlg_title)).setText(title);
        LinearLayout container = (LinearLayout) d.findViewById(R.id.dlg_content_container);
        if (contentView != null) container.addView(contentView);

        View btnPos = d.findViewById(R.id.dlg_btn_positive);
        View btnNeg = d.findViewById(R.id.dlg_btn_negative);

        if (positive != null) {
            btnPos.setVisibility(View.VISIBLE);
            btnPos.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (posListener != null) posListener.onClick(d, 0);
                    else d.dismiss();
                }
            });
        } else btnPos.setVisibility(View.GONE);

        if (negative != null) {
            btnNeg.setVisibility(View.VISIBLE);
            btnNeg.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (negListener != null) negListener.onClick(d, 0);
                    else d.dismiss();
                }
            });
        } else btnNeg.setVisibility(View.GONE);

        return d;
    }

    public static Dialog showList(Context ctx, String title, String[] items, final OnItemSelectListener listener) {
        final Dialog d = new Dialog(ctx);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_watch_list);
        setupWindow(ctx, d);
        applyWearStyle(ctx, d, d.findViewById(R.id.dlg_root));

        ((TextView) d.findViewById(R.id.dlg_title)).setText(title);
        LinearLayout container = (LinearLayout) d.findViewById(R.id.dlg_list_container);
        container.removeAllViews();

        for (int i = 0; i < items.length; i++) {
            final int idx = i;
            TextView tv = new TextView(ctx);
            tv.setText(items[i]);
            tv.setTextSize(15);
            tv.setPadding(16, 16, 16, 16);
            tv.setTextColor(0xFFE0E0E0);
            tv.setGravity(Gravity.CENTER); // 居中排版更像系统原生
            android.util.TypedValue tv2 = new android.util.TypedValue();
            ctx.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tv2, true);
            tv.setBackgroundResource(tv2.resourceId);
            tv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    d.dismiss();
                    if (listener != null) listener.onItemSelect(idx);
                }
            });
            container.addView(tv);
        }
        d.findViewById(R.id.dlg_btn_negative).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { d.dismiss(); }
        });
        return d;
    }

    public static Dialog showInput(Context ctx, String title, String def, final OnInputListener listener) {
        final Dialog d = new Dialog(ctx);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_watch_input);
        setupWindow(ctx, d);
        applyWearStyle(ctx, d, d.findViewById(R.id.dlg_root));

        ((TextView) d.findViewById(R.id.dlg_title)).setText(title);
        final EditText et = (EditText) d.findViewById(R.id.dlg_input);
        et.setText(def);
        et.setTextColor(Color.WHITE);

        d.findViewById(R.id.dlg_btn_negative).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { d.dismiss(); }
        });
        d.findViewById(R.id.dlg_btn_positive).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String s = et.getText().toString();
                d.dismiss();
                if (listener != null) listener.onInput(s);
            }
        });
        d.show();
        et.requestFocus();
        if (d.getWindow() != null) d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return d;
    }

    public static Dialog showConfirm(Context ctx, String title, String msg, final DialogInterface.OnClickListener positive) {
        TextView tv = new TextView(ctx);
        tv.setText(msg); tv.setTextSize(14);
        tv.setTextColor(0xFFE0E0E0);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(16, 8, 16, 8);
        return createBaseDialog(ctx, title, tv, I18nManager.get("confirm"), positive, I18nManager.get("cancel"), null);
    }
}

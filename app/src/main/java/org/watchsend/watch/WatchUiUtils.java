package org.watchsend.watch;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
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

    private static void setupWindow(Context ctx, Dialog d) {
        Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(w.getAttributes());
            lp.width = (int) (ctx.getResources().getDisplayMetrics().widthPixels * 0.95);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            w.setAttributes(lp);
        }
    }

    private static void applyDarkDialog(Context ctx, Dialog d, View root) {
        if (!isDark(ctx)) return;
        if (root != null) root.setBackgroundResource(R.drawable.watch_card_dark);
        TextView title = (TextView) d.findViewById(R.id.dlg_title);
        if (title != null) title.setTextColor(0xFFE0E0E0);
        TextView pos = (TextView) d.findViewById(R.id.dlg_btn_positive);
        if (pos != null) pos.setTextColor(0xFF81C784);
        TextView neg = (TextView) d.findViewById(R.id.dlg_btn_negative);
        if (neg != null) neg.setTextColor(0xFFAAAAAA);
    }

    public static Dialog createBaseDialog(Context ctx, String title, View contentView,
                                          String positive, final DialogInterface.OnClickListener posListener,
                                          String negative, final DialogInterface.OnClickListener negListener) {
        final Dialog d = new Dialog(ctx);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_watch_base);
        setupWindow(ctx, d);
        applyDarkDialog(ctx, d, d.findViewById(R.id.dlg_root));

        ((TextView) d.findViewById(R.id.dlg_title)).setText(title);
        LinearLayout container = (LinearLayout) d.findViewById(R.id.dlg_content_container);
        if (contentView != null) container.addView(contentView);

        TextView btnPos = (TextView) d.findViewById(R.id.dlg_btn_positive);
        TextView btnNeg = (TextView) d.findViewById(R.id.dlg_btn_negative);

        if (positive != null) {
            btnPos.setText(positive); btnPos.setVisibility(View.VISIBLE);
            btnPos.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (posListener != null) posListener.onClick(d, 0);
                    else d.dismiss();
                }
            });
        } else btnPos.setVisibility(View.GONE);

        if (negative != null) {
            btnNeg.setText(negative); btnNeg.setVisibility(View.VISIBLE);
            btnNeg.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (negListener != null) negListener.onClick(d, 0);
                    else d.dismiss();
                }
            });
        } else btnNeg.setVisibility(View.GONE);

        return d;
    }

    public static Dialog showList(Context ctx, String title, String[] items,
                                  final OnItemSelectListener listener) {
        final Dialog d = new Dialog(ctx);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_watch_list);
        setupWindow(ctx, d);
        applyDarkDialog(ctx, d, d.findViewById(R.id.dlg_root));

        ((TextView) d.findViewById(R.id.dlg_title)).setText(title);
        LinearLayout container = (LinearLayout) d.findViewById(R.id.dlg_list_container);
        container.removeAllViews();
        boolean dark = isDark(ctx);

        for (int i = 0; i < items.length; i++) {
            final int idx = i;
            TextView tv = new TextView(ctx);
            tv.setText(items[i]);
            tv.setTextSize(14);
            tv.setPadding(24, 18, 24, 18);
            tv.setTextColor(dark ? 0xFFE0E0E0 : 0xFF333333);
            tv.setGravity(Gravity.CENTER_VERTICAL);
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

            if (i < items.length - 1) {
                View div = new View(ctx);
                div.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                div.setBackgroundColor(dark ? 0xFF3E3E3E : 0xFFE0E0E0);
                container.addView(div);
            }
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
        applyDarkDialog(ctx, d, d.findViewById(R.id.dlg_root));

        ((TextView) d.findViewById(R.id.dlg_title)).setText(title);
        final EditText et = (EditText) d.findViewById(R.id.dlg_input);
        et.setText(def);
        if (isDark(ctx)) { et.setTextColor(0xFFE0E0E0); et.setHintTextColor(0xFF888888); }

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

    public static Dialog showConfirm(Context ctx, String title, String msg,
                                     final DialogInterface.OnClickListener positive) {
        TextView tv = new TextView(ctx);
        tv.setText(msg); tv.setTextSize(13);
        tv.setTextColor(isDark(ctx) ? 0xFFAAAAAA : 0xFF49454F);
        tv.setPadding(8, 8, 8, 8);
        return createBaseDialog(ctx, title, tv, I18nManager.get("confirm"), positive,
            I18nManager.get("cancel"), null);
    }
}

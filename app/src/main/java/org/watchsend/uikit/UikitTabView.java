package org.watchsend.uikit;

import org.watchsend.R;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
public class UikitTabView extends LinearLayout {
    public interface OnTabChangeListener { void onTabChanged(int index); }
    private String[] tabs;
    private TextView[] textViews;
    private int currentIndex = 0;
    private OnTabChangeListener listener;
    public UikitTabView(Context context, String[] tabs) {
        super(context);
        this.tabs = tabs;
        setOrientation(HORIZONTAL);
        setBackground(UikitUi.getPillBackground(context));
        setPadding(UikitScreenUtils.dp2px(context, 4), UikitScreenUtils.dp2px(context, 4),
                UikitScreenUtils.dp2px(context, 4), UikitScreenUtils.dp2px(context, 4));
        textViews = new TextView[tabs.length];
        for (int i = 0; i < tabs.length; i++) {
            final int index = i;
            TextView tv = new TextView(context);
            tv.setText(tabs[i]);
            tv.setTextSize(14);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, UikitScreenUtils.dp2px(context, 8), 0, UikitScreenUtils.dp2px(context, 8));
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
            tv.setOnClickListener(new OnClickListener() {
                public void onClick(View v) { setCurrentTab(index); }
            });
            textViews[i] = tv;
            addView(tv);
        }
        setCurrentTab(0);
    }
    public void setOnTabChangeListener(OnTabChangeListener listener) { this.listener = listener; }
    public void setCurrentTab(int index) {
        this.currentIndex = index;
        boolean dark = UikitTheme.isDarkMode(getContext());
        for (int i = 0; i < textViews.length; i++) {
            if (i == index) {
                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(UikitScreenUtils.dp2px(getContext(), 16));
                bg.setColor(UikitColor.getPrimaryColor(getContext()));
                textViews[i].setBackground(bg);
                textViews[i].setTextColor(UikitColor.getOnPrimaryColor(getContext()));
            } else {
                textViews[i].setBackground(null);
                textViews[i].setTextColor(dark ? 0xFFAAAAAA : 0xFF666666);
            }
        }
        if (listener != null) listener.onTabChanged(index);
    }
}

package org.watchsend.uikit;

import org.watchsend.R;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.TextView;
public class UikitBadge extends TextView {
    public UikitBadge(Context context) {
        super(context);
        setGravity(Gravity.CENTER);
        setTextColor(Color.WHITE);
        setTextSize(10);
        setPadding(UikitScreenUtils.dp2px(context, 4), 0, UikitScreenUtils.dp2px(context, 4), 0);
        setMinWidth(UikitScreenUtils.dp2px(context, 16));
        setMinHeight(UikitScreenUtils.dp2px(context, 16));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(UikitScreenUtils.dp2px(context, 8));
        bg.setColor(0xFFF44336); // 红色
        setBackground(bg);
        setVisibility(GONE);
    }
    public void setBadge(String text) {
        if (text == null || text.isEmpty() || "0".equals(text)) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
            setText(text);
        }
    }
    public void setDot() {
        setVisibility(VISIBLE);
        setText("");
        setPadding(0, 0, 0, 0);
        setMinWidth(UikitScreenUtils.dp2px(getContext(), 8));
        setMinHeight(UikitScreenUtils.dp2px(getContext(), 8));
    }
}

package org.watchsend.uikit;

import org.watchsend.R;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class UikitMarqueeTextView extends TextView {
    public UikitMarqueeTextView(Context context) {
        super(context);
        init();
    }

    public UikitMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setSingleLine(true);
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1); // 无限循环
        setHorizontallyScrolling(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public boolean isFocused() {
        // 强制返回 true，确保在没有焦点的情况下也能自动滚动
        return true;
    }
}

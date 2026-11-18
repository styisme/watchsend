package org.watchsend.uikit;

import org.watchsend.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class UikitLinearList extends LinearLayout {
    public UikitLinearList(Context context) {
        super(context);
        init();
    }

    public UikitLinearList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        setBackground(UikitUi.getPillBackground(getContext()));
        setPadding(UikitScreenUtils.dp2px(getContext(), 8), 
                   UikitScreenUtils.dp2px(getContext(), 8), 
                   UikitScreenUtils.dp2px(getContext(), 8), 
                   UikitScreenUtils.dp2px(getContext(), 8));
    }

    /**
     * 添加一个标准列表项
     */
    public void addItem(UikitListItem item, boolean showDivider) {
        addView(item, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        if (showDivider) {
            UikitDivider divider = new UikitDivider(getContext());
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, UikitScreenUtils.dp2px(getContext(), 1));
            divider.setLayoutParams(lp);
            addView(divider);
        }
    }

    /**
     * 清空列表
     */
    public void clear() {
        removeAllViews();
    }
}

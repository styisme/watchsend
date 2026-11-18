package org.watchsend.uikit;

import org.watchsend.R;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class UikitPicker extends FrameLayout {

    public interface OnItemSelectedListener {
        void onItemSelected(int index, String item);
    }

    private ScrollView scrollView;
    private LinearLayout container;
    private String[] items;
    private OnItemSelectedListener listener;
    private int lastSelectedIndex = -1;
    private final int ITEM_HEIGHT = 64; // 每个选项的高度 dp

    public UikitPicker(Context context) {
        super(context);

        scrollView = new ScrollView(context);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setClipToPadding(false);
        scrollView.setOverScrollMode(OVER_SCROLL_NEVER);
        
        scrollView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_UP || 
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                    snapToNearest();
                }
                return false;
            }
        });
        
        scrollView.setOnScrollChangeListener(new OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                updateScale(scrollY);
            }
        });
        
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER_HORIZONTAL);
        scrollView.addView(container);
        addView(scrollView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public String getSelectedItem() {
        if (items == null || lastSelectedIndex < 0) return "";
        return items[lastSelectedIndex];
    }
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    public void setItems(String[] items) {
        this.items = items;
        container.removeAllViews();
        
        if (items == null || items.length == 0) return;

        // 1. 顶部占位（高度 = 屏幕高度的一半 - 单项高度的一半）
        View topSpacer = new View(getContext());
        topSpacer.setLayoutParams(new LinearLayout.LayoutParams(1, getScreenHeight() / 2 - dp(ITEM_HEIGHT) / 2));
        container.addView(topSpacer);

        // 2. 选项列表
        for (String item : items) {
            TextView tv = new TextView(getContext());
            tv.setText(item);
            tv.setTextSize(22);
            tv.setGravity(Gravity.CENTER);
            tv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, dp(ITEM_HEIGHT)));
            tv.setPadding(dp(32), 0, dp(32), 0);
            container.addView(tv);
        }

        // 3. 底部占位
        View bottomSpacer = new View(getContext());
        bottomSpacer.setLayoutParams(new LinearLayout.LayoutParams(1, getScreenHeight() / 2 - dp(ITEM_HEIGHT) / 2));
        container.addView(bottomSpacer);

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                updateScale(0);
                snapToNearest(); // 初始定位到第一项
            }
        });
    }

    private int getScreenHeight() {
        if (getHeight() > 0) return getHeight();
        // 如果还没测量出来，用屏幕高度凑合
        return getResources().getDisplayMetrics().heightPixels;
    }


    public void setSelectedIndex(int index) {
        if (items == null || index < 0 || index >= items.length) return;
        lastSelectedIndex = index;
        final int finalIndex = index;
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                if (container.getChildCount() > finalIndex + 1) {
                    View target = container.getChildAt(finalIndex + 1);
                    if (target != null && getHeight() > 0) {
                        int targetScrollY = target.getTop() - (getHeight() - target.getHeight()) / 2;
                        scrollView.scrollTo(0, targetScrollY);
                        updateScale(targetScrollY);
                    }
                }
            }
        });
    }

    private void updateScale(int scrollY) {
        if (getHeight() == 0 || items == null || container.getChildCount() == 0) return;

        int centerY = getHeight() / 2 + scrollY;
        int minDistance = Integer.MAX_VALUE;
        int nearestIndex = -1;

        for (int i = 0; i < items.length; i++) {
            // 注意：container 包含了 topSpacer，所以索引要 +1
            TextView child = (TextView) container.getChildAt(i + 1);
            if (child.getHeight() == 0) continue;

            int childCenterY = child.getTop() + child.getHeight() / 2;
            int distance = Math.abs(centerY - childCenterY);
            
            float maxDistance = getHeight() / 2f;
            float fraction = distance / maxDistance;
            if (fraction > 1f) fraction = 1f;
            if (Float.isNaN(fraction)) fraction = 1f; 

            // 直上直下缩放
            float scale = 1.0f - 0.35f * fraction;
            float alpha = 1.0f - 0.7f * fraction;
            
            child.setScaleX(scale);
            child.setScaleY(scale);
            child.setAlpha(alpha);
            
            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }

        // 更新选中状态样式
        for (int i = 0; i < items.length; i++) {
            TextView child = (TextView) container.getChildAt(i + 1);
            if (i == nearestIndex) {
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE);
                bg.setCornerRadius(dp(32));
                bg.setColor(UikitColor.getPrimaryColor(getContext()));
                child.setBackground(bg);
                child.setTextColor(UikitColor.getOnPrimaryColor(getContext()));
            } else {
                child.setBackground(null);
                child.setTextColor(UikitTheme.isDarkMode(getContext()) ? 0xFFAAAAAA : 0xFF666666);
            }
        }

        if (nearestIndex != -1 && lastSelectedIndex != nearestIndex) {
            lastSelectedIndex = nearestIndex;
            if (listener != null) {
                listener.onItemSelected(nearestIndex, items[nearestIndex]);
            }
        }
    }

    private void snapToNearest() {
        if (getHeight() == 0 || items == null || items.length == 0) return;
        
        int centerY = getHeight() / 2 + scrollView.getScrollY();
        int minDistance = Integer.MAX_VALUE;
        int nearestIndex = 0;

        for (int i = 0; i < items.length; i++) {
            View child = container.getChildAt(i + 1);
            if (child.getHeight() == 0) continue;
            
            int childCenterY = child.getTop() + child.getHeight() / 2;
            int distance = Math.abs(centerY - childCenterY);
            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }

        View target = container.getChildAt(nearestIndex + 1);
        int targetScrollY = target.getTop() - (getHeight() - target.getHeight()) / 2;
        scrollView.smoothScrollTo(0, targetScrollY);
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density);
    }
}

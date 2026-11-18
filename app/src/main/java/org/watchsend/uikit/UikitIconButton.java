package org.watchsend.uikit;

import org.watchsend.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class UikitIconButton extends FrameLayout {
    private ImageView iconView;
    private int iconSize = 24; // dp

    public UikitIconButton(Context context) {
        super(context);
        init(context, null);
    }

    public UikitIconButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setClickable(true);
        setFocusable(true);

        int defaultRadius = UikitScreenUtils.dp2px(context, 48);
        int bgColor = UikitColor.getPrimaryColor(context);
        int tintColor = UikitColor.getOnPrimaryColor(context);
        
        float radius = defaultRadius;
        Drawable iconDrawable = null;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UikitIconButton);
            if (a != null) {
                radius = a.getDimension(R.styleable.UikitIconButton_uikit_icon_radius, defaultRadius);
                bgColor = a.getColor(R.styleable.UikitIconButton_uikit_icon_bg, bgColor);
                tintColor = a.getColor(R.styleable.UikitIconButton_uikit_icon_tint, tintColor);
                a.recycle();
            }
            
            TypedArray sysA = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.src});
            if (sysA != null) {
                iconDrawable = sysA.getDrawable(0);
                sysA.recycle();
            }
        }

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(radius);
        bg.setColor(bgColor);
        setBackground(bg);

        iconView = new ImageView(context);
        iconSize = UikitScreenUtils.dp2px(context, iconSize);
        LayoutParams lp = new LayoutParams(iconSize, iconSize);
        lp.gravity = Gravity.CENTER;
        iconView.setLayoutParams(lp);
        
        if (iconDrawable != null) {
            iconView.setImageDrawable(iconDrawable);
        }
        applyTint(tintColor);
        addView(iconView);
    }

    public void setIconResource(int resId) {
        if (resId != 0) {
            iconView.setImageResource(resId);
        }
    }

    public void setIconTint(int color) {
        applyTint(color);
    }

    private void applyTint(int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            iconView.setImageTintList(android.content.res.ColorStateList.valueOf(color));
        } else {
            iconView.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 【核心修复】仅 >= 12 才允许执行动画
        if (Build.VERSION.SDK_INT >= 12 && isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animate().scaleX(0.9f).scaleY(0.9f).alpha(0.8f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(150).start();
                    break;
            }
        }
        return super.onTouchEvent(event);
    }
}

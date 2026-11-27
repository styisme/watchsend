package org.watchsend.uikit;

import org.watchsend.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UikitButton extends LinearLayout {

    protected ImageView iconView;
    protected TextView textView;
    protected int textColor;
    protected int iconSize = 18; // dp

    public UikitButton(Context context) {
        super(context);
        init(context, null);
    }

    public UikitButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public UikitButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(HORIZONTAL);
        
        // 【核心修复】必须调用 super.setGravity，防止此时 textView 为 null 导致 NPE 闪退
        super.setGravity(Gravity.CENTER); 
        
        setClickable(true);
        setFocusable(true);

        int defaultRadius = UikitScreenUtils.dp2px(context, 32);
        int defaultBgColor = UikitColor.getPrimaryColor(context);
        textColor = UikitColor.getOnPrimaryColor(context);
        
        float radius = defaultRadius;
        int bgColor = defaultBgColor;

        CharSequence text = "";
        Drawable drawableStart = null;
        int drawablePadding = 0;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UikitButton);
            if (a != null) {
                radius = a.getDimension(R.styleable.UikitButton_uikit_radius, defaultRadius);
                bgColor = a.getColor(R.styleable.UikitButton_uikit_backgroundColor, defaultBgColor);
                textColor = a.getColor(R.styleable.UikitButton_uikit_textColor, textColor);
                a.recycle();
            }

            TypedArray sysA = context.obtainStyledAttributes(attrs, new int[]{
                    android.R.attr.text,
                    android.R.attr.drawableStart,
                    android.R.attr.drawablePadding
            });
            if (sysA != null) {
                text = sysA.getText(0);
                drawableStart = sysA.getDrawable(1);
                drawablePadding = sysA.getDimensionPixelSize(2, 0);
                sysA.recycle();
            }
        }

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(radius);
        bg.setColor(bgColor);
        setBackground(bg);

        iconView = new ImageView(context);
        iconView.setVisibility(GONE);
        iconSize = UikitScreenUtils.dp2px(context, iconSize);
        LayoutParams iconLp = new LayoutParams(iconSize, iconSize);
        iconLp.gravity = Gravity.CENTER_VERTICAL;
        iconLp.rightMargin = drawablePadding > 0 ? drawablePadding : UikitScreenUtils.dp2px(context, 8);
        iconView.setLayoutParams(iconLp);
        addView(iconView);

        textView = new TextView(context);
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER); // 初始化内部 TextView 的对齐
        textView.setTextColor(textColor);
        addView(textView);

        setText(text);
        setIcon(drawableStart);
    }

    // ================= 兼容原生 API =================

    public void setText(CharSequence text) {
        if (textView != null) {
            textView.setText(text);
            textView.setVisibility(text != null && text.length() > 0 ? VISIBLE : GONE);
        }
    }

    public CharSequence getText() {
        return textView != null ? textView.getText() : "";
    }

    public void setTextColor(int color) {
        textColor = color;
        if (textView != null) textView.setTextColor(color);
        if (iconView != null && iconView.getDrawable() != null) {
            applyIconTint();
        }
    }

    public void setTextColor(ColorStateList colors) {
        if (colors != null) {
            textColor = colors.getDefaultColor();
            if (textView != null) textView.setTextColor(colors);
            if (iconView != null && iconView.getDrawable() != null) {
                applyIconTint();
            }
        }
    }

    public void setTextSize(float size) {
        if (textView != null) textView.setTextSize(size);
    }

    public void setTextSize(int unit, float size) {
        if (textView != null) textView.setTextSize(unit, size);
    }

    @Override
    public void setGravity(int gravity) {
        super.setGravity(gravity);
        if (textView != null) {
            textView.setGravity(gravity);
        }
    }

    public void setTypeface(Typeface tf) {
        if (textView != null) textView.setTypeface(tf);
    }

    public void setAllCaps(boolean allCaps) {
        if (textView != null) textView.setAllCaps(allCaps);
    }

    public ColorStateList getTextColors() {
        return textView != null ? textView.getTextColors() : null;
    }

    // ================= 库原生 API =================

    public void setIcon(Drawable drawable) {
        if (iconView != null) {
            if (drawable != null) {
                iconView.setVisibility(VISIBLE);
                iconView.setImageDrawable(drawable);
                applyIconTint();
            } else {
                iconView.setVisibility(GONE);
            }
        }
    }

    public void setIconResource(int resId) {
        if (iconView != null) {
            if (resId != 0) {
                iconView.setVisibility(VISIBLE);
                iconView.setImageResource(resId);
                applyIconTint();
            } else {
                iconView.setVisibility(GONE);
            }
        }
    }

    protected void applyIconTint() {
        if (iconView == null) return;
        if (Build.VERSION.SDK_INT >= 21) {
            iconView.setImageTintList(ColorStateList.valueOf(textColor));
        } else {
            iconView.setColorFilter(textColor, PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setAlpha(enabled ? 1.0f : 0.5f);
        if (textView != null) textView.setEnabled(enabled);
        if (iconView != null) iconView.setEnabled(enabled);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 低性能模式下不跑按压缩放动画，避免每次点击都触发一轮属性动画
        if (Build.VERSION.SDK_INT >= 12 && isEnabled() && !UikitConfig.isLowPerf(getContext())) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animate().scaleX(0.92f).scaleY(0.92f).alpha(0.7f)
                        .setDuration(UikitConfig.animDuration(getContext(), 100)).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    animate().scaleX(1f).scaleY(1f).alpha(1f)
                        .setDuration(UikitConfig.animDuration(getContext(), 150)).start();
                    break;
            }
        }
        return super.onTouchEvent(event);
    }
}

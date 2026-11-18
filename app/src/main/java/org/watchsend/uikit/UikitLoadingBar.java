package org.watchsend.uikit;

import org.watchsend.R;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class UikitLoadingBar extends View {
    private Paint trackBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint trackFgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rect = new RectF();

    private float trackHeight;
    private float animRatio = 0f;
    private ValueAnimator animator;

    public UikitLoadingBar(Context context) {
        super(context);
        trackHeight = UikitScreenUtils.dp2px(context, 8);
        
        trackBgPaint.setStyle(Paint.Style.FILL);
        trackFgPaint.setStyle(Paint.Style.FILL);
        
        boolean dark = UikitTheme.isDarkMode(context);
        trackBgPaint.setColor(dark ? 0xFF3E3E3E : 0xFFE0E0E0);
        trackFgPaint.setColor(UikitColor.getPrimaryColor(context));
    }

    public void startLoading() {
        if (animator != null && animator.isRunning()) return;
        
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1200);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animRatio = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

    public void stopLoading() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        animRatio = 0f;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float width = getWidth();
        float height = getHeight();
        float cy = height / 2f;

        // 1. 背景轨道
        rect.set(0, cy - trackHeight / 2f, width, cy + trackHeight / 2f);
        canvas.drawRoundRect(rect, trackHeight / 2f, trackHeight / 2f, trackBgPaint);

        // 2. 动画块
        // 动画块的宽度占总宽度的 30%
        float blockWidth = width * 0.3f;
        // 动画块的起始 X 轴位置从 -blockWidth 移动到 width
        float startX = -blockWidth + (width + blockWidth) * animRatio;
        float endX = startX + blockWidth;
        
        // 确保不超出左右边界
        if (startX < 0) startX = 0;
        if (endX > width) endX = width;

        rect.set(startX, cy - trackHeight / 2f, endX, cy + trackHeight / 2f);
        canvas.drawRoundRect(rect, trackHeight / 2f, trackHeight / 2f, trackFgPaint);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startLoading();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopLoading();
    }
}

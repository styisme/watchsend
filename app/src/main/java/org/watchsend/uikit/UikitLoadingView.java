package org.watchsend.uikit;

import org.watchsend.R;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.view.animation.LinearInterpolator;
public class UikitLoadingView extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float animValue = 0f;
    private ValueAnimator animator;
    public UikitLoadingView(Context context) {
        super(context);
        paint.setColor(UikitColor.getPrimaryColor(context));
    }
    public void start() {
        if (animator != null && animator.isRunning()) return;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator a) { animValue = (Float) a.getAnimatedValue(); invalidate(); }
        });
        animator.start();
    }
    public void stop() {
        if (animator != null) { animator.cancel(); animator = null; }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        float radius = UikitScreenUtils.dp2px(getContext(), 6);
        float gap = UikitScreenUtils.dp2px(getContext(), 16);
        for (int i = 0; i < 3; i++) {
            // 为每个圆点增加不同的相位
            float phase = (animValue + i * 0.33f) % 1f;
            // 使用正弦函数实现缩放呼吸
            float scale = 0.5f + 0.5f * (float)Math.sin(phase * Math.PI);
            float x = cx + (i - 1) * gap;
            paint.setAlpha((int)(255 * scale));
            canvas.drawCircle(x, cy, radius * scale, paint);
        }
    }
    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); start(); }
    @Override protected void onDetachedFromWindow() { super.onDetachedFromWindow(); stop(); }
}

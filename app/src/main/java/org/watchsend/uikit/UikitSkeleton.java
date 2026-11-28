package org.watchsend.uikit;

import org.watchsend.R;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import android.view.animation.LinearInterpolator;
public class UikitSkeleton extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rect = new RectF();
    private LinearGradient gradient;
    private Matrix matrix = new Matrix();
    private float translateX = 0f;
    private ValueAnimator animator;
    
    public UikitSkeleton(Context context) {
        super(context);
        boolean dark = UikitTheme.isDarkMode(context);
        if (UikitConfig.isLowPerf(context)) {
            // 低性能模式：不建渐变，纯色灰块，省掉 shader 的逐像素插值与每帧矩阵运算
            paint.setColor(dark ? 0xFF2C2C2E : 0xFFEAEAEA);
            return;
        }
        int color1 = dark ? 0xFF2C2C2E : 0xFFE0E0E0;
        int color2 = dark ? 0xFF3E3E42 : 0xFFF5F5F5;
        gradient = new LinearGradient(0, 0, 400, 0, new int[]{color1, color2, color1}, new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP);
        paint.setShader(gradient);
    }
    public void start() {
        if (animator != null && animator.isRunning()) return;
        // 低性能模式：去掉微光扫动，只保留静态灰块
        if (UikitConfig.isLowPerf(getContext())) {
            translateX = 0f;
            invalidate();
            return;
        }
        animator = ValueAnimator.ofFloat(-400f, 400f);
        animator.setDuration(1000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator a) {
                translateX = (Float) a.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (UikitConfig.isLowPerf(getContext())) {
            // 纯色路径：没有 shader 可算，直接画圆角矩形
            rect.set(0, 0, getWidth(), getHeight());
            canvas.drawRoundRect(rect, getHeight() / 2f, getHeight() / 2f, paint);
            return;
        }
        matrix.setTranslate(translateX, 0);
        gradient.setLocalMatrix(matrix);
        rect.set(0, 0, getWidth(), getHeight());
        float r = getHeight() / 2f;
        canvas.drawRoundRect(rect, r, r, paint);
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        start();
    }
}

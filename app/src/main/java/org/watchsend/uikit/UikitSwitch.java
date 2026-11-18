package org.watchsend.uikit;

import org.watchsend.R;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.CheckBox;

public class UikitSwitch extends CheckBox {
    private Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint thumbShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF trackRect = new RectF();
    private float animProgress = 0f;

    public UikitSwitch(Context context) { super(context); init(); }
    public UikitSwitch(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        trackPaint.setStyle(Paint.Style.FILL);
        thumbShadowPaint.setStyle(Paint.Style.FILL);
        thumbShadowPaint.setColor(0x33000000);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setColor(Color.WHITE);
        setClickable(true);
        setFocusable(true);
    }
    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        float target = checked ? 1f : 0f;
        if (animProgress != target) {
            if (Build.VERSION.SDK_INT >= 11) {
                ValueAnimator animator = ValueAnimator.ofFloat(animProgress, target);
                animator.setDuration(200);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override public void onAnimationUpdate(ValueAnimator a) {
                        animProgress = (float) a.getAnimatedValue();
                        invalidate();
                    }
                });
                animator.start();
            } else { animProgress = target; invalidate(); }
        }
    }
    @Override protected void onDraw(Canvas canvas) {
        int width = getWidth(), height = getHeight();
        if (width <= 0 || height <= 0) return;
        float radius = height / 2f;
        trackRect.set(0, 0, width, height);
        int colorOff = UikitTheme.isDarkMode(getContext()) ? 0xFF3E3E3E : 0xFFE0E0E0;
        int colorOn = UikitColor.getPrimaryColor(getContext());
        trackPaint.setColor(evaluateColor(animProgress, colorOff, colorOn));
        canvas.drawRoundRect(trackRect, radius, radius, trackPaint);
        float thumbRadius = radius - 5f;
        float minX = radius, maxX = width - radius;
        float thumbX = minX + (maxX - minX) * animProgress;
        canvas.drawCircle(thumbX, radius + 1f, thumbRadius + 1f, thumbShadowPaint);
        canvas.drawCircle(thumbX, radius, thumbRadius, thumbPaint);
    }
    private int evaluateColor(float f, int s, int e) {
        int sa=(s>>24)&0xff,sr=(s>>16)&0xff,sg=(s>>8)&0xff,sb=s&0xff;
        int ea=(e>>24)&0xff,er=(e>>16)&0xff,eg=(e>>8)&0xff,eb=e&0xff;
        return ((sa+(int)(f*(ea-sa)))<<24)|((sr+(int)(f*(er-sr)))<<16)
              |((sg+(int)(f*(eg-sg)))<<8)|((sb+(int)(f*(eb-sb))));
    }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animProgress = isChecked() ? 1f : 0f;
        invalidate();
    }
}

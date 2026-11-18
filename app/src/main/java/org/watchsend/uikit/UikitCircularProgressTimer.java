package org.watchsend.uikit;

import org.watchsend.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.view.View;

public class UikitCircularProgressTimer extends View {
    public interface OnTimerFinishListener { void onFinish(); }

    private Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint fgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rect = new RectF();
    
    private CountDownTimer countDownTimer;
    private long totalMillis = 0;
    private long millisUntilFinished = 0;
    private OnTimerFinishListener listener;

    public UikitCircularProgressTimer(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        int stroke = UikitScreenUtils.dp2px(context, 6);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(stroke);
        bgPaint.setColor(UikitTheme.isDarkMode(context) ? 0xFF3E3E3E : 0xFFE0E0E0);

        fgPaint.setStyle(Paint.Style.STROKE);
        fgPaint.setStrokeWidth(stroke);
        fgPaint.setStrokeCap(Paint.Cap.ROUND);
        fgPaint.setColor(UikitColor.getPrimaryColor(context));

        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(UikitScreenUtils.dp2px(context, 24));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(UikitTheme.isDarkMode(context) ? 0xFFFFFFFF : 0xFF1C1B1F);
    }

    public void start(long millis) {
        if (countDownTimer != null) countDownTimer.cancel();
        this.totalMillis = millis;
        this.millisUntilFinished = millis;
        
        countDownTimer = new CountDownTimer(millis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                UikitCircularProgressTimer.this.millisUntilFinished = millisUntilFinished;
                if (getWindowToken() != null) { // 防御：只有 View 还在屏幕上才重绘
                    invalidate();
                }
            }

            @Override
            public void onFinish() {
                UikitCircularProgressTimer.this.millisUntilFinished = 0;
                if (getWindowToken() != null) {
                    invalidate();
                }
                if (listener != null) listener.onFinish();
                WearHardwareUtils.vibrate(getContext(), WearHardwareUtils.HAPTIC_ERROR);
            }
        }.start();
    }

    public void stop() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    public void setOnTimerFinishListener(OnTimerFinishListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) return; // 防御

        int padding = (int) fgPaint.getStrokeWidth() + 4;
        rect.set(padding, padding, getWidth() - padding, getHeight() - padding);

        canvas.drawArc(rect, 0, 360, false, bgPaint);

        if (totalMillis > 0) {
            float progress = (float) millisUntilFinished / totalMillis;
            canvas.drawArc(rect, -90, progress * 360, false, fgPaint);

            int seconds = (int) (millisUntilFinished / 1000);
            String timeText = String.valueOf(seconds);
            float centerY = (rect.centerY()) - ((textPaint.descent() + textPaint.ascent()) / 2);
            canvas.drawText(timeText, rect.centerX(), centerY, textPaint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop(); // 核心修复：View 销毁时强制停掉倒计时，绝不允许在后台空跑触发 invalidate
    }
}

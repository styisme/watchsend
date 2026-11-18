package org.watchsend.uikit;

import org.watchsend.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

public class UikitSlider extends View {
    public interface OnProgressChangedListener {
        void onProgressChanged(float progress);
    }

    private Paint trackBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint trackFgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rect = new RectF();

    private int maxProgress = 100;
    private int currentProgress = 0;
    
    private float trackHeight;
    private float thumbRadius;
    
    private OnProgressChangedListener listener;
    private SeekBar.OnSeekBarChangeListener seekBarListener;

    public UikitSlider(Context context) {
        super(context);
        trackHeight = UikitScreenUtils.dp2px(context, 8);
        thumbRadius = UikitScreenUtils.dp2px(context, 12);
        
        trackBgPaint.setStyle(Paint.Style.FILL);
        trackFgPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setStyle(Paint.Style.FILL);
        
        boolean dark = UikitTheme.isDarkMode(context);
        trackBgPaint.setColor(dark ? 0xFF3E3E3E : 0xFFE0E0E0);
        trackFgPaint.setColor(UikitColor.getPrimaryColor(context));
        thumbPaint.setColor(UikitColor.getPrimaryColor(context));
    }

    public void setMax(int max) {
        this.maxProgress = max;
        if (this.maxProgress <= 0) this.maxProgress = 100; // 防御
        invalidate();
    }

    public int getMax() {
        return maxProgress;
    }

    public void setProgress(int progress) {
        if (progress < 0) progress = 0;
        if (progress > maxProgress) progress = maxProgress;
        this.currentProgress = progress;
        invalidate();
        if (seekBarListener != null) {
            seekBarListener.onProgressChanged(null, currentProgress, false);
        }
    }

    public int getProgress() {
        return currentProgress;
    }

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
        this.seekBarListener = listener;
    }

    public void setMaxProgress(float max) {
        this.maxProgress = (int) max;
        if (this.maxProgress <= 0) this.maxProgress = 100;
        invalidate();
    }

    public float getProgressFloat() {
        if (maxProgress == 0) return 0f;
        return (float) currentProgress / maxProgress;
    }

    public void setProgress(float progress) {
        setProgress((int)(progress * maxProgress));
    }

    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) return; // 防御
        
        float width = getWidth();
        float height = getHeight();
        float cy = height / 2f;
        
        rect.set(0, cy - trackHeight / 2f, width, cy + trackHeight / 2f);
        canvas.drawRoundRect(rect, trackHeight / 2f, trackHeight / 2f, trackBgPaint);

        if (maxProgress == 0) return; // 防御除零
        float ratio = (float) currentProgress / maxProgress;
        float progressX = width * ratio;
        
        if (progressX > 0) {
            rect.set(0, cy - trackHeight / 2f, progressX, cy + trackHeight / 2f);
            canvas.drawRoundRect(rect, trackHeight / 2f, trackHeight / 2f, trackFgPaint);
        }

        float thumbX = progressX;
        if (thumbX < thumbRadius) thumbX = thumbRadius;
        if (thumbX > width - thumbRadius) thumbX = width - thumbRadius;
        canvas.drawCircle(thumbX, cy, thumbRadius, thumbPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() == 0) return false; // 防御
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (seekBarListener != null) seekBarListener.onStartTrackingTouch(null);
                updateProgressFromTouch(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                updateProgressFromTouch(event);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (seekBarListener != null) seekBarListener.onStopTrackingTouch(null);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void updateProgressFromTouch(MotionEvent event) {
        float x = event.getX();
        float width = getWidth();
        if (width == 0) return;
        
        float ratio = x / width;
        if (ratio < 0) ratio = 0;
        if (ratio > 1) ratio = 1;
        
        currentProgress = (int) (ratio * maxProgress);
        invalidate();
        
        if (seekBarListener != null) seekBarListener.onProgressChanged(null, currentProgress, true);
        if (listener != null) listener.onProgressChanged((float) currentProgress);
    }
}

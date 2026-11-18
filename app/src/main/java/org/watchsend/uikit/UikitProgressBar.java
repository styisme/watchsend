package org.watchsend.uikit;

import org.watchsend.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class UikitProgressBar extends View {
    private Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint fgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rect = new RectF();
    
    private int maxProgress = 100;
    private int currentProgress = 0;

    public UikitProgressBar(Context context) {
        super(context);
        bgPaint.setColor(UikitTheme.isDarkMode(context) ? 0xFF3E3E3E : 0xFFE0E0E0);
        fgPaint.setColor(UikitColor.getPrimaryColor(context));
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
    }

    public int getProgress() {
        return currentProgress;
    }

    public void setProgress(float progress) {
        setProgress((int)(progress * maxProgress));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) return; // 防御
        
        float r = getHeight() / 2f;
        rect.set(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(rect, r, r, bgPaint);
        
        // 防御除零
        if (maxProgress == 0) return;
        float ratio = (float) currentProgress / maxProgress;
        
        rect.set(0, 0, getWidth() * ratio, getHeight());
        canvas.drawRoundRect(rect, r, r, fgPaint);
    }
}

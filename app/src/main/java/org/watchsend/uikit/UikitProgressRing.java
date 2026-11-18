package org.watchsend.uikit;

import org.watchsend.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
public class UikitProgressRing extends View {
    private Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint fgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rect = new RectF();
    private float progress = 0f;
    
    public UikitProgressRing(Context context) {
        super(context);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(UikitScreenUtils.dp2px(context, 6));
        bgPaint.setColor(UikitTheme.isDarkMode(context) ? 0xFF3E3E3E : 0xFFE0E0E0);
        
        fgPaint.setStyle(Paint.Style.STROKE);
        fgPaint.setStrokeWidth(UikitScreenUtils.dp2px(context, 6));
        fgPaint.setStrokeCap(Paint.Cap.ROUND);
        fgPaint.setColor(UikitColor.getPrimaryColor(context));
    }
    public void setProgress(float progress) {
        this.progress = Math.max(0, Math.min(1, progress));
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int padding = (int) fgPaint.getStrokeWidth() + 4;
        rect.set(padding, padding, getWidth() - padding, getHeight() - padding);
        canvas.drawArc(rect, 0, 360, false, bgPaint);
        canvas.drawArc(rect, -90, progress * 360, false, fgPaint);
    }
}

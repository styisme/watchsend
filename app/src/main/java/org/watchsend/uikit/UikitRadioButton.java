package org.watchsend.uikit;

import org.watchsend.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RadioButton;

public class UikitRadioButton extends RadioButton {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public UikitRadioButton(Context context) { super(context); init(); }
    public UikitRadioButton(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(UikitScreenUtils.dp2px(getContext(), 2));
        dotPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int size = getHeight();
        if (size == 0) return;

        int cx = getWidth() / 2;
        int padding = UikitScreenUtils.dp2px(getContext(), 8);
        float radius = size / 2f - padding;
        
        // 【防御】如果半径小于等于0，直接返回
        if (radius <= 0) return;

        float centerX = cx;
        float centerY = size / 2f;

        int primaryColor = UikitColor.getPrimaryColor(getContext());
        int unCheckColor = UikitTheme.isDarkMode(getContext()) ? 0xFF888888 : 0xFFCCCCCC;

        if (isChecked()) {
            paint.setColor(primaryColor);
            canvas.drawCircle(centerX, centerY, radius, paint);
            dotPaint.setColor(primaryColor);
            canvas.drawCircle(centerX, centerY, radius * 0.5f, dotPaint);
        } else {
            paint.setColor(unCheckColor);
            canvas.drawCircle(centerX, centerY, radius, paint);
        }
    }
}

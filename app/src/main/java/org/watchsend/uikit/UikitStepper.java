package org.watchsend.uikit;

import org.watchsend.R;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UikitStepper extends LinearLayout {
    public interface OnValueChangeListener {
        void onValueChanged(int value);
    }

    private int minValue = 0;
    private int maxValue = 100;
    private int currentValue = 0;
    private TextView tvValue;
    private OnValueChangeListener listener;

    public UikitStepper(Context context) {
        super(context);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackground(UikitUi.getPillBackground(getContext()));
        setPadding(UikitScreenUtils.dp2px(getContext(), 8), UikitScreenUtils.dp2px(getContext(), 8),
                UikitScreenUtils.dp2px(getContext(), 8), UikitScreenUtils.dp2px(getContext(), 8));

        TextView btnMinus = createButton("-");
        TextView btnPlus = createButton("+");
        tvValue = new TextView(getContext());
        tvValue.setTextSize(18);
        tvValue.setTextColor(UikitTheme.isDarkMode(getContext()) ? 0xFFFFFFFF : 0xFF1C1B1F);
        tvValue.setGravity(Gravity.CENTER);
        tvValue.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        btnMinus.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (currentValue > minValue) {
                    currentValue--;
                    updateUI();
                }
            }
        });

        btnPlus.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (currentValue < maxValue) {
                    currentValue++;
                    updateUI();
                }
            }
        });

        addView(btnMinus);
        addView(tvValue);
        addView(btnPlus);
        
        updateUI();
    }

    private TextView createButton(String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextSize(20);
        tv.setTextColor(UikitColor.getPrimaryColor(getContext()));
        tv.setGravity(Gravity.CENTER);
        int size = UikitScreenUtils.dp2px(getContext(), 36);
        tv.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(UikitTheme.isDarkMode(getContext()) ? 0xFF2C2C2E : 0xFFF0F0F0);
        tv.setBackground(bg);
        return tv;
    }

    public void setMinValue(int min) {
        this.minValue = min;
        if (currentValue < min) currentValue = min;
        updateUI();
    }

    public void setMaxValue(int max) {
        this.maxValue = max;
        if (currentValue > max) currentValue = max;
        updateUI();
    }

    public void setCurrentValue(int value) {
        if (value >= minValue && value <= maxValue) {
            this.currentValue = value;
            updateUI();
        }
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setOnValueChangeListener(OnValueChangeListener listener) {
        this.listener = listener;
    }

    private void updateUI() {
        tvValue.setText(String.valueOf(currentValue));
        if (listener != null) listener.onValueChanged(currentValue);
    }
}

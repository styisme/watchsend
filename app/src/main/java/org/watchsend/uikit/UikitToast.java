package org.watchsend.uikit;

import org.watchsend.R;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public final class UikitToast {
    private UikitToast() {}

    public static void show(Context ctx, String msg) {
        Toast toast = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT);
        TextView tv = new TextView(ctx);
        tv.setText(msg);
        tv.setTextSize(14);
        tv.setTextColor(0xFFFFFFFF);
        tv.setPadding(24, 12, 24, 12);
        
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(32);
        bg.setColor(0xE6333333);
        tv.setBackground(bg);
        
        toast.setView(tv);
        
        // 【核心修复】将 Toast 显示位置设为屏幕正中央
        toast.setGravity(Gravity.CENTER, 0, 0);
        
        toast.show();
    }
}

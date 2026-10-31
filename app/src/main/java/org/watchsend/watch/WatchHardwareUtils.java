package org.watchsend.watch;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;

public class WatchHardwareUtils {
    public static final int HAPTIC_LIGHT = 0;
    public static final int HAPTIC_SUCCESS = 1;
    public static final int HAPTIC_ERROR = 2;

    // 兼容极低版本与高版本的震动控制
    public static void vibrate(Context context, int type) {
        if (context == null) return;
        try {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v == null || !v.hasVibrator()) return;

            if (Build.VERSION.SDK_INT >= 26) {
                VibrationEffect effect = null;
                if (type == HAPTIC_LIGHT) {
                    effect = VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE);
                } else if (type == HAPTIC_SUCCESS) {
                    effect = VibrationEffect.createWaveform(new long[]{0, 50, 50, 50}, -1);
                } else if (type == HAPTIC_ERROR) {
                    effect = VibrationEffect.createWaveform(new long[]{0, 100, 100, 100, 100, 100}, -1);
                }
                if (effect != null) v.vibrate(effect);
            } else {
                // API 1 ~ 25
                if (type == HAPTIC_LIGHT) {
                    v.vibrate(20);
                } else if (type == HAPTIC_SUCCESS) {
                    v.vibrate(new long[]{0, 50, 50, 50}, -1);
                } else if (type == HAPTIC_ERROR) {
                    v.vibrate(new long[]{0, 100, 100, 100, 100, 100}, -1);
                }
            }
        } catch (Exception e) {
            // 忽略缺少权限或无震动马达的异常
        }
    }

    // 适配 Wear OS 3 的表冠旋转
    public static void bindRotaryEncoder(final ScrollView scrollView) {
        if (scrollView == null) return;
        scrollView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent ev) {
                // SOURCE_ROTARY_ENCODER 是 API 26 引入的
                if (Build.VERSION.SDK_INT >= 26) {
                    if (ev.getAction() == MotionEvent.ACTION_SCROLL && 
                        ev.isFromSource(InputDevice.SOURCE_ROTARY_ENCODER)) {
                        float delta = -ev.getAxisValue(MotionEvent.AXIS_SCROLL) * 120;
                        scrollView.smoothScrollBy(0, (int) delta);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    // 传输时保持屏幕常亮
    public static void keepScreenOn(Activity activity, boolean enable) {
        if (activity == null) return;
        try {
            if (enable) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        } catch (Exception ignored) {}
    }
}

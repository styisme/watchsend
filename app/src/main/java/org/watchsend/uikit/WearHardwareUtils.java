package org.watchsend.uikit;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ScrollView;

public final class WearHardwareUtils {
    public static final int HAPTIC_LIGHT=0, HAPTIC_SUCCESS=1, HAPTIC_ERROR=2;
    private static long sLastRotaryVibrateTime = 0;
    private WearHardwareUtils() {}

    public static void vibrate(Context context, int type) {
        if (context == null) return;
        try {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v == null || !v.hasVibrator()) return;
            if (Build.VERSION.SDK_INT >= 26) {
                VibrationEffect e = null;
                if (type == HAPTIC_LIGHT) e = VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE);
                else if (type == HAPTIC_SUCCESS) e = VibrationEffect.createWaveform(new long[]{0,50,50,50}, -1);
                else if (type == HAPTIC_ERROR) e = VibrationEffect.createWaveform(new long[]{0,100,100,100,100,100}, -1);
                if (e != null) v.vibrate(e);
            } else {
                if (type == HAPTIC_LIGHT) v.vibrate(15);
                else if (type == HAPTIC_SUCCESS) v.vibrate(new long[]{0,50,50,50}, -1);
                else if (type == HAPTIC_ERROR) v.vibrate(new long[]{0,100,100,100,100,100}, -1);
            }
        } catch (Exception ignored) {}
    }
    public static void bindRotaryEncoder(final ScrollView sv) {
        if (sv == null || Build.VERSION.SDK_INT < 12) return;
        sv.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override public boolean onGenericMotion(View v, MotionEvent ev) {
                if (Build.VERSION.SDK_INT >= 26
                    && ev.getAction() == MotionEvent.ACTION_SCROLL
                    && ev.isFromSource(InputDevice.SOURCE_ROTARY_ENCODER)) {
                    float d = -ev.getAxisValue(MotionEvent.AXIS_SCROLL) * 120;
                    sv.smoothScrollBy(0, (int) d);
                    long now = System.currentTimeMillis();
                    if (now - sLastRotaryVibrateTime > 50) {
                        vibrate(sv.getContext(), HAPTIC_LIGHT);
                        sLastRotaryVibrateTime = now;
                    }
                    return true;
                }
                return false;
            }
        });
    }
    public static void keepScreenOn(Activity a, boolean on) {
        if (a == null) return;
        try {
            if (on) a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            else a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception ignored) {}
    }
    public static boolean needChinPadding(Context ctx) {
        return UikitConfig.isChinPaddingEnabled(ctx);
    }
    public static boolean isRoundScreen(View view) {
        if (view == null) return false;
        Context ctx = view.getContext();
        int shapeMode = UikitConfig.getScreenShape(ctx);
        if (shapeMode == UikitConfig.SHAPE_ROUND) return true;
        if (shapeMode == UikitConfig.SHAPE_SQUARE) return false;
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                WindowInsets insets = view.getRootWindowInsets();
                if (insets != null) return insets.isRound();
            } catch (Exception ignored) {}
        }
        try {
            DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
            float r = (float) dm.widthPixels / dm.heightPixels;
            return r >= 0.95f && r <= 1.05f;
        } catch (Exception e) { return false; }
    }
}

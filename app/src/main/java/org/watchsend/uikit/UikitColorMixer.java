package org.watchsend.uikit;

import org.watchsend.R;
import android.graphics.Color;
public final class UikitColorMixer {
    private UikitColorMixer() {}
    public static int withAlpha(int color, float alpha) {
        int a = (int) (Color.alpha(color) * alpha);
        return Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
    }
    public static int blend(int c1, int c2, float ratio) {
        float ir = 1.0f - ratio;
        return Color.argb(
            (int)(Color.alpha(c1)*ir + Color.alpha(c2)*ratio),
            (int)(Color.red(c1)*ir + Color.red(c2)*ratio),
            (int)(Color.green(c1)*ir + Color.green(c2)*ratio),
            (int)(Color.blue(c1)*ir + Color.blue(c2)*ratio)
        );
    }
}

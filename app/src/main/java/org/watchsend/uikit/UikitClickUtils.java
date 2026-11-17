package org.watchsend.uikit;
public final class UikitClickUtils {
    private static long lastClickTime = 0;
    public static final int MIN_DELAY = 600; // 默认 600ms 内不重复触发
    private UikitClickUtils() {}
    public static boolean isFastDoubleClick() {
        return isFastDoubleClick(MIN_DELAY);
    }
    public static boolean isFastDoubleClick(int delay) {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < delay) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}

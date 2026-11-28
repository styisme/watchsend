package org.watchsend.uikit;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import org.watchsend.util.I18nManager;

/**
 * 系统返回键的唯一闸门。
 *
 * <p>开关语义只有一条：{@link UikitConfig#isSwipeBackEnabled(Context)} 为 true 时，
 * 所有页面都不再响应实体/手势返回键，改为由界面内横滑退出（{@link UikitUi#handleSwipeBack}）；
 * 为 false 时返回键交回系统，滑动也不再接管触摸。
 *
 * <p>以前各页面有的拦、有的不拦，还有两页写死了中文提示，所以这里统一收口：
 * 任何带返回的 Activity 只调用 {@link #handleBack(Activity)}，不再自己判键、不再自己弹文案。
 */
public final class BackGate {

    /** 连续按返回键时的提示节流，避免长按把 Toast 刷成一串。 */
    private static final long TOAST_GAP_MS = 800;
    private static long sLastToastAt = 0;

    private BackGate() {}

    /** 当前是否启用软件内返回（等价于旧的"拦截系统返回"）。 */
    public static boolean isEnabled(Context ctx) {
        return UikitConfig.isSwipeBackEnabled(ctx);
    }

    /**
     * 在 Activity.onBackPressed() 里调用。
     *
     * @return true 表示返回键已被吞掉（调用方必须直接 return，不能再 super.onBackPressed()），
     *         false 表示交回系统默认行为。
     */
    public static boolean handleBack(Activity activity) {
        if (activity == null || activity.isFinishing()) return false;
        if (!isEnabled(activity)) return false;
        long now = System.currentTimeMillis();
        if (now - sLastToastAt > TOAST_GAP_MS) {
            sLastToastAt = now;
            Toast.makeText(activity, I18nManager.get("swipe_back_toast"), Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}

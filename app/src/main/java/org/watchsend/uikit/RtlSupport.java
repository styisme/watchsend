package org.watchsend.uikit;

import android.app.Activity;
import android.os.Build;
import android.view.View;

import org.watchsend.util.I18nManager;

import java.util.Locale;

/**
 * 从右到左（RTL）语言的布局适配。
 *
 * <p>语言包里有 4 种 RTL 语言：العربية（ar）、فارسی（fa）、עברית（he）、اردو（ur）。
 * App 内切换语言不会改系统 Locale，系统的 layoutDirection 仍是 LTR，所以这里按“当前选中的语言”
 * 主动把根视图设成 RTL，让 LinearLayout 的子项顺序、START/END 对齐、marginStart 跟着镜像。
 *
 * <p>三点说明：
 * <ul>
 *   <li>只在 Android 4.2（API 17）及以上生效，更低版本保持 LTR，避免布局错位；</li>
 *   <li>方向设在 decorView 上，子 View 继承，不需要逐层设置；</li>
 *   <li>切换语言时各页面本来就会 recreate，重新 apply 一次即可。</li>
 * </ul>
 */
public final class RtlSupport {

    private static final String[] RTL_LANGS = {"ar", "fa", "he", "ur"};

    private RtlSupport() {}

    /** 当前生效的语言代码；“跟随系统”时取系统语言。 */
    public static String resolveCode() {
        String code = I18nManager.current();
        if (code == null || code.length() == 0 || "system".equals(code)) {
            code = Locale.getDefault().getLanguage();
        }
        if ("iw".equals(code)) code = "he"; // 历史代码
        return code;
    }

    public static boolean isRtl() {
        String code = resolveCode();
        for (String c : RTL_LANGS) {
            if (c.equals(code)) return true;
        }
        return false;
    }

    /** 横向起始边对齐：LTR 是左，RTL 是右。旧系统没有 RELATIVE 位，会退化成左对齐。 */
    public static int startGravity() {
        return android.view.Gravity.START;
    }

    /** 结束边对齐（列表右侧的箭头、尾部图标）。 */
    public static int endGravity() {
        return android.view.Gravity.END;
    }

    /** 在 Activity 里调用一次：按当前语言把根视图方向定下来。 */
    public static void apply(Activity activity) {
        if (activity == null || Build.VERSION.SDK_INT < 17) return;
        View decor = activity.getWindow().getDecorView();
        if (decor == null) return;
        decor.setLayoutDirection(isRtl() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
    }
}

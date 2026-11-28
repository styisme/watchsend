package org.watchsend.uikit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;

import org.watchsend.R;

/**
 * 应用图标的动态取色版本。
 *
 * 原图标（ic_launcher.xml）是一整块：黑圆底 + 绿色外圈 + 绿色上箭头 + 白色下箭头，
 * 单一 vector 没法只染绿色部分。这里拆成三层：黑圆底、绿色层（参与染色）、白色箭头，
 * 运行时把绿色层用 SRC_IN 换上动态主色，白箭头与黑底保持原样。
 *
 * 取不到动态色（系统低于 Android 12、ROM 没有 system_accent1、或用户在设置里关掉）
 * 时不做染色，绿色层保留图标原本的 #81C784。
 *
 * 说明：启动器上的图标由系统绘制，第三方应用无法运行时改色，所以 launcher / 磁贴
 * 仍用静态的 ic_launcher.xml；动态版本用在应用内与桌面小组件这类自己渲染的地方。
 */
public final class AppIcon {

    /** 图标原本的绿，也是动态色不可用时的兜底。 */
    public static final int ICON_GREEN = 0xFF81C784;

    private AppIcon() {}

    /** 当前应使用的图标强调色：动态取色可用时取系统浅色调（黑底上更亮），否则保持绿。 */
    public static int tint(Context ctx) {
        // 图标底固定是黑的，所以固定取"深色主题档" accent1-200，不随界面明暗切换
        return UikitColor.getToneColor(ctx, true,
                "system_accent1_200", "system_accent1_200", ICON_GREEN);
    }

    /** 组装好的图标：黑圆 + 染色后的绿色层 + 白箭头。 */
    public static Drawable build(Context ctx) {
        Drawable disc = drawable(ctx, R.drawable.ic_launcher_disc);
        Drawable accent = drawable(ctx, R.drawable.ic_launcher_accent);
        Drawable arrow = drawable(ctx, R.drawable.ic_launcher_arrow);

        int color = tint(ctx);
        if (color != ICON_GREEN) {
            // SRC_IN：把这一层里所有不透明像素换成目标色，描边与填充一起换
            accent.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

        LayerDrawable layers = new LayerDrawable(new Drawable[]{disc, accent, arrow});
        // 三层都是同一个 108dp 画布，直接给满边界
        return layers;
    }

    /** 渲染成位图，供 RemoteViews（桌面小组件）这类只能塞图片的场景使用。 */
    public static Bitmap toBitmap(Context ctx, int sizePx) {
        if (sizePx <= 0) sizePx = 1;
        Drawable d = build(ctx);
        Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        d.setBounds(0, 0, sizePx, sizePx);
        d.draw(canvas);
        return bmp;
    }

    private static Drawable drawable(Context ctx, int resId) {
        if (Build.VERSION.SDK_INT >= 21) return ctx.getDrawable(resId);
        return ctx.getResources().getDrawable(resId);
    }
}

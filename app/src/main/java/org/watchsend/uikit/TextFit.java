package org.watchsend.uikit;

import android.os.Build;
import android.text.Layout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * 多语言文本适配工具。
 *
 * <p>同一句话在简中、日、韩里只占一半宽度，翻成德文、俄文会多出 60%～120%。
 * 手表屏幕只有 320px 左右，固定字号 + 单行 + 无省略，结果要么文字被裁到看不懂，
 * 要么一个德语复合词把整行撑出屏幕。这里把两类处理收口：
 *
 * <ul>
 *   <li>{@link #wrap(TextView, int)}：可以换行的标题/条目，限定行数、尾部省略，
 *       并打开高质量断行与断词，让 "Benutzerhandbuches" 这种长词在必要时从词中断开；</li>
 *   <li>{@link #fitSingleLine(TextView, int, int)}：按钮这类不能换行的控件，
 *       宽度不够时自动降字号（Android 8.0+ 交给系统 autosize，更低版本按视觉宽度估算），
 *       降到下限仍放不下就尾部省略。</li>
 * </ul>
 */
public final class TextFit {

    private TextFit() {}

    /** 标题、列表条目、说明文字：最多 maxLines 行，超出用省略号，拉丁长词允许断字。 */
    public static void wrap(TextView tv, int maxLines) {
        if (tv == null) return;
        tv.setMaxLines(maxLines);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        if (Build.VERSION.SDK_INT >= 23) {
            tv.setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY);
            tv.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NORMAL);
        }
    }

    /**
     * 按钮等不可换行的控件：保持单行，放不下先缩字号，缩到 minSp 仍放不下再省略。
     * 调用前请先 setText()，否则测不到文本长度（低版本分支会按空文本给字号）。
     */
    public static void fitSingleLine(TextView tv, int maxSp, int minSp) {
        if (tv == null) return;
        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        if (maxSp <= minSp) return;
        if (Build.VERSION.SDK_INT >= 26) {
            tv.setAutoSizeTextTypeUniformWithConfiguration(minSp, maxSp, 1, TypedValue.COMPLEX_UNIT_SP);
        } else {
            tv.setTextSize(estimateSp(tv.getText(), maxSp, minSp));
        }
    }

    /**
     * 低版本 fallback：按"视觉宽度"估算字号。
     * 中日韩字符与全角符号算 1 em，拉丁字母、数字与半角符号算 0.55 em。
     */
    public static float estimateSp(CharSequence text, int maxSp, int minSp) {
        float em = visualEm(text);
        float sp = maxSp;
        if (em > 18) sp = maxSp - 3;
        else if (em > 14) sp = maxSp - 2;
        else if (em > 10) sp = maxSp - 1;
        return Math.max(minSp, sp);
    }

    /** 估算一段文本的视觉宽度（单位 em），用于旧版本没有 autosize 时的降字号判断。 */
    public static float visualEm(CharSequence text) {
        if (text == null) return 0f;
        float em = 0f;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // CJK 统一表意文字、假名、谚文、全角标点大致都按 1 em 计
            em += (c >= 0x1100 && c <= 0x115F) || (c >= 0x2E80 && c <= 0xA4CF)
                    || (c >= 0xAC00 && c <= 0xD7A3) || (c >= 0xF900 && c <= 0xFAFF)
                    || (c >= 0xFF00 && c <= 0xFF60) ? 1.0f : 0.55f;
        }
        return em;
    }
}

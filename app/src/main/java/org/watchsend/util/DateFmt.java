package org.watchsend.util;

import java.util.Calendar;
import java.util.Date;

/**
 * 时间本地化格式（移植官方 util/system_date_time_formatter.dart 的展示语义）。
 *
 * 官方优先读系统给的日期时间 pattern；Android 上没有等价 API 的低版本可用，
 * 所以这里用"相对日期 + 时分"的稳妥写法：今天/昨天只显示 HH:mm，同年显示 月日 HH:mm，
 * 跨年才带年份。比固定 "HH:mm" 更有信息量，也不会因 pattern 差异冒出怪格式。
 */
public class DateFmt {

    public static String shortDateTime(long ts) {
        if (ts <= 0) return "";
        Calendar now = Calendar.getInstance();
        Calendar then = Calendar.getInstance();
        then.setTime(new Date(ts));

        boolean sameDay = now.get(Calendar.YEAR) == then.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR);

        String hm = two(then.get(Calendar.HOUR_OF_DAY)) + ":" + two(then.get(Calendar.MINUTE));
        if (sameDay) return hm;

        // 昨天
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        if (yesterday.get(Calendar.YEAR) == then.get(Calendar.YEAR)
                && yesterday.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)) {
            return I18nManager.get("day_yesterday") + " " + hm;
        }

        String md = (then.get(Calendar.MONTH) + 1) + I18nManager.get("month_suffix") + then.get(Calendar.DAY_OF_MONTH) + I18nManager.get("day_suffix");
        if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR)) return md + " " + hm;
        return then.get(Calendar.YEAR) + I18nManager.get("year_suffix") + md + " " + hm;
    }

    private static String two(int v) { return v < 10 ? "0" + v : String.valueOf(v); }
}

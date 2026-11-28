package org.watchsend.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import org.watchsend.uikit.UikitConfig;

/**
 * 传输层的省电策略：给上传/接收的字节流设速率上限。
 *
 * 为什么不在 UI 层做：手表传大文件时，真正吃电的是 Wi-Fi 射频长时间满速发射，
 * 限速的作用是把峰值功率压下来、让 SoC 有机会进低功耗态，而不是让界面少刷几帧。
 */
public final class PowerGuard {

    /** 低性能模式下的传输上限：2 MB/s。常见手表的 Wi-Fi 实际吞吐也就 2~3 MB/s，损失有限。 */
    public static final int LOW_PERF_BPS = 2 * 1024 * 1024;
    /** 电量低于该百分比时进一步限速，避免"传完文件表也关机了"。 */
    public static final int LOW_BATTERY_PCT = 15;
    /** 低电量下的上限：512 KB/s。 */
    public static final int LOW_BATTERY_BPS = 512 * 1024;

    private PowerGuard() {}

    /**
     * 当前允许的最大传输速率（字节/秒），0 表示不限速。
     * 规则：正常设备 + 电量充足 → 不限；低性能模式 → 2MB/s；电量低于 15% 且未充电 → 512KB/s。
     * 同时命中时取更小的那个。
     */
    public static int maxBytesPerSecond(Context ctx) {
        if (ctx == null) return 0;
        int bps = 0;
        if (UikitConfig.isLowPerf(ctx)) bps = LOW_PERF_BPS;

        int pct = batteryPercent(ctx);
        if (pct >= 0 && pct < LOW_BATTERY_PCT && !isCharging(ctx)) {
            bps = (bps == 0 || LOW_BATTERY_BPS < bps) ? LOW_BATTERY_BPS : bps;
        }
        return bps;
    }

    /** 当前电量百分比，取不到时返回 -1（不传 receiver 读 sticky 广播，无需权限）。 */
    public static int batteryPercent(Context ctx) {
        try {
            Intent i = ctx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (i == null) return -1;
            int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level < 0 || scale <= 0) return -1;
            return (int) (level * 100L / scale);
        } catch (Throwable t) {
            return -1;
        }
    }

    /** 是否在充电（充电时不按低电量限速）。 */
    public static boolean isCharging(Context ctx) {
        try {
            Intent i = ctx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (i == null) return false;
            int status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        } catch (Throwable t) {
            return false;
        }
    }
}

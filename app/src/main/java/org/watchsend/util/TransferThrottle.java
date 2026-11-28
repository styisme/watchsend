package org.watchsend.util;

import android.content.Context;

/**
 * 字节流限速器：按"已发字节数 × 期望耗时"与"实际耗时"的差值来 sleep，
 * 让平均速率贴近上限，且不会因为单次 sleep 过长而把整个传输卡成顿挫。
 *
 * 用法：每写完/读完一块数据调一次 {@link #onBytes(int)}。上限为 0 时完全是空操作，
 * 因此正常设备上没有任何额外开销（只有一个 branch）。
 */
public final class TransferThrottle {

    private final int bps;
    private final long startNanos;
    private long bytes;
    private long sleptMs;

    private TransferThrottle(int bps) {
        this.bps = bps;
        this.startNanos = System.nanoTime();
    }

    /** 按当前设置（低性能 / 低电量）创建限速器。 */
    public static TransferThrottle create(Context ctx) {
        return new TransferThrottle(PowerGuard.maxBytesPerSecond(ctx));
    }

    public boolean isLimited() { return bps > 0; }

    /** 已传输多少字节；用于判断是否真的因为限速而变慢（日志/诊断用）。 */
    public long bytes() { return bytes; }

    public long sleptMs() { return sleptMs; }

    /** 记录本次写了 n 字节，必要时 sleep 到时间片。 */
    public void onBytes(int n) {
        if (bps <= 0 || n <= 0) return;
        bytes += n;
        // 期望耗时（纳秒）；bytes 最大到 GB 级，1e18 量级仍在 long 安全范围内
        long expectNanos = bytes * 1000000000L / bps;
        long usedNanos = System.nanoTime() - startNanos;
        long gapMs = (expectNanos - usedNanos) / 1000000L;
        if (gapMs <= 0) return;
        // 单次最多睡 50ms，避免一次大块数据导致界面/取消响应长时间冻结
        if (gapMs > 50) gapMs = 50;
        try {
            Thread.sleep(gapMs);
            sleptMs += gapMs;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

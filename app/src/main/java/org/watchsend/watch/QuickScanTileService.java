package org.watchsend.watch;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import org.watchsend.MainActivity;
import org.watchsend.OldSendService;

@TargetApi(Build.VERSION_CODES.N)
public class QuickScanTileService extends TileService {
    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        // 磁贴改成纯开关：下拉点一下就知道服务是开是停，不必进 App 再翻设置
        boolean running = OldSendService.isServiceRunning;
        if (running) {
            OldSendService.stopServer(this);
        } else {
            Intent serviceIntent = new Intent(this, OldSendService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
        WatchHardwareUtils.vibrate(this, WatchHardwareUtils.HAPTIC_LIGHT);
        // 服务起停是异步的，稍等一下再刷新状态，避免磁贴停在旧值
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() { updateTile(); }
        }, 700);
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) return;
        boolean running = OldSendService.isServiceRunning
                && !OldSendService.isListeningStopped();
        try {
            tile.setState(running ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.setLabel(running ? "WatchSend · ON" : "WatchSend · OFF");
        } catch (Throwable ignored) {}
        try { tile.updateTile(); } catch (Throwable ignored) {}
    }

    /** App 内启停服务后让磁贴也刷新（系统只在下拉时才回调 onStartListening）。 */
    public static void requestRefresh(android.content.Context ctx) {
        if (ctx == null || Build.VERSION.SDK_INT < 24) return;
        try {
            android.service.quicksettings.TileService.requestListeningState(ctx,
                    new android.content.ComponentName(ctx, QuickScanTileService.class));
        } catch (Throwable ignored) {}
    }
}

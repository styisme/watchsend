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
        // 如果服务未启动，先拉起服务
        Intent serviceIntent = new Intent(this, OldSendService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        WatchHardwareUtils.vibrate(this, WatchHardwareUtils.HAPTIC_LIGHT);

        // 拉起 App
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("auto_scan", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityAndCollapse(intent);
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel("WatchSend");
            tile.updateTile();
        }
    }
}

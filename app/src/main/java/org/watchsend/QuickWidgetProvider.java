package org.watchsend;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

public class QuickWidgetProvider extends AppWidgetProvider {
    
    public static final String ACTION_TOGGLE_SERVICE = "org.watchsend.ACTION_TOGGLE_SERVICE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main);
        
        // 1. 点击图标区域拉起 App
        Intent launchIntent = new Intent(context, MainActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent piLaunch = PendingIntent.getActivity(context, 0, launchIntent, flags);
        views.setOnClickPendingIntent(R.id.widget_launch, piLaunch);
        
        // 2. 点击底部按钮切换服务状态
        Intent toggleIntent = new Intent(context, QuickWidgetProvider.class);
        toggleIntent.setAction(ACTION_TOGGLE_SERVICE);
        PendingIntent piToggle = PendingIntent.getBroadcast(context, 1, toggleIntent, flags);
        views.setOnClickPendingIntent(R.id.widget_toggle_btn, piToggle);

        // 3. 检查服务是否在运行 (通过 OldSendService 的静态变量)
        boolean isRunning = OldSendService.isServiceRunning;
        
        if (isRunning) {
            views.setTextViewText(R.id.widget_status, "● 运行中");
            views.setTextColor(R.id.widget_status, 0xFF388E3C);
            views.setTextViewText(R.id.widget_toggle_btn, "停止服务");
            views.setInt(R.id.widget_toggle_btn, "setBackgroundResource", R.drawable.watch_btn_send);
        } else {
            views.setTextViewText(R.id.widget_status, "○ 已停止");
            views.setTextColor(R.id.widget_status, 0xFF666666);
            views.setTextViewText(R.id.widget_toggle_btn, "启动服务");
            views.setInt(R.id.widget_toggle_btn, "setBackgroundResource", R.drawable.watch_btn_primary);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_TOGGLE_SERVICE.equals(intent.getAction())) {
            boolean isRunning = OldSendService.isServiceRunning;
            Intent serviceIntent = new Intent(context, OldSendService.class);
            if (isRunning) {
                context.stopService(serviceIntent);
            } else {
                if (Build.VERSION.SDK_INT >= 26) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            }
            // 发送全局广播刷新 UI
            sendUpdateBroadcast(context);
        }
    }

    public static void sendUpdateBroadcast(Context context) {
        Intent intent = new Intent(context, QuickWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, QuickWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}

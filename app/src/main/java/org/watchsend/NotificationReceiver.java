package org.watchsend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.concurrent.CountDownLatch;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_ACCEPT = "org.watchsend.ACCEPT";
    public static final String ACTION_REJECT = "org.watchsend.REJECT";
    public static final String ACTION_CANCEL_TRANSFER = "org.watchsend.CANCEL_TRANSFER";

    public static CountDownLatch waitingLatch = null;
    public static boolean[] waitingResult = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // 传输通知上的"取消"：直接置位发送器，不依赖接收确认的等待闸门
        if (ACTION_CANCEL_TRANSFER.equals(action)) {
            org.watchsend.transfer.Uploader.cancelCurrent();
            return;
        }

        if (waitingLatch == null || waitingResult == null) return;

        if (ACTION_ACCEPT.equals(action)) {
            waitingResult[0] = true;
            waitingLatch.countDown();
        } else if (ACTION_REJECT.equals(action)) {
            waitingResult[0] = false;
            waitingLatch.countDown();
        }
    }
}

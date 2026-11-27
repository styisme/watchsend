package org.watchsend;

import android.os.Handler;
import android.os.Looper;
import org.watchsend.util.FileLogger;

public class ReceiveManager {
    public interface Callback {
        void onResult(boolean accepted);
    }

    public static Callback pendingCallback;
    private static final Object lock = new Object();
    private static boolean waiting = false;
    private static boolean result = false;

    public static boolean askUser(final String alias, final int count) {
        return askUser(alias, count, null);
    }

    /**
     * @param messagePreview 文本消息的原文（协议 fileType=text 的 preview），
     *                       非空时接收界面直接展示这句话，而不是"想给你发 N 个文件"
     */
    public static boolean askUser(final String alias, final int count, final String messagePreview) {
        FileLogger.log("ReceiveManager", "Ask user to receive from: " + alias);
        
        // 防御性清理上一次的残留
        pendingCallback = null;

        pendingCallback = new Callback() {
            public void onResult(boolean accepted) {
                synchronized (lock) {
                    result = accepted;
                    waiting = false;
                    lock.notifyAll();
                }
            }
        };

        waiting = true;
        result = false;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                try {
                    android.content.Intent intent = new android.content.Intent(OldSendService.getContext(), TransferActivity.class);
                    intent.putExtra("mode", 1);
                    intent.putExtra("senderName", alias);
                    intent.putExtra("fileCount", count);
                    if (messagePreview != null) intent.putExtra("messagePreview", messagePreview);
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    OldSendService.getContext().startActivity(intent);
                } catch (Exception e) {
                    FileLogger.log("ReceiveManager", "Failed to start UI: " + e.getMessage());
                    synchronized (lock) { result = false; waiting = false; lock.notifyAll(); }
                }
            }
        });

        synchronized (lock) {
            try {
                long start = System.currentTimeMillis();
                while (waiting) {
                    lock.wait(30000);
                    if (System.currentTimeMillis() - start > 30000) break;
                }
            } catch (Exception ignored) {}
        }
        return result;
    }
}
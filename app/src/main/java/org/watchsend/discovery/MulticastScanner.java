package org.watchsend.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.content.Context;
import android.net.wifi.WifiManager;

import org.json.JSONObject;
import org.watchsend.OldSendService;
import org.watchsend.Protocol;
import org.watchsend.api.InfoHandler;
import org.watchsend.model.Device;

public class MulticastScanner {

    public interface Callback {
        void onDevice(Device d);
        void onFinished();
    }

    public static void scanAsync(final Callback cb) {
        new Thread(new Runnable() {
            public void run() { doScan(cb); }
        }, "multicast-scan").start();
    }

    static void doScan(final Callback cb) {
        DatagramSocket sock = null;
        WifiManager.MulticastLock lock = null;
        int found = 0;
        try {
            Context ctx = OldSendService.getContext();
            if (ctx != null) {
                WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
                if (wm != null) {
                    lock = wm.createMulticastLock("oldsend_scan");
                    lock.acquire();
                }
            }
            sock = new DatagramSocket();
            sock.setSoTimeout(2000);
            sock.setBroadcast(true);

            // 自述信息必须与真实监听协议/端口一致，否则加密设备会被对端当成明文设备
            byte[] data = InfoHandler.selfInfo(android.os.Build.MODEL).toString().getBytes("UTF-8");
            InetAddress group = InetAddress.getByName(Protocol.MULTICAST_GROUP);
            DatagramPacket out = new DatagramPacket(data, data.length, group, Protocol.DEFAULT_PORT);
            sock.send(out);

            long deadline = System.currentTimeMillis() + 2000;
            byte[] buf = new byte[65535];
            while (System.currentTimeMillis() < deadline) {
                try {
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    sock.receive(p);
                    String body = new String(p.getData(), 0, p.getLength(), "UTF-8").trim();
                    if (body.length() == 0 || body.startsWith("[")) continue;
                    JSONObject o = new JSONObject(body);
                    Device d = Device.fromJson(o, p.getAddress().getHostAddress());
                    if (cb != null) cb.onDevice(d);
                    found++;
                } catch (java.net.SocketTimeoutException ste) {
                    break;
                } catch (Exception e) {
                    OldSendService.log("scan recv: " + e);
                }
            }
        } catch (Exception e) {
            OldSendService.log("scan error: " + e);
        } finally {
            try { if (sock != null) sock.close(); } catch (Exception ignored) {}
            if (lock != null && lock.isHeld()) lock.release();
        }

        // 多播一无所获时，退化为网段探测（很多企业/宿舍路由器会拦组播）
        if (found == 0) {
            OldSendService.log("多播无结果，启动网段探测");
            SubnetScanner.scanAsync(new SubnetScanner.Callback() {
                public void onDevice(Device d) { if (cb != null) cb.onDevice(d); }
                public void onFinished() { if (cb != null) cb.onFinished(); }
            });
        } else if (cb != null) {
            cb.onFinished();
        }
    }
}

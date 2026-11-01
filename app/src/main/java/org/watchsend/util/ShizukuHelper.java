package org.watchsend.util;

import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import java.lang.reflect.Method;

public class ShizukuHelper {
    private static final String SHIZUKU_PKG = "moe.shizuku.privileged.api";

    // 检测 Shizuku 是否在运行
    public static boolean isRunning() {
        try {
            Class<?> smClass = Class.forName("android.os.ServiceManager");
            Method getService = smClass.getDeclaredMethod("getService", String.class);
            getService.setAccessible(true);
            IBinder binder = (IBinder) getService.invoke(null, "shizuku");
            return binder != null && binder.pingBinder();
        } catch (Throwable t) {
            return false;
        }
    }

    // 纯手写 AIDL 调用 Shizuku 执行 Shell 命令 (极度硬核)
    public static boolean execShell(String cmd) {
        try {
            Class<?> smClass = Class.forName("android.os.ServiceManager");
            Method getService = smClass.getDeclaredMethod("getService", String.class);
            getService.setAccessible(true);
            IBinder binder = (IBinder) getService.invoke(null, "shizuku");
            if (binder == null) return false;

            // 构造数据
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                // Shizuku IShizukuService 的接口描述符
                data.writeInterfaceToken("moe.shizuku.server.IShizukuService");
                
                // 获取系统进程的 uid/gid (通常 Shizuku 帮忙执行就是拿这个)
                int uid = android.os.Process.myUid();
                int pid = android.os.Process.myPid();
                data.writeInt(uid);
                data.writeInt(pid);
                
                // 写入命令参数
                data.writeStringArray(new String[]{"sh", "-c", cmd});
                data.writeStringArray(null); // env
                data.writeString("/data/local/tmp"); // dir
                
                // 执行 newProcess (在部分 Shizuku 版本中 code 是 1，不同版本可能有变，加 try-catch 保护)
                // 注意：这里传 null 替代 IShizukuServiceCommand 回调，仅作简单示范
                data.writeStrongBinder(null); 
                
                // 发送 transact 到 Shizuku 服务端
                binder.transact(1, data, reply, 0);
                
                // 检查返回值是否正常
                reply.readException();
                return true; // 只要没抛异常，我们就认为命令已经提交给 Shizuku 了
            } finally {
                data.recycle();
                reply.recycle();
            }
        } catch (Throwable t) {
            // 如果 Binder 通信失败（版本不匹配或未授权），直接返回 false 降级
            return false;
        }
    }
}

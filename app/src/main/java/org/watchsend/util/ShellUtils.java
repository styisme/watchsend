package org.watchsend.util;

import android.content.Context;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class ShellUtils {
    
    // 检测 Root 状态
    public static boolean isRootAvailable() {
        try {
            Process p = Runtime.getRuntime().exec("su -c id");
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = reader.readLine();
            reader.close();
            p.waitFor();
            return line != null && line.contains("uid=0");
        } catch (Exception e) {
            return false;
        }
    }

    // 检测 Shizuku 状态 (纯 Java 检查进程或文件)
    public static boolean isShizukuAvailable() {
        try {
            // 检查 Shizuku 的典型服务进程或包
            Process p = Runtime.getRuntime().exec("ps -A");
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("shizuku_server")) {
                    reader.close();
                    return true;
                }
            }
            reader.close();
        } catch (Exception ignored) {}
        return false;
    }

    // 通过 Root 执行命令
    public static boolean execRootCommand(String command) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();
            os.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

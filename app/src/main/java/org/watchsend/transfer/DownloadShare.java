package org.watchsend.transfer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.watchsend.OldSendService;

/**
 * 协议下载 API（LocalSend v2 的 download 能力）的共享目录。
 *
 * 官方实现里，`download:true` 表示"本设备当前有文件可供对端拉取"，
 * 对端通过 POST /v2/prepare-download 取列表、GET /v2/download 取内容
 * （packages/core/src/http/server/mod.rs:647）。
 *
 * 手表端不另做一套"共享清单"界面，直接把已接收文件目录（OldSend 四个媒体目录）
 * 作为可下载源，默认关闭，用户在设置里显式开启后才对局域网暴露。
 */
public class DownloadShare {

    private static final String PREF = "oldsend";
    private static final String KEY = "download_share";

    private static SharedPreferences prefs() {
        Context ctx = OldSendService.getContext();
        if (ctx == null) return null;
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static boolean isEnabled() {
        SharedPreferences sp = prefs();
        return sp != null && sp.getBoolean(KEY, false);
    }

    public static void setEnabled(boolean enabled) {
        SharedPreferences sp = prefs();
        if (sp != null) sp.edit().putBoolean(KEY, enabled).commit();
    }

    /** 用于协议上报的目录集合，与 FileSaver 的落盘目录保持一致。 */
    public static List<File> dirs() {
        // 与实际落盘目录保持一致（Android 10+ 落应用私有目录，见 FileSaver.getTargetDir）
        return FileSaver.receivedDirs();
    }

    public static List<File> listFiles() {
        List<File> out = new ArrayList<File>();
        for (File dir : dirs()) {
            if (dir == null || !dir.isDirectory()) continue;
            File[] files = dir.listFiles();
            if (files == null) continue;
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (f != null && f.isFile() && f.length() > 0) out.add(f);
            }
        }
        return out;
    }

    /** 按文件名在共享目录里查找（下载 API 的 fileId 由文件名派生，见 idFor）。 */
    public static File find(String fileName) {
        if (fileName == null || fileName.length() == 0) return null;
        for (File dir : dirs()) {
            if (dir == null) continue;
            File f = new File(dir, fileName);
            if (f.isFile()) return f;
        }
        return null;
    }

    /** 稳定的 fileId：文件名 + 长度的 SHA-256 前 16 位，避免路径泄露。 */
    public static String idFor(File f) {
        String digest = org.watchsend.util.CryptoUtils.sha256Hex(
                (f.getName() + "#" + f.length()).getBytes());
        if (digest == null) return f.getName();
        return digest.substring(0, 16);
    }
}

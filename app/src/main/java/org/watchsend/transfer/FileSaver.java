package org.watchsend.transfer;

import android.os.Environment;
import java.io.File;

public class FileSaver {
    public static File createTarget(String fileName, boolean saveAlbum) {
        File dir = getTargetDir(fileName, saveAlbum);
        if (!dir.exists()) dir.mkdirs();
        String safe = sanitize(fileName);
        File f = new File(dir, safe);
        int i = 1;
        while (f.exists()) {
            int dot = safe.lastIndexOf('.');
            String base = dot > 0 ? safe.substring(0, dot) : safe;
            String ext = dot > 0 ? safe.substring(dot) : "";
            f = new File(dir, base + " (" + i + ")" + ext);
            i++;
        }
        // 最后一道保险：解析出的真实路径必须仍在目标目录内（防符号链接/异常名穿越）
        try {
            String dirPath = dir.getCanonicalPath();
            String filePath = f.getCanonicalPath();
            if (!filePath.startsWith(dirPath + File.separator)) {
                f = new File(dir, "untitled_" + System.currentTimeMillis());
            }
        } catch (Exception ignored) {
        }
        return f;
    }

    private static File getTargetDir(String name, boolean saveAlbum) {
        String lower = name.toLowerCase();
        // 图片依然进相册
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif")) {
            return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OldSend");
        } 
        // 视频进 Movies
        else if (lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".avi")) {
            return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "OldSend");
        } 
        // 音频与歌词进 Music (直接在根目录或 OldSend 目录均可，这里进 OldSend 以便管理)
        else if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".flac") || 
                 lower.endsWith(".m4a") || lower.endsWith(".aac") || lower.endsWith(".lrc")) {
            return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "OldSend");
        }
        // 其他所有文件（包括 apk、txt、zip）默认进系统的 Download/OldSend 目录
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "OldSend");
    }

    // 与官方 util/filename.rs 对齐的规则（Android 外置存储按 FAT 处理）
    private static final String ILLEGAL = "<>:\"/\\|?*";
    private static final String[] RESERVED = {
            "con", "prn", "aux", "nul",
            "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
            "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9"
    };
    private static final int MAX_NAME_BYTES = 255;

    /**
     * 对端给的文件名属于不可信输入：
     * 1) 只取最后一段（防 ../ 目录穿越）；
     * 2) 去掉控制字符与 FAT/Windows 非法字符；
     * 3) 去掉结尾的点与空格（".." 会被清成空，再走占位名）；
     * 4) Windows 保留设备名加前缀（con.txt 之类）；
     * 5) 限制 255 字节（UTF-8），保留扩展名。
     */
    static String sanitize(String name) {
        if (name == null) return "untitled";
        int sep = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (sep >= 0) name = name.substring(sep + 1);

        String normalized = name;
        try {
            normalized = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFC);
        } catch (Throwable ignored) {
        }

        StringBuilder sb = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (c < 0x20 || c == 0x7F || ILLEGAL.indexOf(c) >= 0) sb.append('_');
            else sb.append(c);
        }

        while (sb.length() > 0 && (sb.charAt(sb.length() - 1) == '.' || sb.charAt(sb.length() - 1) == ' ')) {
            sb.deleteCharAt(sb.length() - 1);
        }
        String result = sb.toString();
        if (result.length() == 0) return "untitled";

        String lower = result.toLowerCase();
        int dot = lower.indexOf('.');
        String base = dot >= 0 ? lower.substring(0, dot) : lower;
        for (int i = 0; i < RESERVED.length; i++) {
            if (base.equals(RESERVED[i])) {
                result = "_" + result;
                break;
            }
        }
        return truncateToBytes(result, MAX_NAME_BYTES);
    }

    /** 按 UTF-8 字节数截断，尽量保留扩展名。 */
    private static String truncateToBytes(String name, int maxBytes) {
        try {
            if (name.getBytes("UTF-8").length <= maxBytes) return name;
            int dot = name.lastIndexOf('.');
            String base = dot > 0 ? name.substring(0, dot) : name;
            String ext = dot > 0 ? name.substring(dot) : "";
            int extBytes = ext.getBytes("UTF-8").length;
            int budget = maxBytes - extBytes;
            if (budget < 8) { base = name; ext = ""; budget = maxBytes; }
            StringBuilder out = new StringBuilder();
            int used = 0;
            for (int i = 0; i < base.length(); i++) {
                char c = base.charAt(i);
                int add = String.valueOf(c).getBytes("UTF-8").length;
                if (used + add > budget) break;
                out.append(c);
                used += add;
            }
            if (out.length() == 0) return "untitled";
            return out.toString() + ext;
        } catch (Throwable e) {
            return "untitled";
        }
    }
}

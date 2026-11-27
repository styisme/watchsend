package org.watchsend.util;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 极简 FileProvider（零依赖，替代 androidx 的 FileProvider）。
 *
 * 为什么必须有它：Android 7.0（API 24）起禁止把 `file://` 的 Uri 传给别的应用，
 * 否则抛 FileUriExposedException。我们此前"一键安装 APK / 打开文件 / 分享文件"全都用的是
 * Uri.fromFile —— 也就是说这三个按钮在 Android 7+ 的手表上**必然失败**，
 * 只会落到 catch 里显示 "Cannot open"。这里把文件改成 content:// 暴露给系统安装器/查看器。
 *
 * 只暴露三类根目录（外部存储根、应用外部私有目录、应用内部目录），
 * 并且在 openFile 时做 canonical path 前缀校验，避免通过构造 Uri 读取任意文件。
 */
public class FileProviderEx extends ContentProvider {

    public static final String AUTHORITY = "org.watchsend.files";

    /** 生成 content:// Uri；path 段用 URL 安全的 Base64 编码，避免路径里的斜杠被 Uri 解析拆段。 */
    public static Uri uriFor(Context ctx, File f) {
        String encoded = android.util.Base64.encodeToString(f.getAbsolutePath().getBytes(),
                android.util.Base64.URL_SAFE | android.util.Base64.NO_PADDING | android.util.Base64.NO_WRAP);
        return new Uri.Builder().scheme("content").authority(AUTHORITY).appendPath("f").appendPath(encoded).build();
    }

    private static String decode(Uri uri) {
        String last = uri.getLastPathSegment();
        if (last == null) return null;
        try {
            byte[] raw = android.util.Base64.decode(last,
                    android.util.Base64.URL_SAFE | android.util.Base64.NO_PADDING | android.util.Base64.NO_WRAP);
            return new String(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean allowed(Context ctx, File f) {
        try {
            String path = f.getCanonicalPath();
            // 只放行我们自己的目录，避免变成"任意文件读取器"（provider 是对外可见的）
            android.os.Environment env = null;
            String ext = android.os.Environment.getExternalStorageDirectory().getCanonicalPath();
            String[] subDirs = {"Download", "Pictures", "Movies", "Music"};
            for (String d : subDirs) {
                String dir = android.os.Environment.getExternalStoragePublicDirectory(d).getCanonicalPath();
                if (path.startsWith(dir + File.separator + "OldSend" + File.separator)
                        || path.startsWith(dir + File.separator + "WatchSend" + File.separator)) return true;
            }
            if (!path.startsWith(ext + File.separator)) return false;
            File extFiles = ctx.getExternalFilesDir(null);
            if (extFiles != null && path.startsWith(extFiles.getCanonicalPath() + File.separator)) return true;
            String files = ctx.getFilesDir().getCanonicalPath();
            if (path.startsWith(files + File.separator)) return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public boolean onCreate() { return true; }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        String path = decode(uri);
        if (path == null) throw new FileNotFoundException("bad uri");
        File f = new File(path);
        if (!allowed(getContext(), f)) throw new FileNotFoundException("not allowed: " + path);
        int flags;
        if ("r".equals(mode)) flags = ParcelFileDescriptor.MODE_READ_ONLY;
        else if ("w".equals(mode) || "wt".equals(mode)) flags = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_TRUNCATE | ParcelFileDescriptor.MODE_CREATE;
        else if ("wa".equals(mode)) flags = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_APPEND;
        else if ("rw".equals(mode)) flags = ParcelFileDescriptor.MODE_READ_WRITE;
        else flags = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE;
        return ParcelFileDescriptor.open(f, flags);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String path = decode(uri);
        File f = path == null ? null : new File(path);
        if (f == null || !f.exists()) return null;
        String[] cols = projection != null ? projection : new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
        MatrixCursor c = new MatrixCursor(cols, 1);
        Object[] row = new Object[cols.length];
        for (int i = 0; i < cols.length; i++) {
            if (OpenableColumns.DISPLAY_NAME.equals(cols[i])) row[i] = f.getName();
            else if (OpenableColumns.SIZE.equals(cols[i])) row[i] = f.length();
            else row[i] = null;
        }
        c.addRow(row);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        String path = decode(uri);
        if (path == null) return null;
        String lower = path.toLowerCase();
        if (lower.endsWith(".apk")) return "application/vnd.android.package-archive";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".mp4")) return "video/mp4";
        String ext = lower.substring(lower.lastIndexOf('.') + 1);
        String mime = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        return mime == null ? "application/octet-stream" : mime;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) { return null; }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }
}

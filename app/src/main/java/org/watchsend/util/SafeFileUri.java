package org.watchsend.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.io.File;

/**
 * 跨应用传文件时的 Uri 选择器。
 *
 * Android 7.0（API 24）之前 `file://` 是合法做法；之后必须用 content:// 并授予临时读权限。
 * 工程 minSdk 是 10，所以按版本分支：老系统仍用 file://，新系统走 FileProviderEx。
 */
public class SafeFileUri {

    public static Uri uriFor(Context ctx, File f) {
        if (Build.VERSION.SDK_INT >= 24) return FileProviderEx.uriFor(ctx, f);
        return Uri.fromFile(f);
    }

    /** 给跨应用 Intent 补上读权限（API 24+ 必须显式授予，或由系统按 provider 配置放行）。 */
    public static Intent grantRead(Intent i) {
        if (Build.VERSION.SDK_INT >= 16) {
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        return i;
    }
}

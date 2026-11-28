package org.watchsend.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileOutputStream;

/**
 * 发送前图片压缩（设置项 compress_image，默认关闭）。
 *
 * 手表屏常见 320~400px，对方收到的原图动辄 4K，传一张几 MB 的图要几十秒。
 * 这里只做最保守的处理：长边超过 720 才缩、JPEG 质量 85、保留透明通道的 PNG 不转 JPEG。
 * 压缩失败一律返回原文件，宁可不压也不能把要发的东西弄丢。
 */
public class ImageCompressor {

    private static final int MAX_EDGE = 720;
    private static final int JPEG_QUALITY = 85;

    public static File maybeCompress(Context ctx, File src) {
        if (ctx == null || src == null || !src.isFile()) return src;
        if (!isEnabled(ctx)) return src;
        String name = src.getName().toLowerCase();
        if (!name.endsWith(".jpg") && !name.endsWith(".jpeg") && !name.endsWith(".png")
                && !name.endsWith(".webp")) return src;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(src.getAbsolutePath(), opts);
        int w = opts.outWidth, h = opts.outHeight;
        if (w <= 0 || h <= 0) return src;
        int longEdge = Math.max(w, h);
        if (longEdge <= MAX_EDGE) return src;

        int sample = 1;
        while (longEdge / sample > MAX_EDGE * 2) sample *= 2;

        BitmapFactory.Options dec = new BitmapFactory.Options();
        dec.inSampleSize = sample;
        Bitmap bmp;
        try { bmp = BitmapFactory.decodeFile(src.getAbsolutePath(), dec); } catch (Throwable t) { return src; }
        if (bmp == null) return src;

        try {
            int nw = bmp.getWidth(), nh = bmp.getHeight();
            int targetLong = Math.max(nw, nh);
            float scale = targetLong > MAX_EDGE ? (float) MAX_EDGE / targetLong : 1f;
            Bitmap out = bmp;
            if (scale < 1f) {
                out = Bitmap.createScaledBitmap(bmp, Math.max(1, (int) (nw * scale)),
                        Math.max(1, (int) (nh * scale)), true);
            }
            String base = src.getName();
            int dot = base.lastIndexOf('.');
            String outName = (dot > 0 ? base.substring(0, dot) : base) + ".jpg";
            File dst = new File(ctx.getCacheDir(), outName);
            FileOutputStream fos = new FileOutputStream(dst);
            try {
                out.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            } finally {
                fos.close();
            }
            if (out != bmp) { try { out.recycle(); } catch (Throwable ignored) {} }
            if (dst.isFile() && dst.length() > 0 && dst.length() < src.length()) {
                org.watchsend.OldSendService.log("图片压缩: " + src.getName() + " "
                        + src.length() + " -> " + dst.length() + " 字节");
                return dst;
            }
            dst.delete();
            return src;
        } catch (Throwable t) {
            return src;
        } finally {
            try { bmp.recycle(); } catch (Throwable ignored) {}
        }
    }

    private static boolean isEnabled(Context ctx) {
        return ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE)
                .getBoolean("compress_image", false);
    }
}

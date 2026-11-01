package org.watchsend.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {
    public static boolean unzip(File zipFile, File targetDir) {
        if (!targetDir.exists()) targetDir.mkdirs();
        try {
            String canonicalTargetPath = targetDir.getCanonicalPath();
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(targetDir, entry.getName());
                // 防御 Zip Slip 漏洞
                if (!file.getCanonicalPath().startsWith(canonicalTargetPath)) {
                    throw new SecurityException("Zip entry outside target dir");
                }
                
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) parent.mkdirs();
                    FileOutputStream fos = new FileOutputStream(file);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
            }
            zis.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

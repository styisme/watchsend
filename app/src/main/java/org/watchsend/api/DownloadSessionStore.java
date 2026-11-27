package org.watchsend.api;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.watchsend.transfer.DownloadShare;

/**
 * 下载会话（协议 v2 download API）。
 *
 * 官方语义：发送方（持有文件的一端）通过 POST /v2/prepare-download 把文件清单交给对端，
 * 对端拿到 sessionId + fileId→token 后，用 GET /v2/download 逐个取走
 * （packages/core/src/http/server/mod.rs:647）。
 *
 * WatchSend 的映射：可下载内容就是 OldSend 接收目录里的文件，fileId 由文件名+长度派生。
 */
public class DownloadSessionStore {

    public static class Entry {
        public final String fileId;
        public final File file;
        public final String token;
        public final String fileName;
        public final long size;

        public Entry(String fileId, File file, String token) {
            this.fileId = fileId;
            this.file = file;
            this.token = token;
            this.fileName = file.getName();
            this.size = file.length();
        }
    }

    private final Map<String, Map<String, Entry>> sessions = new HashMap<String, Map<String, Entry>>();
    private final Map<String, Long> created = new HashMap<String, Long>();

    private static final long TTL_MS = 10L * 60 * 1000;

    public synchronized String create(List<File> files) {
        sweep();
        String id = Long.toHexString(System.currentTimeMillis())
                + Long.toHexString(Double.doubleToLongBits(Math.random()));
        Map<String, Entry> map = new HashMap<String, Entry>();
        for (int i = 0; i < files.size(); i++) {
            File f = files.get(i);
            String fileId = DownloadShare.idFor(f);
            String token = Long.toHexString(System.nanoTime() ^ (long) (Math.random() * 1e18));
            map.put(fileId, new Entry(fileId, f, token));
        }
        sessions.put(id, map);
        created.put(id, Long.valueOf(System.currentTimeMillis()));
        return id;
    }

    public synchronized Map<String, Entry> get(String sessionId) {
        sweep();
        return sessions.get(sessionId);
    }

    public synchronized void remove(String sessionId) {
        sessions.remove(sessionId);
        created.remove(sessionId);
    }

    private void sweep() {
        long now = System.currentTimeMillis();
        java.util.Iterator<Map.Entry<String, Long>> it = created.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> e = it.next();
            if (now - e.getValue().longValue() > TTL_MS) {
                sessions.remove(e.getKey());
                it.remove();
            }
        }
    }
}

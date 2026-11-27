package org.watchsend.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UploadSession {
    public static class FileEntry {
        public final String fileId;
        public final String fileName;
        public final long size;
        public final String token;
        /** 发送方声明的 SHA-256（可选）；接收完成后比对。 */
        public final String sha256;
        /** RFC3339 修改时间（可选），落盘后还原。 */
        public final String modified;
        /** 文本消息的原文（fileType=text 时的 preview 字段）。 */
        public final String preview;

        public FileEntry(String id, String name, long s, String t) {
            this(id, name, s, t, null, null, null);
        }

        public FileEntry(String id, String name, long s, String t, String sha256, String modified, String preview) {
            this.fileId = id;
            this.fileName = name;
            this.size = s;
            this.token = t;
            this.sha256 = sha256;
            this.modified = modified;
            this.preview = preview;
        }
    }

    public final String sessionId;
    public final String peerIp;
    /** 创建时间，用于过期回收（手表内存有限，避免会话无限堆积）。 */
    public final long createdAt;
    public volatile long lastActiveAt;

    private final Map<String, FileEntry> files = new HashMap<String, FileEntry>();
    private final Set<String> done = new HashSet<String>();

    public UploadSession(String id, String peer) {
        this.sessionId = id;
        this.peerIp = peer;
        this.createdAt = System.currentTimeMillis();
        this.lastActiveAt = this.createdAt;
    }

    public void addFile(String fileId, String fileName, long size, String token) {
        addFile(fileId, fileName, size, token, null, null, null);
    }

    public void addFile(String fileId, String fileName, long size, String token, String sha256, String modified, String preview) {
        files.put(fileId, new FileEntry(fileId, fileName, size, token, sha256, modified, preview));
        lastActiveAt = System.currentTimeMillis();
    }

    public FileEntry getFile(String fileId) {
        lastActiveAt = System.currentTimeMillis();
        return files.get(fileId);
    }

    public int fileCount() { return files.size(); }

    public void markDone(String fileId) {
        synchronized (done) { done.add(fileId); }
        lastActiveAt = System.currentTimeMillis();
    }

    public boolean allDone() {
        synchronized (done) { return done.size() >= files.size(); }
    }
}

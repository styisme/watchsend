package org.watchsend.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 上传会话存储。
 *
 * 官方对并发连接和会话生命周期都有约束（connection_limit.rs），
 * 手表只有几百 MB 可用内存，这里更保守：会话超过 10 分钟无活动即回收，
 * 避免对端中途放弃后残留条目一直占着内存。
 */
public class SessionStore {

    private static final long SESSION_TTL_MS = 10L * 60 * 1000;

    private final Map<String, UploadSession> sessions = new HashMap<String, UploadSession>();

    public UploadSession create(String peerIp) {
        String id = Long.toHexString(System.currentTimeMillis())
                + Long.toHexString(Double.doubleToLongBits(Math.random()));
        UploadSession s = new UploadSession(id, peerIp);
        synchronized (sessions) {
            sweepLocked();
            sessions.put(id, s);
        }
        return s;
    }

    public UploadSession get(String id) {
        synchronized (sessions) {
            sweepLocked();
            return sessions.get(id);
        }
    }

    public void remove(String id) {
        synchronized (sessions) { sessions.remove(id); }
    }

    public int size() {
        synchronized (sessions) { return sessions.size(); }
    }

    private void sweepLocked() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, UploadSession>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            UploadSession s = it.next().getValue();
            if (now - s.lastActiveAt > SESSION_TTL_MS) {
                it.remove();
            }
        }
    }
}

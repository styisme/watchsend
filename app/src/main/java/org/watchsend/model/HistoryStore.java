package org.watchsend.model;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class HistoryStore {
    public static class Record {
        public String fileName;
        public String path;
        public long size;
        public String time;
        public boolean isReceive;
        /** 真实时间戳（毫秒）。老记录没有这个字段，为 0 时退回显示 time 字符串。 */
        public long ts;

        public Record(String name, String path, long size, String time, boolean isReceive) {
            this(name, path, size, time, isReceive, 0L);
        }

        public Record(String name, String path, long size, String time, boolean isReceive, long ts) {
            this.fileName = name; this.path = path; this.size = size; this.time = time; this.isReceive = isReceive;
            this.ts = ts;
        }
    }

    private static final HistoryStore INSTANCE = new HistoryStore();
    public static HistoryStore get() { return INSTANCE; }

    private final List<Record> records = new ArrayList<Record>();
    private static final String PREF_NAME = "oldsend_history";
    private static final String KEY_HISTORY = "history_json";

    public void init(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_HISTORY, "");
        if (json.length() > 0) {
            try {
                JSONArray arr = new JSONArray(json);
                synchronized (records) {
                    records.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        records.add(new Record(o.getString("name"), o.getString("path"), o.getLong("size"), o.getString("time"), o.getBoolean("isRecv"), o.optLong("ts", 0L)));
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public void add(Context ctx, Record r) {
        synchronized (records) {
            records.add(0, r);
            if (records.size() > 100) records.remove(records.size() - 1);
            
            try {
                JSONArray arr = new JSONArray();
                for (Record rec : records) {
                    JSONObject o = new JSONObject();
                    o.put("name", rec.fileName); o.put("path", rec.path);
                    o.put("size", rec.size); o.put("time", rec.time); o.put("isRecv", rec.isReceive); o.put("ts", rec.ts);
                    arr.put(o);
                }
                ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_HISTORY, arr.toString()).commit();
            } catch (Exception ignored) {}
        }
    }

    public void remove(Context ctx, String path) {
        synchronized (records) {
            for (int i = 0; i < records.size(); i++) {
                if (records.get(i).path.equals(path)) { records.remove(i); break; }
            }
            try {
                JSONArray arr = new JSONArray();
                for (Record rec : records) {
                    JSONObject o = new JSONObject();
                    o.put("name", rec.fileName); o.put("path", rec.path);
                    o.put("size", rec.size); o.put("time", rec.time); o.put("isRecv", rec.isReceive); o.put("ts", rec.ts);
                    arr.put(o);
                }
                ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_HISTORY, arr.toString()).commit();
            } catch (Exception ignored) {}
        }
    }

    public List<Record> snapshot() {
        synchronized (records) { return new ArrayList<Record>(records); }
    }
}
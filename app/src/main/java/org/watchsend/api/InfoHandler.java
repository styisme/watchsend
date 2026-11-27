package org.watchsend.api;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;
import org.watchsend.OldSendService;
import org.watchsend.Protocol;
import org.watchsend.http.HttpRequest;
import org.watchsend.http.HttpResponse;
import org.watchsend.http.Router;
import org.watchsend.protocol.Identity;
import org.watchsend.transfer.DownloadShare;

public class InfoHandler implements Router.Handler {
    private final String alias;

    public InfoHandler(String alias) { this.alias = alias; }

    public HttpResponse handle(HttpRequest req) throws Exception {
        return HttpResponse.json(buildSelfJson(alias));
    }

    public static String buildSelfJson(String alias) {
        return selfInfo(alias).toString();
    }

    /**
     * 本机对外声明（/v2/info、/v2/register、组播回应、上传请求里的 info 字段共用）。
     *
     * 与官方 RegisterDtoV2 对齐：protocol 必须是真实监听协议，
     * download 表示当前是否提供下载 API；写死 "http"/false 会让
     * 开了加密的设备被对端当成明文设备（官方 2.x 默认 https）。
     */
    public static JSONObject selfInfo(String alias) {
        try {
            JSONObject o = new JSONObject();
            o.put("alias", alias == null || alias.length() == 0 ? android.os.Build.MODEL : alias);
            o.put("version", Protocol.VERSION);
            o.put("deviceModel", "Android " + android.os.Build.VERSION.RELEASE);
            o.put("deviceType", "mobile");
            o.put("fingerprint", Identity.getFingerprint());
            o.put("port", selfPort());
            o.put("protocol", selfProtocol());
            o.put("download", DownloadShare.isEnabled());
            return o;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public static boolean isHttpsEnabled() {
        Context ctx = OldSendService.getContext();
        if (ctx == null) return false;
        return ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE).getBoolean("use_https", false);
    }

    public static String selfProtocol() {
        return isHttpsEnabled() ? "https" : "http";
    }

    public static int selfPort() {
        Context ctx = OldSendService.getContext();
        if (ctx == null) return Protocol.DEFAULT_PORT;
        SharedPreferences sp = ctx.getSharedPreferences("oldsend", Context.MODE_PRIVATE);
        return sp.getInt("port", Protocol.DEFAULT_PORT);
    }
}

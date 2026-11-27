package org.watchsend.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import org.watchsend.R;
import org.watchsend.model.Device;
import org.watchsend.model.FavoritesStore;
import org.watchsend.util.I18nManager;
import org.watchsend.util.ThemeUtils;

/**
 * 设备列表项：首字母彩色头像 + 名称/地址两行 + 右侧标记（TLS / 收藏）。
 *
 * 对齐官方设备列表的信息层级（设备类型、协议、收藏状态都在一行里可见），
 * 手表屏幕窄，所以把类型与协议压到第二行，用一个小徽标表示收藏。
 */
public class DeviceAdapter extends BaseAdapter {
    private final Context ctx;
    private final List<Device> data = new ArrayList<Device>();

    /** 头像配色：从名称哈希里取一个稳定颜色，同一台设备每次都是同一种。 */
    private static final int[] AVATAR_COLORS = {
            0xFF3F51B5, 0xFF00897B, 0xFF8E24AA, 0xFFEF6C00,
            0xFF2E7D32, 0xFFD81B60, 0xFF5E35B1, 0xFF00838F
    };

    public DeviceAdapter(Context ctx) { this.ctx = ctx; }
    public void setData(List<Device> list) { data.clear(); if (list != null) data.addAll(list); notifyDataSetChanged(); }
    public int getCount() { return data.size(); }
    public Device getItem(int pos) { return data.get(pos); }
    public long getItemId(int pos) { return pos; }

    public View getView(int pos, View cv, ViewGroup parent) {
        ViewHolder h;
        if (cv == null) {
            cv = LayoutInflater.from(ctx).inflate(R.layout.item_device, parent, false);
            h = new ViewHolder();
            h.avatar = (TextView) cv.findViewById(R.id.item_avatar);
            h.alias = (TextView) cv.findViewById(R.id.item_alias);
            h.info = (TextView) cv.findViewById(R.id.item_info);
            h.badge = (TextView) cv.findViewById(R.id.item_badge);
            cv.setTag(h);
        } else { h = (ViewHolder) cv.getTag(); }
        Device d = data.get(pos);
        boolean isDark = ThemeUtils.isDarkMode(ctx);

        String name = FavoritesStore.displayName(d);
        h.alias.setText(name);
        h.alias.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF1C1B1F);

        h.avatar.setText(initialOf(name));
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(colorOf(name));
        h.avatar.setBackground(circle);

        String type = typeLabel(d.deviceType);
        h.info.setText(d.ip + ":" + d.port + " · " + type + (d.https ? " · TLS" : ""));
        h.info.setTextColor(isDark ? 0xFFAAAAAA : 0xFF666666);

        String badge = "";
        if (FavoritesStore.isFavorite(d.identity())) badge = "★";
        else if (d.https) badge = "🔒";
        h.badge.setText(badge);
        h.badge.setTextColor(0xFFF9A825);
        return cv;
    }

    private String typeLabel(String type) {
        if ("desktop".equals(type)) return I18nManager.get("device_desktop");
        if ("web".equals(type)) return I18nManager.get("device_web");
        if ("headless".equals(type)) return I18nManager.get("device_headless");
        if ("server".equals(type)) return I18nManager.get("device_server");
        return I18nManager.get("device_mobile");
    }

    /** 取首个有意义的字符作头像文字（中文取首字，英文取首字母大写）。 */
    public static String initialOf(String name) {
        if (name == null) return "?";
        String n = name.trim();
        if (n.length() == 0) return "?";
        char c = n.charAt(0);
        return String.valueOf(Character.toUpperCase(c));
    }

    public static int colorOf(String name) {
        int hash = 0;
        if (name != null) {
            for (int i = 0; i < name.length(); i++) hash = 31 * hash + name.charAt(i);
        }
        return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length];
    }

    static class ViewHolder { TextView avatar; TextView alias; TextView info; TextView badge; }
}

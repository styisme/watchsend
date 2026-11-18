package org.watchsend.uikit;

import org.watchsend.R;

import java.io.Serializable;

public class UikitListItemModel implements Serializable {
    public String title;
    public String subtitle;
    public int accentColor; // 标题强调色，传 0 表示默认
    public int cardColor;   // 卡片背景色，传 0 表示使用系统默认卡片色

    public UikitListItemModel(String title) {
        this(title, null, 0, 0);
    }

    public UikitListItemModel(String title, String subtitle) {
        this(title, subtitle, 0, 0);
    }

    public UikitListItemModel(String title, String subtitle, int accentColor) {
        this(title, subtitle, accentColor, 0);
    }

    public UikitListItemModel(String title, String subtitle, int accentColor, int cardColor) {
        this.title = title;
        this.subtitle = subtitle;
        this.accentColor = accentColor;
        this.cardColor = cardColor;
    }
}

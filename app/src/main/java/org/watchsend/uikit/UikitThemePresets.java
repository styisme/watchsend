package org.watchsend.uikit;

public final class UikitThemePresets {
    private UikitThemePresets() {}
    
    // 常见的 MD3 预设颜色
    public static final int GREEN   = 0xFF388E3C;
    public static final int BLUE    = 0xFF1976D2;
    public static final int RED     = 0xFFD32F2F;
    public static final int PURPLE  = 0xFF7B1FA2;
    public static final int YELLOW  = 0xFFFBC02D;
    public static final int ORANGE  = 0xFFF57C00;
    public static final int TEAL    = 0xFF00796B;

    /**
     * 一键应用预设主题（解决老设备没有动态取色的痛点）
     */
    public static void applyPreset(android.content.Context ctx, int color) {
        UikitConfig.setCustomPrimaryColor(ctx, color);
        // 自动计算一个偏亮的颜色作为强调色
        int accent = UikitColorMixer.blend(color, 0xFFFFFFFF, 0.3f);
        UikitConfig.setCustomAccentColor(ctx, accent);
    }
}

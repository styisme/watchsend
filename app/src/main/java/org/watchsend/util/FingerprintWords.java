package org.watchsend.util;

/**
 * 指纹的文字短语（移植官方 util/fingerprint_alphabet.dart 的思路，图标换成词）。
 *
 * 官方用 60 多个图标表示指纹，两个人可以隔着屏幕"念图标"来比对，防止中间人。
 * 手表上没有图标资源，也不该为此拉进几百 KB 图片，于是改成"四段词"：
 * 取指纹前 16 个 hex 字符，每 4 个一组 → 16 位 → 取 64 词表里的一词，
 * 得到形如「月亮-书包-灯塔-鲸鱼」的短语，一样可以口头核对，且完全由指纹决定。
 *
 * 只用于人工比对展示，不参与任何校验逻辑：真正的校验始终是比较完整 SHA-256。
 */
public class FingerprintWords {

    private static final String[] WORDS = {
            "苹果", "香蕉", "月亮", "太阳", "星星", "云朵", "雨伞", "灯塔",
            "鲸鱼", "老虎", "狮子", "兔子", "熊猫", "狐狸", "海豚", "猫头鹰",
            "书包", "椅子", "钥匙", "时钟", "蜡烛", "气球", "风车", "火车",
            "飞机", "火箭", "轮船", "自行车", "吉他", "钢琴", "画笔", "书签",
            "咖啡", "面包", "蛋糕", "糖果", "冰淇淋", "西瓜", "柠檬", "葡萄",
            "山峰", "河流", "沙漠", "森林", "雪花", "火焰", "彩虹", "闪电",
            "镜子", "眼镜", "帽子", "手套", "皮鞋", "雨靴", "帐篷", "锚",
            "钻石", "金币", "地图", "指南针", "望远镜", "显微镜", "电池", "齿轮"
    };

    /** 把指纹（hex 字符串）转成 4 段词，无法解析时返回空串。 */
    public static String phrase(String fingerprint) {
        if (fingerprint == null || fingerprint.length() < 16) return "";
        String hex = fingerprint.toLowerCase().replace(":", "").replace(" ", "");
        if (hex.length() < 16) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            String group = hex.substring(i * 4, i * 4 + 4);
            int idx;
            try {
                idx = Integer.parseInt(group, 16) % WORDS.length;
            } catch (Exception e) {
                return "";
            }
            if (sb.length() > 0) sb.append("-");
            sb.append(WORDS[Math.abs(idx)]);
        }
        return sb.toString();
    }
}

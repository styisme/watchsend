package org.watchsend.util;

import java.util.Random;

/**
 * 首启设备名的随机生成（移植官方 app/lib/util/alias_generator.dart）。
 *
 * 官方是「形容词 + 水果」的组合，词表随语言变化，例如中文的"愉快的香蕉"。
 * 手表上没有 Flutter 的 ARB 词表，这里直接内置三种语言的最小词表：
 * 简体、繁体、英文；其余语言（ja/ko/de/fr/es/ru/pt）落回英文，避免出现半截翻译。
 *
 * 目的很实际：同型号手表在同一网段下默认名都是 Build.MODEL，br 列表里会撞名，
 * 随机别名能让"谁的手机"一眼分清。
 */
public class AliasGenerator {

    private static final String[] ZH_ADJ = {
            "愉快的", "安静的", "勇敢的", "聪明的", "闪亮的",
            "温柔的", "灵活的", "开心的", "认真的", "轻盈的",
            "可靠的", "好奇的", "专注的", "踏实的", "活泼的",
            "清爽的", "沉稳的", "利落的", "随和的", "精神的"
    };
    private static final String[] ZH_FRUIT = {
            "香蕉", "苹果", "橘子", "桃子", "葡萄",
            "西瓜", "柠檬", "樱桃", "草莓", "菠萝",
            "芒果", "梨子", "荔枝", "蓝莓", "椰子",
            "石榴", "杏子", "柚子", "猕猴桃", "哈密瓜"
    };

    private static final String[] TW_ADJ = {
            "愉快的", "安靜的", "勇敢的", "聰明的", "閃亮的",
            "溫柔的", "靈活的", "開心的", "認真的", "輕盈的",
            "可靠的", "好奇的", "專注的", "踏實的", "活潑的",
            "清爽的", "沉穩的", "俐落的", "隨和的", "精神的"
    };
    private static final String[] TW_FRUIT = {
            "香蕉", "蘋果", "橘子", "桃子", "葡萄",
            "西瓜", "檸檬", "櫻桃", "草莓", "鳳梨",
            "芒果", "梨子", "荔枝", "藍莓", "椰子",
            "石榴", "杏子", "柚子", "奇異果", "哈密瓜"
    };

    private static final String[] EN_ADJ = {
            "Happy", "Quiet", "Brave", "Clever", "Shiny",
            "Gentle", "Nimble", "Cheerful", "Earnest", "Light",
            "Steady", "Curious", "Focused", "Solid", "Lively",
            "Fresh", "Calm", "Tidy", "Easy", "Spirited"
    };
    private static final String[] EN_FRUIT = {
            "Banana", "Apple", "Orange", "Peach", "Grape",
            "Melon", "Lemon", "Cherry", "Strawberry", "Pineapple",
            "Mango", "Pear", "Lychee", "Blueberry", "Coconut",
            "Pomegranate", "Apricot", "Pomelo", "Kiwi", "Cantaloupe"
    };

    /** 生成一个随机别名，随当前界面语言变化。 */
    public static String generate() {
        String lang = I18nManager.current();
        String[] adj;
        String[] fruit;
        if ("zh".equals(lang)) {
            adj = ZH_ADJ; fruit = ZH_FRUIT;
        } else if ("zh_TW".equals(lang)) {
            adj = TW_ADJ; fruit = TW_FRUIT;
        } else {
            adj = EN_ADJ; fruit = EN_FRUIT;
        }
        Random r = new Random();
        String a = adj[r.nextInt(adj.length)];
        String f = fruit[r.nextInt(fruit.length)];
        // 中文直接拼接，西文之间留空格
        return ("zh".equals(lang) || "zh_TW".equals(lang)) ? a + f : a + " " + f;
    }
}

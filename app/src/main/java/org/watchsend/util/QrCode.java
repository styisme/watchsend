package org.watchsend.util;

/**
 * 最小 QR 码编码器（字节模式，纠错等级 L，版本 1–5）。
 *
 * 为什么自己写：工程约束是零第三方依赖 + minSdk 10，任何二维码库都带不进这个包体预算
 * （主流库动辄 100KB 以上，还依赖 Java 8 API）。而网页分享只需要编码一条
 * `http://192.168.x.x:53317` 这样的短 URL（30–40 字节），版本 2–3 就够。
 *
 * 覆盖范围按需求裁剪：
 * - 只做 byte mode（URL 是 ASCII，不需要数字/字母数字模式）
 * - 只做纠错 L（屏幕近距离扫描，L 级 7% 冗余足够，且容量最大）
 * - 版本 1–5 都是单块结构，不需要交织（interleaving）
 *
 * 实现按 ISO/IEC 18004：功能图形 → 数据摆放（zigzag）→ 8 种掩码 → 罚分选最优。
 */

public class QrCode {

    /** 版本 1–5 在纠错 L 下的数据码字数 / 纠错码字数（单块）。 */
    private static final int[] DATA_CODEWORDS = {19, 34, 55, 80, 108};
    private static final int[] EC_CODEWORDS = {7, 10, 15, 20, 26};
    private static final int[] MAX_CHARS = {17, 32, 53, 78, 106};

    /** 编码后的模块矩阵，true = 深色。 */
    public static class Matrix {
        public final int size;
        public final boolean[][] modules;
        Matrix(int size) { this.size = size; this.modules = new boolean[size][size]; }
    }

    /** 编码文本，自动选最小可用版本，并用罚分挑选最优掩码。 */
    public static Matrix encode(String text) {
        int version = pickVersion(text);
        int size = 17 + version * 4;
        boolean[][] reserved = new boolean[size][size];
        Matrix m = new Matrix(size);
        drawFunctionPatterns(m, version, reserved);
        byte[] codewords = buildCodewords(text, version);
        placeData(m, codewords, reserved);

        int bestMask = 0;
        int bestScore = Integer.MAX_VALUE;
        for (int mask = 0; mask < 8; mask++) {
            boolean[][] trial = copy(m.modules);
            applyMask(trial, mask, reserved);
            drawFormatInfo(trial, mask);
            int score = penalty(trial);
            if (score < bestScore) { bestScore = score; bestMask = mask; }
        }
        // 用选中的掩码生成最终矩阵
        Matrix out = new Matrix(size);
        for (int y = 0; y < size; y++) System.arraycopy(m.modules[y], 0, out.modules[y], 0, size);
        applyMask(out.modules, bestMask, reserved);
        drawFormatInfo(out.modules, bestMask);
        return out;
    }

    /** 指定掩码，主要用于与参考实现逐模块比对。 */
    public static Matrix encodeWithMask(String text, int mask) {
        int version = pickVersion(text);
        int size = 17 + version * 4;
        boolean[][] reserved = new boolean[size][size];
        Matrix m = new Matrix(size);
        drawFunctionPatterns(m, version, reserved);
        placeData(m, buildCodewords(text, version), reserved);
        applyMask(m.modules, mask, reserved);
        drawFormatInfo(m.modules, mask);
        return m;
    }

    private static int pickVersion(String text) {
        int len = text.getBytes().length;
        for (int v = 0; v < MAX_CHARS.length; v++) if (len <= MAX_CHARS[v]) return v + 1;
        throw new IllegalArgumentException("内容太长，本编码器只支持到版本 5（106 字节）");
    }

    // ===== 数据编码 =====

    private static byte[] buildCodewords(String text, int version) {
        byte[] data = text.getBytes();
        int dataWords = DATA_CODEWORDS[version - 1];
        int capacityBits = dataWords * 8;

        // 模式指示符 0100 + 8 位字符计数（版本 1–9）
        BitBuffer bb = new BitBuffer();
        bb.put(4, 4);
        bb.put(data.length, 8);
        for (byte b : data) bb.put(b & 0xFF, 8);

        // 终止符 + 补齐到字节边界
        int remain = capacityBits - bb.size;
        if (remain > 0) bb.put(0, Math.min(4, remain));
        while (bb.size % 8 != 0) bb.put(0, 1);

        byte[] out = new byte[dataWords];
        int n = 0;
        for (int i = 0; i < bb.size; i += 8) out[n++] = (byte) bb.bits(i, 8);
        // 填充码字 0xEC / 0x11 交替
        boolean flip = true;
        while (n < dataWords) { out[n++] = (byte) (flip ? 0xEC : 0x11); flip = !flip; }

        // 纠错（单块，直接整体算）
        byte[] ec = reedSolomon(out, EC_CODEWORDS[version - 1]);
        byte[] result = new byte[out.length + ec.length];
        System.arraycopy(out, 0, result, 0, out.length);
        System.arraycopy(ec, 0, result, out.length, ec.length);
        return result;
    }

    /** GF(256) 上的 Reed-Solomon：生成多项式与余数计算（单块纠错）。 */
    private static byte[] reedSolomon(byte[] data, int ecCount) {
        int[] divisor = computeDivisor(ecCount);
        int[] rem = new int[divisor.length];
        for (int i = 0; i < data.length; i++) {
            int factor = ((data[i] & 0xFF) ^ rem[0]) & 0xFF;
            System.arraycopy(rem, 1, rem, 0, rem.length - 1);
            rem[rem.length - 1] = 0;
            for (int j = 0; j < rem.length; j++) rem[j] ^= mul(divisor[j], factor);
        }
        byte[] out = new byte[ecCount];
        for (int i = 0; i < ecCount; i++) out[i] = (byte) rem[i];
        return out;
    }

    private static int[] computeDivisor(int degree) {
        int[] result = new int[degree];
        result[degree - 1] = 1;
        int root = 1;
        for (int i = 0; i < degree; i++) {
            for (int j = 0; j < degree; j++) {
                result[j] = mul(result[j], root);
                if (j + 1 < degree) result[j] ^= result[j + 1];
            }
            root = mul(root, 2);
        }
        return result;
    }

    /** GF(256) 乘法，本原多项式 x^8+x^4+x^3+x^2+1 = 0x11D。 */
    private static int mul(int a, int b) {
        int r = 0;
        while (b > 0) {
            if ((b & 1) != 0) r ^= a;
            b >>= 1;
            a <<= 1;
            if (a >= 0x100) a ^= 0x11D;
        }
        return r & 0xFF;
    }

    // ===== 模块摆放 =====

    private static void drawFunctionPatterns(Matrix m, int version, boolean[][] reserved) {
        int size = m.size;
        // 定位图形（左上/右上/左下）+ 分隔符
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                // 每个 8x8 块 = 7x7 定位图形 + 1 行/列分隔符（第 8 圈留给格式信息/数据）
                setReserved(m, reserved, j, i, finderModule(j, i));
                setReserved(m, reserved, size - 1 - j, i, finderModule(j, i));
                setReserved(m, reserved, j, size - 1 - i, finderModule(j, i));
            }
        }
        // 定时图形
        for (int i = 8; i < size - 8; i++) {
            boolean dark = (i % 2 == 0);
            setReserved(m, reserved, i, 6, dark);
            setReserved(m, reserved, 6, i, dark);
        }
        // 校正图形（v2–v5 只有一个，位于右下）
        if (version >= 2) {
            int cx = size - 7;
            for (int dy = -2; dy <= 2; dy++) {
                for (int dx = -2; dx <= 2; dx++) {
                    boolean dark = Math.abs(dx) == 2 || Math.abs(dy) == 2 || (dx == 0 && dy == 0);
                    setReserved(m, reserved, cx + dx, cx + dy, dark);
                }
            }
        }
        // 预留格式信息区（先占位，掩码选完再写）
        for (int i = 0; i < 9; i++) {
            if (i != 6) { reserved[8][i] = true; reserved[i][8] = true; }
        }
        for (int i = 0; i < 8; i++) { reserved[8][size - 1 - i] = true; reserved[size - 1 - i][8] = true; }
        reserved[size - 8][8] = true;
    }

    /** 以定位图形左上角为原点，8x8 区域内 true=深色（含分隔符）。 */
    private static boolean finderModule(int x, int y) {
        if (x < 0 || y < 0 || x > 7 || y > 7) return false;
        if (x == 7 || y == 7) return false;                       // 分隔符
        if (x == 0 || x == 6 || y == 0 || y == 6) return true;     // 外环
        if (x >= 2 && x <= 4 && y >= 2 && y <= 4) return true;      // 3x3 内核
        return false;
    }

    private static void setReserved(Matrix m, boolean[][] reserved, int x, int y, boolean dark) {
        if (x < 0 || y < 0 || x >= m.size || y >= m.size) return;
        m.modules[y][x] = dark;
        reserved[y][x] = true;
    }

    private static void placeData(Matrix m, byte[] codewords, boolean[][] reserved) {
        int size = m.size;
        int bitIndex = 0;
        int direction = -1;   // -1 向上，+1 向下
        for (int right = size - 1; right >= 1; right -= 2) {
            if (right == 6) right = 5;  // 跳过竖直定时图形列
            for (int vert = 0; vert < size; vert++) {
                int y = direction == -1 ? size - 1 - vert : vert;
                for (int j = 0; j < 2; j++) {
                    int x = right - j;
                    if (reserved[y][x]) continue;
                    boolean dark = false;
                    if (bitIndex < codewords.length * 8) {
                        int byteVal = codewords[bitIndex >> 3] & 0xFF;
                        dark = ((byteVal >> (7 - (bitIndex & 7))) & 1) == 1;
                    }
                    m.modules[y][x] = dark;
                    bitIndex++;
                }
            }
            direction = -direction;
        }
    }

    private static void applyMask(boolean[][] modules, int mask, boolean[][] reserved) {
        int size = modules.length;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (reserved != null && reserved[y][x]) continue;  // 功能图形与格式区不参与掩码
                boolean invert;
                switch (mask) {
                    case 0: invert = (x + y) % 2 == 0; break;
                    case 1: invert = y % 2 == 0; break;
                    case 2: invert = x % 3 == 0; break;
                    case 3: invert = (x + y) % 3 == 0; break;
                    case 4: invert = ((y / 2) + (x / 3)) % 2 == 0; break;
                    case 5: invert = ((x * y) % 2) + ((x * y) % 3) == 0; break;
                    case 6: invert = (((x * y) % 2) + ((x * y) % 3)) % 2 == 0; break;
                    default: invert = (((x + y) % 2) + ((x * y) % 3)) % 2 == 0; break;
                }
                if (invert) modules[y][x] = !modules[y][x];
            }
        }
    }

    private static void drawFormatInfo(boolean[][] modules, int mask) {
        int size = modules.length;
        // 纠错等级 L = 01
        int data = (1 << 3) | mask;          // 纠错 L = 01，低 3 位为掩码编号
        int rem = data;
        for (int i = 0; i < 10; i++) rem = (rem << 1) ^ (((rem >> 9) & 1) * 0x537);  // BCH(15,5)
        int format = ((data << 10) | (rem & 0x3FF)) ^ 0x5412;

        // 第一份：环绕左上定位图形（注意矩阵是 [y][x]，官方图示给的是 (x, y)）
        for (int i = 0; i <= 5; i++) modules[i][8] = bit(format, i);
        modules[7][8] = bit(format, 6);
        modules[8][8] = bit(format, 7);
        modules[8][7] = bit(format, 8);
        for (int i = 9; i <= 14; i++) modules[8][14 - i] = bit(format, i);

        // 第二份：右上与左下
        for (int i = 0; i <= 7; i++) modules[8][size - 1 - i] = bit(format, i);
        for (int i = 8; i <= 14; i++) modules[size - 15 + i][8] = bit(format, i);
        modules[size - 8][8] = true;  // 固定深色模块
    }

    private static boolean bit(int v, int i) { return ((v >> i) & 1) == 1; }

    private static boolean[][] copy(boolean[][] src) {
        boolean[][] out = new boolean[src.length][src.length];
        for (int i = 0; i < src.length; i++) System.arraycopy(src[i], 0, out[i], 0, src.length);
        return out;
    }

    // ===== 掩码罚分（ISO 18004 规则 N1–N4）=====

    private static int penalty(boolean[][] m) {
        int size = m.length;
        int score = 0;

        // N1：连续同色 5 个以上
        for (int i = 0; i < size; i++) {
            int runRow = 1, runCol = 1;
            for (int j = 1; j < size; j++) {
                if (m[i][j] == m[i][j - 1]) runRow++; else { if (runRow >= 5) score += 3 + (runRow - 5); runRow = 1; }
                if (m[j][i] == m[j - 1][i]) runCol++; else { if (runCol >= 5) score += 3 + (runCol - 5); runCol = 1; }
            }
            if (runRow >= 5) score += 3 + (runRow - 5);
            if (runCol >= 5) score += 3 + (runCol - 5);
        }

        // N2：2x2 同色块
        for (int y = 0; y < size - 1; y++) {
            for (int x = 0; x < size - 1; x++) {
                boolean c = m[y][x];
                if (c == m[y][x + 1] && c == m[y + 1][x] && c == m[y + 1][x + 1]) score += 3;
            }
        }

        // N3：1:1:3:1:1 类定位图形
        for (int i = 0; i < size; i++) {
            for (int j = 0; j + 10 < size; j++) {
                if (m[i][j] && !m[i][j + 1] && m[i][j + 2] && m[i][j + 3] && m[i][j + 4]
                        && !m[i][j + 5] && m[i][j + 6] && !m[i][j + 7] && !m[i][j + 8] && !m[i][j + 9] && !m[i][j + 10]) score += 40;
                if (!m[i][j] && !m[i][j + 1] && !m[i][j + 2] && !m[i][j + 3] && m[i][j + 4]
                        && !m[i][j + 5] && m[i][j + 6] && m[i][j + 7] && m[i][j + 8] && !m[i][j + 9] && m[i][j + 10]) score += 40;
                if (m[j][i] && !m[j + 1][i] && m[j + 2][i] && m[j + 3][i] && m[j + 4][i]
                        && !m[j + 5][i] && m[j + 6][i] && !m[j + 7][i] && !m[j + 8][i] && !m[j + 9][i] && !m[j + 10][i]) score += 40;
                if (!m[j][i] && !m[j + 1][i] && !m[j + 2][i] && !m[j + 3][i] && m[j + 4][i]
                        && !m[j + 5][i] && m[j + 6][i] && m[j + 7][i] && m[j + 8][i] && !m[j + 9][i] && m[j + 10][i]) score += 40;
            }
        }

        // N4：深浅比例偏离 50%
        int dark = 0;
        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) if (m[y][x]) dark++;
        int percent = (int) Math.round(dark * 100.0 / (size * size));
        int k = Math.abs(percent - 50) / 5;
        score += k * 10;
        return score;
    }

    /** 位缓冲，按 MSB-first 写入。 */
    private static class BitBuffer {
        private final byte[] buf = new byte[512];
        int size = 0;

        void put(int value, int bits) {
            for (int i = bits - 1; i >= 0; i--) {
                int bytePos = size >> 3;
                if (((value >> i) & 1) == 1) buf[bytePos] |= (byte) (1 << (7 - (size & 7)));
                size++;
            }
        }

        int bits(int start, int count) {
            int v = 0;
            for (int i = 0; i < count; i++) {
                int pos = start + i;
                v = (v << 1) | (((buf[pos >> 3] >> (7 - (pos & 7))) & 1));
            }
            return v;
        }
    }
}

package org.watchsend.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * HTTP/1.1 `Transfer-Encoding: chunked` 解码流。
 *
 * 为什么必须有：官方 LocalSend 2.x 客户端上传文件用的是
 * `reqwest::Body::wrap_stream(...)`（packages/core/src/http/client/mod.rs:upload_body），
 * 流式 body 没有 Content-Length，reqwest 会改用 chunked 传输：
 *
 *   1a\r\n<26 字节数据>\r\n2b\r\n<43 字节数据>\r\n...0\r\n\r\n
 *
 * 我们之前把 `req.body` 当成原始文件字节直接写盘，于是写进去的"文件"里混着
 * `1a\r\n`、`\r\n` 这些分块标记——1.7.5 之前表现为"传输成功但文件打不开"，
 * 加了长度校验后就直接 400。这里把分块在 HTTP 层解掉，上层 Handler 拿到的就是纯数据。
 */
public class ChunkedInputStream extends InputStream {

    private final InputStream in;
    private long remaining;      // 当前分块剩余未读字节
    private boolean needCrlf;    // 当前分块读完后还需吃掉一个 CRLF
    private boolean eof;

    public ChunkedInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        byte[] one = new byte[1];
        int n = read(one, 0, 1);
        return n < 0 ? -1 : (one[0] & 0xFF);
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        if (len == 0) return 0;
        if (eof) return -1;
        if (remaining == 0) {
            if (needCrlf) {
                skipCrlf();
                needCrlf = false;
            }
            long size = readChunkSize();
            if (size == 0) {
                readTrailers();
                eof = true;
                return -1;
            }
            remaining = size;
            needCrlf = true;
        }
        int want = (int) Math.min((long) len, remaining);
        int n = in.read(buf, off, want);
        if (n < 0) throw new IOException("chunk 数据意外中断");
        remaining -= n;
        return n;
    }

    @Override
    public int available() throws IOException {
        return eof ? 0 : (int) Math.min(Integer.MAX_VALUE, remaining);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    /** 读分块长度行：十六进制，可能带 `;` 扩展段，末尾 CRLF。 */
    private long readChunkSize() throws IOException {
        String line = HttpRequest.readLine(in);
        if (line == null) throw new IOException("chunk 头缺失");
        line = line.trim();
        int semi = line.indexOf(';');
        if (semi >= 0) line = line.substring(0, semi).trim();
        if (line.length() == 0) throw new IOException("chunk 长度为空");
        try {
            return Long.parseLong(line, 16);
        } catch (NumberFormatException e) {
            throw new IOException("非法 chunk 长度: " + line);
        }
    }

    private void skipCrlf() throws IOException {
        int c = in.read();
        if (c == -1) throw new IOException("chunk 结束标记缺失");
        if (c == '\n') return;         // 容忍裸 LF
        if (c != '\r') throw new IOException("chunk 结束标记异常: " + c);
        int c2 = in.read();
        if (c2 == -1) throw new IOException("chunk 结束标记缺失");
        if (c2 != '\n') throw new IOException("chunk 结束标记异常");
    }

    /** 0 号分块之后是可选 trailer（每行一个头），直到空行结束。 */
    private void readTrailers() throws IOException {
        while (true) {
            String line = HttpRequest.readLine(in);
            if (line == null || line.length() == 0) return;
        }
    }
}

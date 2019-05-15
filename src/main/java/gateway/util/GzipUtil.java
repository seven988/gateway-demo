package gateway.util;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtil {

    public static String uncompress(byte[] bytes, String charset) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = null;
        ByteArrayInputStream byteArrayInputStream = null;
        GZIPInputStream gzipInputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            gzipInputStream = new GZIPInputStream(byteArrayInputStream);
            // 使用 org.apache.commons.io.IOUtils 简化流的操作
            IOUtils.copy(gzipInputStream, byteArrayOutputStream);
            return byteArrayOutputStream.toString(charset);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 释放流资源
            IOUtils.closeQuietly(gzipInputStream);
            IOUtils.closeQuietly(byteArrayInputStream);
            IOUtils.closeQuietly(byteArrayOutputStream);
        }
        return null;
    }

    public static byte[] compress(String str) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes());
        gzip.close();
        return out.toByteArray();
    }
}
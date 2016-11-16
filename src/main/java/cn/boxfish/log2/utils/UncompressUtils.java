package cn.boxfish.log2.utils;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by lvtu on 2016/11/16.
 */
public class UncompressUtils {

    public static InputStream uncompress(File zipFile) {
        GZIPInputStream gzipInputStream = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(zipFile);
            gzipInputStream = new GZIPInputStream(fileInputStream, 4 * 1024);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(4 * 1024);
            byte[] buf = new byte[1024];
            int actualBytes;
            while ((actualBytes = gzipInputStream.read(buf, 0, 1024)) != -1) {
                byteArrayOutputStream.write(buf, 0, actualBytes);
            }
            byteArrayOutputStream.close();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            return byteArrayInputStream;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (gzipInputStream != null) {
                    gzipInputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

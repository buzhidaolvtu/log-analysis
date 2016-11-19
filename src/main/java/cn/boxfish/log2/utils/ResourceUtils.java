package cn.boxfish.log2.utils;

import java.io.Closeable;

/**
 * Created by lvtu on 2016/11/18.
 */
public class ResourceUtils {
    public static void close(Closeable closeable){
        try {
            closeable.close();
        }catch (Exception e){
            //ignore ex
        }
    }
}

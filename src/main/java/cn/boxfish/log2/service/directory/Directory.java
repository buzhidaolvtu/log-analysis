package cn.boxfish.log2.service.directory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lvtu on 2016/11/16.
 */
public abstract class Directory {
    private final static Logger logger = LoggerFactory.getLogger(Directory.class);

    public static List<String> traversalDir(String dirPath){
        File dir =  new File(dirPath);
        if(!dir.isDirectory()){
            throw new RuntimeException("not a directory.");
        }
        return Arrays.asList(dir.list());
    }

    public static void main(String[] args) {
        List<String> strings = Directory.traversalDir("/Users/lvtu/Desktop/temp/earthshaker_log");
        logger.info("",strings);
    }
}

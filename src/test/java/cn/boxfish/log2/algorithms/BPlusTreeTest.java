package cn.boxfish.log2.algorithms;

import com.google.common.base.Strings;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by lvtu on 2016/11/22.
 */
public class BPlusTreeTest {

    private final static Logger logger = LoggerFactory.getLogger(BPlusTreeTest.class);

    @Test
    public void BPlusTree_search() throws Exception {

    }

    @Test
    public void BPlusTree_delete() throws Exception {

    }

    @Test
    public void BPlusTree_insert() throws Exception {
        BPlusTree<String, String> bPlusTree = new BPlusTree<>(10);
        for (int i = 0; i < 100000; i++) {
            String s = Strings.padStart(String.valueOf(i), 1, '0');
            String key = "key" + s;
            String value = "value" + s;
            bPlusTree.insert(key, value);
        }
        for (int i = 0; i < 100000; i++) {
            String s = Strings.padStart(String.valueOf(i), 1, '0');
            String key = "key" + s;
            String value = "value" + s;
            bPlusTree.insert(key, value);
        }
//        logger.info(bPlusTree.printTree());
//        logger.info("height of tree is : {}", bPlusTree.treeHeight());
        logger.info("value is {}",bPlusTree.search("key0"));
        System.exit(0);
    }

    @Test
    public void sort() {
        String[] a = {"1"};
        Arrays.sort(a);
    }

}
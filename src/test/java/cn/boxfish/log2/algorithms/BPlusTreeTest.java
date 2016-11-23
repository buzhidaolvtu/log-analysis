package cn.boxfish.log2.algorithms;

import com.google.common.base.Strings;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by lvtu on 2016/11/22.
 */
public class BPlusTreeTest {

    @Test
    public void BPlusTree_search() throws Exception {

    }

    @Test
    public void BPlusTree_delete() throws Exception {

    }

    @Test
    public void BPlusTree_insert() throws Exception {
        BPlusTree<String, String> bPlusTree = new BPlusTree<>(3);
        for (int i = 0; i < 50; i++) {
            String s = Strings.padStart(String.valueOf(i), 3, '0');
            bPlusTree.BPlusTree_insert("key" + s, "value" + s);
        }
        System.exit(0);
    }

    @Test
    public void sort() {
        String[] a = {"1"};
        Arrays.sort(a);
    }

}
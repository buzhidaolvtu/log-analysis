package cn.boxfish.log2.algorithms;

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
        BPlusTree<String, String> bPlusTree = new BPlusTree<>(4);
        bPlusTree.BPlusTree_insert("key1","value1");
        bPlusTree.BPlusTree_insert("key2","value2");
        bPlusTree.BPlusTree_insert("key3","value3");
        bPlusTree.BPlusTree_insert("key4","value4");
        bPlusTree.BPlusTree_insert("key5","value5");
        bPlusTree.BPlusTree_insert("key6","value6");
        bPlusTree.BPlusTree_insert("key7","value7");
    }

    @Test
    public void sort(){
        String[] a = {"1"};
        Arrays.sort(a);
    }

}
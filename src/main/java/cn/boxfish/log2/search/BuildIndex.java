package cn.boxfish.log2.search;

import cn.boxfish.log2.utils.ResourceUtils;
import com.google.common.base.Verify;
import com.jwetherell.algorithms.data_structures.BTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by lvtu on 2016/11/18.
 */
public class BuildIndex {

    private File file;

    public BuildIndex(File file) {
        Verify.verifyNotNull(file);
        this.file = file;
    }

    //TODO
    private BTree<Node> indexTree = new BTree<>(50);

    public void addIndex(String key) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if(line.contains(key)){
                    //TODO
                    indexTree.add(new Node(key,line));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ResourceUtils.close(bufferedReader);
        }
    }

    static class Node implements  Comparable<Node>{
        private String key;
        private String value;

        Node(String key,String value){
            this.key = key;
            this.value = value;
        }
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public int compareTo(Node o) {
            return key.compareTo(o.key);
        }
    }
}

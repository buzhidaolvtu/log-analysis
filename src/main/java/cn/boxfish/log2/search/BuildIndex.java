package cn.boxfish.log2.search;

import cn.boxfish.log2.algorithms.BPlusTree;
import cn.boxfish.log2.utils.ResourceUtils;
import com.google.common.base.Verify;

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

    private BPlusTree<String, String> indexTree = new BPlusTree<>(50);

    public void addIndex(String key) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(key)) {
                    indexTree.insert(key, line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ResourceUtils.close(bufferedReader);
        }
    }

}

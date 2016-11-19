package cn.boxfish.log2.search;

/**
 * Created by lvtu on 2016/11/18.
 */
public interface FileIndex {
    void createIndex(String key);
    void addIndex(String key);
    void deleteIndex(String key);
}

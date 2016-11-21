package cn.boxfish.log2.algorithms;

import java.util.Arrays;
import java.util.Vector;

/**
 * Created by lvtu on 2016/11/19.
 */
public class BPlusTree<K extends Comparable<K>, V> {

    private int order;

    private Node root;

    public BPlusTree(int order) {
        this.order = order;
    }

    public void BPlusTree_search(K k) {

    }

    //删除索引是k的数据
    public void BPlusTree_delete(K k) {

    }

    public void BPlusTree_insert(K k, V v) {
        if (root == null) {
            LeafNode<K, V> leafNode = new LeafNode<K, V>(order, null);
            leafNode.add(k, v);
            root = leafNode;
        } else {
            //寻找数据节点LeafNode
            Node node = root;
            label1:
            while (node != null) {
                if (node.isLeaf()) {
                    ((LeafNode) node).add(k, v);
                    if (node.numberOfKeys() >= order) {
                        split(node);
                    }
                    return;
                }
                K greater = (K) node.getKey(node.numberOfKeys() - 1);
                if (k.compareTo(greater) > 0) {
                    node = node.getChild(node.numberOfKeys());
                    continue;
                }
                for (int i = 0; i < node.numberOfKeys(); i++) {
                    int compare = k.compareTo((K) (node.keys[i]));
                    if (compare <= 0) {
                        node = node.getChild(i);
                        continue label1;
                    }
                }
            }
        }
    }

    private void split(Node nodeToSplit) {
        int numberOfKeys = nodeToSplit.numberOfKeys();
        int medialIndex = numberOfKeys / 2;

        Node left = new Node(order, null);
        for (int i = 0; i < medialIndex; i++) {
            left.addKey(nodeToSplit.getKey(i));
        }

        Node right = new Node(order, null);
        Node parentOfNodeToSplit;
        if (nodeToSplit.parent == null) {
            parentOfNodeToSplit = new Node(order, null);
        } else {
            parentOfNodeToSplit = nodeToSplit.parent;
        }
        left.parent = parentOfNodeToSplit;
        right.parent = parentOfNodeToSplit;
        parentOfNodeToSplit.addChild(left);
        parentOfNodeToSplit.addChild(right);
        if (parentOfNodeToSplit.numberOfKeys() >= order) {
            split(parentOfNodeToSplit);
        }
    }

    private void splitLeafNode(Node nodeToSplit) {
        int numberOfKeys = nodeToSplit.numberOfKeys();
        int medialIndex = numberOfKeys / 2;

        Node left = new Node(order, null);
        for (int i = 0; i < medialIndex; i++) {
            left.addKey(nodeToSplit.getKey(i));
        }

        Node right = new Node(order, null);
        Node parentOfNodeToSplit;
        if (nodeToSplit.parent == null) {
            parentOfNodeToSplit = new Node(order, null);
        } else {
            parentOfNodeToSplit = nodeToSplit.parent;
        }
        left.parent = parentOfNodeToSplit;
        right.parent = parentOfNodeToSplit;
        parentOfNodeToSplit.addChild(left);
        parentOfNodeToSplit.addChild(right);
        if (parentOfNodeToSplit.numberOfKeys() >= order) {
            split(parentOfNodeToSplit);
        }
    }

    private static class Node<K extends Comparable<K>> {
        private Node parent;
        private K[] keys;
        protected int keySize;
        private Node[] children;
        protected int childrenSize;

        Node() {

        }

        Node(int order, Node parent) {
            this.parent = parent;
            keys = (K[]) new Comparable[order];
            keySize = 0;
            children = new Node[order + 1];
            childrenSize = 0;
        }

        public void addKey(K key) {
            keys[keySize] = key;
            keySize++;
            Arrays.sort(keys);
        }

        public void addChild(Node node) {
            children[childrenSize] = node;
            childrenSize++;
            node.parent = this;
        }

        public K getKey(int index) {
            return keys[index];
        }

        public Node getChild(int index) {
            return children[index];
        }

        public int numberOfKeys() {
            return keySize;
        }

        public int numberOfChildren() {
            return childrenSize;
        }

        public void split() {

        }

        public void combine() {

        }

        public boolean isLeaf() {
            return false;
        }
    }

    private static class LeafNode<K extends Comparable<K>, V> extends Node<K> {
        private DataElement[] keyAndValues;

        LeafNode(int order, Node parent) {
            super.parent = parent;
            keyAndValues = new DataElement[order + 1];
            keySize = 0;
            childrenSize = 0;
        }

        public void add(K k, V v) {
            for (int index = 0; index < keySize; index++) {
                if (keyAndValues[index].key.compareTo(k) == 0) {
                    keyAndValues[index].values.add(v);
                    return;
                }
            }
            keyAndValues[keySize] = new DataElement(k, v);
            keySize++;
        }

        public DataElement getDataElement(int index) {
            return keyAndValues[index];
        }

        public void addDataElement(DataElement dataElement) {
            keyAndValues[keySize] = dataElement;
            keySize++;
        }

        public K getKey(int index) {
            return (K) (keyAndValues[index].key);
        }

        public boolean isLeaf() {
            return true;
        }
    }

    private static class DataElement<K extends Comparable<K>, V> implements Comparable<K> {
        private K key;
        private Vector<V> values = new Vector<V>(100);

        DataElement(K k, V v) {
            key = k;
            values.add(v);
        }

        @Override
        public int compareTo(K o) {
            return key.compareTo(o);
        }
    }
}

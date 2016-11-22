package cn.boxfish.log2.algorithms;

import java.util.Arrays;
import java.util.Vector;

/**
 * Created by lvtu on 2016/11/19.
 * 1.每一个内部节点的索引不会相同:K1<K2<K3...Kn
 * 2.子节点的索引Ki <CK1<CK2<...<CKn< Ki+1
 * <p>
 */
public class BPlusTree<K extends Comparable<K>, V> {

    // Default to 2-3 Tree
    private int minKeySize = 1;
    private int minChildrenSize = minKeySize + 1; // 2
    private int maxKeySize = 2 * minKeySize; // 2
    private int maxChildrenSize = maxKeySize + 1; // 3

    private Node root;

    public BPlusTree() {

    }

    public BPlusTree(int order) {
        //TODO
        this.minKeySize = order;
        this.minChildrenSize = minKeySize + 1;
        this.maxKeySize = 2 * minKeySize;
        this.maxChildrenSize = maxKeySize + 1;
    }

    public Vector<V> BPlusTree_search(K k) {
        if (root == null) {
            return null;
        }

        LeafNode dataNode = findDataNode(root, k);
        return dataNode.getValues(k);
    }

    private LeafNode findDataNode(Node startNode, K k) {
        Node node = startNode;
        label1:
        while (node != null) {
            if (node.isLeaf()) {
                return ((LeafNode) node);
            }

            for (int i = 0; i < node.numberOfKeys(); i++) {
                if (k.compareTo((K) node.getKey(i)) <= 0) {
                    node = node.getChild(i);
                    continue label1;
                }
                //key比最大的值还大
                if (i == node.numberOfKeys() - 1 && k.compareTo((K) node.getKey(i)) > 0) {
                    node = node.getChild(node.numberOfKeys());
                    continue label1;
                }
            }
        }

        throw new RuntimeException("can't find data node.");
    }

    //删除索引是k的数据
    public Vector<V> BPlusTree_delete(K k) {
        if (root == null) {
            return null;
        }
        LeafNode dataNode = findDataNode(root, k);
        Vector<V> values = dataNode.remove(k);
        rebalance(dataNode);
        return values;
    }

    /**
     * @param node
     */
    private void rebalance(Node node) {
        if (node.numberOfKeys() < minKeySize) {
            Node leftSibling = getLeftSibling(node);
            if (leftSibling != null && leftSibling.numberOfKeys() > minKeySize) {
                //borrow(leftSibling);
                return;
            } else {
                Node rightSibling = getRightSibling(node);
                if (rightSibling != null && rightSibling.numberOfKeys() > minKeySize) {
                    //borrow(rightSibling);
                    return;
                }

                // Can't borrow from neighbors, try to combined with left neighbor
                if (leftSibling != null) {
                    //combine(leftSibling);
                    return;
                }
                // Can't borrow from neighbors, try to combined with right neighbor
                if (rightSibling != null) {
                    //combine(rightSibling);
                    return;
                }
            }

        }
    }

    private void combineLeafNode(LeafNode combinedNode) {
        if (combinedNode.parent == null) {
            return;
        }
        if (combinedNode.numberOfKeys() < minKeySize) {
            LeafNode leftSibling = (LeafNode) getLeftSibling(combinedNode);
            if (leftSibling != null && leftSibling.numberOfKeys() > minKeySize) {
                //borrow from left neighbor;
                DataElement dataElement = leftSibling.getDataElement(leftSibling.numberOfKeys() - 1);
                leftSibling.remove(dataElement.key);
                combinedNode.addDataElement(dataElement);
                //use the greatest key of left node as the index entry
                K greatestKey = (K) leftSibling.getGreatestKey();
                Node parent = combinedNode.parent;
                int indexOfChild = parent.indexOfChild(combinedNode);
                int indexOfKey = indexOfChild - 1;
                parent.replaceKey(indexOfKey, greatestKey);
                return;
            } else {
                LeafNode rightSibling = (LeafNode) getRightSibling(combinedNode);
                if (rightSibling != null && rightSibling.numberOfKeys() > minKeySize) {
                    //borrow from right neighbor;
                    DataElement dataElement = rightSibling.getDataElement(0);
                    rightSibling.remove(dataElement.key);
                    combinedNode.addDataElement(dataElement);
                    //use the greatest key of left node as the index entry
                    K greatestKey = (K) combinedNode.getGreatestKey();
                    Node parent = combinedNode.parent;
                    int indexOfChild = parent.indexOfChild(combinedNode);
                    int indexOfKey = indexOfChild;//因为借的兄弟不同,更新的索引位置也不相同
                    parent.replaceKey(indexOfKey, greatestKey);
                    return;
                }

                // Can't borrow from neighbors, try to combine with left neighbor
                if (leftSibling != null) {
                    Node parent = combinedNode.parent;
                    int indexOfChild = parent.indexOfChild(combinedNode);
                    parent.shiftKeys(indexOfChild - 1);
                    combinedNode.addNode(leftSibling);
                    parent.shiftChildren(indexOfChild - 1);
                    return;
                }
                // Can't borrow from neighbors, try to combine with right neighbor
                if (rightSibling != null) {
                    Node parent = combinedNode.parent;
                    int indexOfChild = parent.indexOfChild(combinedNode);
                    parent.shiftKeys(indexOfChild + 1);
                    combinedNode.addNode(rightSibling);
                    parent.shiftChildren(indexOfChild + 1);
                    return;
                }
            }


        }
    }

    private Node getRightSibling(Node node) {
        if (node.parent == null) {
            return null;
        }
        Node parent = node.parent;
        int index = parent.indexOfChild(node);
        if (index < 0) {
            return null;
        }
        return parent.getChild(index + 1);
    }

    private Node getLeftSibling(Node node) {
        if (node.parent == null) {
            return null;
        }
        Node parent = node.parent;
        int index = parent.indexOfChild(node);
        if (index < 0) {
            return null;
        }
        return parent.getChild(index - 1);
    }

    public void BPlusTree_insert(K k, V v) {
        if (root == null) {
            LeafNode<K, V> leafNode = new LeafNode<K, V>(maxKeySize, null);
            leafNode.add(k, v);
            root = leafNode;
        } else {
            //寻找数据节点LeafNode
            LeafNode dataNode = findDataNode(root, k);
            dataNode.add(k, v);
            if (dataNode.numberOfKeys() > maxKeySize) {
                splitLeafNode(dataNode);
            }
        }
    }

    /**
     * 该节点不能是数据节点,当根节点是数据节点时,也不能使用该方法分割。
     *
     * @param nodeToSplit
     */
    private void splitInternalNode(Node nodeToSplit) {
        int numberOfKeys = nodeToSplit.numberOfKeys();
        int medialIndex = numberOfKeys / 2;//TODO
        K medialKey = (K) (nodeToSplit.getKey(medialIndex));

        Node left = new Node(maxKeySize, maxChildrenSize, null);
        for (int i = 0; i < medialIndex; i++) {//// TODO: 2016/11/21 这里不能<=medialIndex?加上等号之后,无法分配孩子节点。
            left.addKey(nodeToSplit.getKey(i));
        }

        Node right = new Node(maxKeySize, maxChildrenSize, null);
        for (int i = medialIndex + 1; i < numberOfKeys; i++) {
            right.addKey(nodeToSplit.getKey(i));
        }

        Node parentOfNodeToSplit;
        if (nodeToSplit.parent == null) {
            parentOfNodeToSplit = new Node(maxKeySize, maxChildrenSize, null);
            root = parentOfNodeToSplit;
        } else {
            parentOfNodeToSplit = nodeToSplit.parent;
        }
        left.parent = parentOfNodeToSplit;
        right.parent = parentOfNodeToSplit;
        parentOfNodeToSplit.addChild(left);
        parentOfNodeToSplit.addChild(right);
        parentOfNodeToSplit.addKey(medialKey);
        if (parentOfNodeToSplit.numberOfKeys() > maxKeySize) {
            splitInternalNode(parentOfNodeToSplit);
        }
    }

    private void splitLeafNode(LeafNode nodeToSplit) {
        int numberOfKeys = nodeToSplit.numberOfKeys();
        int medialIndex = numberOfKeys / 2;
        K medialKey = (K) (nodeToSplit.getKey(medialIndex));

        LeafNode left = new LeafNode(maxKeySize, null);
        for (int i = 0; i <= medialIndex; i++) {
            left.addDataElement(nodeToSplit.getDataElement(i));
        }

        LeafNode right = new LeafNode(maxKeySize, null);
        for (int i = medialIndex + 1; i < numberOfKeys; i++) {
            right.addDataElement(nodeToSplit.getDataElement(i));
        }

        Node parentOfNodeToSplit;
        if (nodeToSplit.parent == null) {
            parentOfNodeToSplit = new Node(maxKeySize, maxChildrenSize, null);
            root = parentOfNodeToSplit;
        } else {
            parentOfNodeToSplit = nodeToSplit.parent;
        }
        left.parent = parentOfNodeToSplit;
        right.parent = parentOfNodeToSplit;
        parentOfNodeToSplit.addChild(left);
        parentOfNodeToSplit.addChild(right);
        parentOfNodeToSplit.addKey(medialKey);
        if (parentOfNodeToSplit.numberOfKeys() > maxKeySize) {
            splitInternalNode(parentOfNodeToSplit);
        }
    }

    private static class Node<K extends Comparable<K>> {
        protected Node parent;
        private K[] keys;
        protected int keysSize;
        private Node[] children;
        protected int childrenSize;

        Node() {

        }

        /**
         * 从fromIndex开始,后面的数据一次向前便宜
         * fromIndex是第一个被覆盖的元素
         *
         * @param fromIndex
         */
        public void shiftKeys(int fromIndex) {
            if (fromIndex == keysSize - 1) return;
            for (int i = fromIndex; i < keysSize - 1; i++) {
                keys[i] = keys[i + 1];
            }
            keys[keysSize - 1] = null;
            keysSize--;
        }

        public void shiftChildren(int fromIndex) {
            if (fromIndex == childrenSize - 1) return;
            for (int i = fromIndex; i < childrenSize - 1; i++) {
                children[i] = children[i + 1];
            }
            children[childrenSize - 1] = null;
            childrenSize--;
        }

        public void addNode(Node node) {
            throw new RuntimeException("todo");
        }

        Node(int maxKeySize, int maxChildrenSize, Node parent) {
            this.parent = parent;
            keys = (K[]) new Comparable[maxKeySize];
            this.keysSize = 0;
            children = new Node[maxChildrenSize];
            this.childrenSize = 0;
        }

        public K getGreatestKey() {
            return keys[keysSize - 1];
        }

        public void addKey(K key) {
            keys[keysSize] = key;
            keysSize++;
            Arrays.sort(keys);
        }

        public void addChild(Node node) {
            children[childrenSize] = node;
            childrenSize++;
            node.parent = this;
        }

        public void replaceKey(int index, K k) {
            keys[index] = k;
        }

        public K getKey(int index) {
            return keys[index];
        }

        public Node getChild(int index) {
            if (index > childrenSize) {
                return null;
            }
            return children[index];
        }

        public int numberOfKeys() {
            return keysSize;
        }

        public int numberOfChildren() {
            return childrenSize;
        }

        public boolean isLeaf() {
            return false;
        }

        public int indexOfChild(Node child) {
            for (int i = 0; i < childrenSize; i++) {
                if (children[i] == child) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static class LeafNode<K extends Comparable<K>, V> extends Node<K> {
        private DataElement[] keyAndValues;

        LeafNode(int maxKeySize, Node parent) {
            super.parent = parent;
            keyAndValues = new DataElement[maxKeySize];
            super.keysSize = 0;
            childrenSize = 0;
        }

        public void add(K k, V v) {
            for (int index = 0; index < keysSize; index++) {
                if (keyAndValues[index].key.compareTo(k) == 0) {
                    keyAndValues[index].values.add(v);
                    return;
                }
            }
            keyAndValues[keysSize] = new DataElement(k, v);
            keysSize++;
            Arrays.sort(keyAndValues);
        }

        public void addNode(LeafNode node) {
            for (int i = 0; i < node.keysSize; i++) {
                keyAndValues[keysSize + i] = node.getDataElement(i);
            }
            keysSize = keysSize + node.keysSize;
            Arrays.sort(keyAndValues);
        }

        public Vector<V> remove(K k) {
            DataElement[] newDataElements = new DataElement[keyAndValues.length];
            int index = 0;
            Vector<V> valuesTodelete = null;
            for (int i = 0; i < keysSize; i++) {
                if (keyAndValues[i].key.compareTo(k) == 0) {
                    //remove this element
                    index = i;
                    valuesTodelete = keyAndValues[i].values;
                    break;
                }
                return null;
            }
            for (int i = 0; i < index; i++) {
                newDataElements[i] = keyAndValues[i];
            }
            for (int i = index + 1; i < keyAndValues.length; i++) {
                newDataElements[i - 1] = keyAndValues[i];
            }
            keyAndValues = newDataElements;
            keysSize--;

            return valuesTodelete;
        }

        public DataElement getDataElement(int index) {
            return keyAndValues[index];
        }

        public void addDataElement(DataElement dataElement) {
            keyAndValues[keysSize] = dataElement;
            keysSize++;
            Arrays.sort(keyAndValues);
        }

        public K getKey(int index) {
            return (K) (keyAndValues[index].key);
        }

        public Vector<V> getValues(K k) {
            for (int index = 0; index < keysSize; index++) {
                if (keyAndValues[index].key.compareTo(k) == 0) {
                    return keyAndValues[index].values;
                }
            }
            return null;
        }

        public K getGreatestKey() {
            return (K) (keyAndValues[keysSize - 1].key);
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

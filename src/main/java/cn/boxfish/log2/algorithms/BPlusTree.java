package cn.boxfish.log2.algorithms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Vector;

/**
 * Created by lvtu on 2016/11/19.
 * 1.每一个内部节点的索引不会相同:K1<K2<K3...Kn
 * 2.子节点的索引Ki <CK1<CK2<...<CKn< Ki+1
 * <p>
 */
public class BPlusTree<K extends Comparable<K>, V> {

    private final static Logger logger = LoggerFactory.getLogger(BPlusTree.class);

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

        try {
            LeafNode dataNode = findDataNode(root, k);
            return dataNode.getValues(k);
        } catch (Exception e) {
            System.out.println(k);
            return null;
        }
    }

    public void BPlusTree_insert(K k, V v) {
        try {
            if (root == null) {
                LeafNode<K, V> leafNode = new LeafNode<K, V>(maxKeySize, null);
                leafNode.add(k, v);
                root = leafNode;
            } else {
                //寻找数据节点LeafNode
                LeafNode dataNode = findDataNode(root, k);
                if (dataNode.numberOfKeys() > maxKeySize) {
                    logger.error("error array index out of bound. {},{}", dataNode.getKeyAt(0), dataNode.getGreatestKey());
                }
                dataNode.add(k, v);
                if (dataNode.numberOfKeys() > maxKeySize) {
                    split(dataNode);
                }
            }
        } catch (Exception e) {
            logger.error("key:{},value:{}", k, v);
            throw e;
        }
    }

    public int treeHeight() {
        if (root == null) return 0;
        Node node = root;
        int height = 0;
        while (node != null) {
            height++;
            if (node.numberOfChildren() > 0) {
                node = node.getChildAt(0);
                continue;
            }
            break;
        }
        return height;
    }

    private LeafNode findDataNode(Node startNode, K k) {
        Node node = startNode;
        label1:
        while (node != null) {
            if (node.isLeaf()) {
                return ((LeafNode) node);
            }

            //key比最大的索引值还大
            K greatestKey = (K) node.getGreatestKey();
            if (k.compareTo(greatestKey) > 0) {
                node = node.getChildAt(node.numberOfChildren() - 1);
                continue label1;
            }

            for (int i = 0; i < node.numberOfKeys(); i++) {
                if (k.compareTo((K) node.getKeyAt(i)) <= 0) {
                    node = node.getChildAt(i);
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
        combineLeafNode(dataNode);
        return values;
    }

    /**
     * 这个方法借的是索引,与借数据的方式不一样
     *
     * @param node
     */
    private void combineInternalNode(Node node) {
        if (node.parent == null) {
            root = node.getChildAt(0);
            return;
        }

        if (node.numberOfKeys() < minKeySize) {
            Node leftSibling = getLeftSibling(node);
            if (leftSibling != null && leftSibling.numberOfKeys() > minKeySize) {
                Node parent = node.parent;
                //索引偏移
                int indexOfChild = parent.indexOfChild(node);
                K entry = (K) parent.getKeyAt(indexOfChild - 1);

                K greatestKey = (K) leftSibling.getGreatestKey();
                Node childOfLeftSibling = leftSibling.getChildAt(leftSibling.numberOfKeys());
                //更新左孩子
                childOfLeftSibling.removeKeyAt(leftSibling.numberOfKeys() - 1);
                childOfLeftSibling.removeChildAt(leftSibling.numberOfChildren() - 1);
                //更新父索引
                parent.replaceKeyAt(indexOfChild - 1, greatestKey);
                //更新自己
                node.addKey(entry);
                //把child放在什么位置
                node.insertChildAt(0, childOfLeftSibling);
                return;
            } else {
                Node rightSibling = getRightSibling(node);
                if (rightSibling != null && rightSibling.numberOfKeys() > minKeySize) {
                    Node parent = node.parent;
                    //索引偏移
                    int indexOfChild = parent.indexOfChild(node);
                    K entry = (K) parent.getKeyAt(indexOfChild);

                    K leastKey = (K) rightSibling.getKeyAt(0);
                    Node childOfRightSibling = rightSibling.getChildAt(0);
                    //更新右孩子
                    childOfRightSibling.removeKeyAt(0);
                    childOfRightSibling.removeChildAt(0);
                    //更新父索引
                    parent.replaceKeyAt(indexOfChild, leastKey);
                    //更新自己
                    node.addKey(entry);
                    //把child放在什么位置
                    node.insertChildAt(node.numberOfChildren(), childOfRightSibling);
                    return;
                }

                // Can't borrow from neighbors, try to combine with left neighbor
                if (leftSibling != null) {
                    Node parent = node.parent;
                    int indexOfChild = parent.indexOfChild(node);
                    K entry = (K) parent.getKeyAt(indexOfChild - 1);

                    parent.removeKeyAt(indexOfChild - 1);

                    leftSibling.addKey(entry);
                    leftSibling.addKeys(node.keys, node.numberOfKeys());
                    leftSibling.addChildren(node.children, node.numberOfChildren());
                    parent.shiftChildren(indexOfChild);

                    if (parent.numberOfKeys() < minKeySize) {
                        this.combineInternalNode(parent);
                    }
                    return;
                }
                // Can't borrow from neighbors, try to combine with right neighbor
                if (rightSibling != null) {
                    Node parent = node.parent;
                    int indexOfChild = parent.indexOfChild(node);
                    K entry = (K) parent.getKeyAt(indexOfChild);

                    parent.removeKeyAt(indexOfChild);

                    node.addKey(entry);
                    node.addKeys(rightSibling.keys, rightSibling.numberOfKeys());
                    node.addChildren(rightSibling.children, rightSibling.numberOfChildren());
                    parent.shiftChildren(indexOfChild + 1);

                    if (parent.numberOfKeys() < minKeySize) {
                        this.combineInternalNode(parent);
                    }
                    return;
                }
            }

        }
    }

    //这个方法借的是数据,然后更新索引
    private void combineLeafNode(LeafNode combinedNode) {
        if (combinedNode.parent == null) {
            return;
        }
        if (combinedNode.numberOfKeys() < minKeySize) {
            LeafNode leftSibling = (LeafNode) getLeftSibling(combinedNode);
            if (leftSibling != null && leftSibling.numberOfKeys() > minKeySize) {
                //borrow data from left neighbor;
                DataElement dataElement = leftSibling.getDataElement(leftSibling.numberOfKeys() - 1);
                leftSibling.remove(dataElement.key);
                combinedNode.addDataElement(dataElement);
                //use the greatest key of left node as the index entry
                K greatestKey = (K) leftSibling.getGreatestKey();
                Node parent = combinedNode.parent;
                int indexOfChild = parent.indexOfChild(combinedNode);
                int indexOfKey = indexOfChild - 1;
                parent.replaceKeyAt(indexOfKey, greatestKey);
                return;
            } else {
                LeafNode rightSibling = (LeafNode) getRightSibling(combinedNode);
                if (rightSibling != null && rightSibling.numberOfKeys() > minKeySize) {
                    //borrow data from right neighbor;
                    DataElement dataElement = rightSibling.getDataElement(0);
                    rightSibling.remove(dataElement.key);
                    combinedNode.addDataElement(dataElement);
                    //use the greatest key of left node as the index entry
                    K greatestKey = (K) combinedNode.getGreatestKey();
                    Node parent = combinedNode.parent;
                    int indexOfChild = parent.indexOfChild(combinedNode);
                    int indexOfKey = indexOfChild;//因为借的兄弟不同,更新的索引位置也不相同
                    parent.replaceKeyAt(indexOfKey, greatestKey);
                    return;
                }

                // Can't borrow from neighbors, try to combine with left neighbor
                if (leftSibling != null) {
                    Node parent = combinedNode.parent;
                    int indexOfChild = parent.indexOfChild(combinedNode);
                    parent.shiftKeys(indexOfChild - 1);
                    combinedNode.addNode(leftSibling);
                    parent.shiftChildren(indexOfChild - 1);

                    if (parent.numberOfKeys() < minKeySize) {
                        combineInternalNode(parent);
                    }
                    return;
                }
                // Can't borrow from neighbors, try to combine with right neighbor
                if (rightSibling != null) {
                    Node parent = combinedNode.parent;
                    int indexOfChild = parent.indexOfChild(combinedNode);
                    parent.shiftKeys(indexOfChild);
                    combinedNode.addNode(rightSibling);
                    parent.shiftChildren(indexOfChild + 1);

                    if (parent.numberOfKeys() < minKeySize) {
                        combineInternalNode(parent);
                    }
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
        if (index >= parent.numberOfChildren()) {
            return null;
        }
        return parent.getChildAt(index + 1);
    }

    private Node getLeftSibling(Node node) {
        if (node.parent == null) {
            return null;
        }
        Node parent = node.parent;
        int index = parent.indexOfChild(node);
        if (index <= 0) {
            return null;
        }
        return parent.getChildAt(index - 1);
    }

    /**
     * 该节点不能是数据节点,当根节点是数据节点时,也不能使用该方法分割。
     *
     * @param nodeToSplit
     */
    private void split(Node nodeToSplit) {
        int numberOfKeys = nodeToSplit.numberOfKeys();
        int numberOfChildren = nodeToSplit.numberOfChildren();
        int medialIndex = numberOfKeys / 2;
        K medialKey = (K) (nodeToSplit.getKeyAt(medialIndex));

        Node left;
        Node right;
        if (nodeToSplit.isLeaf()) {
            left = new LeafNode(maxKeySize, null);
            for (int i = 0; i <= medialIndex; i++) {
                ((LeafNode) left).addDataElement(((LeafNode) nodeToSplit).getDataElement(i));
            }

            right = new LeafNode(maxKeySize, null);
            for (int i = medialIndex + 1; i < numberOfKeys; i++) {
                ((LeafNode) right).addDataElement(((LeafNode) nodeToSplit).getDataElement(i));
            }

        } else {
            left = new Node(maxKeySize, maxChildrenSize, null);
            for (int i = 0; i < medialIndex; i++) {// TODO: 2016/11/21 这里不能<=medialIndex?加上等号之后,无法分配孩子节点。
                left.addKey(nodeToSplit.getKeyAt(i));
            }

            for (int i = 0; i <= medialIndex; i++) {
                left.addChild(nodeToSplit.getChildAt(0));
            }

            right = new Node(maxKeySize, maxChildrenSize, null);
            for (int i = medialIndex + 1; i < numberOfKeys; i++) {
                right.addKey(nodeToSplit.getKeyAt(i));
            }

            for (int i = medialIndex + 1; i < numberOfChildren; i++) {
                right.addChild(nodeToSplit.getChildAt(i));
            }

        }

        Node parent;
        if (nodeToSplit.parent == null) {
            parent = new Node(maxKeySize, maxChildrenSize, null);

            left.parent = parent;
            right.parent = parent;
            parent.addChild(left);
            parent.addChild(right);
            parent.addKey(medialKey);

            root = parent;
        } else {
            parent = nodeToSplit.parent;

            int indexOfChild = parent.indexOfChild(nodeToSplit);
            parent.insertKeyAt(indexOfChild, medialKey);
            //把一个子节点分割成两个子节点
            parent.insertChildAt(indexOfChild, left);
            left.parent = parent;
            parent.replaceChildAt(indexOfChild + 1, right);
            right.parent = parent;

            if (parent.numberOfKeys() > maxKeySize) {
                split(parent);
            }
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

        Node(int maxKeySize, int maxChildrenSize, Node parent) {
            this.parent = parent;
            keys = (K[]) new Comparable[maxKeySize + 1];
            this.keysSize = 0;
            children = new Node[maxChildrenSize + 1];
            this.childrenSize = 0;
        }

        /**
         * 从fromIndex开始,后面的数据一次向前便宜
         * fromIndex是第一个被覆盖的元素
         *
         * @param fromIndex
         */
        public void shiftKeys(int fromIndex) {
            if (fromIndex == keysSize - 1) {
                keys[fromIndex] = null;
                keysSize--;
                return;
            }
            for (int i = fromIndex; i < keysSize - 1; i++) {
                keys[i] = keys[i + 1];
            }
            keys[keysSize - 1] = null;
            keysSize--;
        }

        public void removeKeyAt(int index) {
            shiftKeys(index);
        }

        public void removeChildAt(int index) {
            shiftChildren(index);
        }

        public void shiftChildren(int fromIndex) {
            if (fromIndex == childrenSize - 1) {
                children[fromIndex] = null;
                childrenSize--;
                return;
            }
            for (int i = fromIndex; i < childrenSize - 1; i++) {
                children[i] = children[i + 1];
            }
            children[childrenSize - 1] = null;
            childrenSize--;
        }

        public void addKeys(K[] keys, int keysSize) {
            for (int i = 0; i < keysSize; i++) {
                this.keys[this.keysSize + i] = keys[i];
            }
            this.keysSize += keysSize;
        }

        public void addChildren(Node[] children, int childrenSize) {
            for (int i = 0; i < childrenSize; i++) {
                this.children[this.childrenSize + i] = children[i];
            }
            this.childrenSize += childrenSize;
        }

        public K getGreatestKey() {
            return keys[keysSize - 1];
        }

        public void addKey(K key) {
            keys[keysSize] = key;
            keysSize++;
            Arrays.sort(keys, 0, keysSize);
        }

        public void addChild(Node node) {
            children[childrenSize] = node;
            childrenSize++;
            node.parent = this;
        }

        public void insertChildAt(int index, Node node) {
            for (int i = childrenSize; i > index; i--) {
                children[i] = children[i - 1];
            }
            children[index] = node;
            childrenSize++;
        }

        public void insertKeyAt(int index, K k) {
            for (int i = keysSize; i > index; i--) {
                keys[i] = keys[i - 1];
            }
            keys[index] = k;
            keysSize++;
        }

        public void replaceChildAt(int index, Node node) {
            children[index] = node;
        }

        public void replaceKeyAt(int index, K k) {
            keys[index] = k;
        }

        public K getKeyAt(int index) {
            return keys[index];
        }

        public Node getChildAt(int index) {
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
            keyAndValues = new DataElement[maxKeySize + 1];
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
            Arrays.sort(keyAndValues, 0, keysSize);
        }

        public void addNode(LeafNode node) {
            for (int i = 0; i < node.keysSize; i++) {
                keyAndValues[keysSize + i] = node.getDataElement(i);
            }
            keysSize = keysSize + node.keysSize;
            Arrays.sort(keyAndValues, 0, keysSize);
        }

        public Vector<V> remove(K k) {
            int index = 0;
            Vector<V> valuesToDelete = null;
            for (int i = 0; i < keysSize; i++) {
                if (keyAndValues[i].key.compareTo(k) == 0) {
                    //remove this element
                    index = i;
                    valuesToDelete = keyAndValues[i].values;
                    break;
                }
                return null;
            }

            for (int i = index; i < keyAndValues.length - 1; i++) {
                keyAndValues[i] = keyAndValues[i + 1];
            }
            keyAndValues[keyAndValues.length - 1] = null;
            keysSize--;

            return valuesToDelete;
        }

        public DataElement getDataElement(int index) {
            return keyAndValues[index];
        }

        public void addDataElement(DataElement dataElement) {
            keyAndValues[keysSize] = dataElement;
            keysSize++;
            Arrays.sort(keyAndValues, 0, keysSize);
        }

        public K getKeyAt(int index) {
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

    private static class DataElement<K extends Comparable<K>, V> implements Comparable<DataElement> {
        private K key;
        private Vector<V> values = new Vector<V>(100);

        DataElement(K k, V v) {
            key = k;
            values.add(v);
        }

        @Override
        public int compareTo(DataElement o) {
            return key.compareTo((K) o.key);
        }
    }
}

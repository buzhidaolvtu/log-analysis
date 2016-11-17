package cn.boxfish.log2.analysis;

import org.junit.Test;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by lvtu on 2016/11/12.
 */
public class AnalysisTest {

    @Test
    public void treeHtml() {

    }

    @Test
    public void test() {
        try {
            DefaultMutableTreeNode n5 = new DefaultMutableTreeNode("n5");
            DefaultMutableTreeNode n4 = new DefaultMutableTreeNode("n4");
            DefaultMutableTreeNode n3 = new DefaultMutableTreeNode("n3");
            DefaultMutableTreeNode n2 = new DefaultMutableTreeNode("n2");
            DefaultMutableTreeNode n1 = new DefaultMutableTreeNode("n1");
            n5.add(n1);
            n5.add(n4);
            n4.add(n2);
            n4.add(n3);
//            String html = traversal(n5);
//            FileWriter fileWriter = new FileWriter("/Users/lvtu/workspace/log2/src/main/resources/web/test.html");
//            fileWriter.write(html);
//            fileWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
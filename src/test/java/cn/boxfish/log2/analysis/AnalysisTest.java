package cn.boxfish.log2.analysis;

import org.junit.Test;

import java.io.FileWriter;

import static org.junit.Assert.*;

/**
 * Created by lvtu on 2016/11/12.
 */
public class AnalysisTest {

    @Test
    public void treeHtml() {
        try {
            String html = Analysis.treeHtml();
            FileWriter fileWriter = new FileWriter("/Users/lvtu/workspace/log2/src/test/resources/tree.html");
            fileWriter.write(html);
            fileWriter.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
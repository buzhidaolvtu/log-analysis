package cn.boxfish.log2.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lvtu on 2016/11/11.
 */
public class Analysis {

    private final static Logger logger = LoggerFactory.getLogger(Analysis.class);

    private final static Pattern methodStartPattern = Pattern.compile("params=");
    private final static Pattern packagePattern = Pattern.compile("\\w+\\.\\w+\\.\\w+\\.\\w+");
    private final static Pattern methodEndPattern = Pattern.compile("return=");

    private final static Stack<DefaultMutableTreeNode> stack = new Stack<>();

    public static List<MethodNode> parse() {
        DefaultMutableTreeNode root = parseAst();
        logRoot(root);
        return preorderToList(root);

    }

    private static DefaultMutableTreeNode parseAst() {
        try {
            MethodNode rootMethodNode = new MethodNode("root");
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootMethodNode);
            stack.push(root);

            URL resource = Analysis.class.getClassLoader().getResource("log2.log");
            Path path = Paths.get(resource.toURI());
            FileReader fileReader = new FileReader(path.toFile());
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                Matcher methodStartMatcher = methodStartPattern.matcher(line);
                if (methodStartMatcher.find()) {
                    String pkg = getPkg(line);
                    String logStartTime = getlogTime(line);
                    MethodNode methodNode = new MethodNode(pkg, logStartTime);
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(methodNode);
                    stack.peek().add(node);
                    stack.push(node);
                }
                Matcher methodEndMatcher = methodEndPattern.matcher(line);
                if (methodEndMatcher.find()) {
                    String rtn = methodEndMatcher.group();
                    DefaultMutableTreeNode pop = stack.pop();
                    if (pop.isRoot()) {
                        throw new RuntimeException("can't end root method.");
                    }
                    MethodNode methodNode = ((MethodNode) pop.getUserObject());
                    methodNode.labelEnd(rtn);
                    methodNode.setCost(Long.valueOf(getCostTime(line)));
                    methodNode.setLogEndTime(getlogTime(line));
                }
            }
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String treeHtml() {
        StringBuffer sb = new StringBuffer();
        DefaultMutableTreeNode root = parseAst();
        Enumeration enumeration = root.postorderEnumeration();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) enumeration.nextElement();
            MethodNode methodNode = ((MethodNode) n.getUserObject());
            if (n.isLeaf()) {
                sb.append("\n<li class=\"dd-item\">\n<div class=\"dd-handle\">" + methodNode.getPkg() + "\n</div>\n</li>");
            } else if (!n.isRoot()) {
                StringBuffer newSb = new StringBuffer();
                newSb.append("\n<li class=\"dd-item\">\n<div class=\"dd-handle\">"+methodNode.getPkg()+"\n</div>\n<ol class=\"dd-list\">\n" + sb.toString() + "\n</ol>\n</li>");
                sb = newSb;
            }else if(n.isRoot()){
                StringBuffer newSb = new StringBuffer();
                newSb.append("<ol class=\"dd-list\">\n" + sb.toString() + "\n</ol>");
                sb = newSb;
            }

        }
        return sb.toString();
    }


    private static String getPkg(String line) {
        Matcher pkgMatcher = packagePattern.matcher(line);
        while (pkgMatcher.find()) {
            String pkg = pkgMatcher.group();
            if (pkg.contains("echoslam")) {
                return pkg;
            }
        }
        logger.error("no package:{}", line);
        return "";
    }

    private final static Pattern logTimePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}");

    private static String getlogTime(String line) {
        Matcher matcher = logTimePattern.matcher(line);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new RuntimeException("no log time");
    }

    private final static Pattern costPattern = Pattern.compile("cost=(\\d+)");

    private static String getCostTime(String line) {
        Matcher matcher = costPattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("no log time");
    }

    public static void main(String[] args) {
        logger.info(treeHtml());
    }


    private static void logRoot(DefaultMutableTreeNode node) {
        Enumeration enumeration = node.preorderEnumeration();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) enumeration.nextElement();
            logger.info(line(n.getLevel()) + ((MethodNode) n.getUserObject()).toString());
        }
    }

    private static List<MethodNode> preorderToList(DefaultMutableTreeNode node) {
        List<MethodNode> list = new ArrayList<>();
        Enumeration enumeration = node.preorderEnumeration();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) enumeration.nextElement();
            list.add((MethodNode) n.getUserObject());
        }
        return list;
    }

    private static String line(int n) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < n; i++) {
            sb.append("-");
        }
        return sb.toString();
    }
}

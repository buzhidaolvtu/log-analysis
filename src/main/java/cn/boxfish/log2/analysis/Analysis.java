package cn.boxfish.log2.analysis;

import cn.boxfish.log2.storage.log.LogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by lvtu on 2016/11/11.
 */
@Component
public class Analysis {

    private final static Logger logger = LoggerFactory.getLogger(Analysis.class);

    private final static Pattern methodStartPattern = Pattern.compile("params=");
    private final static Pattern methodEndPattern = Pattern.compile("return=");
    private final static Pattern packagePattern = Pattern.compile("\\w+\\.\\w+\\.\\w+\\.\\w+");

    private final static Stack<DefaultMutableTreeNode> stack = new Stack<>();

    @Autowired
    private MongoOperations mongoOperations;

    private List<String> getDataFromDb() {
        List<LogRecord> all = mongoOperations.findAll(LogRecord.class);
        return all.stream().map(logRecord -> logRecord.getLine()).collect(Collectors.toList());
    }

    public List<MethodNode> parse() {
        DefaultMutableTreeNode root = parseAst(getDataFromDb());
        logRoot(root);
        return preorderToList(root);
    }

    private static DefaultMutableTreeNode parseAst(List<String> lines) {
        try {
            MethodNode rootMethodNode = new MethodNode("root");
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootMethodNode);
            stack.push(root);

            lines.stream().forEach(line -> {
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
            });
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static String getPkg(String line) {
        Matcher pkgMatcher = packagePattern.matcher(line);
        while (pkgMatcher.find()) {
            String pkg = pkgMatcher.group();
            if (pkg.contains("c.b.e")) {
                continue;
            }
            return pkg;
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

    public String treeHtml() {
        StringBuffer sb = new StringBuffer();
        DefaultMutableTreeNode root = parseAst(getDataFromDb());
        Enumeration children = root.children();
        if (children == null || !children.hasMoreElements()) {
            return "";
        } else {
            sb.append("<ol class=\"dd-list\">");
            while (children.hasMoreElements()) {
                sb.append(traversal((DefaultMutableTreeNode) children.nextElement()));
            }
            sb.append("</ol>");
        }

        return sb.toString();
    }

    private static String traversal(DefaultMutableTreeNode node) {
        Enumeration children = node.children();
        MethodNode methodNode = (MethodNode) node.getUserObject();
        if (children == null || !children.hasMoreElements()) {
            return ("\n<li class=\"dd-item\">\n<div class=\"dd-handle\">" + methodNode.getPkg() + "\n</div>\n</li>\n");
        } else {
            StringBuffer containerSb = new StringBuffer();
            containerSb.append("\n<li class=\"dd-item\">\n<div class=\"dd-handle\">" + methodNode.getPkg() + "\n</div>\n");
            containerSb.append("<ol>\n");
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
                containerSb.append(traversal(child));
            }
            containerSb.append("\n</ol>\n");
            containerSb.append("\n</li>");
            return containerSb.toString();
        }
    }

    public static void main(String[] args) {
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
            String html = traversal(n5);
            FileWriter fileWriter = new FileWriter("/Users/lvtu/workspace/log2/src/main/resources/web/test.html");
            fileWriter.write(html);
            fileWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package cn.boxfish.log2.controller;

import cn.boxfish.log2.analysis.Analysis;
import cn.boxfish.log2.search.BuildIndex;
import cn.boxfish.log2.service.FileNameServiceImpl;
import cn.boxfish.log2.service.directory.Directory;
import cn.boxfish.log2.storage.LabelAndStoreLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lvtu on 2016/11/11.
 */
@RestController
@CrossOrigin
public class TimelineController {

    private final static String dir = "/Users/lvtu/Desktop/temp/earthshaker_log";

    @Autowired
    private FileNameServiceImpl fileNameService;

    @Autowired
    private Analysis analysis;

    @Autowired
    private LabelAndStoreLog labelAndStoreLog;

    private BuildIndex buildIndex;

    @RequestMapping(value = "/tree")
    public String tree(String filename, String tId) {
        String fullpathName = dir + "/" + filename;
        String fileIdAsCollectionName = fileNameService.fileId(fullpathName);
        return analysis.treeHtml(fileIdAsCollectionName,tId);
    }

    @RequestMapping(value = "/buildIndex")
    public String buildIndex(String filename) {
        String fullpathName = dir + "/" + filename;
        File file = new File(fullpathName);
        buildIndex= new BuildIndex(file);
        buildIndex.addIndex("userId");
        return "success";
    }

    @RequestMapping(value = "/directory")
    public List<String> directory() {
        List<String> stringList = Directory.traversalDir(dir);
        return stringList.stream().filter(filename -> filename.endsWith("gz")).collect(Collectors.toList());
    }

}

package cn.boxfish.log2.controller;

import cn.boxfish.log2.analysis.Analysis;
import cn.boxfish.log2.storage.LogPipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lvtu on 2016/11/11.
 */
@RestController
@CrossOrigin
public class TimelineController {

    @Autowired
    private Analysis analysis;

    @Autowired
    private LogPipeline logPipeline;

    @RequestMapping(value = "/timeline")
    public Object analyzeTimeline(){
        return analysis.parse("14792058368991761");
    }

    @RequestMapping(value = "/tree")
    public String tree(){
        return analysis.treeHtml("14792058368991761");
    }

    @RequestMapping(value = "/pipeline")
    public String store(){
        logPipeline.transform(null);
        return "success";
    }

}

package cn.boxfish.log2.controller;

import cn.boxfish.log2.analysis.Analysis;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lvtu on 2016/11/11.
 */
@RestController
@CrossOrigin
public class TimelineController {
    @RequestMapping(value = "/timeline")
    public Object analyzeTimeline(){
        return Analysis.parse();
    }

    @RequestMapping(value = "/tree")
    public String tree(){
        return Analysis.treeHtml();
    }

}

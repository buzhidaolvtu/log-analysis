package cn.boxfish.log2.storage;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by lvtu on 2016/11/16.
 */
public class LogPipelineTest {
    @Test
    public void get_tId() throws Exception {
        System.out.println(LogPipeline.get_tId("- params=[int echoslam.service.ActivityAwardConfigService.clearConsumedSum(HOUR)],t_id=14786801752850025,key=1479146400008"));
    }

    @Test
    public void transform() throws Exception {

    }

}
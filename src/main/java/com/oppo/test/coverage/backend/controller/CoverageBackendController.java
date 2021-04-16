package com.oppo.test.coverage.backend.controller;

import com.alibaba.fastjson.JSON;
import com.oppo.test.coverage.backend.model.entity.ApplicationCodeInfo;
import com.oppo.test.coverage.backend.model.entity.Data;
import com.oppo.test.coverage.backend.service.CoverageBackendService;
import esa.restlight.spring.shaded.org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;


/**
 * @author 80264236
 */
@RestController
public class CoverageBackendController {

    private static final Logger logger = LoggerFactory.getLogger(CoverageBackendController.class);

    @Resource
    CoverageBackendService coverageBackendService;

    @PostMapping("/startcoveragetask")
    public Data startCoverageTask(@RequestBody ApplicationCodeInfo applicationCodeInfo) {
        logger.info("receive task : {}", JSON.toJSONString(applicationCodeInfo));
        coverageBackendService.startCoverageTask(applicationCodeInfo);
        return new Data();
    }

    @GetMapping("/stopcoveragetask")
    public Data stopTimerTask(@RequestParam(name = "taskID") Long taskId,
                              @RequestParam(name = "appCode") String appCode) {
        logger.info("user stop timer task : {} , {}", taskId, appCode);
        return coverageBackendService.stopTimerTask(taskId, appCode);
    }


}

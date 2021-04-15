package com.oppo.test.coverage.backend.service.impl;

import com.oppo.test.coverage.backend.biz.TaskBiz;
import com.oppo.test.coverage.backend.model.entity.ApplicationCodeInfo;
import com.oppo.test.coverage.backend.model.entity.Data;
import com.oppo.test.coverage.backend.service.CoverageBackendService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author 80264236
 * @date 2021/4/8 17:42
 */
@Service
public class CoverageBackendServiceImpl implements CoverageBackendService {

    @Resource
    TaskBiz taskBiz;

    @Override
    public Data startCoverageTask(ApplicationCodeInfo applicationCodeInfo) {
        return taskBiz.addTaskToQueue(applicationCodeInfo);
    }

    @Override
    public Data stopTimerTask(Long taskId, String appCode) {
        return taskBiz.stopTimerTask(taskId, appCode);
    }
}

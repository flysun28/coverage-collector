package com.oppo.test.coverage.backend.service;

import com.oppo.test.coverage.backend.model.entity.ApplicationCodeInfo;
import com.oppo.test.coverage.backend.model.entity.Data;

import java.util.concurrent.BlockingQueue;

/**
 * @author 80264236
 * @date 2021/4/8 17:40
 */
public interface CoverageBackendService {

    /**
     * 开始执行覆盖率采集任务
     *
     * @param applicationCodeInfo : 覆盖率信息
     * @return : 任务启动结果
     */
    Data startCoverageTask(ApplicationCodeInfo applicationCodeInfo);

    /**
     * 停止轮询任务
     *
     * @param taskId  : 停止任务id
     * @param appCode : 应用名
     * @return : 停止结果
     */
    Data stopTimerTask(Long taskId, String appCode);

    /**
     * 获取队列
     *
     * @return : 阻塞队列
     */
    BlockingQueue<ApplicationCodeInfo> appInfoQueue();

}

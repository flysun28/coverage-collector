package com.oppo.test.coverage.backend.biz;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.oppo.test.coverage.backend.model.constant.ErrorEnum;
import com.oppo.test.coverage.backend.model.entity.ApplicationCodeInfo;
import com.oppo.test.coverage.backend.model.entity.Data;
import com.oppo.test.coverage.backend.util.GitUtil;
import com.oppo.test.coverage.backend.util.file.FolderFileScanner;
import com.oppo.test.coverage.backend.util.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.*;

/**
 * @author 80264236
 * @date 2021/4/8 17:27
 */
@Service
public class TaskBiz {

    TaskBiz() {
        startTakeThread();
    }

    private static final Logger logger = LoggerFactory.getLogger(TaskBiz.class);

    @Resource
    TimerTaskBiz timerTaskBiz;

    @Resource
    FolderFileScanner folderFileScanner;

    @Resource
    HttpUtils httpUtils;

    @Resource
    private ExecutorService cacheThreadPool;

    private static final int MAX_CASE_SIZE = 100;

    private BlockingQueue<ApplicationCodeInfo> taskQueue = new LinkedBlockingQueue<>(MAX_CASE_SIZE);

    private ThreadPoolExecutor pool = new ThreadPoolExecutor(1,
            2,
            10L,
            TimeUnit.MINUTES,
            new LinkedBlockingQueue<Runnable>(512),
            new ThreadFactoryBuilder().setNameFormat("task-biz-%d").build());


    public Data addTaskToQueue(ApplicationCodeInfo applicationCodeInfo) {

        if (!applicationCodeInfo.enableCheck()) {
            logger.error("缺少必要数据 : {} , {}", applicationCodeInfo.getId(), applicationCodeInfo.getApplicationID());
            return new Data(-1, "必要信息为空！");
        }

        taskQueue.add(applicationCodeInfo);

        return new Data(200, "success");
    }


    private void startTakeThread() {
        pool.submit(this::startTakeQueue);
    }


    private void startTakeQueue() {
        logger.info("start take");
        while (true) {
            ApplicationCodeInfo applicationCodeInfo;
            try {
                applicationCodeInfo = taskQueue.take();
                startCoverageTask(applicationCodeInfo);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("take exception {}", e.getMessage());
            }
        }
    }

    /**
     * 启动覆盖率任务
     */
    private void startCoverageTask(ApplicationCodeInfo applicationCodeInfo) {

        logger.info("任务开始 : {}, {}", applicationCodeInfo.getId(), applicationCodeInfo.getApplicationID());

        //将相关文件从OCS下载到本地,分支统计需要下载历史数据
//        String projectName = GitUtil.getLastUrlString(applicationCodeInfo.getGitPath());
//        folderFileScanner.fileDownLoad(projectName, applicationCodeInfo.getId());
//        if (applicationCodeInfo.getIsBranchTask() != 0) {
//            folderFileScanner.fileDownLoad(projectName, applicationCodeInfo.getBranchTaskID());
//        }

        logger.info("文件下载完毕 : {} ,{}", applicationCodeInfo.getId(), applicationCodeInfo.getApplicationID());

        ReportGenerateTask task = new ReportGenerateTask(applicationCodeInfo);

//        if (applicationCodeInfo.getIsTimerTask() == 1) {
//            timerTaskBiz.addTimerTask(task, applicationCodeInfo.getTimerInterval());
//            logger.info("add timer task : {}", applicationCodeInfo.getId());
//            return;
//        }
//
//        logger.info("once task : {}", applicationCodeInfo.getId());
//
//        cacheThreadPool.submit(task);
    }

    /**
     * 结束覆盖率任务执行
     */
    public void endCoverageTask(Long taskId, ErrorEnum errorEnum, String projectName) {

        //轮询执行完成,不停止
        if (timerTaskBiz.isTimerTask(taskId) && errorEnum == null) {
            folderFileScanner.reportUpload(projectName, taskId);
            logger.info("timer task continue : {}", taskId);
            return;
        }

        //轮询中止,异常停止
        if (timerTaskBiz.isTimerTask(taskId) && errorEnum != null) {
            logger.error("timer task error and stop : {} , {}", taskId, errorEnum.getErrorMsg());
            timerTaskBiz.stopTimerTask(taskId, errorEnum);
        }

        //非轮询,正常停止
        if (!timerTaskBiz.isTimerTask(taskId) && errorEnum == null) {
            logger.info("finished task : {}", taskId);
        }

        //非轮询,异常停止
        if (!timerTaskBiz.isTimerTask(taskId) && errorEnum != null) {
            logger.warn("task error : {} , {}", taskId, errorEnum.getErrorMsg());
            httpUtils.sendErrorMsg(taskId, errorEnum.getErrorMsg());
        }

        folderFileScanner.fileUpload(projectName, taskId);
    }

    public Data stopTimerTask(Long taskId, String appCode) {
        timerTaskBiz.stopTimerTask(taskId, null);
        folderFileScanner.fileUpload(appCode, taskId);
        return new Data(200, "success");
    }


}

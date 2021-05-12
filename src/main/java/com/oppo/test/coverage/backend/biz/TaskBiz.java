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
import java.io.File;
import java.util.Map;
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

    /**
     * 任务缓冲队列,逐个任务启动
     */
    private BlockingQueue<ApplicationCodeInfo> taskQueue = new LinkedBlockingQueue<>(MAX_CASE_SIZE);

    private ThreadPoolExecutor pool = new ThreadPoolExecutor(1,
            2,
            10L,
            TimeUnit.MINUTES,
            new LinkedBlockingQueue<Runnable>(512),
            new ThreadFactoryBuilder().setNameFormat("task-biz-%d").build());

    private Map<String, Integer> appCodeCount = new ConcurrentHashMap<>(256);

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
        ApplicationCodeInfo applicationCodeInfo;
        while (true) {
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
        String projectName = GitUtil.getLastUrlString(applicationCodeInfo.getGitPath());
        folderFileScanner.fileDownLoad(projectName, applicationCodeInfo.getId());
        if (applicationCodeInfo.getIsBranchTask() != 0) {
            folderFileScanner.fileDownLoad(projectName, applicationCodeInfo.getBranchTaskID());
        }

        logger.info("文件下载完毕 : {} ,{}", applicationCodeInfo.getId(), applicationCodeInfo.getApplicationID());

        ReportGenerateTask task = new ReportGenerateTask(applicationCodeInfo);

        addAppCodeCountMap(applicationCodeInfo.getApplicationID());

        if (applicationCodeInfo.getIsTimerTask() == 1) {
            timerTaskBiz.addTimerTask(task, applicationCodeInfo.getTimerInterval());
            logger.info("add timer task : {}", applicationCodeInfo.getId());
            return;
        }

        logger.info("once task : {}", applicationCodeInfo.getId());

        cacheThreadPool.submit(task);
    }

    /**
     * 结束覆盖率任务执行
     */
    public void endCoverageTask(Long taskId, ErrorEnum errorEnum, String projectName, String appCode, int isBranchTask , File downloadZipFile) {

        //轮询执行完成,不停止
        if (timerTaskBiz.isTimerTask(taskId) && errorEnum == null) {
            folderFileScanner.reportUpload(projectName, taskId);
            if (isBranchTask == 1) {
                folderFileScanner.branchReportUpload(projectName, taskId);
            }
            logger.info("timer task continue : {}", taskId);
            return;
        }

        //非轮询,正常停止(在轮询前处理,否则停止轮询后又成为了非轮询任务)
        if (!timerTaskBiz.isTimerTask(taskId) && errorEnum == null) {
            logger.info("finished task : {}", taskId);
        }

        //非轮询,异常停止
        if (!timerTaskBiz.isTimerTask(taskId) && errorEnum != null) {
            logger.warn("task error : {} , {}", taskId, errorEnum.getErrorMsg());
            httpUtils.sendErrorMsg(taskId, errorEnum.getErrorMsg());
        }

        //轮询中止,异常停止
        if (timerTaskBiz.isTimerTask(taskId) && errorEnum != null) {
            logger.error("timer task error and stop : {} , {}", taskId, errorEnum.getErrorMsg());
            timerTaskBiz.stopTimerTask(taskId, errorEnum, appCode);
        }

        //上传分支覆盖率的报告
        if (isBranchTask == 1) {
            folderFileScanner.branchReportUpload(projectName, taskId);
        }

        //轮询任务结束,并且没有该应用的轮询任务存在了
        if (!timerTaskBiz.stillTimerTask(appCode) && removeAppCodeAndCheckAppFinish(appCode)) {
            logger.info("app task finished and file upload : {} , {}", taskId, appCode);
            folderFileScanner.fileUpload(projectName, taskId);
        }

        downloadZipFile.delete();
    }

    public Data stopTimerTask(Long taskId, String appCode) {
        timerTaskBiz.stopTimerTask(taskId, null, appCode);
        folderFileScanner.fileUpload(appCode, taskId);
        return new Data(200, "success");
    }


    private void addAppCodeCountMap(String appCode) {
        if (appCodeCount.get(appCode) == null) {
            appCodeCount.put(appCode, 1);
            return;
        }
        appCodeCount.put(appCode, appCodeCount.get(appCode) + 1);
    }

    private synchronized boolean removeAppCodeAndCheckAppFinish(String appCode) {

        if (appCodeCount.get(appCode) <= 1) {
            appCodeCount.remove(appCode);
            return true;
        }

        appCodeCount.put(appCode, appCodeCount.get(appCode) - 1);
        return false;
    }

    public BlockingQueue<ApplicationCodeInfo> getTaskQueue() {
        return taskQueue;
    }
}

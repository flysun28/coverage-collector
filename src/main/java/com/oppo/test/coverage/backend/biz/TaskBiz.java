package com.oppo.test.coverage.backend.biz;

import com.alibaba.fastjson.JSON;
import com.oppo.test.coverage.backend.config.HeraclesConfig;
import com.oppo.test.coverage.backend.model.constant.Constant;
import com.oppo.test.coverage.backend.model.constant.ErrorEnum;
import com.oppo.test.coverage.backend.model.entity.ApplicationCodeInfo;
import com.oppo.test.coverage.backend.model.entity.CoverageData;
import com.oppo.test.coverage.backend.model.entity.Data;
import com.oppo.test.coverage.backend.util.GitUtil;
import com.oppo.test.coverage.backend.util.SystemConfig;
import com.oppo.test.coverage.backend.util.file.FolderFileScanner;
import com.oppo.test.coverage.backend.util.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 80264236
 * @date 2021/4/8 17:27
 */
@Service
public class TaskBiz {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskBiz.class);

    @Resource
    TimerTaskBiz timerTaskBiz;

    @Resource
    FolderFileScanner folderFileScanner;

    @Resource
    HttpUtils httpUtils;

    @Resource
    SystemConfig systemConfig;

    @Resource
    private ExecutorService cacheThreadPool;

    @Value("${flag.result.mock}")
    private boolean resultMockFlag;

    @Autowired
    HeraclesConfig heraclesConfig;

    private ThreadPoolExecutor taskExecutor;
    private Map<String, Integer> appCodeCount;

    @PostConstruct
    public void init() {
        appCodeCount = new ConcurrentHashMap<>(256);
        taskExecutor = new ThreadPoolExecutor(heraclesConfig.getCoverageTaskCorePoolSize(),
                heraclesConfig.getCoverageTaskMaxPoolSize(),
                Constant.THREAD_POOL_KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(heraclesConfig.getCoverageTaskQueueSize()),
                new CustomizableThreadFactory("coverage-task-threadExecutor"),
                new ThreadPoolExecutor.AbortPolicy());

        // initDir
        initFileDir(systemConfig.getReportBasePath());
        initFileDir(systemConfig.getCodePath());
        initFileDir(systemConfig.getProjectCovPath());
    }

    private void initFileDir(String path) {
        File fileDir = new File(path);
        if (fileDir.exists()) {
            LOGGER.info("create dir:{} already exists.", path);
            return;
        }

        boolean created = fileDir.mkdirs();
        if (created) {
            LOGGER.warn("create dir:{} success", path);
        } else {
            LOGGER.error("create dir:{} fail.", path);
            throw new RuntimeException("create dir:" + path + " fail");
        }
    }

    public Data<String> addTaskToQueue(ApplicationCodeInfo applicationCodeInfo) {
        if (!applicationCodeInfo.enableCheck()) {
            LOGGER.error("缺少必要数据 : {} , {}", applicationCodeInfo.getId(), applicationCodeInfo.getApplicationID());
            return new Data<>(ErrorEnum.PARAM_ERROR.getErrorCode(), ErrorEnum.PARAM_ERROR.getErrorMsg());
        }

        try {
            taskExecutor.submit(() -> startCoverageTask(applicationCodeInfo));
        } catch (Exception e) {
            LOGGER.error("addTaskToQueue fail, applicationCodeInfo:{}.", applicationCodeInfo, e);
            return new Data<>(ErrorEnum.INTERNAL_ERROR.getErrorCode(), ErrorEnum.INTERNAL_ERROR.getErrorMsg());
        }
        return new Data<>(ErrorEnum.SUCCESS.getErrorCode(), ErrorEnum.SUCCESS.getErrorMsg());
    }

    /**
     * 启动覆盖率任务
     *
     * @param applicationCodeInfo 应用信息
     */
    private void startCoverageTask(ApplicationCodeInfo applicationCodeInfo) {
        try {
            if (resultMockFlag) {
                mockResult(applicationCodeInfo);
                return;
            }

            LOGGER.info("任务开始 : {}, {}", applicationCodeInfo.getId(), applicationCodeInfo.getApplicationID());
            //将相关文件从OCS下载到本地,分支统计需要下载历史数据
            String projectName = GitUtil.getLastUrlString(applicationCodeInfo.getGitPath());
            folderFileScanner.fileDownLoad(projectName, applicationCodeInfo.getId());
            LOGGER.info("文件下载完毕 : {} ,{}", applicationCodeInfo.getId(), applicationCodeInfo.getApplicationID());

            ReportGenerateTask task = new ReportGenerateTask(applicationCodeInfo);
            addAppCodeCountMap(applicationCodeInfo.getApplicationID());
            if (applicationCodeInfo.getIsTimerTask() == 1) {
                timerTaskBiz.addTimerTask(task, applicationCodeInfo.getTimerInterval());
                LOGGER.info("add timer task : {}", applicationCodeInfo.getId());
                return;
            }
            LOGGER.info("once task : {}", applicationCodeInfo.getId());
            cacheThreadPool.submit(task);
        } catch (Exception e) {
            LOGGER.info("startCoverageTask fail : {}", applicationCodeInfo, e);
        }
    }

    /**
     * 结束覆盖率任务执行
     *
     * @param taskId 任务id
     * @param errorEnum 错误码
     * @param projectName 项目名称
     * @param appCode 应用code
     */
    public void endCoverageTask(Long taskId, ErrorEnum errorEnum, String projectName, String appCode) {
        //轮询执行完成,不停止
        if (timerTaskBiz.isTimerTask(taskId) && errorEnum == null) {
            LOGGER.info("timer task continue : {}", taskId);
            return;
        }

        //非轮询,正常停止(在轮询前处理,否则停止轮询后又成为了非轮询任务)
        if (!timerTaskBiz.isTimerTask(taskId) && errorEnum == null) {
            LOGGER.info("finished task : {}", taskId);
        }

        //非轮询,异常停止
        if (!timerTaskBiz.isTimerTask(taskId) && errorEnum != null) {
            LOGGER.warn("task error : {} , {}", taskId, errorEnum.getErrorMsg());
            httpUtils.sendErrorMsg(taskId, errorEnum);
        }

        //轮询中止,异常停止
        if (timerTaskBiz.isTimerTask(taskId) && errorEnum != null) {
            LOGGER.error("timer task error and stop : {} , {}", taskId, errorEnum.getErrorMsg());
            timerTaskBiz.stopTimerTask(taskId, errorEnum, appCode);
        }

        //轮询任务结束,并且没有该应用的轮询任务存在了
        if (!timerTaskBiz.stillTimerTask(appCode) & removeAppCodeAndCheckAppFinish(appCode)) {
            LOGGER.info("app task finished and file upload : {} , {}", taskId, appCode);
            folderFileScanner.fileUpload(projectName, taskId);
        }

    }

    public Data<String> stopTimerTask(Long taskId, String appCode) {
        timerTaskBiz.stopTimerTask(taskId, null, appCode);
        folderFileScanner.fileUpload(appCode, taskId);
        return new Data<>(ErrorEnum.SUCCESS.getErrorCode(), ErrorEnum.SUCCESS.getErrorMsg());
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

    /**
     * 不再使用该方式
     *
     * @return empty queue
     */
    @Deprecated
    public BlockingQueue<ApplicationCodeInfo> getTaskQueue() {
        LOGGER.info("task executor thread pool queue size:{}", taskExecutor.getQueue().size());
        return new LinkedBlockingQueue<>();
    }


    private void mockResult(ApplicationCodeInfo applicationCodeInfo) {
        String url = systemConfig.getSendCoverageResultUrl();
        Map<CharSequence, CharSequence> headersMap = new HashMap<>(1);
        headersMap.put("Content-type", MediaType.APPLICATION_JSON_VALUE);
        CoverageData coverageData = new CoverageData();
        coverageData.setId(applicationCodeInfo.getId());
        coverageData.setAppCode(applicationCodeInfo.getApplicationID());
        coverageData.setFilterTask(1);
        coverageData.setVersionId(applicationCodeInfo.getVersionId());
        coverageData.setMissedInstructions("5");
        coverageData.setTotalInstructions("10");
        coverageData.setMissedLines("2");
        coverageData.setTotalLines("6");
        coverageData.setMissedBranches("2");
        coverageData.setTotalBranches("3");
        coverageData.setMissedClasses("2");
        coverageData.setTotalClasses("3");
        coverageData.setMissedCxty("4");
        coverageData.setTotalCxty("16");
        coverageData.setMissedMethods("5");
        coverageData.setTotalMethods("22");
        coverageData.setTotalCoverageReportPath("www.baidu.com");
        coverageData.setTestedBranch(applicationCodeInfo.getTestedBranch());
        coverageData.setBasicBranch(applicationCodeInfo.getBasicBranch());

        LOGGER.info("mock result : {}", JSON.toJSON(coverageData));
//        HttpRequestUtil.postForObject(url, headersMap, JSON.toJSONBytes(coverageData), Data.class, 1);
    }

}

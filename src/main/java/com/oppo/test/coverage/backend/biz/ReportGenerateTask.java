package com.oppo.test.coverage.backend.biz;

import com.oppo.basic.heracles.client.core.spring.annotation.HeraclesDynamicConfig;
import com.oppo.test.coverage.backend.biz.jacoco.ExecutionDataClient;
import com.oppo.test.coverage.backend.biz.jacoco.MergeDump;
import com.oppo.test.coverage.backend.model.constant.ErrorEnum;
import com.oppo.test.coverage.backend.model.entity.ApplicationCodeInfo;
import com.oppo.test.coverage.backend.model.entity.ReportGeneratorTaskEntity;
import com.oppo.test.coverage.backend.model.request.EcUploadRequest;
import com.oppo.test.coverage.backend.util.SpringContextUtil;
import com.oppo.test.coverage.backend.util.SystemConfig;
import com.oppo.test.coverage.backend.util.file.FileOperateUtil;
import com.oppo.test.coverage.backend.util.http.HttpUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutorService;

/**
 * @author 80264236
 * @date 2021/4/9 16:27
 */
public class ReportGenerateTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerateTask.class);

    private SystemConfig systemConfig;

    private ExecutionDataClient executionDataClient;

    private TaskBiz taskBiz;

    private HttpUtils httpUtils;

    private ReportGeneratorTaskEntity taskEntity;

    private CortBiz cortBiz;

    private ExecutorService cacheThreadPool;

    private final String ocsGoblinEcUrl = "http://s3v2.dg-access-test.wanyol.com/goblin/";

    ReportGeneratorTaskEntity getTaskEntity() {
        return taskEntity;
    }

    /**
     * 构造函数接收任务数据,构造可执行任务
     */
    ReportGenerateTask(ApplicationCodeInfo applicationCodeInfo) {
        taskEntity = new ReportGeneratorTaskEntity(applicationCodeInfo);
        initBean();
        initOnce();
    }

    private void initBean() {
        this.systemConfig = (SystemConfig) SpringContextUtil.getBean("systemConfig");
        this.executionDataClient = (ExecutionDataClient) SpringContextUtil.getBean("executionDataClient");
        this.taskBiz = (TaskBiz) SpringContextUtil.getBean("taskBiz");
        this.httpUtils = (HttpUtils) SpringContextUtil.getBean("httpUtils");
        this.cortBiz = (CortBiz) SpringContextUtil.getBean("cortBiz");
        this.cacheThreadPool = (ExecutorService) SpringContextUtil.getBean("cacheThreadPool");
    }


    /**
     * 任务执行初始化
     * 1：解析字段
     * 2：构造文件路径
     * 3：获取代码
     */
    private void initOnce() {
        //生成开发git代码本地路径
        File localPath = new File(systemConfig.getCodePath(), taskEntity.getProjectName());
        taskEntity.setGitLocalPath(localPath);

        // 项目路径 : /home/service/app/coveragebackend/${uniqueId}/projectCovPath/${projectName}
        File projectCovPath = createFile(systemConfig.getProjectCovPath(), taskEntity.getProjectName());
        taskEntity.setProjectCovPath(projectCovPath.toString());

        createFileDirectory();
    }


    private void createFileDirectory() {
        //创建测试报告文件名
        File coverageReportPath = createCoverageReportPathByTaskId(taskEntity.getAppInfo().getId().toString());
        taskEntity.setCoverageReportPath(coverageReportPath);
        //创建测试taskID目录
        taskEntity.setCoverageExecutionDataPath(createFile(taskEntity.getCoverageReportPath().getPath(), taskEntity.getAppInfo().getTestedBranch().replace("/", "_")));
        //创建jacocoAll汇总文件
        taskEntity.setAllExecutionDataFile(new File(taskEntity.getCoverageExecutionDataPath().getPath(), "jacocoAll.exec"));
    }


    /**
     * 创建 /${basePath}/taskID/${taskId}
     *
     * @param taskId : 任务id
     */
    private File createCoverageReportPathByTaskId(String taskId) {
        File taskPath = createFile(systemConfig.getReportBasePath(), "taskID");
        File file = createFile(taskPath.getPath(), taskId);
        return file;
    }


    /**
     * 创建文件并检测创建成功与否
     */
    private File createFile(String parent, String child) {
        File result = new File(parent, child);
        if (!result.exists()) {
            if (!result.mkdir()) {
                logger.warn("当前路径不存在,创建失败 : {}", parent + child);
            }
        }
        return result;
    }

    @Override
    public void run() {

        //每次轮询起始,注意:异常处理,结果聚合避免重复回调
        ErrorEnum errorEnum = null;

        //设置线程名
        String currentThreadName = Thread.currentThread().getName();
        String[] threadNameArray = currentThreadName.split("-taskId-");
        Thread.currentThread().setName(threadNameArray[0] + "-taskId-" + taskEntity.getAppInfo().getSceneId());

        //组合ip、port,遍历每台机器,获取数据,并将各笔数据聚合在一起,需要处理版本判断

        if(!getGoblinEcFile()) {

            int failCount = 0;

            for (String serverIp : taskEntity.getIpList()) {
                for (String portNum : taskEntity.getPort()) {
                    File executionDataFile = new File(taskEntity.getCoverageExecutionDataPath(), serverIp + System.currentTimeMillis() + "_jacoco.exec");
                    //先获取数据
                    if (!getExecDataFromMachine(executionDataFile, serverIp, portNum)) {
                        failCount++;
                    }

                    // TODO: 2021/11/16  判断是否是新版本?这个怎么办

                }
            }

            if (failCount == taskEntity.getIpList().size() * taskEntity.getPort().length) {
                //没有获取到覆盖率数据,报错结束
                logger.error("获取覆盖率数据失败 : {}, {}, {}", taskEntity.getAppInfo().getId(), taskEntity.getAppInfo().getApplicationID(), taskEntity.getIpList());
                taskBiz.endCoverageTask(taskEntity.getAppInfo().getId(), ErrorEnum.JACOCO_EXEC_FAILED,
                        taskEntity.getProjectName(), taskEntity.getAppInfo().getApplicationID());
                return;
            }

            //合并目录下的各机器数据
            if (!mergeExecData()) {
                logger.error("合并覆盖率数据失败: {},{}", taskEntity.getAppInfo().getApplicationID(), taskEntity.getAppInfo().getId());
            }
        }
        //生成各目录下的数据报告,分别上传回调
        if (taskEntity.getAppInfo().getSceneId() != null && taskEntity.getAppInfo().getSceneId() != 0) {
            // 将jacocoAll上传到cort的OCS
            cortEcFileUpload();
        }

        taskBiz.endCoverageTask(taskEntity.getAppInfo().getId(), errorEnum,
                taskEntity.getProjectName(), taskEntity.getAppInfo().getApplicationID());
    }

    /**
     * 在某一机器某一端口上获取exec文件
     */
    private boolean getExecDataFromMachine(File executionDataFile, String serverIp, String portNum) {
        boolean result;
        try {
            result = executionDataClient.getEcData(serverIp, Integer.parseInt(portNum), executionDataFile, taskEntity.getAppInfo().getTestedEnv());
        } catch (Exception e) {
            logger.warn("获取覆盖率失败: 应用-{} , taskId-{}, ip-{}:{}, {}", taskEntity.getAppInfo().getApplicationID(), taskEntity.getAppInfo().getSceneId(), serverIp, portNum, e.getMessage());
            e.printStackTrace();
            return false;
        }
        return result;
    }

    private boolean getGoblinEcFile(){
        boolean result = true;
        try{
            if(null == taskEntity.getAppInfo().getGoblinEcFile()){
                return false;
            }
            String fileName = taskEntity.getAppInfo().getGoblinEcFile()+".exec";
            String url = ocsGoblinEcUrl + fileName;
            String localPath = taskEntity.getCoverageExecutionDataPath() + File.separator + "jacocoAll.exec";
            logger.info("goblin覆盖率文件url：{}, 保存路径：{}",url,localPath);
            FileUtils.copyURLToFile(new URL(url), new File(localPath));
        } catch (Exception e) {
            logger.warn("获取goblin覆盖率文件失败: 应用-{} , taskId-{}, {}", taskEntity.getAppInfo().getApplicationID(), taskEntity.getAppInfo().getSceneId(),e.getMessage());
            e.printStackTrace();
            return false;
        }
        return result;
    }

    /**
     * 合并各目录覆盖率文件,并加载taskId - jacocoAll
     */
    private boolean mergeExecData() {

        //合并taskId目录代码覆盖率
        MergeDump mergeDump = new MergeDump(taskEntity.getCoverageExecutionDataPath().toString());
        File allExecutionDataFile = mergeDump.executeMerge();
        if (allExecutionDataFile == null) {
            return false;
        }

        taskEntity.setAllExecutionDataFile(allExecutionDataFile);

        return true;
    }

    /**
     * 将覆盖率数据ec上传到OCS,并且上报cort
     */
    private void cortEcFileUpload() {
        File cortEcFile = new File(taskEntity.getCoverageExecutionDataPath().toString(), "jacocoAll-" + taskEntity.getAppInfo().getId() + ".ec");
        FileOperateUtil.copyFile(taskEntity.getAllExecutionDataFile().toString(), cortEcFile.toString());

        //二进制格式上传
        binaryEcUpload(cortEcFile);
    }


    private void binaryEcUpload(File cortEcFile) {
        boolean cortEcUploadResult = cortBiz.uploadEcFile(cortEcFile);
        if (!cortEcUploadResult) {
            logger.error("上传binary ec失败 : {}", cortEcFile.toString());
            return;
        }
        EcUploadRequest ecUploadRequest = new EcUploadRequest(taskEntity.getAppInfo(), cortEcFile.getName());
        boolean result = cortBiz.postEcFileToCort(ecUploadRequest, 1);
        logger.info("upload binary ec file : {} , {} , {} ,  result is {}", cortEcFile.getName(), ecUploadRequest.getAppCode(), ecUploadRequest.getCommitId(), result);
    }

    private void jsonEcUpload(File cortEcFile) {

        //文件所在路径
        File path = taskEntity.getCoverageExecutionDataPath();

        // TODO: 2022/1/4 转换ec为json后上传文件,再上报

        //ec转json
        File jsonEcFile = new File(path, cortEcFile.getName());

        //json上传ocs
        boolean cortEcUploadResult = cortBiz.uploadEcFile(jsonEcFile);
        if (!cortEcUploadResult) {
            logger.error("上传json ec失败 : {}", jsonEcFile.toString());
            return;
        }

        //上报cort
        EcUploadRequest ecUploadRequest = new EcUploadRequest(taskEntity.getAppInfo(), jsonEcFile.getName());
        boolean result = cortBiz.postEcFileToCort(ecUploadRequest, 2);
        logger.info("upload json ec file : {} , result is {}", jsonEcFile.getName(), result);
    }


}

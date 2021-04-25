package com.oppo.test.coverage.backend.biz;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.oppo.test.coverage.backend.biz.jacoco.*;
import com.oppo.test.coverage.backend.model.constant.ErrorEnum;
import com.oppo.test.coverage.backend.model.entity.ApplicationCodeInfo;
import com.oppo.test.coverage.backend.model.entity.CoverageData;
import com.oppo.test.coverage.backend.model.entity.Data;
import com.oppo.test.coverage.backend.model.entity.ReportGeneratorTaskEntity;
import com.oppo.test.coverage.backend.model.response.DefinitionException;
import com.oppo.test.coverage.backend.util.ColumbusUtils;
import com.oppo.test.coverage.backend.util.GitUtil;
import com.oppo.test.coverage.backend.util.SpringContextUtil;
import com.oppo.test.coverage.backend.util.SystemConfig;
import com.oppo.test.coverage.backend.util.file.FileOperateUtil;
import com.oppo.test.coverage.backend.util.http.HttpRequestUtil;
import com.oppo.test.coverage.backend.util.http.HttpUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.internal.diff.ClassInfo;
import org.jacoco.core.internal.diff.CodeDiff;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MultiSourceFileLocator;
import org.jacoco.report.html.HTMLFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author 80264236
 * @date 2021/4/9 16:27
 */
public class ReportGenerateTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerateTask.class);

    private SystemConfig systemConfig;

    private ExecutionDataClient executionDataClient;

    private TimerTaskBiz timerTaskBiz;

    private TaskBiz taskBiz;

    private HttpUtils httpUtils;

    private ReportGeneratorTaskEntity taskEntity;

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
        this.timerTaskBiz = (TimerTaskBiz) SpringContextUtil.getBean("timerTaskBiz");
        this.taskBiz = (TaskBiz) SpringContextUtil.getBean("taskBiz");
        this.httpUtils = (HttpUtils) SpringContextUtil.getBean("httpUtils");
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

        File projectCovPath = createFile(systemConfig.getProjectCovPath(), taskEntity.getProjectName());

        taskEntity.setProjectCovPath(projectCovPath.toString());

        //clone代码到本地
//        String newBranchName = cloneCodeSource(taskEntity.getAppInfo().getGitPath(), systemConfig.getCodePath(), taskEntity.getAppInfo().getTestedBranch(), taskEntity.getAppInfo().getBasicBranch(), taskEntity.getAppInfo().getTestedCommitId());
//        taskEntity.getAppInfo().setTestedBranch(newBranchName);

        ArrayList<File> fileList = new ArrayList<>();
        //解析工程中各个模块路径 : /home/service/app/coveragebackend/xxxxxxx/codeCoverage,源码路径
        ArrayList<File> applicationNames = GitUtil.getApplicationNames(localPath, fileList);
        //模块绑定source地址
        Map<String, Object> sourceApplicationsMap = getApplicationSourceDirectory(applicationNames);

        taskEntity.setSourceApplicationsMap(sourceApplicationsMap);
        taskEntity.initSourceDirectoryList();

        // TODO: 2021/4/13 动态更新ip
        HashMap<String, Object> applicationHash = ColumbusUtils.getAppDeployInfoFromBuildVersionList(taskEntity.getAppInfo().getApplicationID(), taskEntity.getAppInfo().getVersionName(), taskEntity.getAppInfo().getTestedEnv());
        String applicationIpList = applicationHash.get("applicationIP").toString();
        String repositoryUrl = applicationHash.get("repositoryUrl").toString();

        if (CollectionUtils.isEmpty(taskEntity.getIpList())) {
            taskEntity.setIpList(Arrays.asList(applicationIpList.split(",")));
        }

        //创建测试报告文件名
        File coverageReportPath = createCoverageReportPathByTaskId(taskEntity.getAppInfo().getId().toString());
        taskEntity.setCoverageReportPath(coverageReportPath);

        String classPath = null;
        try {
            //获取下载buildVersion.zip包
            String downloadFilePath = ColumbusUtils.downloadColumbusBuildVersion(repositoryUrl, coverageReportPath.toString());
            //解压zip包获取class文件
            classPath = ColumbusUtils.extractColumbusBuildVersionClasses(downloadFilePath, new File(coverageReportPath, "classes").toString(), taskEntity.getAppInfo().getApplicationID(), sourceApplicationsMap);

            //提前过滤类,兼容某些类classId不一致问题
            //过滤配置的ignore class,package文件
            ColumbusUtils.filterIgnoreClass(taskEntity.getIgnoreClassList(), new File(classPath));
        } catch (Exception e) {
            logger.error("classPath 解析流程异常: {} , {} , {}", taskEntity.getAppInfo().getId(), taskEntity.getAppInfo().getApplicationID(), e.getMessage());
            e.printStackTrace();
        }
        taskEntity.setClassPath(classPath);

        //创建未过滤报告存储地址
        taskEntity.setReportAllCovDirectory(createFile(taskEntity.getCoverageReportPath().getPath(), "coveragereport"));
        taskEntity.setReportDiffDirectory(createFile(taskEntity.getCoverageReportPath().getPath(), "coveragediffreport"));
        //创建已过滤报告存储地址
        taskEntity.setFilterReportAllCovDirectory(createFile(taskEntity.getCoverageReportPath().getPath(), "filtercoveragereport"));
        taskEntity.setFilterReportDiffDirectory(createFile(taskEntity.getCoverageReportPath().getPath(), "filtercoveragediffreport"));
        //创建被测分支目录
        taskEntity.setTestedBranchCoverageDirectory(createFile(taskEntity.getProjectCovPath(), taskEntity.getAppInfo().getTestedBranch().replace("/", "_")));
        //创建项目id目录
        if (taskEntity.getAppInfo().getVersionId() != null && taskEntity.getAppInfo().getVersionId() != 0) {
            taskEntity.setVersionIdDataPath(createFile(taskEntity.getProjectCovPath(), taskEntity.getAppInfo().getVersionId().toString()));
        }
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
     * 创建 /${basePath}/taskID/${branchTaskId}/branchcoverage/${branch}
     *
     * @param branchTaskId  : 合并的上一条任务id
     * @param newBranchName : 被测分支名
     */
    private File createBranchCoverageReportPathByTaskId(String branchTaskId, String newBranchName) {
        File branchTaskPath = createCoverageReportPathByTaskId(branchTaskId);
        File branchCoverage1 = createFile(branchTaskPath.getPath(), "branchcoverage");
        File branchCoverage2 = createFile(branchCoverage1.getPath(), newBranchName);
        return branchCoverage2;
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

    /**
     * 解析应用模块对应的代码路径和部署的IP地址
     * {"applicationname":{"ip":"127.0.0.1","sourceDirectory":"D://codecoverage//applicationmodule//src"}}
     *
     * @param applicationNames :
     * @return ：
     */
    private Map<String, Object> getApplicationSourceDirectory(ArrayList<File> applicationNames) {
        Map<String, Object> applicationNameMap = new HashMap<>(applicationNames.size());
        for (File applicationPath : applicationNames) {
            Map<String, Object> applicationInfo = new HashMap<>(2);
            applicationInfo.put("sourceDirectory", applicationPath.toString());
            applicationNameMap.put(applicationPath.getName(), applicationInfo);
        }
        return applicationNameMap;
    }

    /**
     * 下载代码,然后切换分支
     *
     * @param newBranchName : 被测分支
     * @param oldBranchName : 基线分支
     * @param newTag        : 被测commitId
     */
    private String cloneCodeSource(String urlString, String codePath, String newBranchName, String oldBranchName, String newTag) throws DefinitionException {
        //localPath  : /home/service/app/coveragebackend/2qpiyetftazy/codeCoverage/pandora
        logger.info("开始下载开发项目代码到本地 : {} , {}", urlString, taskEntity.getGitLocalPath().getPath());
        GitUtil.cloneRepository(urlString, taskEntity.getGitLocalPath(), newBranchName);
        //checkout分支代码
        newBranchName = GitUtil.checkoutBranch(taskEntity.getGitLocalPath().getPath(), newBranchName, oldBranchName, newTag);
        return newBranchName;
    }


    /**
     * 生成差异化覆盖率
     */
    private IBundleCoverage analyzeStructureDiff(ArrayList<File> classesDirectoryList, String title) throws IOException {
        //全量覆盖
        //基于分支比较覆盖，参数1：本地仓库，参数2：开发分支（预发分支），参数3：基线分支(不传时默认为master)
        //本地Git路径，新分支 第三个参数不传时默认比较maser，传参数为待比较的基线分支
        //基于Tag比较的覆盖 参数1：本地仓库，参数2：代码分支，参数3：新Tag(预发版本)，参数4：基线Tag（变更前的版本）
        CoverageBuilder coverageBuilder = new CoverageBuilder(taskEntity.getGitLocalPath().getPath(), taskEntity.getAppInfo().getTestedBranch(), taskEntity.getAppInfo().getTestedCommitId(), taskEntity.getAppInfo().getBasicCommitId());

        final Analyzer analyzer = new Analyzer(taskEntity.getExecFileLoader().getExecutionDataStore(), coverageBuilder);
        for (File classesDirectory : classesDirectoryList) {
            analyzer.analyzeAll(classesDirectory);
        }

        return coverageBuilder.getBundle(title);
    }

    /**
     * 创建覆盖率报告
     */
    private void createReport(final IBundleCoverage bundleCoverage, File reportDir, ArrayList<File> sourceDirectoryList) throws IOException {

        // Create a concrete report visitor based on some supplied configuration.
        // In this case we use the defaults
        final HTMLFormatter htmlFormatter = new HTMLFormatter();
        final IReportVisitor visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(reportDir));

        // Initialize the report with all of the execution and session information.
        // At this point the report doesn't know about the structure of the report being created
        visitor.visitInfo(taskEntity.getExecFileLoader().getSessionInfoStore().getInfos(), taskEntity.getExecFileLoader().getExecutionDataStore().getContents());

        // Populate the report structure with the bundle coverage information.
        // Call visitGroup if you need groups in your report.
//        visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(sourceDirectory, "utf-8", 4));

        //多源码路径
        int tabWidth = sourceDirectoryList.size();
        MultiSourceFileLocator sourceLocator = new MultiSourceFileLocator(tabWidth);
        for (File sourceDirectory : sourceDirectoryList) {
            sourceLocator.add(new DirectorySourceFileLocator(sourceDirectory, "utf-8", tabWidth));
        }
        visitor.visitBundle(bundleCoverage, sourceLocator);

        // Signal end of structure information to allow report to write all information out
        visitor.visitEnd();
    }


    private void loadExecutionData(File executionDataFile) throws Exception {
        taskEntity.setExecFileLoader(new ExecFileLoader());
        taskEntity.getExecFileLoader().load(executionDataFile);
    }


    /**
     * @param reportAllCovDirectory : 全量报告地址
     * @param reportDiffDirectory   : 增量报告地址
     * @param filterTask            : 0-未过滤任务;1-过滤任务
     * @param resultType            : 1-普通结果;2-分支结果;3-版本报告结果
     */
    private void sendCoverageDataResult(File reportAllCovDirectory, File reportDiffDirectory, int filterTask, int resultType) {

        File coverageReport = new File(reportAllCovDirectory, "index.html");
        File diffCoverageReport = new File(reportDiffDirectory, "index.html");

        Jsouphtml jsouphtml = new Jsouphtml(coverageReport, diffCoverageReport);

        CoverageData coverageData = jsouphtml.getCoverageData(
                resultType == 2 ? taskEntity.getAppInfo().getBranchTaskID() : taskEntity.getAppInfo().getId(),
                taskEntity.getAppInfo().getApplicationID(),
                taskEntity.getAppInfo().getTestedBranch().replace("/", "_"),
                taskEntity.getAppInfo().getBasicBranch(),
                resultType == 3 ? taskEntity.getAppInfo().getVersionId() : null,
                taskEntity.getProjectName());

        coverageData.setFilterTask(filterTask);

        logger.info("生成报告: {}", coverageData.toString());

        String url;
        switch (resultType) {
            case 1:
                url = systemConfig.getSendCoverageResultUrl();
                break;
            case 2:
                url = systemConfig.getSendBranchResultUrl();
                break;
            case 3:
                url = systemConfig.getSendVersionResultUrl();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + resultType);
        }

        Map<CharSequence, CharSequence> headersMap = new HashMap<>(1);
        headersMap.put("Content-type", MediaType.APPLICATION_JSON_VALUE);

        HttpRequestUtil.postForObject(url, headersMap, JSON.toJSONBytes(coverageData), Data.class, 1);
    }


    /**
     * 生成覆盖率报告
     */
    private void createCoverageReport(ArrayList<File> classesDirectoryList,
                                      File reportAllCovDirectory,
                                      String title,
                                      ArrayList<File> sourceDirectoryList) throws IOException {

        // Read the jacoco.exec file. Multiple data files could be merged at this point

        // Run the structure analyzer on a single class folder to build up the coverage model.
        // The process would be similar if your classes were in a jar file.
        // Typically you would create a bundle for each class folder and each jar you want in your report.
        // If you have more than one bundle you will need to add a grouping node to your report
        final IBundleCoverage bundleCoverage = analyzeStructure(classesDirectoryList, title);
        createReport(bundleCoverage, reportAllCovDirectory, sourceDirectoryList);
    }

    private IBundleCoverage analyzeStructure(ArrayList<File> classesDirectoryList, String title) throws IOException {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(taskEntity.getExecFileLoader().getExecutionDataStore(), coverageBuilder);
        for (File classesDirectory : classesDirectoryList) {
            analyzer.analyzeAll(classesDirectory);
        }
        return coverageBuilder.getBundle(title);
    }

    /**
     * 生成差异覆盖率
     */
    private void createDiff(ArrayList<File> classesDirectoryList,
                            File reportDiffDirectory,
                            ArrayList<File> sourceDirectoryList,
                            String title) throws IOException {
        //差异化代码覆盖率
        List<ClassInfo> classInfos;
        try {
            classInfos = CodeDiff.diffTagToTag(taskEntity.getGitLocalPath().getPath(), taskEntity.getAppInfo().getTestedBranch(), taskEntity.getAppInfo().getTestedCommitId(), taskEntity.getAppInfo().getBasicCommitId());
        } catch (IllegalArgumentException e) {
            logger.error("exception in createDiff : {}, {}", taskEntity.getGitLocalPath(), e.getMessage());
            throw e;
        }
        if (classInfos != null && classInfos.size() > 0) {
            final IBundleCoverage bundleCoverageDiff = analyzeStructureDiff(classesDirectoryList, title);
            createReport(bundleCoverageDiff, reportDiffDirectory, sourceDirectoryList);
        }
    }


    private void filterClassAndPackage(String classPath) {
        String[] ignorePackageList = taskEntity.getIgnorePackageList();
        String[] containPackagesList = taskEntity.getContainPackageList();

        //过滤配置的ignore class,package文件
        ColumbusUtils.filterIgnorePackage(ignorePackageList, new File(classPath));

        //只统计指定包
        if (containPackagesList != null && containPackagesList.length > 0) {
            HashSet containPackagesSet = ColumbusUtils.getContainPackageHashSet(containPackagesList, classPath);
            if (containPackagesSet.size() > 0) {
                ColumbusUtils.filterContainPackages(containPackagesSet, new File(classPath));
            }
        }
    }

    private File filterBranchData(File localPath, File branchTaskCoverageReportPath, String classPath) throws Exception {
        //将当前的覆盖率数据做一轮清洗，过滤class文件中不存在的classID
        File filterExecFile = new File(branchTaskCoverageReportPath, "jacoco.exec");
        File jacocoAll = new File(localPath, "jacocoAll.exec");
        if (jacocoAll.exists()) {
            AnalyExecData analyExecData = new AnalyExecData(filterExecFile.toString(), jacocoAll.toString());
            analyExecData.filterOldExecData(classPath);
        }
        return filterExecFile;
    }


    private void startVersionCoverageTask() {
        try {
            File versionIdDataPath = createFile(taskEntity.getProjectCovPath(), taskEntity.getAppInfo().getVersionId().toString());
            File versionCoverageReportBasicPath = createFile(versionIdDataPath.getPath(), "coverage");
            File versionIdClassPath = new File(taskEntity.getClassPath());
            File filterExecFile = filterBranchData(versionIdDataPath, versionCoverageReportBasicPath, versionIdClassPath.toString());

            //要保存报告的地址
            File versionAllCovDirectory = createFile(versionCoverageReportBasicPath.getPath(), "versioncoveragereport");
            File versionDiffDirectory = createFile(versionCoverageReportBasicPath.getPath(), "versiondiffcoveragereport");

            //目录下必须包含源码编译过的class文件,用来统计覆盖率。所以这里用server打出的jar包地址即可,运行的jar或者Class目录
            ArrayList<File> classesDirectoryList = Lists.newArrayList(versionIdClassPath);
            ArrayList<File> sourceDirectoryList = taskEntity.getSourceDirectoryList();

            //合并taskID目录代码覆盖率
            if (!filterExecFile.exists()) {
                throw new DefinitionException(ErrorEnum.JACOCO_EXEC_FAILED.getErrorCode(), ErrorEnum.JACOCO_EXEC_FAILED.getErrorMsg());
            }
            loadExecutionData(filterExecFile);
            //生成差异化覆盖率
            if (!taskEntity.getAppInfo().getTestedCommitId().equals(taskEntity.getAppInfo().getBasicCommitId())) {
                createDiff(classesDirectoryList, versionDiffDirectory, sourceDirectoryList, versionCoverageReportBasicPath.getName());
            }
            //生成整体覆盖率报告
            createCoverageReport(classesDirectoryList, versionAllCovDirectory, versionCoverageReportBasicPath.getName(), sourceDirectoryList);
            //上传覆盖率报告
            sendCoverageDataResult(versionAllCovDirectory, versionDiffDirectory, 1, 3);
        } catch (DefinitionException e) {
            logger.error("版本报告生成失败 : {},{}", taskEntity.getAppInfo().getId(), e.getErrorMsg());
            httpUtils.sendErrorMsg(taskEntity.getAppInfo().getId(), e.getErrorMsg());
        } catch (Exception e) {
            logger.error("分支报告生成失败 : {},{}", taskEntity.getAppInfo().getId(), e.getMessage());
            e.printStackTrace();
        }
    }

    private void startBranchCoverageTask() {
        try {
            File branchTaskCoverageReportPath = createBranchCoverageReportPathByTaskId(taskEntity.getAppInfo().getBranchTaskID().toString(), taskEntity.getAppInfo().getTestedBranch().replace("/", "_"));
            File branchTaskPath = createCoverageReportPathByTaskId(taskEntity.getAppInfo().getBranchTaskID().toString());
            File branchClassPath = createFile(branchTaskPath.getPath(), "classes");
            File gitLocalExecutionDataPath = createFile(taskEntity.getProjectCovPath(), taskEntity.getAppInfo().getTestedBranch().replace("/", "_"));
            File filterExecFile = filterBranchData(gitLocalExecutionDataPath, branchTaskCoverageReportPath, branchClassPath.toString());

            //要保存报告的地址
            File reportAllCovDirectory = createFile(branchTaskCoverageReportPath.getPath(), "totalcoveragereport");
            File reportDiffDirectory = createFile(branchTaskCoverageReportPath.getPath(), "diffcoveragereport");

            //目录下必须包含源码编译过的class文件,用来统计覆盖率。所以这里用server打出的jar包地址即可,运行的jar或者Class目录
            ArrayList<File> classesDirectoryList = Lists.newArrayList(branchClassPath);
            ArrayList<File> sourceDirectoryList = taskEntity.getSourceDirectoryList();

            //合并taskID目录代码覆盖率
            if (!filterExecFile.exists()) {
                throw new DefinitionException(ErrorEnum.JACOCO_EXEC_FAILED.getErrorCode(), ErrorEnum.JACOCO_EXEC_FAILED.getErrorMsg());
            }
            loadExecutionData(filterExecFile);

            //生成差异化覆盖率
            if (!taskEntity.getAppInfo().getTestedCommitId().equals(taskEntity.getAppInfo().getBasicCommitId())) {
                createDiff(classesDirectoryList, reportDiffDirectory, sourceDirectoryList, branchTaskCoverageReportPath.getName());
            }
            //生成整体覆盖率报告
            createCoverageReport(classesDirectoryList, reportAllCovDirectory, branchTaskCoverageReportPath.getName(), sourceDirectoryList);
            //上传覆盖率报告
            sendCoverageDataResult(reportAllCovDirectory, reportDiffDirectory, 1, 2);
        } catch (DefinitionException e) {
            logger.error("分支报告生成失败 : {},{}", taskEntity.getAppInfo().getId(), e.getErrorMsg());
            httpUtils.sendErrorMsg(taskEntity.getAppInfo().getId(), e.getErrorMsg());
        } catch (Exception e) {
            logger.error("分支报告生成失败 : {},{}", taskEntity.getAppInfo().getId(), e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        //每次轮询起始,注意:异常处理,结果聚合避免重复回调
        ErrorEnum errorEnum = null;

        //重新下载代码,因为过滤条件会删除源码,导致未过滤数据丢失
        String newBranch = cloneCodeSource(taskEntity.getAppInfo().getGitPath(),
                systemConfig.getCodePath(),
                taskEntity.getAppInfo().getTestedBranch(),
                taskEntity.getAppInfo().getBasicBranch(),
                taskEntity.getAppInfo().getTestedCommitId());
        taskEntity.getAppInfo().setTestedBranch(newBranch);


        //组合ip、port,遍历每台机器,获取数据,并将各笔数据聚合在一起,需要处理版本判断
        int failCount = 0;

        for (String serverIp : taskEntity.getIpList()) {
            for (String portNum : taskEntity.getPort()) {
                File executionDataFile = new File(taskEntity.getCoverageExecutionDataPath(), serverIp + System.currentTimeMillis() + "_jacoco.exec");
                //先获取数据
                if (!getExecDataFromMachine(executionDataFile, serverIp, portNum)) {
                    failCount++;
                    continue;
                }
                //将数据复制到项目、分支目录下
                copyExecToBranchAndVersionDirectory(executionDataFile, serverIp);

                //判断是否是新版本
                if (isNewVersion(executionDataFile)) {
                    logger.warn("记录-{},应用-{},存在新版本 : {}:{}", taskEntity.getAppInfo().getId(), taskEntity.getAppInfo().getApplicationID(), serverIp, portNum);
                    failCount++;
                }
            }
        }

        if (failCount == taskEntity.getIpList().size() * taskEntity.getPort().length) {
            //没有获取到覆盖率数据,报错结束
            logger.error("获取覆盖率数据失败");
            timerTaskBiz.stopTimerTask(taskEntity.getAppInfo().getId(), ErrorEnum.JACOCO_EXEC_FAILED, taskEntity.getAppInfo().getApplicationID());
            return;
        }

        //合并目录下的各机器数据
        if (!mergeExecData()) {
            logger.error("合并覆盖率数据失败: {},{}", taskEntity.getAppInfo().getApplicationID(), taskEntity.getAppInfo().getId());
        }

        //生成各目录下的数据报告,分别上传回调

        //目录下必须包含源码编译过的class文件,用来统计覆盖率。所以这里用server打出的jar包地址即可,运行的jar或者Class目录
        ArrayList<File> classesDirectoryList = Lists.newArrayList(new File(taskEntity.getClassPath()));
        //生成未过滤报告;过滤数据,生成过滤后报告
        try {
            generateReportAndSend(classesDirectoryList);
        } catch (IOException e) {
            logger.error("生成报告失败: {}, {}, {}", taskEntity.getAppInfo().getApplicationID(), taskEntity.getAppInfo().getId(), e.getMessage());
            e.printStackTrace();
            errorEnum = ErrorEnum.PRODUCT_REPORT;
            httpUtils.sendErrorMsg(taskEntity.getAppInfo().getId(), "报告生成失败 :" + e.getMessage());
        }

        //生成分支数据
        if (taskEntity.getAppInfo().getIsBranchTask() == 1) {
            startBranchCoverageTask();
        }

        //生成版本数据
        if (taskEntity.getAppInfo().getVersionId() != null && taskEntity.getAppInfo().getVersionId() != 0) {
            startVersionCoverageTask();
        }

        taskBiz.endCoverageTask(taskEntity.getAppInfo().getId(), errorEnum, taskEntity.getProjectName(), taskEntity.getAppInfo().getApplicationID(), taskEntity.getAppInfo().getIsBranchTask());
    }

    /**
     * 在某一机器某一端口上获取exec文件
     */
    private boolean getExecDataFromMachine(File executionDataFile, String serverIp, String portNum) {
        boolean result;
        try {
            result = executionDataClient.getExecutionData(serverIp, Integer.parseInt(portNum), executionDataFile, taskEntity.getAppInfo().getTestedEnv());
        } catch (Exception e) {
            logger.warn("获取覆盖率失败: 应用-{} , taskId-{}, ip-{}:{}, {}", taskEntity.getAppInfo().getApplicationID(), taskEntity.getAppInfo().getId(), serverIp, portNum, e.getMessage());
            e.printStackTrace();
            return false;
        }
        return result;
    }

    /**
     * 是否为一个新版本
     */
    private boolean isNewVersion(File executionDataFile) {
        AnalyNewBuildVersion analyNewBuildVersion;
        try {
            analyNewBuildVersion = new AnalyNewBuildVersion(taskEntity.getClassPath(), executionDataFile.toString());
        } catch (IOException e) {
            logger.error("新版本解析启动失败 : {} , {}", executionDataFile.getAbsolutePath(), e.getMessage());
            e.printStackTrace();
            //记为新版本,不纳入统计
            return true;
        }
        boolean newVersion = analyNewBuildVersion.findNewBuildVersion();
        //不是新版本
        if (!newVersion) {
            return false;
        }
        //如果存在新版本,删除本次覆盖率数据
        if (executionDataFile.delete()) {
            logger.warn("新版本覆盖率数据删除失败 : {}", executionDataFile.getPath());
        }
        return true;
    }

    /**
     * 将taskId下的exec复制到branch、version文件目录下
     */
    private void copyExecToBranchAndVersionDirectory(File executionDataFile, String serverIp) {
        File branchFile = new File(taskEntity.getTestedBranchCoverageDirectory().getPath(), serverIp + System.currentTimeMillis() + "_jacoco.exec");

        FileOperateUtil.copyFile(executionDataFile.getAbsolutePath(), branchFile.getPath());
        if (taskEntity.getAppInfo().getVersionId() != null && taskEntity.getAppInfo().getVersionId() != 0) {
            File versionFile = new File(taskEntity.getVersionIdDataPath().getPath(), serverIp + System.currentTimeMillis() + "_jacoco.exec");
            FileOperateUtil.copyFile(executionDataFile.getAbsolutePath(), versionFile.getPath());
        }
    }


    /**
     * 合并各目录覆盖率文件,并加载taskId - jacocoAll
     */
    private boolean mergeExecData() {
        //合并gitLocalPath目录覆盖率
        MergeDump mergeDumpGitLocalPath = new MergeDump(taskEntity.getTestedBranchCoverageDirectory().toString());
        mergeDumpGitLocalPath.executeMerge();

        //合并versionIDPath目录覆盖率
        if (taskEntity.getVersionIdDataPath() != null) {
            MergeDump versionIdMerge = new MergeDump(taskEntity.getVersionIdDataPath().toString());
            versionIdMerge.executeMerge();
        }

        //合并taskId目录代码覆盖率
        MergeDump mergeDump = new MergeDump(taskEntity.getCoverageExecutionDataPath().toString());
        File allExecutionDataFile = mergeDump.executeMerge();
        if (allExecutionDataFile == null) {
            return false;
        }

        taskEntity.setAllExecutionDataFile(allExecutionDataFile);

        try {
            loadExecutionData(allExecutionDataFile);
        } catch (Exception e) {
            logger.error("merge error : {},{},{}", taskEntity.getAppInfo().getApplicationID(), taskEntity.getAppInfo().getId(), e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 生成覆盖率报告并且回调结果
     */
    private void generateReportAndSend(ArrayList<File> classesDirectoryList) throws IOException {
        //生成差异化覆盖率报告
        if (taskEntity.getAppInfo().isNeedDiff()) {
            createDiff(classesDirectoryList, taskEntity.getReportDiffDirectory(), taskEntity.getSourceDirectoryList(), taskEntity.getCoverageReportPath().getName());
        }
        //生成未过滤整体覆盖率报告
        createCoverageReport(classesDirectoryList,
                taskEntity.getReportAllCovDirectory(),
                taskEntity.getCoverageReportPath().getName(),
                taskEntity.getSourceDirectoryList());
        //上传覆盖率报告
        sendCoverageDataResult(taskEntity.getReportAllCovDirectory(), taskEntity.getReportDiffDirectory(), 0, 1);

        //过滤package文件
        logger.info("开始jar过滤 : {}", taskEntity.getAppInfo().getId());
        filterClassAndPackage(taskEntity.getClassPath());
        logger.info("完成jar过滤 : {}", taskEntity.getAppInfo().getId());
        //生成已过滤差异覆盖率报告
        if (taskEntity.getAppInfo().isNeedDiff()) {
            createDiff(classesDirectoryList, taskEntity.getFilterReportDiffDirectory(), taskEntity.getSourceDirectoryList(), taskEntity.getCoverageReportPath().getName());
        }
        //生成已过滤整体覆盖率报告
        createCoverageReport(classesDirectoryList, taskEntity.getFilterReportAllCovDirectory(), taskEntity.getCoverageReportPath().getName(), taskEntity.getSourceDirectoryList());
        //上传已过滤覆盖率报告
        sendCoverageDataResult(taskEntity.getFilterReportAllCovDirectory(), taskEntity.getFilterReportDiffDirectory(), 1, 1);

    }


}

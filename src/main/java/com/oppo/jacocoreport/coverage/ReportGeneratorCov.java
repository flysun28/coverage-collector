package com.oppo.jacocoreport.coverage;

import com.oppo.jacocoreport.coverage.entity.ApplicationCodeInfo;
import com.oppo.jacocoreport.coverage.entity.BranchCoverageData;
import com.oppo.jacocoreport.coverage.entity.CoverageData;
import com.oppo.jacocoreport.coverage.entity.Data;
import com.oppo.jacocoreport.coverage.jacoco.AnalyExecData;
import com.oppo.jacocoreport.coverage.jacoco.AnalyNewBuildVersion;
import com.oppo.jacocoreport.coverage.jacoco.ExecutionDataClient;
import com.oppo.jacocoreport.coverage.jacoco.MergeDump;
import com.oppo.jacocoreport.coverage.utils.*;
import com.oppo.jacocoreport.response.DefinitionException;
import com.oppo.jacocoreport.response.ErrorEnum;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.internal.diff.ClassInfo;
import org.jacoco.core.internal.diff.CodeDiff;
import org.jacoco.core.internal.diff.GitAdapter;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MultiSourceFileLocator;
import org.jacoco.report.html.HTMLFormatter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 生成覆盖率报告主入口
 */

public class ReportGeneratorCov {
    private Long taskId = 0L;
    private  String port = "";
    private  String gitName = "";
    private  String gitPassword = "";
    private String gitlocalPath = "";
    private String applicationgitlabUrl = "";
    private  String newBranchName = "";
    private  String oldBranchName = "";
    private  String newTag = "";
    private  String oldTag = "";
    private String versionname = "";
    private CoverageBuilder coverageBuilder;
    private File coverageReportPath;
    private int isTimerTask = 0;
    private int isBranchTask = 0;
    private Long branchTaskID = 0L;//分支覆盖率taskID


    private ExecFileLoader execFileLoader;
    private static Map<String,Timer> timerMap = new HashMap<String,Timer>();
    public static Map<String, Timer> getTimerMap() {
        return timerMap;
    }

    public static void setTimerMap(Map<String, Timer> timerMap) {
        ReportGeneratorCov.timerMap = timerMap;
    }



    /**
     * Create a new generator based for the given project.
     *
     * @param
     */
    public ReportGeneratorCov(ApplicationCodeInfo applicationCodeInfo) {
        //从配置文件中获取当期工程的source目录，以及服务ip地址
        this.taskId = applicationCodeInfo.getId();
        this.port = applicationCodeInfo.getJacocoPort();
        if("".equals(this.port) ||"0".equals(this.port)|| null == this.port){
          this.port = ""+Config.Port;
        }
        this.gitName = Config.GitName;
        this.gitPassword = Config.GitPassword;
        this.applicationgitlabUrl = applicationCodeInfo.getGitPath();
        this.newBranchName = applicationCodeInfo.getTestedBranch();
        this.oldBranchName = applicationCodeInfo.getBasicBranch();
        this.versionname = applicationCodeInfo.getVersionName();
        this.newTag = applicationCodeInfo.getTestedCommitId();
        this.oldTag = applicationCodeInfo.getBasicCommitId();
        this.isTimerTask = applicationCodeInfo.getIsTimerTask();
        this.isBranchTask = applicationCodeInfo.getIsBranchTask();
        this.branchTaskID = applicationCodeInfo.getBranchTaskID();

    }
    private static File createCoverageReportPathBySysTime(){

        //1.读取系统时间
        Calendar calendar = Calendar.getInstance();
        Date time = calendar.getTime();

        //2.格式化系统时间
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = format.format(time);
        File file = new File(fileName);
        if(!file.exists()){
            if(!file.mkdir()){
                System.out.println("当前路径不存在，创建失败");
            }
        }
        System.out.println("创建成功"+fileName);
        return file;
    }

    private static File createCoverageReportPathByTaskid(String taskId){
        File taskPath = new File(Config.ReportBasePath,"taskID");
        if(!taskPath.exists()){
            if(!taskPath.mkdir()){
                System.out.println("当前路径不存在，创建失败");
            }
        }

        File file = new File(taskPath,taskId);
        if(!file.exists()){
            if(!file.mkdir()){
                System.out.println("当前路径不存在，创建失败");
            }
        }

        return file;
    }

    private static File createBranchCoverageReportPathByTaskid(String branchTaskID,String newBranchName){
        File branchTaskPath =createCoverageReportPathByTaskid(branchTaskID);
        File branchcoverage1 = new File(branchTaskPath,"branchcoverage");
        if(!branchcoverage1.exists()) {
            if (!branchcoverage1.mkdir()) {
                System.out.println("当前路径不存在，创建失败");
            }
        }
        File branchcoverage2 = new File(branchcoverage1,newBranchName);
        if(!branchcoverage2.exists()) {
            if (!branchcoverage2.mkdir()) {
                System.out.println("当前路径不存在，创建失败");
            }
        }
        return branchcoverage2;
    }
    /**
     * Create the report.
     *
     * @throws IOException
     */
    private void createAll(File executionDataFile,ArrayList<File> classesDirectoryList,File reportAllCovDirectory,String title,ArrayList<File> sourceDirectoryList) throws Exception {

        // Read the jacoco.exec file. Multiple data files could be merged
        // at this point
        loadExecutionData(executionDataFile);

        // Run the structure analyzer on a single class folder to build up
        // the coverage model. The process would be similar if your classes
        // were in a jar file. Typically you would create a bundle for each
        // class folder and each jar you want in your report. If you have
        // more than one bundle you will need to add a grouping node to your
        // report
        final IBundleCoverage bundleCoverage = analyzeStructure(classesDirectoryList,title);

        createReport(bundleCoverage,reportAllCovDirectory,sourceDirectoryList);

    }

    private void createDiff(ArrayList<File> classesDirectoryList,File reportDiffDirectory,ArrayList<File> sourceDirectoryList,String title) throws Exception {
        //差异化代码覆盖率
        List<ClassInfo> classInfos = CodeDiff.diffTagToTag(gitlocalPath, newBranchName, newTag, oldTag);
        if(classInfos != null && classInfos.size() > 0) {
            final IBundleCoverage bundleCoverageDiff = analyzeStructureDiff(classesDirectoryList, title);
            createReport(bundleCoverageDiff, reportDiffDirectory, sourceDirectoryList);
        }
    }

    private void createReport(final IBundleCoverage bundleCoverage,File reportDir,ArrayList<File> sourceDirectoryList)
            throws Exception {

        // Create a concrete report visitor based on some supplied
        // configuration. In this case we use the defaults
        final HTMLFormatter htmlFormatter = new HTMLFormatter();
        final IReportVisitor visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(reportDir));

        // Initialize the report with all of the execution and session
        // information. At this point the report doesn't know about the
        // structure of the report being created
        visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),execFileLoader.getExecutionDataStore().getContents());

        // Populate the report structure with the bundle coverage information.
        // Call visitGroup if you need groups in your report.
//        visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(sourceDirectory, "utf-8", 4));


        //多源码路径
        int tabwidth = sourceDirectoryList.size();
        MultiSourceFileLocator sourceLocator = new MultiSourceFileLocator(tabwidth);
        for(File sourceDirectory : sourceDirectoryList){
            sourceLocator.add( new DirectorySourceFileLocator(sourceDirectory, "utf-8",tabwidth));
        }
        visitor.visitBundle(bundleCoverage,sourceLocator);

        // Signal end of structure information to allow report to write all
        // information out
        visitor.visitEnd();

    }

    private void loadExecutionData(File executionDataFile) throws Exception {
        execFileLoader = new ExecFileLoader();
        execFileLoader.load(executionDataFile);
    }

    private IBundleCoverage analyzeStructure(ArrayList<File> classesDirectoryList,String title) throws Exception {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);
        for(File classesDirectory : classesDirectoryList) {
            analyzer.analyzeAll(classesDirectory);
        }
        return coverageBuilder.getBundle(title);
    }
    /**
     * 生成差异化覆盖率
     * @return
     * @throws IOException
     */
    private IBundleCoverage analyzeStructureDiff(ArrayList<File> classesDirectoryList,String title) throws Exception {
        //全量覆盖
//		final CoverageBuilder coverageBuilder = new CoverageBuilder();


        //基于分支比较覆盖，参数1：本地仓库，参数2：开发分支（预发分支），参数3：基线分支(不传时默认为master)
        //本地Git路径，新分支 第三个参数不传时默认比较maser，传参数为待比较的基线分支
        //"D:\\tools\\JacocoTest","daily","master"
        GitAdapter.setCredentialsProvider(gitName, gitPassword);
//        if(!oldBranchName.equals("")){
//            coverageBuilder = new CoverageBuilder(gitlocalPath,newBranchName,oldBranchName);
//        }else{
            //基于Tag比较的覆盖 参数1：本地仓库，参数2：代码分支，参数3：新Tag(预发版本)，参数4：基线Tag（变更前的版本）
            //final CoverageBuilder coverageBuilder = new CoverageBuilder("E:\\Git-pro\\JacocoTest","daily","v004","v003");
            coverageBuilder = new CoverageBuilder(gitlocalPath, newBranchName, newTag, oldTag);
//        }
        final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);
            for (File classesDirectory : classesDirectoryList) {
                analyzer.analyzeAll(classesDirectory);
            }
        return coverageBuilder.getBundle(title);
    }

    /**
     * 上传覆盖率报告
     */
    public void sendcoveragedata(File reportAllCovDirectory,File reportDiffDirectory) throws DefinitionException{
        try {

            File coveragereport = new File(reportAllCovDirectory, "index.html");
            File diffcoveragereport = new File(reportDiffDirectory, "index.html");
            Jsouphtml jsouphtml = new Jsouphtml(coveragereport, diffcoveragereport);
            CoverageData coverageData = jsouphtml.getCoverageData(taskId,"","","");
            System.out.println(new Date().toString()+coverageData.toString());
            Data data = HttpUtils.sendPostRequest(Config.SEND_COVERAGE_URL, coverageData);
        }catch (Exception e){
            e.printStackTrace();
            throw new DefinitionException(ErrorEnum.PRODUCT_REPORT.getErrorCode(),ErrorEnum.PRODUCT_REPORT.getErrorMsg());
        }
    }

    /**
     * 上传分支覆盖率报告
     */
    public void sendBranchCoverageData(File reportAllCovDirectory,File reportDiffDirectory,String appCode,String testedBranch,String basicBranch) throws DefinitionException{
        try {
            File coveragereport = new File(reportAllCovDirectory, "index.html");
            File  diffcoveragereport = new File(reportDiffDirectory, "index.html");

            Jsouphtml jsouphtml = new Jsouphtml(coveragereport, diffcoveragereport);
            CoverageData branchCoverageData = jsouphtml.getCoverageData(branchTaskID,appCode,testedBranch,basicBranch);
            System.out.println(new Date().toString()+branchCoverageData.toString());
            Data data = HttpUtils.sendPostRequest(Config.SEND_BRANCHCOVERAGE_URL, branchCoverageData);
        }catch (Exception e){
            e.printStackTrace();
            throw new DefinitionException(ErrorEnum.PRODUCT_REPORT.getErrorCode(),ErrorEnum.PRODUCT_REPORT.getErrorMsg());
        }
    }

    private void timerTask(Map<String,Object> applicationMap) throws Exception {

        final ExecutionDataClient executionDataClient = new ExecutionDataClient();
        timerMap.put(String.valueOf(taskId), new Timer());
        timerMap.get(String.valueOf(taskId)).schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    File executionDataFile = null;
                    File sourceDirectory = null;
                    File reportAllCovDirectory = new File(coverageReportPath, "coveragereport");////要保存报告的地址
                    File reportDiffDirectory = new File(coverageReportPath, "coveragediffreport");

                    ArrayList<File> classesDirectoryList = new ArrayList<>();
                    ArrayList<File> sourceDirectoryList = new ArrayList<>();

                    String addressIPList = applicationMap.getOrDefault("ip", "").toString();
                    //获取覆盖率生成数据
                    String[] iplist = addressIPList.split(",");
                    File gitlocalexecutionDataPath = new File(gitlocalPath,newBranchName.replace("/","_"));
                    if(!gitlocalexecutionDataPath.exists()){
                        gitlocalexecutionDataPath.mkdir();
                    }
                    File coverageexecutionDataPath = new File(coverageReportPath,newBranchName.replace("/","_"));
                    if(!coverageexecutionDataPath.exists()){
                        coverageexecutionDataPath.mkdir();
                    }
                    for (String serverip : iplist) {
                        //获取覆盖率生成数据
                        if (!serverip.isEmpty()) {
                            String[] portList = port.split(",");
                            for (String portNum:portList) {
                                //保持2分覆盖率数据,源代码gitlocalPath工程下存一份
                                executionDataFile = new File(gitlocalexecutionDataPath, serverip+"_jacoco.exec");//第一步生成的exec的文件
                                executionDataClient.getExecutionData(serverip, Integer.valueOf(portNum), executionDataFile);
                                //保存到taskID目录下再存一份
                                executionDataFile = new File(coverageexecutionDataPath, serverip+"_jacoco.exec");//第一步生成的exec的文件
                                boolean getedexecdata = executionDataClient.getExecutionData(serverip, Integer.valueOf(portNum), executionDataFile);
                                //如果取得覆盖率数据，判断是否有新版本
                                if (getedexecdata) {
                                    AnalyNewBuildVersion analyNewBuildVersion = new AnalyNewBuildVersion(applicationMap.get("classPath").toString(), executionDataFile.toString());
                                    Boolean newversion = analyNewBuildVersion.findNewBuildVersion();
                                    //如果存在新版本，则结束当前的覆盖率任务，同时删除本次覆盖率数据
                                    if (newversion) {
                                        System.out.println("exist new version");
                                        executionDataFile.delete();
                                        cancel();
                                        timerMap.remove(String.valueOf(taskId));
                                        HttpUtils.sendGet(Config.SEND_STOPTIMERTASK_URL + taskId);
                                    }
                                }
                            }
                        }
                    }
                    //如果超过24小时，覆盖率文件不更新，取消定时任务，避免CPU资源消耗
                    File allexecutionDataFile = new File(coverageexecutionDataPath, "jacocoAll.exec");
                    if (allexecutionDataFile.exists() && !AnalyNewBuildVersion.fileNotUpdateBy24Hours(allexecutionDataFile)) {
                        cancel();
                        timerMap.remove(String.valueOf(taskId));

                        HttpUtils.sendGet(Config.SEND_STOPTIMERTASK_URL + taskId);
                    }
                    Map<String, Object> sourceapplications = (Map) applicationMap.get("sourceapplications");
                    for (String key : sourceapplications.keySet()) {
                        String projectDirectoryStr = ((Map) sourceapplications.get(key)).getOrDefault("sourceDirectory", "").toString();
                        File projectDirectory = new File(projectDirectoryStr);
                        sourceDirectory = new File(projectDirectory, "src/main/java");//源码目录
                        if (sourceDirectory.exists()) {
                            sourceDirectoryList.add(sourceDirectory);
                        }

                    }
                    classesDirectoryList.add(new File(applicationMap.get("classPath").toString()));//目录下必须包含源码编译过的class文件,用来统计覆盖率。所以这里用server打出的jar包地址即可,运行的jar或者Class目录
                    //合并gitlocalPath目录覆盖率
                    MergeDump mergeDumpGitLocalPath = new MergeDump(gitlocalexecutionDataPath.toString());
                    mergeDumpGitLocalPath.executeMerge();

                    //合并taskID目录代码覆盖率
                    MergeDump mergeDump = new MergeDump(coverageexecutionDataPath.toString());
                    allexecutionDataFile = mergeDump.executeMerge();

                    if (allexecutionDataFile != null && !allexecutionDataFile.exists()) {
                        cancel();
                        timerMap.remove(String.valueOf(taskId));
                        if (isTimerTask == 1) {
                            HttpUtils.sendGet(Config.SEND_STOPTIMERTASK_URL + taskId);
                        }
                        throw new DefinitionException(ErrorEnum.JACOCO_EXEC_FAILED.getErrorCode(),ErrorEnum.JACOCO_EXEC_FAILED.getErrorMsg());
                    }

                    //生成整体覆盖率报告
                    createAll(allexecutionDataFile, classesDirectoryList, reportAllCovDirectory, coverageReportPath.getName(), sourceDirectoryList);
                    if (!newTag.equals(oldTag)) {
                        createDiff(classesDirectoryList, reportDiffDirectory, sourceDirectoryList, coverageReportPath.getName());
                    }
                    //上传覆盖率报告
                    sendcoveragedata(reportAllCovDirectory,reportDiffDirectory);
                    Thread.sleep(1000);
                    if (isTimerTask == 0) {
                        cancel();
                        timerMap.remove(String.valueOf(taskId));
                    }
                    if (timerMap.containsKey(String.valueOf(taskId))) {
                        System.out.println(applicationMap.get("applicationID").toString() + " taskId : " + taskId + " is timertask");
                    }
//                    //执行分支覆盖率任务
//                    if(isBranchTask == 1){
//                        startBranchCoverageTask(applicationMap);
//                    }
                } catch (DefinitionException e) {
                    HttpUtils.sendErrorMSG(taskId, e.getErrorMsg());
                } catch (Exception e) {
                    e.printStackTrace();
                    cancel();
                    timerMap.remove(String.valueOf(taskId));
                    if(isTimerTask == 1) {
                        HttpUtils.sendGet(Config.SEND_STOPTIMERTASK_URL + taskId);
                    }
                }
            }
        }, 0, 10000);
    }

    private void startBranchCoverageTask(Map<String,Object> applicationMap){
        try {
            File branchTaskCoverageReportPath = createBranchCoverageReportPathByTaskid(this.branchTaskID+"",newBranchName.replace("/","_"));
            File branchTaskPath = createCoverageReportPathByTaskid(this.branchTaskID+"");
            File branchclassPath = new File(branchTaskPath,"classes");
            File gitlocalexecutionDataPath = new File(gitlocalPath,newBranchName.replace("/","_"));
            File filterExecFile = filterBranchData(gitlocalexecutionDataPath,branchTaskCoverageReportPath,branchclassPath.toString());
            File sourceDirectory = null;

            File reportAllCovDirectory = new File(branchTaskCoverageReportPath, "totalcoveragereport");////要保存报告的地址
            File reportDiffDirectory = new File(branchTaskCoverageReportPath, "diffcoveragereport");

            ArrayList<File> classesDirectoryList = new ArrayList<>();
            ArrayList<File> sourceDirectoryList = new ArrayList<>();

            Map<String, Object> sourceapplications = (Map) applicationMap.get("sourceapplications");
            for (String key : sourceapplications.keySet()) {
                String projectDirectoryStr = ((Map) sourceapplications.get(key)).getOrDefault("sourceDirectory", "").toString();
                File projectDirectory = new File(projectDirectoryStr);
                sourceDirectory = new File(projectDirectory, "src/main/java");//源码目录
                if (sourceDirectory.exists()) {
                    sourceDirectoryList.add(sourceDirectory);
                }
            }

            classesDirectoryList.add(branchclassPath);//目录下必须包含源码编译过的class文件,用来统计覆盖率。所以这里用server打出的jar包地址即可,运行的jar或者Class目录

            //合并taskID目录代码覆盖率
            if (filterExecFile != null && !filterExecFile.exists()) {
                throw new DefinitionException(ErrorEnum.JACOCO_EXEC_FAILED.getErrorCode(),ErrorEnum.JACOCO_EXEC_FAILED.getErrorMsg());
            }

            //生成整体覆盖率报告
            createAll(filterExecFile, classesDirectoryList, reportAllCovDirectory, branchTaskCoverageReportPath.getName(), sourceDirectoryList);
            if (!newTag.equals(oldTag)) {

                createDiff(classesDirectoryList, reportDiffDirectory, sourceDirectoryList, branchTaskCoverageReportPath.getName());
            }
            //上传覆盖率报告
            sendBranchCoverageData(reportAllCovDirectory,reportDiffDirectory,applicationMap.get("applicationID").toString(),newBranchName.replace("/","_"),oldBranchName);
        } catch (DefinitionException e) {
            HttpUtils.sendErrorMSG(taskId, e.getErrorMsg());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String cloneCodeSource(String gitName,String gitPassword,String urlString,String codePath,String newBranchName,String oldBranchName,String newTag) throws DefinitionException{

        GitUtil gitUtil = new GitUtil(gitName,gitPassword);
        String projectName = gitUtil.getLastUrlString(urlString);
        File localPath = new File(codePath,projectName);
        //如果工程目录已存在，则不需要clone代码，直接返回
        if(!localPath.exists()){
            System.out.println("开始下载开发项目代码到本地");
            gitUtil.cloneRepository(urlString, localPath);
        }
        //checkout分支代码
        newBranchName = gitUtil.checkoutBranch(localPath.toString(),newBranchName,oldBranchName,newTag);
        return newBranchName;
    }

    /**
     * 解析应用模块对应的代码路径和部署的IP地址
     * {"applicationname":{"ip":"127.0.0.1","sourceDirectory":"D://codecoverage//applicationmodule//src"}}
     * @param applicationNames
     * @return
     */
    public static HashMap getApplicationSourceDirectoryp(ArrayList<File>  applicationNames){
        HashMap<String,HashMap> applicationNameMap = new HashMap<String ,HashMap>();
        for(File applicationPath : applicationNames){
            HashMap<String,Object> applicationInfo = new HashMap<>();
            applicationInfo.put("sourceDirectory",applicationPath.toString());
            applicationNameMap.put(applicationPath.getName(),applicationInfo);
        }
        return applicationNameMap;
    }
    public void startCoverageTask(String applicationID,String[] ignoreclassList,String[] ignorepackageList,String[] containPackages) throws Exception{
        HashMap<String, Object> projectMap = new HashMap<String, Object>();
        //通过git url地址解析应用名
        String projectName = GitUtil.getLastUrlString(this.applicationgitlabUrl);
        //生成开发git代码本地路径
        File localPath = new File(Config.CodePath,projectName);
        this.gitlocalPath = localPath.toString();
        //clone代码到本地
        newBranchName = cloneCodeSource(Config.GitName, Config.GitPassword, this.applicationgitlabUrl, Config.CodePath,newBranchName,oldBranchName,newTag);

        ArrayList filelist = new ArrayList();
        //解析工程中各个模块路径
        ArrayList<File> applicationNames = GitUtil.getApplicationNames(localPath, filelist);
        //模块绑定source地址
        Map sourceapplicationsMap = getApplicationSourceDirectoryp(applicationNames);
        projectMap.put("sourceapplications",sourceapplicationsMap);

        HashMap<String,Object> applicationHash = ColumbusUtils.getAppDeployInfoFromBuildVersionList(applicationID,versionname);
        String applicationIPList = applicationHash.get("applicationIP").toString();
        String repositoryUrl = applicationHash.get("repositoryUrl").toString();

        projectMap.put("ip",applicationIPList);
        //创建测试报告文件名
        File coverageReportPath = createCoverageReportPathByTaskid(this.taskId+"");
        this.coverageReportPath = coverageReportPath;
        //获取下载buildversion.zip包
        String downloadFilePath = ColumbusUtils.downloadColumbusBuildVersion(repositoryUrl,coverageReportPath.toString());
        //解压zip包获取class文件
        String classPath = ColumbusUtils.extractColumsBuildVersionClasses(downloadFilePath,new File(coverageReportPath,"classes").toString(),applicationID,sourceapplicationsMap);

        //过滤配置的ignore class,package文件
        ColumbusUtils.filterIgnoreClass(ignoreclassList,ignorepackageList,new File(classPath));

        //只统计指定包
        if(containPackages!= null && containPackages.length >0) {
            HashSet containPackagesSet = ColumbusUtils.getcontainPackageHashSet(containPackages);
            ColumbusUtils.filterContainPackages(containPackagesSet, new File(classPath));
        }

        projectMap.put("classPath",classPath);
        projectMap.put("applicationID",applicationID);

        //开始生成覆盖率报告任务
        timerTask(projectMap);

    }

    public static File filterBranchData(File localPath,File branchTaskCoverageReportPath,String classPath) throws Exception{
        //将当前的覆盖率数据做一轮清洗，过滤class文件中不存在的classID
        File filterExecFile = new File(branchTaskCoverageReportPath,"jacoco.exec");
        File jacocoAll =  new File(localPath,"jacocoAll.exec");
        if(jacocoAll.exists()) {
            AnalyExecData analyExecData = new AnalyExecData(filterExecFile.toString(), jacocoAll.toString());
            analyExecData.filterOldExecData(classPath);
        }
        return filterExecFile;
    }
    /**
     * Starts the report generation process
     *
     * @param args
     *            Arguments to the application. This will be the location of the
     *            eclipse projects that will be used to generate reports for
     * @throws IOException
     */
    public static void main(final String[] args) throws Exception {
        Long taskID = 10010L;
        String gitPath = "git@gitlab.os.adc.com:fin/p2p-loan-id/fin-loan.git";
        String testedBranch = "release/fin-2.3";
        String basicBranch = "master";
        String newTag = "aaa59b53e98ad5da5a4a5f7a49411dd114972fa5";
        String oldTag = "d57b610a37ae3b04287a98b55148cdd074e9c3a8";
        String versionName = "fin-loan-api-20200916145059-291";
        String applicationID = "fin-loan-api";
        String[] ignoreclassList = new String[]{};
        String[] ignorepackageList = new String[]{};
        ApplicationCodeInfo applicationCodeInfo = new ApplicationCodeInfo();
        applicationCodeInfo.setId(taskID);
        applicationCodeInfo.setGitPath(gitPath);
        applicationCodeInfo.setTestedBranch(testedBranch);
        applicationCodeInfo.setBasicBranch(basicBranch);
        applicationCodeInfo.setTestedCommitId(newTag);
        applicationCodeInfo.setBasicCommitId(oldTag);
        applicationCodeInfo.setVersionName(versionName);
        applicationCodeInfo.setApplicationID(applicationID);
        applicationCodeInfo.setIsTimerTask(0);
        applicationCodeInfo.setBranchTaskID(10010L);
        applicationCodeInfo.setIsBranchTask(1);
        try {
            ReportGeneratorCov reportGeneratorCov = new ReportGeneratorCov(applicationCodeInfo);
            reportGeneratorCov.startCoverageTask(applicationID, ignoreclassList, ignorepackageList, null);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}

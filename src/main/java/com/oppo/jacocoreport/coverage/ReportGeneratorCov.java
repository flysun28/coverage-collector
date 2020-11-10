package com.oppo.jacocoreport.coverage;

import com.oppo.jacocoreport.coverage.entity.ApplicationCodeInfo;
import com.oppo.jacocoreport.coverage.entity.CoverageData;
import com.oppo.jacocoreport.coverage.entity.Data;
import com.oppo.jacocoreport.coverage.jacoco.AnalyExecData;
import com.oppo.jacocoreport.coverage.jacoco.AnalyNewBuildVersion;
import com.oppo.jacocoreport.coverage.jacoco.ExecutionDataClient;
import com.oppo.jacocoreport.coverage.jacoco.MergeDump;
import com.oppo.jacocoreport.coverage.utils.*;
import com.oppo.jacocoreport.response.DefinitionException;
import com.oppo.jacocoreport.response.ErrorEnum;
import org.eclipse.jgit.util.StringUtils;
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
    private  String port = "";
    private  String gitName = "";
    private  String gitPassword = "";
    private String gitlocalPath = "";
    private CoverageBuilder coverageBuilder;
    private File coverageReportPath;
    private ApplicationCodeInfo applicationCodeInfo;


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
        this.port = applicationCodeInfo.getJacocoPort();
        if("".equals(this.port) ||"0".equals(this.port)|| null == this.port){
          this.port = ""+Config.Port;
        }
        this.gitName = Config.GitName;
        this.gitPassword = Config.GitPassword;
        this.applicationCodeInfo = applicationCodeInfo;

    }
    private File createCoverageReportPathBySysTime(){

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

    private File createCoverageReportPathByTaskid(String taskId){
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

    private File createBranchCoverageReportPathByTaskid(String branchTaskID,String newBranchName){
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
    private void createAll(ArrayList<File> classesDirectoryList,File reportAllCovDirectory,String title,ArrayList<File> sourceDirectoryList) throws Exception {

        // Read the jacoco.exec file. Multiple data files could be merged
        // at this point


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
        List<ClassInfo> classInfos = CodeDiff.diffTagToTag(gitlocalPath, applicationCodeInfo.getTestedBranch(), applicationCodeInfo.getTestedCommitId(), applicationCodeInfo.getBasicCommitId());
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
        coverageBuilder = new CoverageBuilder(gitlocalPath, applicationCodeInfo.getTestedBranch(), applicationCodeInfo.getTestedCommitId(), applicationCodeInfo.getBasicCommitId());
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
    public void sendcoveragedata(File reportAllCovDirectory,File reportDiffDirectory,int filterTask) throws DefinitionException{
        try {

            File coveragereport = new File(reportAllCovDirectory, "index.html");
            File diffcoveragereport = new File(reportDiffDirectory, "index.html");
            Jsouphtml jsouphtml = new Jsouphtml(coveragereport, diffcoveragereport);
            CoverageData coverageData = jsouphtml.getCoverageData(applicationCodeInfo.getId(),"","","");
            coverageData.setFilterTask(filterTask);
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
            CoverageData branchCoverageData = jsouphtml.getCoverageData(applicationCodeInfo.getBranchTaskID(),appCode,testedBranch,basicBranch);
            System.out.println(new Date().toString()+branchCoverageData.toString());
            Data data = HttpUtils.sendPostRequest(Config.SEND_BRANCHCOVERAGE_URL, branchCoverageData);
        }catch (Exception e){
            e.printStackTrace();
            throw new DefinitionException(ErrorEnum.PRODUCT_REPORT.getErrorCode(),ErrorEnum.PRODUCT_REPORT.getErrorMsg());
        }
    }
    /**
     * 上传项目覆盖率报告
     */
    public void sendVersionIDCoverageData(File reportAllCovDirectory,File reportDiffDirectory,String appCode,String testedBranch,String basicBranch) throws DefinitionException{
        try {
            File coveragereport = new File(reportAllCovDirectory, "index.html");
            File  diffcoveragereport = new File(reportDiffDirectory, "index.html");

            Jsouphtml jsouphtml = new Jsouphtml(coveragereport, diffcoveragereport);
            CoverageData versionIDCoverageData = jsouphtml.getCoverageData(applicationCodeInfo.getVersionId(),appCode,testedBranch,basicBranch);
            System.out.println(new Date().toString()+versionIDCoverageData.toString());
            Data data = HttpUtils.sendPostRequest(Config.SEND_VERSIONCOVERAGE_URL, versionIDCoverageData);
        }catch (Exception e){
            e.printStackTrace();
            throw new DefinitionException(ErrorEnum.PRODUCT_REPORT.getErrorCode(),ErrorEnum.PRODUCT_REPORT.getErrorMsg());
        }
    }
    private ArrayList<File> getSourceCode(Map<String,Object> applicationMap){
        File sourceDirectory = null;
        ArrayList<File> sourceDirectoryList = new ArrayList<>();
        Map<String, Object> sourceapplications = (Map)applicationMap.get("sourceapplications");
        for (String key : sourceapplications.keySet()) {
            String projectDirectoryStr = ((Map) sourceapplications.get(key)).getOrDefault("sourceDirectory", "").toString();
            File projectDirectory = new File(projectDirectoryStr);
            sourceDirectory = new File(projectDirectory, "src/main/java");//源码目录
            if (sourceDirectory.exists()) {
                sourceDirectoryList.add(sourceDirectory);
            }
        }
        return sourceDirectoryList;
    }
    private void timerTask(Map<String,Object> applicationMap) throws Exception {

        final ExecutionDataClient executionDataClient = new ExecutionDataClient();
        timerMap.put(String.valueOf(applicationCodeInfo.getId()), new Timer());
        timerMap.get(String.valueOf(applicationCodeInfo.getId())).schedule(new TimerTask() {
            @Override
            public void run() {
                File executionDataFile = null;
                File reportAllCovDirectory = new File(coverageReportPath, "coveragereport");////要保存报告的地址
                File reportDiffDirectory = new File(coverageReportPath, "coveragediffreport");
                File filterreportAllCovDirectory = new File(coverageReportPath, "filtercoveragereport");////要保存报告的地址
                File filterreportDiffDirectory = new File(coverageReportPath, "filtercoveragediffreport");

                ArrayList<File> classesDirectoryList = new ArrayList<>();
                ArrayList<File> sourceDirectoryList = new ArrayList<>();

                String addressIPList = applicationMap.getOrDefault("ip", "").toString();
                //获取覆盖率生成数据
                String[] iplist = addressIPList.split(",");
                //创建被测分支目录
                File gitlocalexecutionDataPath = new File(gitlocalPath,applicationCodeInfo.getTestedBranch().replace("/","_"));
                if(!gitlocalexecutionDataPath.exists()){
                    gitlocalexecutionDataPath.mkdir();
                }
                //创建项目id目录
                File versionIdDataPath = new File(gitlocalPath,applicationCodeInfo.getVersionId()+"");
                if(!versionIdDataPath.exists()){
                    versionIdDataPath.mkdir();
                }
                //创建测试taskID目录
                File coverageexecutionDataPath = new File(coverageReportPath,applicationCodeInfo.getTestedBranch().replace("/","_"));
                if(!coverageexecutionDataPath.exists()){
                    coverageexecutionDataPath.mkdir();
                }
                File allexecutionDataFile = new File(coverageexecutionDataPath, "jacocoAll.exec");
                try {
                    int ipindex = 0;
                    for (String serverip : iplist) {
                        //获取覆盖率生成数据
                        if (!serverip.isEmpty()) {
                            String[] portList = port.split(",");
                            for (String portNum:portList) {
                                //保持2分覆盖率数据,源代码gitlocalPath工程下存一份
                                executionDataFile = new File(gitlocalexecutionDataPath, serverip+System.currentTimeMillis()+"_jacoco.exec");//第一步生成的exec的文件
                                executionDataClient.getExecutionData(serverip, Integer.valueOf(portNum), executionDataFile);

                                executionDataFile = new File(versionIdDataPath, serverip+System.currentTimeMillis()+"_jacoco.exec");//第一步生成的exec的文件
                                executionDataClient.getExecutionData(serverip, Integer.valueOf(portNum), executionDataFile);

                                //保存到taskID目录下再存一份
                                executionDataFile = new File(coverageexecutionDataPath, serverip+System.currentTimeMillis()+"_jacoco.exec");//第一步生成的exec的文件
                                boolean getedexecdata = executionDataClient.getExecutionData(serverip, Integer.valueOf(portNum), executionDataFile);
                                //如果取得覆盖率数据，判断是否有新版本
                                if (getedexecdata) {
                                    AnalyNewBuildVersion analyNewBuildVersion = new AnalyNewBuildVersion(applicationMap.get("classPath").toString(), executionDataFile.toString());
                                    Boolean newversion = analyNewBuildVersion.findNewBuildVersion();
                                    //如果存在新版本，则结束当前的覆盖率任务，同时删除本次覆盖率数据
                                    if (newversion) {
                                        ipindex++;
                                        System.out.println("exist new version at "+serverip);
                                        executionDataFile.delete();
                                        //上报新版本
                                        if(ipindex == iplist.length) {
                                            cancel();
                                            if (applicationCodeInfo.getIsTimerTask() == 1) {
                                                timerMap.remove(String.valueOf(applicationCodeInfo.getId()));
                                                HttpUtils.sendGet(Config.SEND_STOPTIMERTASK_URL + applicationCodeInfo.getId());
                                            } else {
                                                throw new DefinitionException(ErrorEnum.DETECTED_NEW_VERSION.getErrorCode(), ErrorEnum.DETECTED_NEW_VERSION.getErrorMsg());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    //如果超过24小时，覆盖率文件不更新，取消定时任务，避免CPU资源消耗
                    if (allexecutionDataFile.exists() && !AnalyNewBuildVersion.fileNotUpdateByHours(allexecutionDataFile,24)) {
                        cancel();
                        timerMap.remove(String.valueOf(applicationCodeInfo.getId()));

                        HttpUtils.sendGet(Config.SEND_STOPTIMERTASK_URL + applicationCodeInfo.getId());
                    }

                    sourceDirectoryList = getSourceCode(applicationMap);
                    classesDirectoryList.add(new File(applicationMap.get("classPath").toString()));//目录下必须包含源码编译过的class文件,用来统计覆盖率。所以这里用server打出的jar包地址即可,运行的jar或者Class目录
                    //合并gitlocalPath目录覆盖率
                    MergeDump mergeDumpGitLocalPath = new MergeDump(gitlocalexecutionDataPath.toString());
                    mergeDumpGitLocalPath.executeMerge();

                    //合并versionIDPath目录覆盖率
                    MergeDump versionIDMerge = new MergeDump(versionIdDataPath.toString());
                    versionIDMerge.executeMerge();

                    //合并taskID目录代码覆盖率
                    MergeDump mergeDump = new MergeDump(coverageexecutionDataPath.toString());
                    allexecutionDataFile = mergeDump.executeMerge();

                    if (allexecutionDataFile == null || !allexecutionDataFile.exists()) {
                        cancel();
                        timerMap.remove(String.valueOf(applicationCodeInfo.getId()));
                        if (applicationCodeInfo.getIsTimerTask() == 1) {
                            HttpUtils.sendGet(Config.SEND_STOPTIMERTASK_URL + applicationCodeInfo.getId());
                        }
                        throw new DefinitionException(ErrorEnum.JACOCO_EXEC_FAILED.getErrorCode(),ErrorEnum.JACOCO_EXEC_FAILED.getErrorMsg());
                    }
                    loadExecutionData(allexecutionDataFile);
                    //生成差异化覆盖率报告
                    if (!applicationCodeInfo.getTestedCommitId().equals(applicationCodeInfo.getBasicCommitId())) {
                        createDiff(classesDirectoryList, reportDiffDirectory, sourceDirectoryList, coverageReportPath.getName());
                    }
                    //生成整体覆盖率报告
                    createAll(classesDirectoryList, reportAllCovDirectory, coverageReportPath.getName(), sourceDirectoryList);
                    //上传覆盖率报告
                    sendcoveragedata(reportAllCovDirectory,reportDiffDirectory,0);

                    //过滤class和package文件
                    filterClassAndPackage(applicationMap.get("classPath").toString());
                    if (!applicationCodeInfo.getTestedCommitId().equals(applicationCodeInfo.getBasicCommitId())) {
                        createDiff(classesDirectoryList, filterreportDiffDirectory, sourceDirectoryList, coverageReportPath.getName());
                    }
                    //生成整体覆盖率报告
                    createAll(classesDirectoryList, filterreportAllCovDirectory, coverageReportPath.getName(), sourceDirectoryList);
                    //上传覆盖率报告
                    sendcoveragedata(filterreportAllCovDirectory, filterreportDiffDirectory, 1);

                    Thread.sleep(1000);
                    if (applicationCodeInfo.getIsTimerTask() == 0) {
                        cancel();
                        timerMap.remove(String.valueOf(applicationCodeInfo.getId()));
                    }
                    if (timerMap.containsKey(String.valueOf(applicationCodeInfo.getId()))) {
                        System.out.println(applicationMap.get("applicationID").toString() + " taskId : " + applicationCodeInfo.getId() + " is timertask");
                    }
                    //创建项目覆盖率任务
                    if(applicationCodeInfo.getVersionId() != null){
                        startVersionCoverageTask(applicationMap);
                    }
                    //执行分支覆盖率任务
                    if(applicationCodeInfo.getIsBranchTask() == 1){
                        startBranchCoverageTask(applicationMap);
                    }

                } catch (DefinitionException e) {
                    HttpUtils.sendErrorMSG(applicationCodeInfo.getId(), e.getErrorMsg());
                    try {
                        //当覆盖率报告被删除后，重新生成覆盖率报告
                        if (allexecutionDataFile.exists() && !reportAllCovDirectory.exists()) {
                            loadExecutionData(allexecutionDataFile);
                            //生成差异化覆盖率报告
                            if (!applicationCodeInfo.getTestedCommitId().equals(applicationCodeInfo.getBasicCommitId())) {
                                createDiff(classesDirectoryList, reportDiffDirectory, sourceDirectoryList, coverageReportPath.getName());
                            }
                            //生成整体覆盖率报告
                            createAll(classesDirectoryList, reportAllCovDirectory, coverageReportPath.getName(), sourceDirectoryList);

                            //过滤class和package文件
                            filterClassAndPackage(applicationMap.get("classPath").toString());
                            if (!applicationCodeInfo.getTestedCommitId().equals(applicationCodeInfo.getBasicCommitId())) {
                                createDiff(classesDirectoryList, filterreportDiffDirectory, sourceDirectoryList, coverageReportPath.getName());
                            }
                            //生成整体覆盖率报告
                            createAll(classesDirectoryList, filterreportAllCovDirectory, coverageReportPath.getName(), sourceDirectoryList);

                        }
                    } catch (Exception ep) {
                        ep.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    cancel();
                    HttpUtils.sendErrorMSG(applicationCodeInfo.getId(), ErrorEnum.OTHER_ERROR.getErrorMsg());
                    timerMap.remove(String.valueOf(applicationCodeInfo.getId()));
                    if(applicationCodeInfo.getIsTimerTask() == 1) {
                        HttpUtils.sendGet(Config.SEND_STOPTIMERTASK_URL + applicationCodeInfo.getId());
                    }
                }
            }
        }, 0, 1800000);
    }
    private void startVersionCoverageTask(Map<String,Object> applicationMap){
        try {
            File versionIdDataPath = new File(gitlocalPath,applicationCodeInfo.getVersionId()+"");
            if(!versionIdDataPath.exists()){
                versionIdDataPath.mkdir();
            }
            File versionCoverageReportBasicPath = new File(versionIdDataPath,"coverage");
            if(!versionCoverageReportBasicPath.exists()){
                versionCoverageReportBasicPath.mkdir();
            }
            File versionIDclassPath = new File(applicationMap.get("classPath").toString());

            File filterExecFile = filterBranchData(versionIdDataPath,versionCoverageReportBasicPath,versionIDclassPath.toString());

            File versionAllCovDirectory = new File(versionCoverageReportBasicPath, "versioncoveragereport");////要保存报告的地址
            File versionDiffDirectory = new File(versionCoverageReportBasicPath, "versiondiffcoveragereport");

            ArrayList<File> classesDirectoryList = new ArrayList<>();
            ArrayList<File>  sourceDirectoryList = getSourceCode(applicationMap);
            classesDirectoryList.add(versionIDclassPath);//目录下必须包含源码编译过的class文件,用来统计覆盖率。所以这里用server打出的jar包地址即可,运行的jar或者Class目录

            //合并taskID目录代码覆盖率
            if (filterExecFile != null && !filterExecFile.exists()) {
                throw new DefinitionException(ErrorEnum.JACOCO_EXEC_FAILED.getErrorCode(),ErrorEnum.JACOCO_EXEC_FAILED.getErrorMsg());
            }
            loadExecutionData(filterExecFile);
            //生成差异化覆盖率
            if (!applicationCodeInfo.getTestedCommitId().equals(applicationCodeInfo.getBasicCommitId())) {
                createDiff(classesDirectoryList, versionDiffDirectory, sourceDirectoryList, versionCoverageReportBasicPath.getName());
            }
            //生成整体覆盖率报告
            createAll(classesDirectoryList, versionAllCovDirectory, versionCoverageReportBasicPath.getName(), sourceDirectoryList);
            //上传覆盖率报告
            sendVersionIDCoverageData(versionAllCovDirectory,versionDiffDirectory,applicationMap.get("applicationID").toString(),applicationCodeInfo.getTestedBranch().replace("/","_"),applicationCodeInfo.getBasicBranch());
        } catch (DefinitionException e) {
            HttpUtils.sendErrorMSG(applicationCodeInfo.getId(), e.getErrorMsg());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void startBranchCoverageTask(Map<String,Object> applicationMap){
        try {
            File branchTaskCoverageReportPath = createBranchCoverageReportPathByTaskid(applicationCodeInfo.getBranchTaskID()+"",applicationCodeInfo.getTestedBranch().replace("/","_"));
            File branchTaskPath = createCoverageReportPathByTaskid(applicationCodeInfo.getBranchTaskID()+"");
            File branchclassPath = new File(branchTaskPath,"classes");
            File gitlocalexecutionDataPath = new File(gitlocalPath,applicationCodeInfo.getTestedBranch().replace("/","_"));
            File filterExecFile = filterBranchData(gitlocalexecutionDataPath,branchTaskCoverageReportPath,branchclassPath.toString());
            File sourceDirectory = null;

            File reportAllCovDirectory = new File(branchTaskCoverageReportPath, "totalcoveragereport");////要保存报告的地址
            File reportDiffDirectory = new File(branchTaskCoverageReportPath, "diffcoveragereport");

            ArrayList<File> classesDirectoryList = new ArrayList<>();
            ArrayList<File> sourceDirectoryList = new ArrayList<>();

            sourceDirectoryList = getSourceCode(applicationMap);

            classesDirectoryList.add(branchclassPath);//目录下必须包含源码编译过的class文件,用来统计覆盖率。所以这里用server打出的jar包地址即可,运行的jar或者Class目录

            //合并taskID目录代码覆盖率
            if (filterExecFile != null && !filterExecFile.exists()) {
                throw new DefinitionException(ErrorEnum.JACOCO_EXEC_FAILED.getErrorCode(),ErrorEnum.JACOCO_EXEC_FAILED.getErrorMsg());
            }
            loadExecutionData(filterExecFile);
            //生成差异化覆盖率
            if (!applicationCodeInfo.getTestedCommitId().equals(applicationCodeInfo.getBasicCommitId())) {
                createDiff(classesDirectoryList, reportDiffDirectory, sourceDirectoryList, branchTaskCoverageReportPath.getName());
            }
            //生成整体覆盖率报告
            createAll(classesDirectoryList, reportAllCovDirectory, branchTaskCoverageReportPath.getName(), sourceDirectoryList);
            //上传覆盖率报告
            sendBranchCoverageData(reportAllCovDirectory,reportDiffDirectory,applicationMap.get("applicationID").toString(),applicationCodeInfo.getTestedBranch().replace("/","_"),applicationCodeInfo.getBasicBranch());
        } catch (DefinitionException e) {
            HttpUtils.sendErrorMSG(applicationCodeInfo.getId(), e.getErrorMsg());
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
    public HashMap getApplicationSourceDirectoryp(ArrayList<File>  applicationNames){
        HashMap<String,HashMap> applicationNameMap = new HashMap<String ,HashMap>();
        for(File applicationPath : applicationNames){
            HashMap<String,Object> applicationInfo = new HashMap<>();
            applicationInfo.put("sourceDirectory",applicationPath.toString());
            applicationNameMap.put(applicationPath.getName(),applicationInfo);
        }
        return applicationNameMap;
    }
    public void startCoverageTask(String applicationID) throws Exception{
        HashMap<String, Object> projectMap = new HashMap<String, Object>();
        //通过git url地址解析应用名
        String projectName = GitUtil.getLastUrlString(applicationCodeInfo.getGitPath());
        //生成开发git代码本地路径
        File localPath = new File(Config.CodePath,projectName);
        this.gitlocalPath = localPath.toString();
        //clone代码到本地
        String newBranchName = cloneCodeSource(Config.GitName, Config.GitPassword, applicationCodeInfo.getGitPath(), Config.CodePath,applicationCodeInfo.getTestedBranch(),applicationCodeInfo.getBasicBranch(),applicationCodeInfo.getTestedCommitId());
        applicationCodeInfo.setTestedCommitId(newBranchName);
        ArrayList filelist = new ArrayList();
        //解析工程中各个模块路径
        ArrayList<File> applicationNames = GitUtil.getApplicationNames(localPath, filelist);
        //模块绑定source地址
        Map sourceapplicationsMap = getApplicationSourceDirectoryp(applicationNames);
        projectMap.put("sourceapplications",sourceapplicationsMap);

        HashMap<String,Object> applicationHash = ColumbusUtils.getAppDeployInfoFromBuildVersionList(applicationID,applicationCodeInfo.getVersionName());
        String applicationIPList = applicationHash.get("applicationIP").toString();
        String repositoryUrl = applicationHash.get("repositoryUrl").toString();
        if (!StringUtils.isEmptyOrNull(applicationCodeInfo.getIp())) {
            projectMap.put("ip",applicationCodeInfo.getIp());
        }else {
            projectMap.put("ip", applicationIPList);
        }
        //创建测试报告文件名
        File coverageReportPath = createCoverageReportPathByTaskid(applicationCodeInfo.getId()+"");
        this.coverageReportPath = coverageReportPath;
        //获取下载buildversion.zip包
        String downloadFilePath = ColumbusUtils.downloadColumbusBuildVersion(repositoryUrl,coverageReportPath.toString());
        //解压zip包获取class文件
        String classPath = ColumbusUtils.extractColumsBuildVersionClasses(downloadFilePath,new File(coverageReportPath,"classes").toString(),applicationID,sourceapplicationsMap);

        projectMap.put("classPath",classPath);
        projectMap.put("applicationID",applicationID);

        //开始生成覆盖率报告任务
        timerTask(projectMap);

    }
    public void filterClassAndPackage(String classPath){
        String applicationID = applicationCodeInfo.getApplicationID();
        String ignoreclassStr = applicationCodeInfo.getIgnoreClass();
        String ignorepackageStr = applicationCodeInfo.getIgnorePackage();
        String containPackagesStr = applicationCodeInfo.getContainPackages();
        String[] ignoreclassList = new String[]{};
        String[] ignorepackageList = new String[]{};
        String[] containPackagesList = new String[]{};
        if(ignoreclassStr != null && !ignoreclassStr.equals("")){
            ignoreclassList = ignoreclassStr.split(",");
        }
        if(ignorepackageStr != null && !ignorepackageStr.equals("")){
            ignorepackageList = ignorepackageStr.split(",");
        }
        if(containPackagesStr != null && !containPackagesStr.equals("")){
            containPackagesList = containPackagesStr.split(",");
        }
        //过滤配置的ignore class,package文件
        ColumbusUtils.filterIgnoreClass(ignoreclassList,ignorepackageList,new File(classPath));
        //只统计指定包
        if(containPackagesList!= null && containPackagesList.length >0) {
            HashSet containPackagesSet = ColumbusUtils.getcontainPackageHashSet(containPackagesList,classPath);
            ColumbusUtils.filterContainPackages(containPackagesSet, new File(classPath));
        }
    }

    public File filterBranchData(File localPath,File branchTaskCoverageReportPath,String classPath) throws Exception{
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
        Long taskID = 10032L;
        String gitPath = "git@gitlab.os.adc.com:fin/p2p-loan-id/fin-loan.git";
        String testedBranch = "release/image";
        String basicBranch = "master";
        String newTag = "41fb7c2d181c26b432d72dadc942086c90d1cc0f";
        String oldTag = "ec918df9d7fe6288700c499f66de419fae686f4d";
        String versionName = "fin-loan-api-20201106111230-368";
        String applicationID = "fin-loan-api";
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
        applicationCodeInfo.setBranchTaskID(10032L);
        applicationCodeInfo.setIsBranchTask(0);
        applicationCodeInfo.setJacocoPort("8098");
        applicationCodeInfo.setVersionId(1002L);
        try {
            ReportGeneratorCov reportGeneratorCov = new ReportGeneratorCov(applicationCodeInfo);
            reportGeneratorCov.startCoverageTask(applicationID);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}

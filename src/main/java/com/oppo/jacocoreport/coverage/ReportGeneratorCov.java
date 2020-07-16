package com.oppo.jacocoreport.coverage;

import com.oppo.jacocoreport.coverage.cloud.AppDeployInfo;
import com.oppo.jacocoreport.coverage.entity.CoverageData;
import com.oppo.jacocoreport.coverage.entity.Data;
import com.oppo.jacocoreport.coverage.jacoco.ExecutionDataClient;
import com.oppo.jacocoreport.coverage.jacoco.MergeDump;
import com.oppo.jacocoreport.coverage.maven.Maveninvoker;
import com.oppo.jacocoreport.coverage.utils.*;
import com.oppo.jacocoreport.coverage.yaml.ReadYml;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.internal.diff.GitAdapter;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MultiSourceFileLocator;
import org.jacoco.report.html.HTMLFormatter;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 生成覆盖率报告主入口
 */

public class ReportGeneratorCov {
    private Long taskId = 0L;
    private  int port = 0;
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
    private String taskIdPath = "";

    private ExecFileLoader execFileLoader;

    /**
     * Create a new generator based for the given project.
     *
     * @param
     */
    public ReportGeneratorCov(Long taskId,String applicationgitlabUrl,String newBranchName,String versionname,String oldBranchName,String newTag,String oldTag) {
        //从配置文件中获取当期工程的source目录，以及服务ip地址
        this.taskId = taskId;
        this.port = Config.Port;
        this.gitName = Config.GitName;
        this.gitPassword = Config.GitPassword;
        this.applicationgitlabUrl = applicationgitlabUrl;
        this.newBranchName = newBranchName;
        this.oldBranchName = oldBranchName;
        this.versionname = versionname;
        this.newTag = newTag;
        this.oldTag = oldTag;

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

        File file = new File(Config.ReportBasePath,taskId);
        if(!file.exists()){
            if(!file.mkdir()){
                System.out.println("当前路径不存在，创建失败");
            }
        }
        System.out.println("创建成功"+taskId);
        return file;
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
            final IBundleCoverage bundleCoverageDiff = analyzeStructureDiff(classesDirectoryList, title);
//            if(bundleCoverageDiff.getClassCounter().getTotalCount() > 0) {
                createReport(bundleCoverageDiff, reportDiffDirectory, sourceDirectoryList);
//            }
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
        final Analyzer analyzer = new Analyzer(
                execFileLoader.getExecutionDataStore(), coverageBuilder);
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
        if(!oldBranchName.trim().equals("")){
            coverageBuilder = new CoverageBuilder(gitlocalPath,newBranchName,oldBranchName);
        }else{
            //基于Tag比较的覆盖 参数1：本地仓库，参数2：代码分支，参数3：新Tag(预发版本)，参数4：基线Tag（变更前的版本）
            //final CoverageBuilder coverageBuilder = new CoverageBuilder("E:\\Git-pro\\JacocoTest","daily","v004","v003");
            coverageBuilder = new CoverageBuilder(gitlocalPath,newBranchName,newTag,oldTag);
        }
//        if(coverageBuilder.getClasses().size() > 0) {
            final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);
            for (File classesDirectory : classesDirectoryList) {
                analyzer.analyzeAll(classesDirectory);
            }
//        }
        return coverageBuilder.getBundle(title);
    }

    /**
     * 上传覆盖率报告
     */
    public void sendcoveragedata(){
        try {
            CoverageData coverageData = new CoverageData();
            File coveragereport = new File(this.coverageReportPath, "coveragereport");
            if (coveragereport.exists()) {
                coveragereport = new File(coveragereport, "index.html");
            }
            System.out.println("coveragereport path"+coveragereport.toString());
            File diffcoveragereport = new File(this.coverageReportPath, "coveragediffreport");
            if (diffcoveragereport.exists()) {
                diffcoveragereport = new File(diffcoveragereport, "index.html");
            }
            System.out.println("diffcoveragereport path"+diffcoveragereport.toString());
            Jsouphtml jsouphtml = new Jsouphtml(coveragereport, diffcoveragereport);
            coverageData = jsouphtml.getCoverageData(taskId);
            System.out.println(coverageData.toString());
            String requstUrl = Config.SEND_COVERAGE_URL;
            Data data = HttpUtils.sendPostRequest(requstUrl, coverageData);
            System.out.println("send coveragedata" + data.getCode());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void timerTask(Map<String,Object> applicationMap) {
        final ExecutionDataClient executionDataClient = new ExecutionDataClient();
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
                try {
                    File executionDataFile = null;
                    File classesDirectory = null;
                    File sourceDirectory = null;
                    File reportAllCovDirectory = new File(coverageReportPath,"coveragereport");////要保存报告的地址
                    File reportDiffDirectory = new File(coverageReportPath,"coveragediffreport");

                    ArrayList<File> classesDirectoryList = new ArrayList<>();
                    ArrayList<File> sourceDirectoryList = new ArrayList<>();
                    for (String key : applicationMap.keySet()) {
                        String projectDirectoryStr = ((Map)applicationMap.get(key)).getOrDefault("sourceDirectory","").toString();
                        String addressIPList = ((Map)applicationMap.get(key)).getOrDefault("ip","").toString();
                        File projectDirectory = new File(projectDirectoryStr);


                        classesDirectory = new File(projectDirectory,"target/classes");//目录下必须包含源码编译过的class文件,用来统计覆盖率。所以这里用server打出的jar包地址即可,运行的jar或者Class目录
                        sourceDirectory = new File(projectDirectory, "src/main/java");//源码目录
                        String[] iplist = addressIPList.split(",");
                        for(String serverip : iplist) {
                            //获取覆盖率生成数据
                            if(!serverip.isEmpty()) {
                                executionDataFile = new File(coverageReportPath, serverip+key+"_jacoco.exec");//第一步生成的exec的文件
                                executionDataClient.getExecutionData(serverip, port, executionDataFile.toString());
                            }
                        }
                        if(classesDirectory.exists()) {
                            classesDirectoryList.add(classesDirectory);
                            sourceDirectoryList.add(sourceDirectory);
                        }

                    }
                    //合并代码覆盖率
                    MergeDump mergeDump = new MergeDump(coverageReportPath.toString());
                    File allexecutionDataFile =  mergeDump.executeMerge();
                    //生成整体覆盖率报告
                    System.out.println("begin product total coverage report");
                    createAll(allexecutionDataFile,classesDirectoryList,reportAllCovDirectory,coverageReportPath.getName(),sourceDirectoryList);
                    System.out.println("begin product diff coverage report");
                    createDiff(classesDirectoryList,reportDiffDirectory,sourceDirectoryList,coverageReportPath.getName());
//                    Thread.sleep(1000);
                    //上传覆盖率报告
                    System.out.println("begin upload coveragedata");
                    sendcoveragedata();

                }catch (Exception e){
                    e.getStackTrace();
                }
            }
//        },0,60000);
//    }
    private Boolean checkApplicationsIP(Map<String,Object> applicationMap){

        for (String key : applicationMap.keySet()) {
            String addressIP = ((Map)applicationMap.get(key)).getOrDefault("ip","").toString();
            if(addressIP.equals("127.0.0.2")){
                return true;
            }
        }
        return false;
    }
    private File cloneCodeSource(String gitName,String gitPassword,String urlString,String codePath,String newBranchName,String oldBranchName){

        GitUtil gitUtil = new GitUtil(gitName,gitPassword);
        String projectName = gitUtil.getLastUrlString(urlString);
        File localPath = new File(codePath,projectName);
        //如果工程目录已存在，则不需要clone代码，直接返回
        if(!localPath.exists()){
            System.out.println("开始下载开发项目代码到本地");
            gitUtil.cloneRepository(urlString, localPath);
        }
        //checkout分支代码
        gitUtil.checkoutBranch(localPath.toString(),newBranchName,oldBranchName);
        return localPath;
    }

    /**
     * 解析应用模块对应的代码路径和部署的IP地址
     * {"applicationname":{"ip":"127.0.0.1","sourceDirectory":"D://codecoverage//applicationmodule//src"}}
     * @param applicationNames
     * @param versionname
     * @return
     */
    private HashMap getApplicationIPMap(ArrayList<File>  applicationNames,String versionname){
        HashMap<String,HashMap> applicationNameMap = new HashMap<String ,HashMap>();
        for(File applicationPath : applicationNames){
            HashMap<String,Object> applicationInfo = new HashMap<>();
            StringBuffer iplist = ColumbusUtils.getAppDeployInfoList(versionname);
            applicationInfo.put("ip", iplist.toString());
            applicationInfo.put("sourceDirectory",applicationPath.toString());
            applicationNameMap.put(applicationPath.getName(),applicationInfo);
        }
        return applicationNameMap;
    }
    public void startCoverageTask(){
        //通过git url地址解析应用名
        String projectName = GitUtil.getLastUrlString(this.applicationgitlabUrl);
        //生成开发git代码本地路径
        File localPath = new File(Config.CodePath,projectName);
        this.gitlocalPath = localPath.toString();
        //clone代码到本地
        cloneCodeSource(Config.GitName, Config.GitPassword, this.applicationgitlabUrl, Config.CodePath,newBranchName,oldBranchName);
        ArrayList filelist = new ArrayList();
        //解析工程中各个模块路径
        ArrayList<File> applicationNames = GitUtil.getApplicationNames(localPath, filelist);
        //模块绑定source地址，以及对应部署的服务地址
        Map applicationNameMap = getApplicationIPMap(applicationNames, versionname);
        //编译项目源代码生成classes文件
        File pompath = new File(localPath.toString(), "pom.xml");
        if (!pompath.exists()) {
            System.out.println("请检查项目pom.xml地址是否存在");
            return;
        }
        Maveninvoker.buildMaven(pompath, Config.MAVENPATH);
        //创建测试报告文件名
        File coverageReportPath = createCoverageReportPathByTaskid(this.taskId+"");
        this.coverageReportPath = coverageReportPath;
        //开始生成覆盖率报告任务
        timerTask(applicationNameMap);

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
        Long taskID = 123456789L;
        String gitPath = "git@gitlab.os.adc.com:cql/CIdemo.git";
        String testedBranch = "feature/cov";
        String basicBranch = "master";
        String newTag = "463e9574257c3d28693c4780688b18f1b7918dc2";
        String oldTag = "04a4134be9b1d6ee04eca362ab4c6182d3b71e0a";
        String versionName = "ci-demo-20200703154236-28";

        ReportGeneratorCov reportGeneratorCov = new ReportGeneratorCov(taskID,gitPath,testedBranch,versionName,basicBranch,newTag,oldTag);
        reportGeneratorCov.startCoverageTask();

    }
}

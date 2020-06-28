package com.oppo.test.jacocoreport.controller;

import com.oppo.test.jacocoreport.jacoco.ExecutionDataClient;
import com.oppo.test.jacocoreport.maven.Maveninvoker;
import com.oppo.test.jacocoreport.utils.ColumbusUtils;
import com.oppo.test.jacocoreport.utils.GitUtil;
import com.oppo.test.jacocoreport.yaml.ReadYml;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.internal.diff.GitAdapter;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportGeneratorCov {
    private final String title;

    private final File executionDataFile;
    private final File classesDirectory;
    private final File sourceDirectory;
    private final File reportAllCovDirectory;
    private final File reportDiffDirectory;
    private String environment = "";
    private String addressIP = "";
    private int port = 0;
    private String gitName = "";
    private String gitPassword = "";
    private String gitPath = "";
    private String newBranchName = "";
    private String oldBranchName = "";
    private String newTag = "";
    private String oldTag = "";
    private File projectDirectory;
    private CoverageBuilder coverageBuilder;


    private ExecFileLoader execFileLoader;

    /**
     * Create a new generator based for the given project.
     *
     * @param
     */
    public ReportGeneratorCov(Map sourceInfo, int port, String projectName,
                              String gitName, String gitPassword, String gitPath,
                              String newBranchName, String oldBranchName, String newTag, String oldTag, File coverageReportPath) {
        //从配置文件中获取当期工程的source目录，以及服务ip地址
        String projectDirectory = sourceInfo.getOrDefault("sourceDirectory", "").toString();
        String addressIP = sourceInfo.getOrDefault("ip", "").toString();
        System.out.println("projectDirectory " + projectDirectory);
        System.out.println("addressIP " + addressIP);

        this.addressIP = addressIP;
        this.projectDirectory = new File(projectDirectory);
        this.title = this.projectDirectory.getName();
        File reprotTimeFolder = new File(coverageReportPath, this.title);
        if (!reprotTimeFolder.exists()) {
            reprotTimeFolder.mkdir();
        }

        this.executionDataFile = new File(reprotTimeFolder, "jacoco.exec");//第一步生成的exec的文件
        this.classesDirectory = new File(projectDirectory, "target/classes");//目录下必须包含源码编译过的class文件,用来统计覆盖率。所以这里用server打出的jar包地址即可,运行的jar或者Class目录
        this.sourceDirectory = new File(projectDirectory, "src/main/java");//源码目录
        this.reportAllCovDirectory = new File(reprotTimeFolder, "coveragereport");////要保存报告的地址
        this.reportDiffDirectory = new File(reprotTimeFolder, "coveragediffreport");
        this.environment = environment;
        this.port = port;
        this.gitName = gitName;
        this.gitPassword = gitPassword;
        this.gitPath = gitPath;
        this.newBranchName = newBranchName;
        this.oldBranchName = oldBranchName;
        this.newTag = newTag;
        this.oldTag = oldTag;

    }

    public static File createCoverageReportPathBySysTime() {

        //1.读取系统时间
        Calendar calendar = Calendar.getInstance();
        Date time = calendar.getTime();

        //2.格式化系统时间
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = format.format(time);
        File file = new File(fileName);
        if (!file.exists()) {
            if (!file.mkdir()) {
                System.out.println("当前路径不存在，创建失败");
            }
        }
        System.out.println("创建成功" + fileName);
        return file;
    }

    /**
     * Create the report.
     *
     * @throws IOException
     */
    public void createAll() throws IOException {

        // Read the jacoco.exec file. Multiple data files could be merged
        // at this point
        loadExecutionData();

        // Run the structure analyzer on a single class folder to build up
        // the coverage model. The process would be similar if your classes
        // were in a jar file. Typically you would create a bundle for each
        // class folder and each jar you want in your report. If you have
        // more than one bundle you will need to add a grouping node to your
        // report
        final IBundleCoverage bundleCoverage = analyzeStructure();

        createReport(bundleCoverage, reportAllCovDirectory);

    }

    public void createDiff() throws IOException {
        //差异化代码覆盖率
        if (gitName != "" && gitPassword != "") {
            final IBundleCoverage bundleCoverageDiff = analyzeStructureDiff();
            createReport(bundleCoverageDiff, reportDiffDirectory);
        }
    }

    private void createReport(final IBundleCoverage bundleCoverage, File reportDir)
            throws IOException {

        // Create a concrete report visitor based on some supplied
        // configuration. In this case we use the defaults
        final HTMLFormatter htmlFormatter = new HTMLFormatter();
        final IReportVisitor visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(reportDir));

        // Initialize the report with all of the execution and session
        // information. At this point the report doesn't know about the
        // structure of the report being created
        visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(), execFileLoader.getExecutionDataStore().getContents());

        // Populate the report structure with the bundle coverage information.
        // Call visitGroup if you need groups in your report.
        visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(sourceDirectory, "utf-8", 4));


//		//多源码路径
//        MultiSourceFileLocator sourceLocator = new MultiSourceFileLocator(4);
//        sourceLocator.add( new DirectorySourceFileLocator(sourceDirectory1, "utf-8", 4));
//        sourceLocator.add( new DirectorySourceFileLocator(sourceDirectory2, "utf-8", 4));
//        sourceLocator.add( new DirectorySourceFileLocator(sourceDirectoryN, "utf-8", 4));
//        visitor.visitBundle(bundleCoverage,sourceLocator);

        // Signal end of structure information to allow report to write all
        // information out
        visitor.visitEnd();

    }

    private void loadExecutionData() throws IOException {
        execFileLoader = new ExecFileLoader();
        execFileLoader.load(executionDataFile);
    }

    private IBundleCoverage analyzeStructure() throws IOException {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(
                execFileLoader.getExecutionDataStore(), coverageBuilder);

        analyzer.analyzeAll(classesDirectory);
        return coverageBuilder.getBundle(title);
    }

    /**
     * 生成差异化覆盖率
     *
     * @return
     * @throws IOException
     */
    private IBundleCoverage analyzeStructureDiff() throws IOException {
        //全量覆盖
//		final CoverageBuilder coverageBuilder = new CoverageBuilder();


        //基于分支比较覆盖，参数1：本地仓库，参数2：开发分支（预发分支），参数3：基线分支(不传时默认为master)
        //本地Git路径，新分支 第三个参数不传时默认比较maser，传参数为待比较的基线分支
        //"D:\\tools\\JacocoTest","daily","master"
        GitAdapter.setCredentialsProvider(gitName, gitPassword);
        if (oldBranchName != "") {
            coverageBuilder = new CoverageBuilder(gitPath, newBranchName, oldBranchName);
        } else {
            //基于Tag比较的覆盖 参数1：本地仓库，参数2：代码分支，参数3：新Tag(预发版本)，参数4：基线Tag（变更前的版本）
            //final CoverageBuilder coverageBuilder = new CoverageBuilder("E:\\Git-pro\\JacocoTest","daily","v004","v003");
            coverageBuilder = new CoverageBuilder(gitPath, newBranchName, newTag, oldTag);
        }

        final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);
        analyzer.analyzeAll(classesDirectory);
        return coverageBuilder.getBundle(title);
    }

    public void timerTask() {
        final ExecutionDataClient executionDataClient = new ExecutionDataClient();
        if (!this.classesDirectory.exists()) {
            System.out.println("请先编译源代码，生成classes文件");
            return;
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    //获取覆盖率生成数据
                    executionDataClient.getExecutionData(addressIP, port, executionDataFile.toString());
                    //生成整体覆盖率报告
                    createAll();
                    createDiff();
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }
        }, 0, 60000);
    }

    public static Boolean checkApplicationsIP(Map<String, Object> applicationMap) {

        for (String key : applicationMap.keySet()) {
            String addressIP = ((Map) applicationMap.get(key)).getOrDefault("ip", "").toString();
            if (addressIP.equals("127.0.0.2")) {
                return true;
            }
        }
        return false;
    }

    public static File cloneCodeSource(String gitName, String gitPassword, String urlString, String codePath, String newBranchName, String oldBranchName) {

        GitUtil gitUtil = new GitUtil(gitName, gitPassword);
        String projectName = gitUtil.getLastUrlString(urlString);
        File localPath = new File(codePath, projectName);
        //如果工程目录已存在，则不需要clone代码，直接返回
        if (!localPath.exists()) {
            System.out.println("开始下载开发项目代码到本地");
            gitUtil.cloneRepository(urlString, localPath);
        }
        //checkout分支代码
        gitUtil.checkoutBranch(localPath.toString(), newBranchName, oldBranchName);
        return localPath;
    }

    public static HashMap getApplicationIPMap(ArrayList<File> applicationNames, String branchName, String environmentname, String isApollo) {
        HashMap<String, HashMap> applicationNameMap = new HashMap<String, HashMap>();
        for (File applicationPath : applicationNames) {
            HashMap<String, Object> applicationInfo = new HashMap<>();
            if (isApollo.equals("1")) {
                applicationInfo.put("ip", "127.0.0.2");
                applicationInfo.put("sourceDirectory", applicationPath.toString());
                applicationNameMap.put(applicationPath.getName(), applicationInfo);
            } else {
                String applicationIP = ColumbusUtils.getAppDeployInfoFromBuildVersionList(applicationPath.getName(), branchName, environmentname);
                if (applicationIP != null && applicationIP != "") {
                    applicationInfo.put("ip", applicationIP);
                    applicationInfo.put("sourceDirectory", applicationPath.toString());
                    applicationNameMap.put(applicationPath.getName(), applicationInfo);
                }
            }

        }
        return applicationNameMap;
    }

    /**
     * Starts the report generation process
     *
     * @param args Arguments to the application. This will be the location of the
     *             eclipse projects that will be used to generate reports for
     * @throws IOException
     */
    public static void main(final String[] args) throws Exception {

        String f = "setting.properties";
        Properties props = new Properties();
        props.load(new java.io.FileInputStream(f));
        String resourceName = props.getProperty("resourceName", "");
        String environment = props.getProperty("environment", "");
        int port = Integer.parseInt(props.getProperty("port", ""));
        String gitName = props.getProperty("gitName", "");
        String gitPassword = props.getProperty("gitPassword", "");
        String newBranchName = props.getProperty("newBranchName", "");
        String oldBranchName = props.getProperty("oldBranchName", "");
        String newTag = props.getProperty("newTag", "");
        String oldTag = props.getProperty("oldTag", "");
        String mavenhome = props.getProperty("mavenhome", "");
        String codePath = props.getProperty("codepath", "");
        String gitUrl = props.getProperty("gitUrl", "");
        String isApollo = props.getProperty("isApollo", "0");


        System.out.println("resourceName : " + resourceName);
        System.out.println("environment : " + environment);
        System.out.println("port : " + port);
        System.out.println("gitName : " + gitName);
        System.out.println("gitPassword : " + gitPassword);
        System.out.println("oldBranchName : " + oldBranchName);
        System.out.println("mavenhome : " + mavenhome);


        if (newBranchName == "") {
            System.out.println("please set new branch name");
            return;
        }
        if (!new File(mavenhome).exists()) {
            System.out.println("Maven Home目录未设置");
            return;
        }
        String projectName = GitUtil.getLastUrlString(gitUrl);
        File localPath = new File(codePath, projectName);

        //clone代码到本地
        ReportGeneratorCov.cloneCodeSource(gitName, gitPassword, gitUrl, codePath, newBranchName, oldBranchName);
        //读取应用列表,并写入yaml配置文件
        System.out.println("读取应用列表,并写入yaml配置文件");

        Map<String, Object> applicationMap = ReadYml.getInstance().getValuesByRootKey(resourceName, environment);
        if (applicationMap.size() == 0) {
            ArrayList filelist = new ArrayList();
            ArrayList<File> applicationNames = GitUtil.getApplicationNames(localPath, filelist);
            Map applicationNameMap = ReportGeneratorCov.getApplicationIPMap(applicationNames, newBranchName, environment, isApollo);
            ReadYml.getInstance().setValueByMap(resourceName, environment, applicationNameMap);

        }
        //检查应用测试环境是否配置
        applicationMap = ReadYml.getInstance().getValuesByRootKey(resourceName, environment);
        boolean ipNeedUpdata = ReportGeneratorCov.checkApplicationsIP(applicationMap);
        if (ipNeedUpdata) {
            System.out.println("请先修改 " + resourceName + "  " + environment + "下的测试环境IP地址");
            return;
        }
        //编译项目源代码生成classes文件
        File pompath = new File(localPath.toString(), "pom.xml");
        if (!pompath.exists()) {
            System.out.println("请检查项目pom.xml地址是否存在");
            return;
        }

        //编译项目源代码生成classes文件
        Maveninvoker.buildMaven(pompath, mavenhome);
        //创建测试报告文件名
        File coverageReportPath = ReportGeneratorCov.createCoverageReportPathBySysTime();
        for (String key : applicationMap.keySet()) {
            ReportGeneratorCov reportGeneratorCov = new ReportGeneratorCov((Map) applicationMap.get(key), port, key, gitName, gitPassword, localPath.toString(), newBranchName, oldBranchName, newTag, oldTag, coverageReportPath);
            reportGeneratorCov.timerTask();
            Thread.sleep(1000);
        }
    }
}

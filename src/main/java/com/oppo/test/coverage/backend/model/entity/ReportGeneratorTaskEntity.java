package com.oppo.test.coverage.backend.model.entity;

import com.oppo.test.coverage.backend.util.GitUtil;
import com.oppo.test.coverage.backend.util.SpringContextUtil;
import com.oppo.test.coverage.backend.util.SystemConfig;
import org.jacoco.core.tools.ExecFileLoader;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author 80264236
 * @date 2021/4/12 11:24
 */

public class ReportGeneratorTaskEntity {

    /**
     * 管理模块下发的信息
     */
    private ApplicationCodeInfo appInfo;

    /**
     * 根据git地址解析出的工程名称
     */
    private String projectName;

    /**
     * 本地代码存储路径
     */
    private File gitLocalPath;

    /**
     * 存储分支的覆盖率数据路径: xxx/projectCovPath/${projectName}
     */
    private String projectCovPath;

    /**
     * jacoco配置端口
     */
    private String[] port;

    /**
     * ip列表
     */
    private List<String> ipList;

    /**
     * 解析应用模块对应的代码路径和部署的IP地址
     */
    private Map<String, Object> sourceApplicationsMap;

    /**
     * 忽略类集合
     */
    private String[] ignoreClassList;

    /**
     * 忽略包集合
     */
    private String[] ignorePackageList;

    /**
     * 仅包含包集合
     */
    private String[] containPackageList;

    /**
     * class文件路径
     */
    private String classPath;

    private ExecFileLoader execFileLoader;


    //-------------------------------------------- file start----------------------------------------------------

    /**
     * 当前任务路径: xxx/taskId/${taskId}
     */
    private File coverageReportPath;

    /**
     * 源码路径列表
     */
    private ArrayList<File> sourceDirectoryList;

    /**
     * 未过滤覆盖率全量报告文件
     */
    private File reportAllCovDirectory;

    /**
     * 未过滤覆盖率增量报告文件
     */
    private File reportDiffDirectory;

    /**
     * 已过滤覆盖率全量报告文件
     */
    private File filterReportAllCovDirectory;

    /**
     * 已过滤覆盖率增量报告文件
     */
    private File filterReportDiffDirectory;

    /**
     * 被测分支目录
     */
    private File testedBranchCoverageDirectory;

    /**
     * 版本测试报告数据存储
     */
    private File versionIdDataPath;

    /**
     * 当前taskId-branch数据目录
     */
    private File coverageExecutionDataPath;

    /**
     * jacocoAll文件
     */
    private File allExecutionDataFile;

    //-------------------------------------------- file end----------------------------------------------------


    public ReportGeneratorTaskEntity(ApplicationCodeInfo codeInfo) {

        SystemConfig systemConfig = (SystemConfig) SpringContextUtil.getBean("systemConfig");

        this.appInfo = codeInfo;

        this.projectName = GitUtil.getLastUrlString(appInfo.getGitPath());

        if (StringUtils.isEmpty(appInfo.getJacocoPort()) || "0".equals(appInfo.getJacocoPort())) {
            this.appInfo.setJacocoPort(systemConfig.getPort());
        }
        this.port = appInfo.getJacocoPort().split(",");

        if (!StringUtils.isEmpty(codeInfo.getIp())) {
            this.ipList = Arrays.asList(codeInfo.getIp().split(","));
        }

        if (!StringUtils.isEmpty(codeInfo.getIgnoreClass())) {
            this.ignoreClassList = codeInfo.getIgnoreClass().split(",");
        }

        if (!StringUtils.isEmpty(codeInfo.getIgnorePackage())) {
            this.ignorePackageList = codeInfo.getIgnorePackage().split(",");
        }

        if (!StringUtils.isEmpty(codeInfo.getContainPackages())) {
            this.containPackageList = codeInfo.getContainPackages().split(",");
        }

    }


    public void initSourceDirectoryList() {
        File sourceDirectory;
        ArrayList<File> sourceDirectoryList = new ArrayList<>();
        Map<String, Object> sourceApplications = this.sourceApplicationsMap;
        for (String key : sourceApplications.keySet()) {
            String projectDirectoryStr = ((Map) sourceApplications.get(key)).getOrDefault("sourceDirectory", "").toString();
            File projectDirectory = new File(projectDirectoryStr);
            //源码目录
            sourceDirectory = new File(projectDirectory, "src/main/java");
            if (sourceDirectory.exists()) {
                sourceDirectoryList.add(sourceDirectory);
            }
        }
        this.sourceDirectoryList = sourceDirectoryList;
    }

    //------------------------------------------- getter and setter -----------------------------------------


    public ApplicationCodeInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(ApplicationCodeInfo appInfo) {
        this.appInfo = appInfo;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public File getGitLocalPath() {
        return gitLocalPath;
    }

    public void setGitLocalPath(File gitLocalPath) {
        this.gitLocalPath = gitLocalPath;
    }

    public String getProjectCovPath() {
        return projectCovPath;
    }

    public void setProjectCovPath(String projectCovPath) {
        this.projectCovPath = projectCovPath;
    }

    public File getCoverageReportPath() {
        return coverageReportPath;
    }

    public void setCoverageReportPath(File coverageReportPath) {
        this.coverageReportPath = coverageReportPath;
    }

    public String[] getPort() {
        return port;
    }

    public void setPort(String[] port) {
        this.port = port;
    }

    public List<String> getIpList() {
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }

    public Map<String, Object> getSourceApplicationsMap() {
        return sourceApplicationsMap;
    }

    public void setSourceApplicationsMap(Map<String, Object> sourceApplicationsMap) {
        this.sourceApplicationsMap = sourceApplicationsMap;
    }

    public String[] getIgnoreClassList() {
        return ignoreClassList;
    }

    public void setIgnoreClassList(String[] ignoreClassList) {
        this.ignoreClassList = ignoreClassList;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public ExecFileLoader getExecFileLoader() {
        return execFileLoader;
    }

    public void setExecFileLoader(ExecFileLoader execFileLoader) {
        this.execFileLoader = execFileLoader;
    }

    public String[] getIgnorePackageList() {
        return ignorePackageList;
    }

    public void setIgnorePackageList(String[] ignorePackageList) {
        this.ignorePackageList = ignorePackageList;
    }

    public String[] getContainPackageList() {
        return containPackageList;
    }

    public void setContainPackageList(String[] containPackageList) {
        this.containPackageList = containPackageList;
    }

    public ArrayList<File> getSourceDirectoryList() {
        return sourceDirectoryList;
    }

    public void setSourceDirectoryList(ArrayList<File> sourceDirectoryList) {
        this.sourceDirectoryList = sourceDirectoryList;
    }

    public File getReportAllCovDirectory() {
        return reportAllCovDirectory;
    }

    public void setReportAllCovDirectory(File reportAllCovDirectory) {
        this.reportAllCovDirectory = reportAllCovDirectory;
    }

    public File getReportDiffDirectory() {
        return reportDiffDirectory;
    }

    public void setReportDiffDirectory(File reportDiffDirectory) {
        this.reportDiffDirectory = reportDiffDirectory;
    }

    public File getFilterReportAllCovDirectory() {
        return filterReportAllCovDirectory;
    }

    public void setFilterReportAllCovDirectory(File filterReportAllCovDirectory) {
        this.filterReportAllCovDirectory = filterReportAllCovDirectory;
    }

    public File getFilterReportDiffDirectory() {
        return filterReportDiffDirectory;
    }

    public void setFilterReportDiffDirectory(File filterReportDiffDirectory) {
        this.filterReportDiffDirectory = filterReportDiffDirectory;
    }

    public File getTestedBranchCoverageDirectory() {
        return testedBranchCoverageDirectory;
    }

    public void setTestedBranchCoverageDirectory(File testedBranchCoverageDirectory) {
        this.testedBranchCoverageDirectory = testedBranchCoverageDirectory;
    }

    public File getVersionIdDataPath() {
        return versionIdDataPath;
    }

    public void setVersionIdDataPath(File versionIdDataPath) {
        this.versionIdDataPath = versionIdDataPath;
    }

    public File getCoverageExecutionDataPath() {
        return coverageExecutionDataPath;
    }

    public void setCoverageExecutionDataPath(File coverageExecutionDataPath) {
        this.coverageExecutionDataPath = coverageExecutionDataPath;
    }

    public File getAllExecutionDataFile() {
        return allExecutionDataFile;
    }

    public void setAllExecutionDataFile(File allExecutionDataFile) {
        this.allExecutionDataFile = allExecutionDataFile;
    }
}

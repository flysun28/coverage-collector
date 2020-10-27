package com.oppo.jacocoreport.coverage.entity;

public class CoverageData {



    /**
     * 对应的测试记录id
     * */
    private Long id;

    /**
     * 语句覆盖率
     * */
    private String missedInstructions;
    private String totalInstructions;
    private String diffMissedInstructions;
    private String diffTotalInstructions;

    /**
     * 分支覆盖率
     * */
    private String missedBranches;
    private String totalBranches;
    private String diffMissedBranches;
    private String diffTotalBranches;

    /**
     * 圈复杂度
     * */
    private String missedCxty;
    private String totalCxty;
    private String diffMissedCxty;
    private String diffTotalCxty;

    /**
     * 代码行
     * */
    private String missedLines;
    private String totalLines;
    private String diffMissedLines;
    private String diffTotalLines;

    /**
     * 方法
     * */
    private String missedMethods;
    private String totalMethods;
    private String diffMissedMethods;
    private String diffTotalMethods;

    /**
     * 类
     * */
    private String missedClasses;
    private String totalClasses;
    private String diffMissedClasses;
    private String diffTotalClasses;

    /**
     * 整体覆盖率报告路径
     * */
    private String totalCoverageReportPath;
    /**
     * 差异化覆盖率报告路径
     * */
    private String diffCoverageReportPath;

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getTestedBranch() {
        return testedBranch;
    }

    public void setTestedBranch(String testedBranch) {
        this.testedBranch = testedBranch;
    }

    public String getBasicBranch() {
        return basicBranch;
    }

    public void setBasicBranch(String basicBranch) {
        this.basicBranch = basicBranch;
    }

    //应用名称
    private String appCode;
    //被测分支
    private String testedBranch;
    //基准分支
    private String basicBranch;

    public int getFilterTask() {
        return filterTask;
    }

    public void setFilterTask(int filterTask) {
        this.filterTask = filterTask;
    }

    //是否过滤任务
    private int filterTask; //0 非过滤 1 过滤


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMissedInstructions() {
        return missedInstructions;
    }

    public void setMissedInstructions(String missedInstructions) {
        this.missedInstructions = missedInstructions;
    }

    public String getTotalInstructions() {
        return totalInstructions;
    }

    public void setTotalInstructions(String totalInstructions) {
        this.totalInstructions = totalInstructions;
    }

    public String getMissedBranches() {
        return missedBranches;
    }

    public void setMissedBranches(String missedBranches) {
        this.missedBranches = missedBranches;
    }

    public String getTotalBranches() {
        return totalBranches;
    }

    public void setTotalBranches(String totalBranches) {
        this.totalBranches = totalBranches;
    }

    public String getMissedCxty() {
        return missedCxty;
    }

    public void setMissedCxty(String missedCxty) {
        this.missedCxty = missedCxty;
    }

    public String getTotalCxty() {
        return totalCxty;
    }

    public void setTotalCxty(String totalCxty) {
        this.totalCxty = totalCxty;
    }

    public String getMissedLines() {
        return missedLines;
    }

    public void setMissedLines(String missedLines) {
        this.missedLines = missedLines;
    }

    public String getTotalLines() {
        return totalLines;
    }

    public void setTotalLines(String totalLines) {
        this.totalLines = totalLines;
    }

    public String getMissedMethods() {
        return missedMethods;
    }

    public void setMissedMethods(String missedMethods) {
        this.missedMethods = missedMethods;
    }

    public String getTotalMethods() {
        return totalMethods;
    }

    public void setTotalMethods(String totalMethods) {
        this.totalMethods = totalMethods;
    }

    public String getMissedClasses() {
        return missedClasses;
    }

    public void setMissedClasses(String missedClasses) {
        this.missedClasses = missedClasses;
    }

    public String getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(String totalClasses) {
        this.totalClasses = totalClasses;
    }

    public String getTotalCoverageReportPath() {
        return totalCoverageReportPath;
    }

    public void setTotalCoverageReportPath(String totalCoverageReportPath) {
        this.totalCoverageReportPath = totalCoverageReportPath;
    }

    public String getDiffCoverageReportPath() {
        return diffCoverageReportPath;
    }

    public void setDiffCoverageReportPath(String diffCoverageReportPath) {
        this.diffCoverageReportPath = diffCoverageReportPath;
    }

    public String getDiffMissedInstructions() {
        return diffMissedInstructions;
    }

    public void setDiffMissedInstructions(String diffMissedInstructions) {
        this.diffMissedInstructions = diffMissedInstructions;
    }

    public String getDiffTotalInstructions() {
        return diffTotalInstructions;
    }

    public void setDiffTotalInstructions(String diffTotalInstructions) {
        this.diffTotalInstructions = diffTotalInstructions;
    }

    public String getDiffMissedBranches() {
        return diffMissedBranches;
    }

    public void setDiffMissedBranches(String diffMissedBranches) {
        this.diffMissedBranches = diffMissedBranches;
    }

    public String getDiffTotalBranches() {
        return diffTotalBranches;
    }

    public void setDiffTotalBranches(String diffTotalBranches) {
        this.diffTotalBranches = diffTotalBranches;
    }

    public String getDiffMissedCxty() {
        return diffMissedCxty;
    }

    public void setDiffMissedCxty(String diffMissedCxty) {
        this.diffMissedCxty = diffMissedCxty;
    }

    public String getDiffTotalCxty() {
        return diffTotalCxty;
    }

    public void setDiffTotalCxty(String diffTotalCxty) {
        this.diffTotalCxty = diffTotalCxty;
    }

    public String getDiffMissedLines() {
        return diffMissedLines;
    }

    public void setDiffMissedLines(String diffMissedLines) {
        this.diffMissedLines = diffMissedLines;
    }

    public String getDiffTotalLines() {
        return diffTotalLines;
    }

    public void setDiffTotalLines(String diffTotalLines) {
        this.diffTotalLines = diffTotalLines;
    }

    public String getDiffMissedMethods() {
        return diffMissedMethods;
    }

    public void setDiffMissedMethods(String diffMissedMethods) {
        this.diffMissedMethods = diffMissedMethods;
    }

    public String getDiffTotalMethods() {
        return diffTotalMethods;
    }

    public void setDiffTotalMethods(String diffTotalMethods) {
        this.diffTotalMethods = diffTotalMethods;
    }

    public String getDiffMissedClasses() {
        return diffMissedClasses;
    }

    public void setDiffMissedClasses(String diffMissedClasses) {
        this.diffMissedClasses = diffMissedClasses;
    }

    public String getDiffTotalClasses() {
        return diffTotalClasses;
    }

    public void setDiffTotalClasses(String diffTotalClasses) {
        this.diffTotalClasses = diffTotalClasses;
    }

    @Override
    public String toString() {
        return "CoverageResponse{" +
                "id=" + id +
                ", missedInstructions='" + missedInstructions + '\'' +
                ", totalInstructions='" + totalInstructions + '\'' +
                ", diffMissedInstructions='" + diffMissedInstructions + '\'' +
                ", diffTotalInstructions='" + diffTotalInstructions + '\'' +
                ", missedBranches='" + missedBranches + '\'' +
                ", totalBranches='" + totalBranches + '\'' +
                ", diffMissedBranches='" + diffMissedBranches + '\'' +
                ", diffTotalBranches='" + diffTotalBranches + '\'' +
                ", missedCxty='" + missedCxty + '\'' +
                ", totalCxty='" + totalCxty + '\'' +
                ", diffMissedCxty='" + diffMissedCxty + '\'' +
                ", diffTotalCxty='" + diffTotalCxty + '\'' +
                ", missedLines='" + missedLines + '\'' +
                ", totalLines='" + totalLines + '\'' +
                ", diffMissedLines='" + diffMissedLines + '\'' +
                ", diffTotalLines='" + diffTotalLines + '\'' +
                ", missedMethods='" + missedMethods + '\'' +
                ", totalMethods='" + totalMethods + '\'' +
                ", diffMissedMethods='" + diffMissedMethods + '\'' +
                ", diffTotalMethods='" + diffTotalMethods + '\'' +
                ", missedClasses='" + missedClasses + '\'' +
                ", totalClasses='" + totalClasses + '\'' +
                ", diffMissedClasses='" + diffMissedClasses + '\'' +
                ", diffTotalClasses='" + diffTotalClasses + '\'' +
                ", totalCoverageReportPath='" + totalCoverageReportPath + '\'' +
                ", diffCoverageReportPath='" + diffCoverageReportPath + '\'' +
                ", filterTask='" + filterTask + '\'' +
                '}';
    }
}

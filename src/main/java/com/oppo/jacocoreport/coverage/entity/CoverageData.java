package com.oppo.jacocoreport.coverage.entity;

public class CoverageData {



    private long id;
    //整体语句覆盖率
    private String totalInstructions = "";
    //整体分支覆盖率
    private String totalBranches = "";
    //整体方法覆盖率
    private String totalMethods = "";
    //差异化语句覆盖率
    private String diffInstructions = "";



    //差异化分支覆盖率
    private String diffBranches = "";
    //差异化方法覆盖率
    private String diffMethods = "";

    //整体覆盖率报告路径
    private String totalCoverageReportPath = "";
    //差异化覆盖率报告路径
    private String diffCoverageReportPath = "";

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



    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTotalInstructions() {
        return totalInstructions;
    }

    public void setTotalInstructions(String totalInstructions) {
        this.totalInstructions = totalInstructions;
    }

    public String getTotalBranches() {
        return totalBranches;
    }

    public void setTotalBranches(String totalBranches) {
        this.totalBranches = totalBranches;
    }

    public String getTotalMethods() {
        return totalMethods;
    }

    public void setTotalMethods(String totalMethods) {
        this.totalMethods = totalMethods;
    }

    public String getDiffInstructions() {
        return diffInstructions;
    }

    public void setDiffInstructions(String diffInstructions) {
        this.diffInstructions = diffInstructions;
    }

    public String getDiffMethods() {
        return diffMethods;
    }

    public void setDiffMethods(String diffMethods) {
        this.diffMethods = diffMethods;
    }

    public String getDiffBranches() {
        return diffBranches;
    }

    public void setDiffBranches(String diffBranches) {
        this.diffBranches = diffBranches;
    }


    public CoverageData(){

    }

    public CoverageData(long id,String totalInstructions,String totalBranches,String totalcoverageReportPath,
                        String totalMethods,String diffInstructions,String diffBranches,String diffMethods,
                        String diffcoverageReportPath){
        this.id = id;
       this.totalInstructions = totalInstructions;
       this.totalBranches = totalBranches;
       this.totalMethods = totalMethods;
       this.diffInstructions = diffInstructions;
       this.diffBranches = diffBranches;
       this.diffMethods = diffMethods;
       this.totalCoverageReportPath = totalcoverageReportPath;
       this.diffCoverageReportPath = diffcoverageReportPath;
    }

    @Override
    public String toString() {
        return this.totalInstructions+" "+this.totalMethods+" "+this.totalBranches+" "+this.diffInstructions+" "+this.diffBranches+" "+this.diffMethods;
    }
}

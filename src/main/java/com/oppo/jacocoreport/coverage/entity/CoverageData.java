package com.oppo.jacocoreport.coverage.entity;

public class CoverageData {

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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
    private String difflBranches = "";
    //差异化方法覆盖率
    private String diffMethods = "";

    //整体覆盖率报告路径
    private String totalcoverageReportPath = "";
    //差异化覆盖率报告路径
    private String diffcoverageReportPath = "";

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

    public String getDifflBranches() {
        return difflBranches;
    }

    public void setDifflBranches(String difflBranches) {
        this.difflBranches = difflBranches;
    }

    public String getDiffMethods() {
        return diffMethods;
    }

    public void setDiffMethods(String diffMethods) {
        this.diffMethods = diffMethods;
    }


    public String getTotalcoverageReportPath() {
        return totalcoverageReportPath;
    }

    public void setTotalcoverageReportPath(String totalcoverageReportPath) {
        this.totalcoverageReportPath = totalcoverageReportPath;
    }

    public String getDiffcoverageReportPath() {
        return diffcoverageReportPath;
    }

    public void setDiffcoverageReportPath(String diffcoverageReportPath) {
        this.diffcoverageReportPath = diffcoverageReportPath;
    }
    public CoverageData(){

    }

    public CoverageData(long id,String totalInstructions,String totalBranches,String totalMethods,String diffInstructions,String difflBranches,String diffMethods){
        this.id = id;
       this.totalInstructions = totalInstructions;
       this.totalBranches = totalBranches;
       this.totalMethods = totalMethods;
       this.diffInstructions = diffInstructions;
       this.difflBranches = difflBranches;
       this.diffMethods = diffMethods;
    }

    @Override
    public String toString() {
        return this.totalInstructions+" "+this.totalMethods+" "+this.totalBranches+" "+this.diffInstructions+" "+this.difflBranches+" "+this.diffMethods;
    }
}

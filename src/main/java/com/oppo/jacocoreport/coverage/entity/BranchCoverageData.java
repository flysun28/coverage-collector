package com.oppo.jacocoreport.coverage.entity;

public class BranchCoverageData extends CoverageData {
    //应用名称
    String appCode;
    //被测分支
    String testedBranch;
    //基准分支
    String basicBranch;

    public BranchCoverageData(CoverageData coverageData){

    }
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


}

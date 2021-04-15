package com.oppo.test.coverage.backend.model.entity;

public class BranchCoverageData extends CoverageData {

    /**
     * 应用id
     * */
    private String appCode;
    /**
     * 被测分支
     * */
    private String testedBranch;
    /**
     * 基准分支
     * */
    private String basicBranch;

    public BranchCoverageData(CoverageData coverageData){

    }
    @Override
    public String getAppCode() {
        return appCode;
    }

    @Override
    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    @Override
    public String getTestedBranch() {
        return testedBranch;
    }

    @Override
    public void setTestedBranch(String testedBranch) {
        this.testedBranch = testedBranch;
    }

    @Override
    public String getBasicBranch() {
        return basicBranch;
    }

    @Override
    public void setBasicBranch(String basicBranch) {
        this.basicBranch = basicBranch;
    }


}

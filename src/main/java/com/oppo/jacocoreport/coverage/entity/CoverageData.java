package com.oppo.jacocoreport.coverage.entity;

public class CoverageData {

    private String toltalcoverage = "";
    private String diffcoverage = "";

    public CoverageData(String toltalcoverage,String diffcoverage){
       this.toltalcoverage = toltalcoverage;
       this.diffcoverage = diffcoverage;
    }
    public String getToltalcoverage() {
        return toltalcoverage;
    }

    public void setToltalcoverage(String toltalcoverage) {
        this.toltalcoverage = toltalcoverage;
    }

    public String getDiffcoverage() {
        return diffcoverage;
    }

    public void setDiffcoverage(String diffcoverage) {
        this.diffcoverage = diffcoverage;
    }

}

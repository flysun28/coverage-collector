package com.oppo.jacocoreport.coverage.entity;

public class ApplicationCodeInfo {

    private Long id;
    private String gitPath = "";
    private String testedBranch = "";
    private String basicBranch = "";
    private String testedCommitId = "";
    private String basicCommitId = "";

    private String versionName = "";
    private String deployKey = "";
    private String ignoreClass = "";
    private String ignorePackage = "";

    public String getIgnoreClass() {
        return ignoreClass;
    }

    public void setIgnoreClass(String ignoreClass) {
        this.ignoreClass = ignoreClass;
    }

    public String getIgnorePackage() {
        return ignorePackage;
    }

    public void setIgnorePackage(String ignorePackage) {
        this.ignorePackage = ignorePackage;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    private String applicationID;

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGitPath() {
        return gitPath;
    }

    public void setGitPath(String gitPath) {
        this.gitPath = gitPath;
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

    public String getTestedCommitId() {
        return testedCommitId;
    }

    public void setTestedCommitId(String testedCommitId) {
        this.testedCommitId = testedCommitId;
    }

    public String getBasicCommitId() {
        return basicCommitId;
    }

    public void setBasicCommitId(String basicCommitId) {
        this.basicCommitId = basicCommitId;
    }

    public String getDeployKey() {
        return deployKey;
    }

    public void setDeployKey(String deployKey) {
        this.deployKey = deployKey;
    }

}

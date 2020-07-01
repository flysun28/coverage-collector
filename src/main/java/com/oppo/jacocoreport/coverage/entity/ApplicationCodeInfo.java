package com.oppo.jacocoreport.coverage.entity;

public class ApplicationCodeInfo {

    private Long id;
    private String gitPath;
    private String testedBranch;
    private String basicBranch;
    private String testedCommitId;
    private String basicCommitId;
    private String environment;
    private String deployId;

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

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getDeployId() {
        return deployId;
    }

    public void setDeployId(String deployId) {
        this.deployId = deployId;
    }

}

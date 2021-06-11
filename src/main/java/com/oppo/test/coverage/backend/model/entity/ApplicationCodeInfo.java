package com.oppo.test.coverage.backend.model.entity;

import org.springframework.util.StringUtils;

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
    //默认为0，不开启轮询任务
    private int isTimerTask = 0;
    private int timerInterval = 600000;
    //默认8098端口
    private String jacocoPort = "";
    // 0 非分支任务 1 分支任务
    private int isBranchTask = 0;
    //分支覆盖率taskID
    private Long branchTaskID;
    private String containPackages = "";
    private String applicationID = "";
    private String ip = "";
    private String versionId;

    /**
     * 被测环境字段:1-测试;2-生产;3-开发
     * */
    private Integer testedEnv;

    /**
     * cort场景id
     * */
    private Long sceneId;

    public int getTimerInterval() {
        return timerInterval;
    }

    public void setTimerInterval(int timerInterval) {
        this.timerInterval = timerInterval;
    }

    public String getContainPackages() {
        return containPackages;
    }

    public void setContainPackages(String containPackages) {
        this.containPackages = containPackages;
    }

    public String getJacocoPort() {
        return jacocoPort;
    }

    public void setJacocoPort(String jacocoPort) {
        this.jacocoPort = jacocoPort;
    }



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

    public int getIsTimerTask() {
        return isTimerTask;
    }

    public void setIsTimerTask(int isTimerTask) {
        this.isTimerTask = isTimerTask;
    }
    public int getIsBranchTask() {
        return isBranchTask;
    }

    public void setIsBranchTask(int isBranchTask) {
        this.isBranchTask = isBranchTask;
    }
    public Long getBranchTaskID() {
        return branchTaskID;
    }

    public void setBranchTaskID(Long branchTaskID) {
        this.branchTaskID = branchTaskID;
    }
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public Integer getTestedEnv() {
        return testedEnv;
    }

    public void setTestedEnv(Integer testedEnv) {
        this.testedEnv = testedEnv;
    }

    public Long getSceneId() {
        return sceneId;
    }

    @Override
    public String toString() {
        return "ApplicationCodeInfo{" +
                "id=" + id +
                ", gitPath='" + gitPath + '\'' +
                ", testedBranch='" + testedBranch + '\'' +
                ", basicBranch='" + basicBranch + '\'' +
                ", testedCommitId='" + testedCommitId + '\'' +
                ", basicCommitId='" + basicCommitId + '\'' +
                ", versionName='" + versionName + '\'' +
                ", deployKey='" + deployKey + '\'' +
                ", ignoreClass='" + ignoreClass + '\'' +
                ", ignorePackage='" + ignorePackage + '\'' +
                ", isTimerTask=" + isTimerTask +
                ", timerInterval=" + timerInterval +
                ", jacocoPort='" + jacocoPort + '\'' +
                ", isBranchTask=" + isBranchTask +
                ", branchTaskID=" + branchTaskID +
                ", containPackages='" + containPackages + '\'' +
                ", applicationID='" + applicationID + '\'' +
                ", ip='" + ip + '\'' +
                ", versionId=" + versionId +
                ", testedEnv=" + testedEnv +
                ", sceneId=" + sceneId +
                '}';
    }


    public boolean enableCheck(){
        if (StringUtils.isEmpty(this.gitPath)){
            return false;
        }
        if (StringUtils.isEmpty(this.testedBranch)){
            return false;
        }
        if (StringUtils.isEmpty(this.testedCommitId)){
            return false;
        }
        if (StringUtils.isEmpty(this.basicBranch)){
            return false;
        }
        if (StringUtils.isEmpty(this.basicCommitId)){
            return false;
        }
        if (StringUtils.isEmpty(this.versionName)){
            return false;
        }
        return true;
    }

    public boolean isNeedDiff(){
        return !this.testedCommitId.equals(this.basicCommitId);
    }

    public void trimString(){
        this.testedCommitId = testedCommitId.trim();
        this.basicCommitId = basicCommitId.trim();
    }


}

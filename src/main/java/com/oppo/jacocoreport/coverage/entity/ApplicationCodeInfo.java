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
    //默认为0，不开启轮询任务
    private int isTimerTask = 0;
    private int timerSecond = 600000;
    //默认8098端口
    private String jacocoPort = "";
    // 0 非分支任务 1 分支任务
    private int isBranchTask = 0;
    //分支覆盖率taskID
    private Long branchTaskID;
    private String containPackages = "";
    private String applicationID = "";
    private String ip = "";
    private Long versionId;

    /**
     * 被测环境字段:1-测试;2-生产
     * */
    private Integer testedEnv;

    public int getTimerSecond() {
        return timerSecond;
    }

    public void setTimerSecond(int timerSecond) {
        this.timerSecond = timerSecond;
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
    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Integer getTestedEnv() {
        return testedEnv;
    }

    public void setTestedEnv(Integer testedEnv) {
        this.testedEnv = testedEnv;
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
                ", jacocoPort='" + jacocoPort + '\'' +
                ", isBranchTask=" + isBranchTask +
                ", branchTaskID=" + branchTaskID +
                ", containPackages='" + containPackages + '\'' +
                ", applicationID='" + applicationID + '\'' +
                ", ip='" + ip + '\'' +
                ", versionId=" + versionId +
                ", testedEnv=" + testedEnv +
                '}';
    }

}

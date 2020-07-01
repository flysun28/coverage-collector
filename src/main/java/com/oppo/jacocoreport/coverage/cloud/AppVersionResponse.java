package com.oppo.jacocoreport.coverage.cloud;

import java.io.Serializable;

public class AppVersionResponse implements Serializable {
    private static final long serialVersionUID = -7058629556598971201L;
    private String appId = "";
    private String versionName = "";
    private String repositoryUrl = "";
    private String versionDesc = "";
    private String targetMd5 = "";
    private Long versionTime;
    private String image = "";
    private Boolean existImage;
    private String incrPublish = "";
    private String sourceBranch = "";
    private String commitId = "";

    public String getSourceBranch() {
        return sourceBranch;
    }

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getVersionDesc() {
        return versionDesc;
    }

    public void setVersionDesc(String versionDesc) {
        this.versionDesc = versionDesc;
    }

    public String getTargetMd5() {
        return targetMd5;
    }

    public void setTargetMd5(String targetMd5) {
        this.targetMd5 = targetMd5;
    }

    public Long getVersionTime() {
        return versionTime;
    }

    public void setVersionTime(Long versionTime) {
        this.versionTime = versionTime;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getExistImage() {
        return existImage;
    }

    public void setExistImage(Boolean existImage) {
        this.existImage = existImage;
    }

    public String getIncrPublish() {
        return incrPublish;
    }

    public void setIncrPublish(String incrPublish) {
        this.incrPublish = incrPublish;
    }


}

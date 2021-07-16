package com.oppo.test.coverage.backend.model.request;

import com.oppo.test.coverage.backend.model.entity.ApplicationCodeInfo;

/**
 * @author 80264236
 * @date 2021/5/21 16:49
 */
public class CompilesFileRequest {

    public CompilesFileRequest(){

    }

    public CompilesFileRequest(ApplicationCodeInfo codeInfo){
        this.appCode = codeInfo.getApplicationID();
        this.packageName = codeInfo.getApplicationID();
        this.commitId = codeInfo.getTestedCommitId();
        this.branchName = codeInfo.getTestedBranch();
    }

    /**
     * 应用id
     * */
    private String appCode;

    /**
     * 包名,应用下级区分
     * */
    private String packageName;

    /**
     * 分支名称
     * */
    private String branchName;

    /**
     * commitId
     * */
    private String commitId;

    /**
     * 制品类型,1-jacoco,2-待定; 固定传1
     * */
    private Integer artifactType = 1;

    /**
     * 文件类型:1-source,2-compiles,3-待定; 固定传2
     * */
    private Integer fileType = 2;

    /**
     * 文件下载链接,传到ocs上去
     * */
    private String fileUrl;

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public Integer getArtifactType() {
        return artifactType;
    }

    public Integer getFileType() {
        return fileType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @Override
    public String toString() {
        return "CompilesFileRequest{" +
                "appCode='" + appCode + '\'' +
                ", packageName='" + packageName + '\'' +
                ", branchName='" + branchName + '\'' +
                ", commitId='" + commitId + '\'' +
                ", artifactType=" + artifactType +
                ", fileType=" + fileType +
                ", fileUrl='" + fileUrl + '\'' +
                '}';
    }
}

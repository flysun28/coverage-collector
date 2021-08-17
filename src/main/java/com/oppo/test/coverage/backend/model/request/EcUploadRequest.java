package com.oppo.test.coverage.backend.model.request;

import com.oppo.test.coverage.backend.model.entity.ApplicationCodeInfo;

import java.util.*;

/**
 * 上传ec文件到cort后台的请求体
 *
 * @author 80264236
 * @date 2021/5/21 11:20
 */
public class EcUploadRequest {

    public EcUploadRequest() {

    }

    public EcUploadRequest(ApplicationCodeInfo codeInfo, String fileKey) {
        this.appCode = codeInfo.getApplicationID();
        this.commitId = codeInfo.getTestedCommitId();
        this.branchName = codeInfo.getTestedBranch();
        this.sceneId = Math.toIntExact(codeInfo.getSceneId());
        this.packageName = codeInfo.getApplicationID();
        this.caseId = codeInfo.getId().toString();
        this.fileKey = fileKey;
        List<String> ipList = Arrays.asList(codeInfo.getIp().split(","));
        Collections.sort(ipList);
        this.deviceId = String.join(",", ipList);
    }

    /**
     * 应用id
     */
    private String appCode;

    /**
     * 单仓库多服务用，作为appCode之外的下一级区分
     */
    private String packageName;

    /**
     * commitId
     */
    private String commitId;

    /**
     * 分支名
     */
    private String branchName;

    /**
     * 在客户端是用于定位到具体手机的,后期可以在前端做一些区分查看
     */
    private String deviceId;

    /**
     * 精准预留的,暂时写死一个数字
     */
    private String caseId;

    /**
     * 场景id,之前通过接口获取
     */
    private Integer sceneId;

    /**
     * 场景类型: 1-自动,2-用例录制,3-版本测试,4-实时染色,5-服务端任务
     */
    private Integer sceneType = 5;

    /**
     * 非必填,手机的imei号,客户端使用
     */
    private String imei;

    /**
     * 非必填,不懂干嘛用的
     */
    private String sn;

    /**
     * 文件名,xxxxxx.ec
     */
    private String fileKey;

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

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public Integer getSceneId() {
        return sceneId;
    }

    public void setSceneId(Integer sceneId) {
        this.sceneId = sceneId;
    }

    public Integer getSceneType() {
        return sceneType;
    }

    public String getImei() {
        return imei;
    }

    public String getSn() {
        return sn;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    @Override
    public String toString() {
        return "EcUploadRequest{" +
                "appCode='" + appCode + '\'' +
                ", packageName='" + packageName + '\'' +
                ", commitId='" + commitId + '\'' +
                ", branchName='" + branchName + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", caseId=" + caseId +
                ", sceneId=" + sceneId +
                ", sceneType=" + sceneType +
                ", imei='" + imei + '\'' +
                ", sn='" + sn + '\'' +
                ", fileKey='" + fileKey + '\'' +
                '}';
    }
}

package com.oppo.test.jacocoreport.cloud;

import java.io.Serializable;

public class AppDeployInfo implements Serializable {

    private static final long serialVersionUID = 8979079792065689133L;
    private String id = "";
    private String instanceId = "";
    private String ip = "";
    private String appVersion = "";
    private String confVersion = "";
    private String confEnv = "";
    private String operator = "";
    private String finish = "";
    private Boolean result = true;
    private String msg = "";
    private String deployTime = "";
    private String deployFinishTime = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getConfVersion() {
        return confVersion;
    }

    public void setConfVersion(String confVersion) {
        this.confVersion = confVersion;
    }

    public String getConfEnv() {
        return confEnv;
    }

    public void setConfEnv(String confEnv) {
        this.confEnv = confEnv;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getFinish() {
        return finish;
    }

    public void setFinish(String finish) {
        this.finish = finish;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDeployTime() {
        return deployTime;
    }

    public void setDeployTime(String deployTime) {
        this.deployTime = deployTime;
    }

    public String getDeployFinishTime() {
        return deployFinishTime;
    }

    public void setDeployFinishTime(String deployFinishTime) {
        this.deployFinishTime = deployFinishTime;
    }


}

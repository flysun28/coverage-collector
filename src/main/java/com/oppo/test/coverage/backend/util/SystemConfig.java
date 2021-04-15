package com.oppo.test.coverage.backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author 80264236
 */
@Component
public class SystemConfig {

    @Value("${jacoco.base.port}")
    public String port;

    @Value("${atms.base.url}")
    public String baseUrl;

    @Value("${report.base.path}")
    public String reportBasePath;

    @Value("${report.base.url}")
    public String reportBaseUrl;


    @Value("${report.append.codePath}")
    public String codePath;

    @Value("${report.append.projectCovPath}")
    public String projectCovPath;


    @Value("${atms.append.coverageResult}")
    public String sendCoverageResultUrl;

    @Value("${atms.append.branchResult}")
    public String sendBranchResultUrl;

    @Value("${atms.append.versionResult}")
    public String sendVersionResultUrl;

    @Value("${cloud.deploy.url}")
    public String cloudDeployInfoUrl;

    @Value("${atms.append.errorMsg}")
    public String sendErrorMsgUrl;

    @Value("${atms.append.stopTimerTask}")
    public String sendStopTimerTaskUrl;

    @Value("${atms.append.recoverTimerTask}")
    public String recoverTimerTaskUrl;

    @Value("${atms.append.sendTeamTalkNotify}")
    public String sendTeamTalkNotify;

    @Value("${atms.transfer.ip}")
    public String transferBaseIp;

    @Value("${atms.transfer.url}")
    public String transferUrl;

    public String getPort() {
        return port;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getReportBasePath() {
        return reportBasePath;
    }

    public String getReportBaseUrl() {
        return reportBaseUrl;
    }

    public String getCodePath() {
        return codePath;
    }

    public String getProjectCovPath() {
        return projectCovPath;
    }

    public String getSendCoverageResultUrl() {
        return sendCoverageResultUrl;
    }

    public String getSendBranchResultUrl() {
        return sendBranchResultUrl;
    }

    public String getSendVersionResultUrl() {
        return sendVersionResultUrl;
    }

    public String getCloudDeployInfoUrl() {
        return cloudDeployInfoUrl;
    }

    public String getSendErrorMsgUrl() {
        return sendErrorMsgUrl;
    }

    public String getSendStopTimerTaskUrl() {
        return sendStopTimerTaskUrl;
    }

    public String getRecoverTimerTaskUrl() {
        return recoverTimerTaskUrl;
    }

    public String getSendTeamTalkNotify() {
        return sendTeamTalkNotify;
    }

    public String getTransferBaseIp() {
        return transferBaseIp;
    }

    public String getTransferUrl() {
        return transferUrl;
    }
}

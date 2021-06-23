package com.oppo.test.coverage.backend.model.response;

import lombok.Data;

@Data
public class AppVersionResponse {
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
}


package com.oppo.test.coverage.backend.model.entity;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class ApplicationCodeInfo {

    private Long id;
    private String gitPath = "";
    private String testedBranch = "";
    private String basicBranch = "";
    private String testedCommitId = "";
    private String basicCommitId = "";

    private String versionName = "";
    private String ignoreClass = "";
    private String ignorePackage = "";
    //默认为0，不开启轮询任务
    private int isTimerTask = 0;
    private int timerInterval = 600000;
    //默认8098端口
    private String jacocoPort = "";

    private String containPackages = "";
    private String applicationID = "";
    private String ip = "";
    private String versionId;

    /**
     * 被测环境字段:1-测试;2-生产;3-开发
     */
    private Integer testedEnv;

    /**
     * cort场景id
     */
    private Long sceneId;

    /**
     * goblin ec文件名
     */
    private String goblinEcFile;

    public boolean enableCheck() {
        if (StringUtils.isEmpty(this.gitPath)) {
            return false;
        }
        if (StringUtils.isEmpty(this.testedBranch)) {
            return false;
        }
        if (StringUtils.isEmpty(this.testedCommitId)) {
            return false;
        }
        if (StringUtils.isEmpty(this.basicBranch)) {
            return false;
        }
        if (StringUtils.isEmpty(this.basicCommitId)) {
            return false;
        }
        if (StringUtils.isEmpty(this.versionName)) {
            return false;
        }
        return true;
    }

    public boolean isNeedDiff() {
        return !this.testedCommitId.equals(this.basicCommitId);
    }

    public void trimString() {
        this.testedCommitId = testedCommitId.trim();
        this.basicCommitId = basicCommitId.trim();
    }


}

package com.oppo.jacocoreport.component;

import com.oppo.jacocoreport.coverage.ReportGeneratorCov;
import com.oppo.jacocoreport.coverage.entity.ApplicationCodeInfo;
import com.oppo.jacocoreport.coverage.entity.Data;
import com.oppo.jacocoreport.coverage.utils.HttpUtils;
import com.oppo.jacocoreport.response.DefinitionException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Async
public class AsyncTask {
    public void startCoverageTask(ApplicationCodeInfo applicationCodeInfo){
        Long taskId = applicationCodeInfo.getId();
        String gitPath = applicationCodeInfo.getGitPath();
        String testedBranch = applicationCodeInfo.getTestedBranch();
        String basicBranch = applicationCodeInfo.getBasicBranch();
        String versionname = applicationCodeInfo.getVersionName();
        String newTag = applicationCodeInfo.getTestedCommitId();
        String oldTag = applicationCodeInfo.getBasicCommitId();
        System.out.println("start coverage test");
        ReportGeneratorCov reportGeneratorCov = new ReportGeneratorCov(taskId,gitPath,testedBranch,versionname,basicBranch,newTag,oldTag);
        try {
            reportGeneratorCov.startCoverageTask();
        }catch (DefinitionException e){
            HttpUtils.sendErrorMSG(taskId,e.getMessage());
        }catch (Exception e){
            HttpUtils.sendErrorMSG(taskId,e.getMessage());
        }
    }
}

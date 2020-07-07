package com.oppo.jacocoreport.component;

import com.oppo.jacocoreport.coverage.ReportGeneratorCov;
import com.oppo.jacocoreport.coverage.entity.ApplicationCodeInfo;
import com.oppo.jacocoreport.coverage.entity.Data;
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
        ReportGeneratorCov reportGeneratorCov = new ReportGeneratorCov(taskId,gitPath,versionname,testedBranch,basicBranch,"","");
        reportGeneratorCov.startCoverageTask();
    }
}

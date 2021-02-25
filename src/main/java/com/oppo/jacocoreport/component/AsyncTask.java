package com.oppo.jacocoreport.component;

import com.oppo.jacocoreport.coverage.ReportGeneratorCov;
import com.oppo.jacocoreport.coverage.entity.ApplicationCodeInfo;
import com.oppo.jacocoreport.coverage.utils.FolderFileScanner;
import com.oppo.jacocoreport.coverage.utils.HttpUtils;
import com.oppo.jacocoreport.response.DefinitionException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Async
public class AsyncTask {
    public void startCoverageTask(ApplicationCodeInfo applicationCodeInfo){
        Long taskId = applicationCodeInfo.getId();
        String applicationID = applicationCodeInfo.getApplicationID();
        System.out.println(new Date().toString()+"start coverage test "+applicationCodeInfo.toString());
        //将相关文件从OCS下载到本地
        FolderFileScanner.fileDownLoad(applicationID,taskId);
        if (applicationCodeInfo.getIsBranchTask()!=0){
            FolderFileScanner.fileDownLoad(applicationID,applicationCodeInfo.getBranchTaskID());
        }
        ReportGeneratorCov reportGeneratorCov = new ReportGeneratorCov(applicationCodeInfo);
        try {
            reportGeneratorCov.startCoverageTask(applicationID);
        }catch (DefinitionException e){
            HttpUtils.sendErrorMSG(taskId,e.getErrorMsg());
        }catch (Exception e){
            e.printStackTrace();
            HttpUtils.sendErrorMSG(taskId,"other error :"+e.getMessage());
        }
    }
}

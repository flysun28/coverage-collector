package com.oppo.test.coverage.backend.util.file;

import com.amazonaws.services.s3.AmazonS3;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.oppo.test.coverage.backend.util.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author 80264236
 * @date 2021/2/9 10:30
 */
@Service
public class FolderFileScanner {

    private static final Logger logger = LoggerFactory.getLogger(FolderFileScanner.class);

    @Resource
    SystemConfig systemConfig;

    private static final List<String> REPORT_EXTENSION_NAME = Lists.newArrayList("html","css","js","gif");
    private static final List<String> FILE_UPLOAD_EXTENSION_NAME = Lists.newArrayList("html","css","js","gif","exec","class");

    /**
     * 执行前下载对应文件
     *@param projectName : 项目名称,分支覆盖率数据目录
     *@param taskId : 任务id，覆盖率任务相关目录
     * */
    public void fileDownLoad(String projectName,Long taskId){
        List<String> fileList = getDownloadFileList(projectName, taskId);
        AmazonS3 s3 = OcsUtil.getAmazonS3();
        for (String fileName : fileList){
            OcsUtil.download(s3,fileName,fileName);
        }
    }

    private List<String> getDownloadFileList(String projectName,Long taskId){
        String taskPath = systemConfig.getReportBasePath() + "/taskID/" + taskId;
        String branchPath = systemConfig.getReportBasePath() + "/projectCovPath/" + projectName;

        List<String> result = new ArrayList<>(OcsUtil.query(taskPath));
        result.addAll(OcsUtil.query(branchPath));
        return result;
    }


    /**
     * 轮询过程上传报告文件
     *@param projectName : git项目名称,分支覆盖率数据目录
     *@param taskId : 任务id，覆盖率任务相关目录
     * */
    public void reportUpload(String projectName,Long taskId){
        ArrayList<File> uploadFileList = getUploadFileList(projectName, taskId, false);
        AmazonS3 s3 = OcsUtil.getAmazonS3();
        for (File file : uploadFileList){
            List<String> splitList = Splitter.on(".").trimResults().splitToList(file.getName());
            String extensionName = splitList.get(splitList.size()-1);
            OcsUtil.upload(s3,file.getAbsolutePath(),file,"html".equals(extensionName)?"text/html":null);
        }
    }

    /**
     * 上传分支覆盖率报告文件
     *@param taskId : 任务id，覆盖率任务相关目录
     * */
    public void branchReportUpload(String projectName,Long taskId){
        ArrayList<File> uploadFileList = getUploadFileList(projectName, taskId, false);
        AmazonS3 s3 = OcsUtil.getAmazonS3();
        for (File file : uploadFileList){
            List<String> splitList = Splitter.on(".").trimResults().splitToList(file.getName());
            String extensionName = splitList.get(splitList.size()-1);
            OcsUtil.upload(s3,file.getAbsolutePath(),file,"html".equals(extensionName)?"text/html":null);
        }
    }



    /**
     * 执行完毕上传对应文件
     *@param projectName : git项目名称,分支覆盖率数据目录
     *@param taskId : 任务id，覆盖率任务相关目录
     * */
    public void fileUpload(String projectName,Long taskId){
        ArrayList<File> uploadFileList = getUploadFileList(projectName, taskId, true);
        AmazonS3 s3 = OcsUtil.getAmazonS3();
        boolean canDelete = true;
        for (File file : uploadFileList){
            List<String> splitList = Splitter.on(".").trimResults().splitToList(file.getName());
            String extensionName = splitList.get(splitList.size()-1);
            if (!OcsUtil.upload(s3,file.getAbsolutePath(),file,"html".equals(extensionName)?"text/html":null)){
                canDelete = false;
            }
        }
        if (canDelete){
            deleteAllFileAfterUpload(projectName, taskId);
        }
    }

    private void deleteAllFileAfterUpload(String projectName, Long taskId){
        String taskPath = systemConfig.getReportBasePath() + "/taskID/" + taskId;
        String branchPath = systemConfig.getReportBasePath() + "/projectCovPath/" + projectName;
        delAllFile(taskPath);
        delAllFile(branchPath);
    }

    private ArrayList<File> getUploadFileList(String projectName, Long taskId, boolean timerFinished){
        ArrayList<File> result = new ArrayList<>();
        String taskPath = systemConfig.getReportBasePath() + "/taskID/" + taskId;
        String branchPath = systemConfig.getReportBasePath() + "/projectCovPath/" + projectName;
        ArrayList<File> tempFileList = scanFilesWithRecursion(taskPath,timerFinished,true);
        if (!CollectionUtils.isEmpty(tempFileList)){
            result.addAll(tempFileList);
        }
        tempFileList = scanFilesWithRecursion(branchPath,timerFinished,true);
        if (!CollectionUtils.isEmpty(tempFileList)){
            result.addAll(tempFileList);
        }
        return result;
    }


    /**
     * 删除指定文件夹下所有文件
     * @param path 文件夹完整绝对路径
     */
    public static void delAllFile(String path){
        ArrayList<File> fileArrayList = scanFilesWithRecursion(path,true,false);
        if (CollectionUtils.isEmpty(fileArrayList)){
            return;
        }
        for (File file :fileArrayList){
            if (!file.delete()){
                logger.warn("delete failed : {}",file.getAbsolutePath());
            }
        }
    }


    /**
     * 递归扫描指定文件夹下面的指定文件
     * @param folderPath : 文件目录
     * @param timerFinished : 是否任务结束,若结束则传所有文件,否则只上传报告
     * @param isOcsCheck : 是否为OCS上传检测
     * @return : 文件列表
     */
    private static ArrayList<File> scanFilesWithRecursion(String folderPath,boolean timerFinished, boolean isOcsCheck){

        ArrayList<File> scanFile = new ArrayList<File>();

        File directory = new File(folderPath);
        //不是目录直接返回
        if (!directory.isDirectory()) {
            return scanFile;
        }
        //取到目录下所有文件
        File[] fileList = directory.listFiles();
        if (fileList==null || fileList.length<1){
            return scanFile;
        }
        // 遍历文件,是文件夹则递归,是文件则获取
        for (File file : fileList) {
            if (file.isDirectory()) {
                scanFile.addAll(Objects.requireNonNull(scanFilesWithRecursion(file.getAbsolutePath(),timerFinished,isOcsCheck)));
            }
            else {
                //筛选指定的需要上传的文件
                List<String> splitList = Splitter.on(".").trimResults().splitToList(file.getName());
                String extensionName = splitList.get(splitList.size()-1);

                if (!isOcsCheck || isUploadFile(extensionName,timerFinished)){
                    scanFile.add(file);
                }
            }
        }
        return scanFile;
    }

    /**
     * 判断该文件是否需要上传OCS的文件
     * @param extensionName : 文件扩展名
     * @param timerFinished : 当时是否轮询任务
     * */
    private static boolean isUploadFile(String extensionName,boolean timerFinished){
        //未完成,只上传报告
        if (!timerFinished && REPORT_EXTENSION_NAME.contains(extensionName)){
            return true;
        }
        //完成后,全部上传
        if (timerFinished && FILE_UPLOAD_EXTENSION_NAME.contains(extensionName)){
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
//        ArrayList<File> result = FolderFileScanner.scanFilesWithRecursion("D:\\文档\\培训课件\\培训材料准备\\2",true);
//        AmazonS3 s3 = OcsUtil.getAmazonS3();
//        for (File file : result){
//            List<String> splitList = Splitter.on(".").trimResults().splitToList(file.getName());
//            String extensionName = splitList.get(splitList.size()-1);
//            String res = file.getAbsolutePath()
//                    .replace("D:\\文档\\培训课件\\培训材料准备\\2","/home/service/app/coveragebackend/fawoknqovs7v/taskID/15483")
//                    .replace("\\","/");
//
//            System.out.println(res);
//
//            OcsUtil.upload(s3,res,file,"html".equals(extensionName)?"text/html":null);
//        }

        delAllFile("F:\\业务场景\\play\\_1");

    }

}

package com.oppo.jacocoreport.coverage.utils;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jacoco.core.internal.diff.GitAdapter;

import java.io.File;
import java.util.ArrayList;

public class GitUtil {
    private String gitName;
    private String gitPassword;


    public GitUtil(String gitName,String gitPassword){
        this.gitName = gitName;
        this.gitPassword = gitPassword;

    }

    public  String cloneRepository(String url,File localPath)
    {
        try{
            System.out.println("开始下载......");
            GitAdapter.setCredentialsProvider(gitName, gitPassword);
            CloneCommand cc = Git.cloneRepository().setURI(url).setCredentialsProvider(new UsernamePasswordCredentialsProvider(this.gitName,this.gitPassword));
            cc.setDirectory(localPath).call();
            System.out.println("下载完成......");
            return  "success";
        }catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("自动clone代码失败，请手动clone代码到本地");
            return "error";
        }
    }
    public void checkoutBranch(String gitPath,String newBranchName,String oldBranchName){
        try {
            GitAdapter.setCredentialsProvider(gitName, gitPassword);
            GitAdapter gitAdapter = new GitAdapter(gitPath);
            Ref localBranchRef = gitAdapter.getRepository().exactRef("refs/heads/" + newBranchName);
            if(!oldBranchName.equals("")){
                Ref localMasterRef = gitAdapter.getRepository().exactRef("refs/heads/" + oldBranchName);
                gitAdapter.checkOutAndPull(localMasterRef, oldBranchName);
            }

            gitAdapter.checkOutAndPull(localBranchRef, newBranchName);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String getLastUrlString(String strUrl){
        String[] splitStr = strUrl.split("/");
        int len = splitStr.length;
        String result = splitStr[len -1];
        //去除末尾.git
        result = result.substring(0,result.length()-4);

        return result;
    }

    /**
     * 获取各个应用名
     * @param codeCoveragePath
     */
    public static ArrayList getApplicationNames(File codeCoveragePath, ArrayList applicationList){
        if(codeCoveragePath.isDirectory()) {
            File[] fileList = codeCoveragePath.listFiles();
            //遍历代码工程
            for (File f : fileList) {
                //判断是否文件夹目录
                if (f.isDirectory()) {
                    //如果当前文件夹名== src
                    if ( "src".equals(f.getName())) {
                        //断定当前是应用名
                        applicationList.add(codeCoveragePath);
                        return applicationList;
                    }
                    else{
                        getApplicationNames(f,applicationList);
                    }
                }
            }
        }
        return applicationList;
    }

    public static void main(String[] args){
        GitUtil gitUtil = new GitUtil("80289528","Zhc_172520");
        String urlString = "git@gitlab.os.adc.com:financeTestTechGroup/luckymonkey.git";
        String projectName = gitUtil.getLastUrlString(urlString);
        System.out.println(projectName);
        File projectPath = new File("D:\\codeCoverage",projectName);
        gitUtil.cloneRepository(urlString,projectPath);

//        ArrayList filelist = new ArrayList();
//        ArrayList applicationNames = GitUtil.getApplicationNames(projectPath,filelist);
//        gitUtil.checkoutBranch(projectPath.toString(),"daily","master");
//        System.out.println(filelist);
    }
}
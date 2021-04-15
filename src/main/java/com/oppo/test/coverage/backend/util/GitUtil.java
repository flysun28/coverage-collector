package com.oppo.test.coverage.backend.util;

import com.oppo.test.coverage.backend.model.response.DefinitionException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.jacoco.core.internal.diff.GitAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;

@Service
public class GitUtil {

    private static final Logger logger = LoggerFactory.getLogger(GitUtil.class);

    public static void cloneRepository(String url, File localPath, String newBranchName) {
        try {
            Git.cloneRepository().setURI(url).setDirectory(localPath).setBranch(newBranchName).call();
            logger.info("代码下载完成 : {}",url);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("自动clone代码失败 : {}",url);
        }
    }

    /**
     * 切换分支
     * @param newBranchName : 被测分支
     * @param oldBranchName : 基线分支
     * @param newTag : 被测commitId
     * */
    public static String checkoutBranch(String gitPath, String newBranchName, String oldBranchName, String newTag) throws DefinitionException {
        GitAdapter gitAdapter = new GitAdapter(gitPath);
        Git git = gitAdapter.getGit();
        Repository repo = gitAdapter.getRepository();
        //默认master分支，如果不存在，取release分支
        try {
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            logger.info("分支切换: {}",repo.getBranch());
            Ref localMasterRef = repo.exactRef("refs/heads/" + oldBranchName);
            gitAdapter.checkOutAndPull(localMasterRef, oldBranchName);
            gitAdapter.checkOut(oldBranchName);
            git.pull().call();

            Ref localBranchRef = repo.exactRef("refs/heads/" + newBranchName);
            gitAdapter.checkOutAndPull(localBranchRef, newBranchName);
            gitAdapter.checkOut(newBranchName);
            git.pull().call();
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(newTag).call();
        } catch (RefNotFoundException rfnf) {
            try {
                git.reset().setMode(ResetCommand.ResetType.HARD).setRef(newTag).call();
                newBranchName = oldBranchName;
            } catch (Exception ep) {
                ep.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newBranchName;
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
     * @param codeCoveragePath :
     */
    public static ArrayList<File> getApplicationNames(File codeCoveragePath, ArrayList<File> applicationList){
        if(codeCoveragePath.isDirectory()) {
            File[] fileList = codeCoveragePath.listFiles();

            if (fileList==null || fileList.length<1){
                return applicationList;
            }

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
//        GitUtil gitUtil = new GitUtil("80289528","Zhc_172520");
//        String urlString = "git@gitlab.os.adc.com:financeTestTechGroup/luckymonkey.git";
//        String projectName = gitUtil.getLastUrlString(urlString);
//        System.out.println(projectName);
//        File projectPath = new File("D:\\codeCoverage",projectName);
//        gitUtil.cloneRepository(urlString,projectPath);

        GitUtil.cloneRepository("git@gitlab.os.adc.com:bot/java/bot-dm-system.git",new File("D:\\codeCoverage\\bot-dm-system"),"revolution");
    }
}
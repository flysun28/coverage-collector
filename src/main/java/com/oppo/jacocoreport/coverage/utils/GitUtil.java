package com.oppo.jacocoreport.coverage.utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.oppo.jacocoreport.response.DefinitionException;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.FS;
import org.jacoco.core.internal.diff.GitAdapter;
import java.io.File;
import java.util.ArrayList;

public class GitUtil {
    private String gitName;
    private String gitPassword;


    public GitUtil(){

    }
    public GitUtil(String gitName,String gitPassword){
        this.gitName = gitName;
        this.gitPassword = gitPassword;

    }
    final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
        @Override
        protected void configure(OpenSshConfig.Host host, Session session) {
            session.setConfig("StrictHostKeyChecking","no");
        }

        @Override
        protected JSch createDefaultJSch(FS fs) throws JSchException {
            JSch jSch = super.createDefaultJSch(fs);
            jSch.addIdentity("C:\\Users\\80289528\\.ssh\\id_rsa");
            jSch.setKnownHosts("C:\\Users\\80289528\\.ssh\\known_hosts");
            return super.createDefaultJSch(fs);

        }
    };

    public String cloneRepositoryBySSH(String url,File localPath){
         CloneCommand cloneCommand = Git.cloneRepository();
         cloneCommand.setTransportConfigCallback(new TransportConfigCallback() {
                                                     @Override
                                                     public void configure(Transport transport) {
                                                         SshTransport sshTransport = (SshTransport) transport;
                                                         sshTransport.setSshSessionFactory(sshSessionFactory);
                                                     }
                                                 }

         );
         cloneCommand.setURI(url);
        cloneCommand.setDirectory(localPath);
        try{
            cloneCommand.call().checkout();
        }catch (GitAPIException e){
            e.printStackTrace();
        }
    return "success";
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
    public String checkoutBranch(String gitPath,String newBranchName,String oldBranchName,String newTag) throws DefinitionException{
        GitAdapter.setCredentialsProvider(gitName, gitPassword);
        GitAdapter gitAdapter = new GitAdapter(gitPath);
        Git git = gitAdapter.getGit();
        //默认master分支，如果不存在，取release分支
        try {
            System.out.println(gitAdapter.getGit().getRepository().getBranch());
            Ref localMasterRef = gitAdapter.getRepository().exactRef("refs/heads/" + oldBranchName);
            gitAdapter.checkOutAndPull(localMasterRef, oldBranchName);
            gitAdapter.checkOut(oldBranchName);
            git.pull().call();


            Ref localBranchRef = gitAdapter.getRepository().exactRef("refs/heads/" + newBranchName);
            gitAdapter.checkOutAndPull(localBranchRef, newBranchName);
            gitAdapter.checkOut(newBranchName);
            git.pull().call();

            ObjectId head = gitAdapter.getRepository().resolve(newTag + "^{tree}");
            //Instanciate a reader to read the data from the Git database
            ObjectReader reader = gitAdapter.getRepository().newObjectReader();
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, head);

        }catch (RefNotFoundException rfnf){
            try {
                ObjectId head = gitAdapter.getRepository().resolve(newTag + "^{tree}");
                //Instanciate a reader to read the data from the Git database
                ObjectReader reader = gitAdapter.getRepository().newObjectReader();
                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                newTreeIter.reset(reader, head);
                newBranchName = oldBranchName;
            }catch (Exception ep){
                ep.printStackTrace();
            }
        }
        catch (Exception e) {
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
//        GitUtil gitUtil = new GitUtil("80289528","Zhc_172520");
//        String urlString = "git@gitlab.os.adc.com:financeTestTechGroup/luckymonkey.git";
//        String projectName = gitUtil.getLastUrlString(urlString);
//        System.out.println(projectName);
//        File projectPath = new File("D:\\codeCoverage",projectName);
//        gitUtil.cloneRepository(urlString,projectPath);

        GitUtil gitUtil1 = new GitUtil();
        gitUtil1.cloneRepositoryBySSH("git@gitlab.os.adc.com:cql/CIdemo.git",new File("D:\\codeCoverage\\CIdemo"));
    }
}
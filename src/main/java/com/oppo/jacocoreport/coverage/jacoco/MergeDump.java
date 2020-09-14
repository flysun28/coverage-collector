package com.oppo.jacocoreport.coverage.jacoco;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MergeDump {
    private final String path;
    private final File destFile ;
    private final String newBranchName;

    public MergeDump(String path,String newBranchName){
        this.path = path;
        this.newBranchName = newBranchName;
        this.destFile = new File(path,newBranchName+"_jacocoAll.exec");
    }
    private List<File> fileSets(String dir){
     List<File> fileSetList = new ArrayList<File>();
     File path = new File(dir);
     if(!path.exists()){
        System.out.println("No path name is :"+dir);
        return null;
     }
     File[] files = path.listFiles();
     try{
        if(files == null || files.length == 0){
            return null;
        }
     }catch (NullPointerException npe){
         npe.printStackTrace();
     }

     for(File file : files){
         if(file.getName().contains(".exec")){
             if(file.getName().contains(this.newBranchName)) {
                 fileSetList.add(file);
             }
         }
     }
     return fileSetList;
    }

    public File executeMerge() {
        final ExecFileLoader loader = new ExecFileLoader();
        //如果没有获取新覆盖率文件，就不merge
        if(fileSets(this.path).size() == 1){
            if(fileSets(this.path).get(0).getName().equals("jacocoAll.exec")){
                return null;
            }
        }
        load(loader);
        save(loader);
        //执行完成后，删除非必须的dump文件
        for(final File fileSet : fileSets(this.path)){
          if(!fileSet.getName().equals("jacocoAll.exec")){
             fileSet.delete();
          }
        }
        return this.destFile;

    }

    /**
     * 加载dump文件
     * @param loader
     */
    public void load(final ExecFileLoader loader){
         for(final File fileSet : fileSets(this.path)){
              final  File inputFile = new File(this.path,fileSet.getName());
              if(inputFile.isDirectory()){
                  continue;
              }
              try{
//                  System.out.println("Loading execution data file " +inputFile.getAbsolutePath());
                  loader.load(inputFile);
//                  System.out.println(loader.getExecutionDataStore().getContents());
              }catch (final IOException e){
                  e.printStackTrace();
              }
         }
    }

    public void save(final ExecFileLoader loader){
        if(loader.getExecutionDataStore().getContents().isEmpty()){
            System.out.println("Skipping JaCoCo merge execution due to missing execution data files");
            return;
        }
        try{
           loader.save(this.destFile,false);
        }catch (final IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        MergeDump mergeDump = new MergeDump("D:\\codeCoverage\\10010","master");
        mergeDump.executeMerge();
    }
}

package com.oppo.jacocoreport.coverage.jacoco;

import org.jacoco.core.data.*;
import org.jacoco.core.runtime.RemoteControlReader;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLClassLoader;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class AnalyNewBuildVersion implements ISessionInfoVisitor, IExecutionDataVisitor {

    private final ExecutionDataReader reader;
    private Set<String> classIDSet;
    private Set<String> classNameSet;
    private Boolean findnewversion = false;
    private HashMap<Long,String> classMap;
    private String classPath;

    public AnalyNewBuildVersion(String classPath,String execFile)  throws IOException {
        reader = new  ExecutionDataReader(new FileInputStream(execFile));
        this.classPath = classPath;
        ClassInfo classInfo =  new ClassInfo(classPath);
        classInfo.execute();
        this.classIDSet = classInfo.getClassIDSet();
        this.classNameSet = classInfo.getClassNameSet();
        this.classMap = classInfo.getClassMap();
        reader.setSessionInfoVisitor(this);
        reader.setExecutionDataVisitor(this);
    }

    /**
     * 判断是否有生成新版本
     * @return
     */
    public boolean findNewBuildVersion(){
        try {
            while (reader.read()) {
                if(findnewversion){
                   break;
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
       return findnewversion;
    }

    public static boolean fileNotUpdateByHours(File execFile,int hour){
        execFile.length();
        long modifedtime = execFile.lastModified();
        long currenttime = System.currentTimeMillis();
        double result = (currenttime - modifedtime)*1.0/(1000*60*60);
        if(result <= hour){
           return true;
        }else{
            return false;
        }
    }

    @Override
    public void visitSessionInfo(SessionInfo sessionInfo) {

    }

    @Override
    public void visitClassExecution(ExecutionData executionData) {
        if(classNameSet.contains(executionData.getName())) {
            if(!classIDSet.contains(Long.toHexString(executionData.getId()))){
//                System.out.println(executionData.getName());
//                System.out.println(Long.toHexString(executionData.getId()));
                try {
                    String classpath = classPath+File.separator+executionData.getName()+".class";
                    FileInputStream in = new FileInputStream(classpath);
                    ClassReader classReader = new ClassReader(in);
                    if(!classReader.getClass().isInterface()){
                        findnewversion = true;
                        System.out.println("changed class "+classpath);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    public static void main(String[] args) throws IOException{
        AnalyNewBuildVersion analyNewBuildVersion = new AnalyNewBuildVersion("D:\\codeCoverage\\taskID\\10017\\classes","D:\\codeCoverage\\taskID\\10017\\master\\jacocoAll.exec");
        Boolean newversion = analyNewBuildVersion.findNewBuildVersion();
        System.out.println(newversion);
//        System.out.println(AnalyNewBuildVersion.fileNotUpdateBy24Hours(new File("D:\\jacocoCov\\20200728102452\\fin-loan-api\\jacocoAll.exec")));
    }
}

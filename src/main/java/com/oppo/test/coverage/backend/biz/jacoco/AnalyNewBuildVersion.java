package com.oppo.test.coverage.backend.biz.jacoco;

import org.jacoco.core.data.*;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class AnalyNewBuildVersion implements ISessionInfoVisitor, IExecutionDataVisitor {

    private static final Logger logger = LoggerFactory.getLogger(AnalyNewBuildVersion.class);

    private final ExecutionDataReader reader;
    private Set<String> classIdSet;
    private Set<String> classNameSet;
    private Boolean findNewVersion = false;
    private String classPath;

    public AnalyNewBuildVersion(String classPath, String execFile) throws IOException {
        reader = new ExecutionDataReader(new FileInputStream(execFile));
        this.classPath = classPath;
        ClassInfo classInfo = new ClassInfo(classPath);
        classInfo.execute();
        this.classIdSet = classInfo.getClassIDSet();
        this.classNameSet = classInfo.getClassNameSet();
        reader.setSessionInfoVisitor(this);
        reader.setExecutionDataVisitor(this);
    }

    /**
     * 判断是否有生成新版本
     *
     * @return
     */
    public boolean findNewBuildVersion() {
        try {
            while (reader.read()) {
                if (findNewVersion) {
                    break;
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return findNewVersion;
    }

    public static boolean fileNotUpdateByHours(File execFile, int hour) {
        long modifiedTime = execFile.lastModified();
        long currentTime = System.currentTimeMillis();
        double result = (currentTime - modifiedTime) * 1.0 / (1000 * 60 * 60);
        return result <= hour;
    }

    @Override
    public void visitSessionInfo(SessionInfo sessionInfo) {

    }

    @Override
    public void visitClassExecution(ExecutionData executionData) {
        if (classNameSet.contains(executionData.getName())) {
            if (!classIdSet.contains(Long.toHexString(executionData.getId()))) {
                try {
                    String classpath = classPath + File.separator + executionData.getName() + ".class";
                    FileInputStream in = new FileInputStream(classpath);
                    ClassReader classReader = new ClassReader(in);
                    if (!classReader.getClass().isInterface()) {
                        findNewVersion = true;
                        logger.warn("changed class : {}",classpath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        AnalyNewBuildVersion analyNewBuildVersion = new AnalyNewBuildVersion("D:\\codeCoverage\\taskID\\10017\\classes", "D:\\codeCoverage\\taskID\\10017\\master\\jacocoAll.exec");
        Boolean newVersion = analyNewBuildVersion.findNewBuildVersion();
        System.out.println(newVersion);
//        System.out.println(AnalyNewBuildVersion.fileNotUpdateBy24Hours(new File("D:\\jacocoCov\\20200728102452\\fin-loan-api\\jacocoAll.exec")));
    }
}

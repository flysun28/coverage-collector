package com.oppo.jacocoreport.coverage.maven;

import com.oppo.jacocoreport.response.DefinitionException;
import com.oppo.jacocoreport.response.ErrorEnum;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class Maveninvoker {

    public static void buildMaven(File pomFile, String mavenhome) throws DefinitionException {
        System.out.println("开始编译源文件");
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomFile);
        request.setGoals(Collections.singletonList("clean compile"));
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(mavenhome));
        invoker.setLogger(new PrintStreamLogger(System.err, InvokerLogger.ERROR) {
        });

        invoker.setOutputHandler(new InvocationOutputHandler() {
            @Override
            public void consumeLine(String s) throws IOException {

            }
        });

        try {
            invoker.execute(request);
        } catch (MavenInvocationException e) {
            e.printStackTrace();
            throw new DefinitionException(ErrorEnum.BUILD_MAVEN.getErrorCode(),e.getMessage());
        }

        try {
            if (invoker.execute(request).getExitCode() == 0) {
                System.out.println("success");
            } else {
                throw new DefinitionException(ErrorEnum.BUILD_MAVEN.getErrorCode(),ErrorEnum.BUILD_MAVEN.getErrorMsg());
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
            throw new DefinitionException(ErrorEnum.BUILD_MAVEN.getErrorCode(),e.getMessage());
        }
    }

    public static void main(String[] args) {
        Maveninvoker.buildMaven(new File("D:\\code\\pandora\\pom.xml"), "D:\\apache-maven-3.6.1");
    }
}

package com.oppo.test.jacocoreport.maven;

import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class Maveninvoker {

    public static void buildMaven(File pomFile, String mavenhome) {
        System.out.println("开始编译源文件");
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomFile);
        request.setGoals(Collections.singletonList("compile"));
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
        }

        try {
            if (invoker.execute(request).getExitCode() == 0) {
                System.out.println("success");
            } else {
                System.err.println("error");
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Maveninvoker.buildMaven(new File("D:\\code\\pandora\\pom.xml"), "D:\\apache-maven-3.6.1");
    }
}

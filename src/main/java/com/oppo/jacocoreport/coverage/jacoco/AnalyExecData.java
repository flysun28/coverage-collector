package com.oppo.jacocoreport.coverage.jacoco;

import org.jacoco.core.data.*;
import org.jacoco.core.runtime.RemoteControlReader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

public class AnalyExecData {
    //    private static final String EXECFILE = "jacoco-server.exec";
//    private static final String ALLEXEC = "D:\\jacocoCov\\20200728102452\\fin-loan-api\\jacocoAll.exec";
    private String filteredExec;
    private String originExec;

    public AnalyExecData(String filteredExec,String originExec){
        this.filteredExec = filteredExec;
        this.originExec = originExec;
    }

    private class Handler implements Runnable, ISessionInfoVisitor,
            IExecutionDataVisitor {


        private final RemoteControlReader reader;

        private final ExecutionDataWriter fileWriter;
        private Set<String> classIDSet;

        Handler(final ExecutionDataWriter fileWriter,String classPath)
                throws IOException {
            this.fileWriter = fileWriter;
            ClassInfo classInfo =  new ClassInfo(classPath);
            classInfo.execute();
            this.classIDSet = classInfo.getClassIDSet();

            // Just send a valid header:
//            new RemoteControlWriter(new FileOutputStream(ALLEXEC));

            reader = new RemoteControlReader(new FileInputStream(originExec));
            reader.setSessionInfoVisitor(this);
            reader.setExecutionDataVisitor(this);

        }

        public void run() {
            try {
                while (reader.read()) {
                }
                synchronized (fileWriter) {
                    fileWriter.flush();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        public void visitSessionInfo(final SessionInfo info) {
            synchronized (fileWriter) {
                fileWriter.visitSessionInfo(info);
            }
        }

        public void visitClassExecution(final ExecutionData data) {
            if(classIDSet.contains(Long.toHexString(data.getId()))) {
                synchronized (fileWriter) {
                    fileWriter.visitClassExecution(data);
                }
            }
        }
    }

    public void filterOldExecData(String classPath) throws Exception{
        final ExecutionDataWriter fileWriter = new ExecutionDataWriter(
                new FileOutputStream(filteredExec));
        final Handler handler = new Handler(fileWriter,classPath);
        Thread thread = new Thread(handler);
        thread.start();
        thread.join();
    }
    public static void main(final String[] args) throws Exception {
        AnalyExecData analyExecData = new  AnalyExecData("jacoco-server.exec","D:\\jacocoCov\\20200728102452\\fin-loan-api\\jacocoAll.exec");
        analyExecData.filterOldExecData("D:\\codeCoverage\\fin-loan\\classes");
    }
}

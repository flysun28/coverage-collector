package com.oppo.jacocoreport.coverage.jacoco;

import org.jacoco.core.data.*;
import org.jacoco.core.runtime.RemoteControlReader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class AnalyDiffExecData {
    //    private static final String EXECFILE = "jacoco-server.exec";
//    private static final String ALLEXEC = "D:\\jacocoCov\\20200728102452\\fin-loan-api\\jacocoAll.exec";
    private String filteredExec;
    private String originExec;

    public AnalyDiffExecData(String filteredExec, String originExec){
        this.filteredExec = filteredExec;
        this.originExec = originExec;
    }

    private class Handler implements Runnable, ISessionInfoVisitor,
            IExecutionDataVisitor {


        private final RemoteControlReader reader;

        private final ExecutionDataWriter fileWriter;
        private List<ClassInfo> classInfos;

        Handler(final ExecutionDataWriter fileWriter,List<ClassInfo> classInfos)
                throws IOException {
            this.fileWriter = fileWriter;
            this.classInfos = classInfos;

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
//            if(classIDSet.contains(Long.toHexString(data.getId()))) {
                synchronized (fileWriter) {
                    fileWriter.visitClassExecution(data);
                }
//            }
        }
    }

    public void filterOldExecData(List<ClassInfo> classInfos) throws Exception{
        final ExecutionDataWriter fileWriter = new ExecutionDataWriter(
                new FileOutputStream(filteredExec));
        final Handler handler = new Handler(fileWriter,classInfos);
        Thread thread = new Thread(handler);
        thread.start();
        thread.join();
    }

    public static void main(final String[] args) throws Exception {
        AnalyDiffExecData analyExecData = new AnalyDiffExecData("jacoco-server.exec","D:\\jacocoCov\\20200728102452\\fin-loan-api\\jacocoAll.exec");
//        analyExecData.filterOldExecData("D:\\codeCoverage\\fin-loan\\classes");
    }
}

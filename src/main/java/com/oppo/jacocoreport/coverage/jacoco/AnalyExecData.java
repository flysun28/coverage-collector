package com.oppo.jacocoreport.coverage.jacoco;

import org.jacoco.core.data.*;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AnalyExecData {
    private static final String EXECFILE = "jacoco-server.exec";
    private static final String ALLEXEC = "D:\\jacocoCov\\20200728102452\\fin-loan-api\\jacocoAll.exec";

    public static void main(final String[] args) throws IOException {
        final ExecutionDataWriter fileWriter = new ExecutionDataWriter(
                new FileOutputStream(EXECFILE));

//        while (true) {
            final Handler handler = new Handler(fileWriter);
            new Thread(handler).start();
//        }
    }

    private static class Handler implements Runnable, ISessionInfoVisitor,
            IExecutionDataVisitor {


        private final RemoteControlReader reader;

        private final ExecutionDataWriter fileWriter;

        Handler(final ExecutionDataWriter fileWriter)
                throws IOException {
            this.fileWriter = fileWriter;

            // Just send a valid header:
//            new RemoteControlWriter(new FileOutputStream(ALLEXEC));

            reader = new RemoteControlReader(new FileInputStream(ALLEXEC));
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
            System.out.printf("Retrieving execution Data for session: %s%n",
                    info.getId());
            synchronized (fileWriter) {
                fileWriter.visitSessionInfo(info);
            }
        }

        public void visitClassExecution(final ExecutionData data) {
            synchronized (fileWriter) {
                fileWriter.visitClassExecution(data);
            }
        }
    }
}

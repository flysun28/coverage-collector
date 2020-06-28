package com.oppo.test.jacocoreport.jacoco;

import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ExecutionDataClient {


    public void getExecutionData(String address, int port, String destfile) throws IOException {
        System.out.println("exec文件路径" + destfile);
        final FileOutputStream localFile = new FileOutputStream(destfile);
        final ExecutionDataWriter localWriter = new ExecutionDataWriter(localFile);

        //连接Jacoco服务
        final Socket socket = new Socket(InetAddress.getByName(address), port);
        final RemoteControlWriter writer = new RemoteControlWriter(socket.getOutputStream());
        final RemoteControlReader reader = new RemoteControlReader(socket.getInputStream());
        reader.setSessionInfoVisitor(localWriter);
        reader.setExecutionDataVisitor(localWriter);

        //每隔1分钟 发送Dump命令，获取Exec数据

        writer.visitDumpCommand(true, false);
        if (!reader.read()) {
            throw new IOException("Socket closed unexpectedly.");
        }
        socket.close();
        localFile.close();
    }

    /**
     * Starts the execution data request.
     *
     * @param args
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {
        ExecutionDataClient executionDataClient = new ExecutionDataClient();
        executionDataClient.getExecutionData("127.0.0.1", 6300, "jacoco.exec");
    }

}

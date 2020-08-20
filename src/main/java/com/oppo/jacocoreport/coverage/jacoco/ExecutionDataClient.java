package com.oppo.jacocoreport.coverage.jacoco;

import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

public class ExecutionDataClient {


    public boolean getExecutionData(String address, int port, File destfile) throws IOException {
        boolean getedExecData = false;
        final FileOutputStream localFile = new FileOutputStream(destfile);
        final ExecutionDataWriter localWriter = new ExecutionDataWriter(localFile);

        //连接Jacoco服务
        try {
            final Socket socket = new Socket(InetAddress.getByName(address), port);
            if (socket.isConnected()) {
                final RemoteControlWriter writer = new RemoteControlWriter(socket.getOutputStream());
                final RemoteControlReader reader = new RemoteControlReader(socket.getInputStream());
                reader.setSessionInfoVisitor(localWriter);
                reader.setExecutionDataVisitor(localWriter);

                writer.visitDumpCommand(true, false);
                if (!reader.read()) {
                    throw new IOException("Socket closed unexpectedly.");
                }
            }
            socket.close();
            getedExecData = true;
        }catch (ConnectException e){
            System.out.println(address+" cann't connect");
            localFile.close();
            destfile.delete();

        }finally {
            localFile.close();
        }
        return getedExecData;
    }

    /**
     * Starts the execution data request.
     *
     * @param args
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {
        ExecutionDataClient executionDataClient = new ExecutionDataClient();
        executionDataClient.getExecutionData("10.3.241.17", 8098, new File("jacoco.exec"));
    }

}

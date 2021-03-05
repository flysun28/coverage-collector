package com.oppo.jacocoreport.coverage.jacoco;

import com.oppo.jacocoreport.coverage.utils.Config;
import com.oppo.jacocoreport.coverage.utils.HttpUtils;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ExecutionDataClient {


    public boolean getExecutionData(String address, int port, File destfile, Integer testedEnv) throws IOException {
        boolean getedExecData = false;
        final FileOutputStream localFile = new FileOutputStream(destfile);
        final ExecutionDataWriter localWriter = new ExecutionDataWriter(localFile);

        //连接Jacoco服务
        try {

            if (testedEnv==2){
                port = getPort(address,port);
                if (port == -1){
                    return false;
                }
                address = Config.TransferBaseIp;
            }

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
        }catch (Exception e){
            System.out.println(address+" cann't connect");
            e.printStackTrace();
            localFile.close();
            destfile.delete();
        }
        finally {
            localFile.close();
        }
        return getedExecData;
    }

    private int getPort(String address, int port){
        String tempUrl = Config.GET_TRANSFER_PORT_URL+"?address="+address+"&port="+port;
        return Integer.parseInt(HttpUtils.sendGet(tempUrl));
    }

    /**
     * Starts the execution data request.
     *
     * @param args
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {
        ExecutionDataClient executionDataClient = new ExecutionDataClient();
        executionDataClient.getExecutionData("10.177.245.87", 8098, new File("cdojacoco.exec"),1);
    }

}

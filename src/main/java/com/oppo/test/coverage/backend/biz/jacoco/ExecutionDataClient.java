package com.oppo.test.coverage.backend.biz.jacoco;

import com.oppo.test.coverage.backend.util.SystemConfig;
import com.oppo.test.coverage.backend.util.http.HttpRequestUtil;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

@Service
public class ExecutionDataClient {

    @Resource
    SystemConfig systemConfig;

    private static final Logger logger = LoggerFactory.getLogger(ExecutionDataClient.class);


    public boolean getExecutionData(String address, int port, File destFile, Integer testedEnv) throws Exception {
        boolean gotExecData = false;
        final FileOutputStream localFile = new FileOutputStream(destFile);
        final ExecutionDataWriter localWriter = new ExecutionDataWriter(localFile);

        //连接Jacoco服务
        try {
            if (testedEnv == 2) {
                //生产环境
                port = getPort(address, port);
                if (port == -1) {
                    throw new Exception("生产环境获取转发端口失败");
                }
                address = systemConfig.getTransferBaseIp();
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
            gotExecData = true;

        } catch (Exception e) {
            logger.error("getExecutionData connect error : {} , {} , {}", address, port, testedEnv == 2 ? "生产环境" : "测试环境");
            e.printStackTrace();
            localFile.close();
            destFile.delete();
        } finally {
            localFile.close();
        }
        return gotExecData;
    }

    private int getPort(String address, int port) {
        String tempUrl = systemConfig.getTransferUrl() + "?address=" + address + "&port=" + port;
        return Integer.parseInt(HttpRequestUtil.getForObject(tempUrl, String.class, 3));
    }

    /**
     * Starts the execution data request.
     *
     * @param args
     * @throws IOException
     */
    public static void main(final String[] args) throws Exception {
        ExecutionDataClient executionDataClient = new ExecutionDataClient();
        executionDataClient.getExecutionData("10.177.245.87", 8098, new File("cdojacoco.exec"), 1);
    }

}

package com.oppo.test.coverage.backend.biz.jacoco;

import com.oppo.test.coverage.backend.util.SystemConfig;
import com.oppo.test.coverage.backend.util.http.HttpRequestUtil;
import org.jacoco.core.tools.ExecDumpClient;
import org.jacoco.core.tools.ExecFileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author 80264236
 */
@Service
public class ExecutionDataClient {

    @Resource
    SystemConfig systemConfig;

    private static final Logger logger = LoggerFactory.getLogger(ExecutionDataClient.class);

    public boolean getEcData(String ip, int port, File destFile, Integer testedEnv) throws Exception {
        switch (testedEnv) {
            case 2:
                port = getPort(ip, port);
                //生产环境
                if (port == -1) {
                    return false;
                }
                ip = systemConfig.getTransferBaseIp();
                break;
            case 3:
                port = getDevPort(ip, port);
                if (port == -1) {
                    return false;
                }
                ip = systemConfig.getTransferDevIp();
                break;
            default:
        }
        ExecDumpClient client = new ExecDumpClient();
        client.setDump(true);
        client.setReset(false);
        ExecFileLoader fileLoader = client.dump(ip, port);
        fileLoader.save(destFile, false);
        return true;
    }


    private int getPort(String address, int port) {
        String tempUrl = systemConfig.getTransferUrl() + "?address=" + address + "&port=" + port;
        return Integer.parseInt(HttpRequestUtil.getForObject(tempUrl, String.class, 3));
    }

    private int getDevPort(String address, int port) {
        String tempUrl = systemConfig.getTransferDevUrl() + "?address=" + address + "&port=" + port;
        return Integer.parseInt(HttpRequestUtil.getForObject(tempUrl, String.class, 3));
    }

}

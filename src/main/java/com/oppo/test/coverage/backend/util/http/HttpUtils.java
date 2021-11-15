package com.oppo.test.coverage.backend.util.http;

import com.alibaba.fastjson.JSON;
import com.oppo.test.coverage.backend.model.constant.ErrorEnum;
import com.oppo.test.coverage.backend.model.entity.Data;
import com.oppo.test.coverage.backend.model.request.ErrorMsg;
import com.oppo.test.coverage.backend.util.SystemConfig;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class HttpUtils {

    @Resource
    SystemConfig systemConfig;

    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    public static String sendGet(String url) {
        System.out.println("\nSending 'GET' request to URL : " + url);
        return HttpRequestUtil.getForObject(url, String.class, 3);
    }

    public void sendErrorMsg(Long taskId, ErrorEnum errorEnum) {
        if (!errorEnum.equals(ErrorEnum.JACOCO_EXEC_FAILED)){
            return;
        }
        ErrorMsg errorMsg = new ErrorMsg();
        errorMsg.setId(taskId);
        errorMsg.setMsg(errorEnum.getErrorMsg());
        HttpUtils.sendPostRequest(systemConfig.getSendErrorMsgUrl(), errorMsg);
    }

    public static Data sendPostRequest(String url, Object obj) {
        ResponseEntity<Data> response = null;

        Map<CharSequence, CharSequence> headersMap = new HashMap<>(1);
        headersMap.put("Content-type", MediaType.APPLICATION_JSON_VALUE);

        try {
            //执行HTTP请求，将返回的结构格式化
            response = HttpRequestUtil.postForObject(url, headersMap, JSON.toJSONBytes(obj), ResponseEntity.class, 3);
        } catch (Exception e) {
            logger.error("post request error : {}, {}", url, e.getMessage());
            e.printStackTrace();
        }

        if (response != null) {
            return response.getBody();
        }
        return null;
    }

    //2021/4/23 换掉这个文件下载

    /**
     * 下载文件
     *
     * @param url
     * @param filePath
     */
    public static void httpDownloadFile(String url,
                                        String filePath,
                                        Map<String, String> headMap) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpGet = new HttpGet(url);
            setGetHead(httpGet, headMap);
            CloseableHttpResponse response1 = httpclient.execute(httpGet);
            try {
                System.out.println(response1.getStatusLine());
                HttpEntity httpEntity = response1.getEntity();
                InputStream is = httpEntity.getContent();
                // 根据InputStream 下载文件
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int r;
                while ((r = is.read(buffer)) > 0) {
                    output.write(buffer, 0, r);
                }
                System.out.println("download finish");
                FileOutputStream fos = new FileOutputStream(filePath);
                output.writeTo(fos);
                output.flush();
                output.close();
                fos.close();
                EntityUtils.consume(httpEntity);
            } finally {
                response1.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置http的HEAD
     *
     * @param httpGet
     * @param headMap
     */
    private static void setGetHead(HttpGet httpGet, Map<String, String> headMap) {
        if (headMap != null && headMap.size() > 0) {
            Set<String> keySet = headMap.keySet();
            for (String key : keySet) {
                httpGet.addHeader(key, headMap.get(key));
            }
        }
    }

    public static void main(String[] args) {
        HttpClientUtil httpClientUtil = new HttpClientUtil();

        sendGet("http://atms-test.itest.wanyol.com/api/codeCoverage/execution/stop/timerTask?id=747&msg=" + URLEncoder.encode("获取测试环境覆盖率文件失败,请检查jacoco服务是否正确部署"));
    }

}

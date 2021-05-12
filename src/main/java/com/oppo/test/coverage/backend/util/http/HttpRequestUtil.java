package com.oppo.test.coverage.backend.util.http;

import com.alibaba.fastjson.JSONObject;
import esa.commons.netty.core.Buffers;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author 80264236
 * @date 2021/4/1 20:35
 */
public class HttpRequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestUtil.class);

    public static <T> T getForObject(String url, Class<T> tClass, int times) {
        T t = null;
        for (int i = 0; i < times; i++) {
            HttpResponse response;
            try {
                response = HttpClientUtil.getHttpClient().execute(HttpRequest.get(url).build()).get();
            } catch (InterruptedException | ExecutionException e) {
                logger.warn("get for object exception : {}, {}", url, e.getMessage());
                e.printStackTrace();
                continue;
            }
            if (response.body() != Buffers.EMPTY_BUFFER) {
                t = JSONObject.parseObject(response.body().string(StandardCharsets.UTF_8), tClass);
            }
            break;
        }
        return t;
    }

    public static <T> T postForObject(String url,
                                      Map<CharSequence, CharSequence> headers,
                                      byte[] body,
                                      Class<T> tClass,
                                      int times) {
        T t = null;
        for (int i = 0; i < times; i++) {
            HttpResponse response;
            try {
                HttpRequest request = HttpRequest.post(url).addHeaders(headers).body(body).build();
                response = HttpClientUtil.getHttpClient().execute(request).get();
            } catch (InterruptedException | ExecutionException e) {
                logger.warn("post for object exception : {}, {}", url, e.getMessage());
                e.printStackTrace();
                continue;
            }
            if (response.body() != Buffers.EMPTY_BUFFER) {
                t = JSONObject.parseObject(response.body().string(StandardCharsets.UTF_8), tClass);
            }
            break;
        }
        return t;
    }


    public static boolean downloadBuildVersionFile(String url, String filePath) {

        HttpResponse response;
        try {
            response = HttpClientUtil.getHttpClient().execute(HttpRequest.get(url).build()).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }

        try {
            File file = new File(filePath);
            FileOutputStream outputStream = new FileOutputStream(file);
            FileChannel channel = outputStream.getChannel();
            channel.write(response.body().getByteBuf().nioBuffer());
            channel.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        HttpClientUtil httpClientUtil = new HttpClientUtil();
        downloadBuildVersionFile("http://ocs-cn-south.oppoer.me/columbus-file-repo/columbus-repo-202104/id-mapping-admin-1.0.1-SNAPSHOT-20210427064202-20210427-14894471.zip","F:\\业务场景\\play33\\id-mapping-admin-1.0.1-SNAPSHOT-20210427064202-20210427-14894471.zip");
    }


}

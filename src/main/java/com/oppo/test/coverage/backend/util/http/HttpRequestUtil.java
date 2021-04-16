package com.oppo.test.coverage.backend.util.http;

import com.alibaba.fastjson.JSONObject;
import esa.commons.netty.core.Buffers;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author 80264236
 * @date 2021/4/1 20:35
 */
public class HttpRequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestUtil.class);

    public static <T> T getForObject(String url, Class<T> tClass , int times){
        T t = null;
        for (int i=0; i<times ; i++){
            HttpResponse response;
            try {
                response = HttpClientUtil.getHttpClient().execute(HttpRequest.get(url).build()).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                continue;
            }
            if (response.body() != Buffers.EMPTY_BUFFER){
                t = JSONObject.parseObject(response.body().toString(),tClass);
            }
            break;
        }
        return t;
    }

    public static <T> T postForObject(String url,
                                      Map<CharSequence,CharSequence> headers ,
                                      byte[] body,
                                      Class<T> tClass ,
                                      int times){
        T t = null;
        for (int i=0; i<times ; i++){
            HttpResponse response;
            try {
                HttpRequest request = HttpRequest.post(url).addHeaders(headers).body(body).build();
                response = HttpClientUtil.getHttpClient().execute(request).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                continue;
            }
            t = JSONObject.parseObject(response.body().toString(),tClass);
            break;
        }
        return t;
    }


}

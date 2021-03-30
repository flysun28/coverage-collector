package com.oppo.jacocoreport.coverage.utils;

import com.oppo.jacocoreport.coverage.entity.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * @author 80264236
 */
public class RestUtil {

    private static final Logger logger = LoggerFactory.getLogger(RestUtil.class);

    public static <T> T getForObject(RestTemplate restTemplate, String url, Class<T> c){
        int times = 2;
        T t = null;
        while (times-- > 0) {
            try {
                t = restTemplate.getForObject(url, c);
            } catch (Exception e) {
                logger.warn("getForObject 1 : {}, {}",url,e.getMessage());
                continue;
            }
            break;
        }

        return t;
    }

    public static ResponseEntity<Data> postForEntity(RestTemplate restTemplate, String url, HttpEntity<Object> obj) {
        int times = 3;
        ResponseEntity<Data> t = null;
        while (times-- > 0) {
            try {
                t = restTemplate.postForEntity(url, obj, Data.class);
            } catch (Exception e) {
                logger.warn("post for object 1：{}",e.getMessage());
                continue;
            }
            break;
        }
        return t;
    }

    public static <T> T postForEntity(RestTemplate restTemplate, String url, Object obj, Class<T> c, int times) {

        T t = null;
        while (times-- > 0) {
            try {
                t = restTemplate.postForObject(url, obj, c);
            } catch (Exception e) {
                logger.warn("post for object 2：{}",e.getMessage());
                continue;
            }
            break;
        }
        return t;
    }



}

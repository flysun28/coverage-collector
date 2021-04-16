/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.oppo.jacocoreport.record.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.oppo.jacocoreport.record.common.HttpRequestException;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OkHttpClientUtil {

    private static final OkHttpClient okHttpClient = new OkHttpClient();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    static {
        okHttpClient.newBuilder().connectionPool(new ConnectionPool()).connectTimeout(6, TimeUnit.SECONDS).build();
    }

    public static String post(String url, String json, Map<String,String> headersMap) throws IOException {
        Headers.Builder headersBuilder = new Headers.Builder();
        if (headersMap != null){
            for (String key : headersMap.keySet()){
                headersBuilder.add(key,headersBuilder.get(key));
            }
        }
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .headers(headersBuilder.build())
                .post(body)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()){
            return response.body().string();
        } else {
            String msg = String.format("Fail to request %s , code %d",url,response.code());
            throw new HttpRequestException(msg);
        }
    }

    public static void main(String[] args) throws Exception{
        String url = "http://apitrace-test.esa.wanyol.com/snake/api/ajax/request/trace?appKey=accurateTest&zoneCode=CN-S01-DGTEST01";
        Map<String,Object> body = new HashMap<>();
        body.put("traceId","6b6bc9a2468d42d0b34cacbabddf648c");
        body.put("startTime","1618553794000");
        List<Map<String,Object>> list = new ArrayList<>();
        list.add(body);
        System.out.println(JSONObject.toJSONString(list));
        String result = OkHttpClientUtil.post(url, JSONObject.toJSONString(list),null);
        System.out.println(result);
    }
}

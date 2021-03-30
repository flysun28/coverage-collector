package com.oppo.jacocoreport.coverage.utils;

import com.oppo.jacocoreport.coverage.entity.CoverageData;
import com.oppo.jacocoreport.coverage.entity.Data;
import com.oppo.jacocoreport.coverage.entity.ErrorMsg;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {
    private static final String USER_AGENT = "Mozilla/5.0";

    public static String sendGet(String url){
        return RestUtil.getForObject(new RestTemplate(),url,String.class);
    }

    public static String sendGet2(String url) {
        StringBuffer response = new StringBuffer();
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //默认值GET
            con.setRequestMethod("GET");

            //添加请求头
            con.setRequestProperty("User-Agent", USER_AGENT);

            System.out.println("\nSending 'GET' request to URL : " + url);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;


            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    public static void sendPost(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //添加请求头
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "sn=C02G8416&cn=&locale=&caller=&num=12345";
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int reponseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + reponseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());
    }

    public static void sendErrorMSG(Long taskID,String msg){
        ErrorMsg errorMsg = new ErrorMsg();
        errorMsg.setId(taskID);
        errorMsg.setMsg(msg);
        HttpUtils.sendPostRequest(Config.SEND_ERRORMESSAGE_URL,errorMsg);
    }

    public static Data sendPostRequest(String url, Object obj){
        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Data> response = null;
        //以表单的方式提交
        headers.setContentType(MediaType.APPLICATION_JSON);
        //将请求头部和参数合成一个请求
        HttpEntity<Object> requestEntity = new HttpEntity<>(obj,headers);
        try {
            //执行HTTP请求，将返回的结构格式化
            response = RestUtil.postForEntity(client,url,requestEntity);
        }catch (Exception e){
            e.printStackTrace();
        }

        if (response != null){
            return response.getBody();
        }
        return null;
    }

    /**
     * 发送get请求
     * @param url
     * @return
     */
    public static Data sendGetRequest(String url){
        RestTemplate client = new RestTemplate();
        ResponseEntity<Data> response = null;
        try{
            response = client.getForEntity(url,Data.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return response.getBody();
    }

    public static void main(String[] args){
        String requstUrl = Config.SEND_COVERAGE_URL;
        CoverageData coverageData = new CoverageData();
        Data data = HttpUtils.sendPostRequest(requstUrl,coverageData);
        System.out.println(data.getCode());
    }
}

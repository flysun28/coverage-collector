package com.oppo.test.coverage.backend.biz;

import com.alibaba.fastjson.JSON;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.oppo.test.coverage.backend.model.request.CompilesFileRequest;
import com.oppo.test.coverage.backend.model.request.EcUploadRequest;
import com.oppo.test.coverage.backend.model.response.CortResponse;
import com.oppo.test.coverage.backend.util.Md5Util;
import com.oppo.test.coverage.backend.util.file.OcsUtil;
import com.oppo.test.coverage.backend.util.http.HttpRequestUtil;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @author 80264236
 * @date 2021/5/21 14:44
 */
@Service
public class CortBiz {

    private static final Logger logger = LoggerFactory.getLogger(CortBiz.class);

    @Value("${cort.url.base}")
    private String baseUrl;

    @Value("${cort.path.sceneId.get}")
    private String getSceneIdPath;

    @Value("${cort.path.preSign.get}")
    private String getPreSignPath;

    @Value("${cort.path.serverTime.get}")
    private String getServerTimePath;

    @Value("${cort.path.compilesFile.post}")
    private String postCompilesFilePath;

    @Value("${cort.path.ec.post}")
    private String postEcFileUpload;

    @Value("${cort.appId}")
    private String appId;

    @Value("${cort.secret}")
    private String secret;

    @Value("${ocs.credential.access-key-id}")
    private String accessKeyId;

    @Value("${ocs.credential.secret-key-id}")
    private String accessKeySecret;

    @Value("${ocs.client.end-point}")
    private String endPoint;

    @Value("${ocs.client.region}")
    private String region;

    @Value("${ocs.config.binary-ec-bucket-name}")
    private String ecBucketName;

    @Value("${ocs.config.compiled-file-bucket-name}")
    private String compiledBucketName;

    /**
     * 获取场景id
     *
     * @param sceneType : 1-自动,2-用例录制,3-版本测试,4-实时染色,5-服务端任务
     * @return : 场景id,获取失败返回null
     */
    public Integer getSceneId(Integer sceneType) {
        Long timeStamp = getServerTimestamp();
        if (timeStamp == null) {
            logger.error("场景id获取时间戳失败");
            return null;
        }
        String url = baseUrl + getSceneIdPath + "/" + sceneType + "?" + urlCombine(getSceneIdPath, timeStamp);
        CortResponse response = HttpRequestUtil.getForObject(url, CortResponse.class, 1);
        if (response == null || response.getErrno() == null || response.getErrno() != 0) {
            logger.error("获取cort场景id失败 : {}", response == null ? sceneType : response);
            return null;
        }
        return (Integer) response.getData();
    }

    /**
     * 获取预签名链接
     *
     * @param type        : 文件类型, "ec"
     * @param fileKey     : 文件名,"xxx.ec"
     * @param contentType : 默认application/octet-stream
     * @return : 预签名链接,未获取到返回null
     */
    public String getPreSignUrl(String type, String fileKey, ContentType contentType) {
        Long timeStamp = getServerTimestamp();
        if (timeStamp == null) {
            logger.error("预签名获取时间戳失败");
            return null;
        }
        String url = baseUrl + getPreSignPath + "?"
                + urlCombine(getPreSignPath, timeStamp)
                + "&type=" + type
                + "&fileKey=" + fileKey
                + "&contentType=" + contentType.getMimeType();
        CortResponse response = HttpRequestUtil.getForObject(url, CortResponse.class, 1);
        if (response == null || response.getErrno() == null || response.getErrno() != 0) {
            logger.error("获取预签名链接失败 : {}", response);
            return null;
        }
        return (String) response.getData();
    }

    /**
     * 获取预签名的链接,并上传文件:ec文件,jacocoAll.exec重命名; zip:classes的压缩包
     *
     * @param type : ec,zip
     * @param file : 上传的文件
     */
    public boolean uploadFileToOcs(File file, String type) {
        String url = getPreSignUrl(type, file.getName(), ContentType.APPLICATION_OCTET_STREAM);
        if (url == null) {
            return false;
        }
        try {
            OcsUtil.preSignObjectPut(url, file);
        } catch (IOException e) {
            logger.error("OCS文件上传失败:{}", file.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 上传jacocoAll文件到cort的ocs
     */
    public boolean uploadEcFile(File jacocoAllFile) {
        boolean result = putObjectToOcs(ecBucketName, jacocoAllFile);
        logger.info("上传jacoco到ocs : {}, {}", jacocoAllFile.toString(), result);
        return result;
    }

    public boolean uploadCompilesFile(File compilesFile) {
        boolean result = putObjectToOcs(compiledBucketName, compilesFile);
        logger.info("上传compiles到ocs : {},  {}", compilesFile.toString(), result);
        return result;
    }


    /**
     * 获取当前服务端时间戳(用于SDK与服务端的签名认证)
     *
     * @return : 时间戳,获取失败返回null
     */
    public Long getServerTimestamp() {
        CortResponse response = HttpRequestUtil.getForObject(baseUrl + getServerTimePath, CortResponse.class, 1);
        if (response == null || response.getErrno() == null || response.getErrno() != 0) {
            logger.error("获取服务端时间戳失败 : {}", response);
            return null;
        }
        return (Long) response.getData();
    }

    /**
     * 上传compiles file
     *
     * @return : 成功true , 失败false
     */
    public boolean postCompilesFile(CompilesFileRequest request) {
        Long timeStamp = getServerTimestamp();
        if (timeStamp == null) {
            logger.error("上报编译产物获取时间戳失败");
            return false;
        }
        String url = baseUrl + postCompilesFilePath + "?" + urlCombine(postCompilesFilePath, timeStamp);
        Map<CharSequence, CharSequence> headersMap = new HashMap<>(1);
        headersMap.put("Content-type", MediaType.APPLICATION_JSON_VALUE);
        CortResponse response = HttpRequestUtil.postForObject(url, headersMap, JSON.toJSONBytes(request), CortResponse.class, 1);
        if (response == null || response.getErrno() == null || response.getErrno() != 0) {
            logger.error("上报编译产物失败 : {} , {}", request, response);
            return false;
        }
        return true;
    }

    /**
     * 上报ec文件
     *
     * @return : 成功true , 失败false
     */
    CortResponse postEcFile(EcUploadRequest request) {
        Long timeStamp = getServerTimestamp();
        if (timeStamp == null) {
            logger.error("上报Ec获取时间戳失败");
            return null;
        }
        String url = baseUrl + postEcFileUpload + "?" + urlCombine(postEcFileUpload, timeStamp);
        Map<CharSequence, CharSequence> headersMap = new HashMap<>(1);
        headersMap.put("Content-type", MediaType.APPLICATION_JSON_VALUE);
        return HttpRequestUtil.postForObject(url, headersMap, JSON.toJSONBytes(request), CortResponse.class, 1);
    }


    /**
     * 上传Ec到cort,遇到无compiledFile进行重试
     */
    public boolean postEcFileToCort(EcUploadRequest request) {
        for (int i = 0; i < 3; i++) {
            CortResponse response = postEcFile(request);
            //调用成功,返回
            if (response != null && response.getErrno() != null && response.getErrno() == 0) {
                return true;
            }
            //complied未完成,等待后重试
            if (response != null && response.getErrno() != null && response.getErrno().equals(20010)) {
                logger.warn("无制品,重试等待ing : {}", JSON.toJSON(request));
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            logger.error("上报ec信息失败 : {}, {}", request, response);
            return false;
        }
        logger.error("上报ec信息失败 : {}", JSON.toJSON(request));
        return false;
    }


    /**
     * url拼接,获取到带签名的所有参数集合
     */
    private String urlCombine(String apiPath, Long timeStamp) {
        Map<String, Object> paramsMap = new HashMap<>(4);
        String nonce = UUID.randomUUID().toString();
        paramsMap.put("appId", appId);
        paramsMap.put("nonce", nonce);
        paramsMap.put("timeStamp", timeStamp);
        String sign = getSign(apiPath, paramsMap);
        paramsMap.put("sign", sign);
        return paramsCombine(paramsMap, "&");
    }


    /**
     * api_path : /xxx/xxx/xx/xxx/xx
     * secret : 密钥
     * 传入map带有参数:param_name_value_pairs : appId + nonce + timeStamp
     */
    private String getSign(String apiPath, Map<String, Object> paramsMap) {
        String paramNameValuePairs = paramsCombine(paramsMap, "\\n");
        String signString = apiPath + "\\n" +
                paramNameValuePairs + "\\n" +
                secret;
        return Md5Util.getMd5(signString);
    }

    /**
     * 将传入参数按字母顺序拼接
     */
    private static String paramsCombine(Map<String, Object> paramsMap, String combineSign) {
        List<String> paramKeyList = new ArrayList<>(paramsMap.keySet());
        // 对参数数组进行排序
        Collections.sort(paramKeyList);
        StringBuilder paramStrBuilder = new StringBuilder();
        paramKeyList.forEach(paramKey -> {
            paramStrBuilder.append(paramKey).append("=").append(paramsMap.get(paramKey)).append(combineSign);
        });
        //删除多添加的一个换行
        paramStrBuilder.delete(paramStrBuilder.lastIndexOf(combineSign), paramStrBuilder.length());
        return paramStrBuilder.toString();
    }


    //---------------------------------------OCS --------------------------------------


    public boolean putObjectToOcs(String bucketName, File file) {
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, accessKeySecret);
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
        ClientConfiguration clientConfiguration = new ClientConfiguration()
                .withProtocol(Protocol.HTTP);
        AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider)
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, region))
                .build();

        PutObjectRequest request = new PutObjectRequest(bucketName, file.getName(), file);
        return amazonS3.putObject(request) != null;
    }

    public String getPreSignObjectUrl(String fileKey){
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, accessKeySecret);
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
        ClientConfiguration clientConfiguration = new ClientConfiguration()
                .withProtocol(Protocol.HTTP);
        AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider)
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, region))
                .build();

        Date expiration = new Date();
        long expTimeMills = expiration.getTime();
        expTimeMills += 1000*60*60;
        expiration.setTime(expTimeMills);
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(compiledBucketName,fileKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);
        URL url = s3.generatePresignedUrl(generatePresignedUrlRequest);
        logger.info("The pre-signed URL is： "+url.toString());
        return url.toString();
    }


    public static void main(String[] args) {
        File file = new File("F:\\业务场景\\play34\\app.log");
        System.out.println(file.getName());
        System.out.println(file.toString());
    }


}

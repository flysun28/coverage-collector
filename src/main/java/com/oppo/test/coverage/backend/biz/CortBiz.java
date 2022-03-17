package com.oppo.test.coverage.backend.biz;

import com.alibaba.fastjson.JSON;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.oppo.basic.heracles.client.core.spring.annotation.HeraclesDynamicConfig;
import com.oppo.test.coverage.backend.model.request.EcUploadRequest;
import com.oppo.test.coverage.backend.model.response.CortResponse;
import com.oppo.test.coverage.backend.util.Md5Util;
import com.oppo.test.coverage.backend.util.http.HttpRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

/**
 * @author 80264236
 * @date 2021/5/21 14:44
 */
@Service
public class CortBiz {

    private static final Logger logger = LoggerFactory.getLogger(CortBiz.class);

    @HeraclesDynamicConfig(key = "cort.url.receiver", fileName = "application.yml")
    private String receiverUrl;

    @HeraclesDynamicConfig(key = "cort.path.sceneId.get", fileName = "application.yml")
    private String getSceneIdPath;

    @HeraclesDynamicConfig(key = "cort.path.preSign.get", fileName = "application.yml")
    private String getPreSignPath;

    @HeraclesDynamicConfig(key = "cort.path.serverTime.get", fileName = "application.yml")
    private String getServerTimePath;

    @HeraclesDynamicConfig(key = "cort.path.compilesFile.post", fileName = "application.yml")
    private String postCompilesFilePath;

    @HeraclesDynamicConfig(key = "cort.path.ec.post", fileName = "application.yml")
    private String postEcFileUpload;

    @HeraclesDynamicConfig(key = "cort.path.json.post", fileName = "application.yml")
    private String postJsonFileUpload;

    @HeraclesDynamicConfig(key = "cort.appId", fileName = "application.yml")
    private String appId;

    @HeraclesDynamicConfig(key = "cort.secret", fileName = "application.yml")
    private String secret;

    @HeraclesDynamicConfig(key = "ocs.credential.access-key-id", fileName = "application.yml")
    private String accessKeyId;

    @HeraclesDynamicConfig(key = "ocs.credential.secret-key-id", fileName = "application.yml")
    private String accessKeySecret;

    @HeraclesDynamicConfig(key = "ocs.client.end-point", fileName = "application.yml")
    private String endPoint;

    @HeraclesDynamicConfig(key = "ocs.client.region", fileName = "application.yml")
    private String region;

    @HeraclesDynamicConfig(key = "ocs.config.binary-ec-bucket-name", fileName = "application.yml")
    private String ecBucketName;

    @HeraclesDynamicConfig(key = "ocs.config.compiled-file-bucket-name", fileName = "application.yml")
    private String compiledBucketName;

    /**
     * 上传jacocoAll文件到cort的ocs
     */
    boolean uploadEcFile(File jacocoAllFile) {
        boolean result = putObjectToOcs(ecBucketName, jacocoAllFile);
        logger.info("上传jacoco到ocs : {}, {}", jacocoAllFile.toString(), result);
        return result;
    }

    /**
     * 获取当前服务端时间戳(用于SDK与服务端的签名认证)
     *
     * @return : 时间戳,获取失败返回null
     */
    Long getServerTimestamp() {
        CortResponse response = HttpRequestUtil.getForObject(receiverUrl + getServerTimePath, CortResponse.class, 1);
        if (response == null || response.getErrno() == null || response.getErrno() != 0) {
            logger.error("获取服务端时间戳失败 : {}", response);
            return null;
        }
        return (Long) response.getData();
    }

    /**
     * 上报ec文件
     *
     * @return : 成功true , 失败false
     */
    CortResponse postEcFile(EcUploadRequest request, int ecType) {
        Long timeStamp = getServerTimestamp();
        if (timeStamp == null) {
            logger.error("上报Ec获取时间戳失败");
            return null;
        }
        String path = ecType == 1 ? postEcFileUpload : postJsonFileUpload;
        String url = receiverUrl + path + "?" + urlCombine(postEcFileUpload, timeStamp);
        Map<CharSequence, CharSequence> headersMap = new HashMap<>(1);
        headersMap.put("Content-type", MediaType.APPLICATION_JSON_VALUE);
        return HttpRequestUtil.postForObject(url, headersMap, JSON.toJSONBytes(request), CortResponse.class, 1);
    }


    /**
     * 上传Ec到cort,遇到无compiledFile进行重试
     */
    boolean postEcFileToCort(EcUploadRequest request, int ecType) {
        for (int i = 0; i < 6; i++) {
            CortResponse response = postEcFile(request, ecType);
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
        paramKeyList.forEach(paramKey -> paramStrBuilder.append(paramKey).append("=").append(paramsMap.get(paramKey)).append(combineSign));
        //删除多添加的一个换行
        paramStrBuilder.delete(paramStrBuilder.lastIndexOf(combineSign), paramStrBuilder.length());
        return paramStrBuilder.toString();
    }


    //---------------------------------------OCS --------------------------------------


    private boolean putObjectToOcs(String bucketName, File file) {
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

}

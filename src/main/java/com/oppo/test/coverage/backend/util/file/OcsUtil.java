package com.oppo.test.coverage.backend.util.file;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author 80264236
 * @date 2021/2/18 15:08
 * <p>
 * 上传到OCS的文件目录
 * <p>
 * 任务id目录
 * /home/service/app/coverageBackend/${unique}/taskID/${taskId}/ xxxreport or classes or downloadzip
 * 其中classes与downloadZip内是class文件,需压缩上传、下载解压(打包减少请求次数)
 * 各report目录直接上传
 * <p>
 * 项目路径目录
 * /home/service/app/coverageBackend/${unique}/projectCovPath/${projectName}/ versionId or branch
 * 其中versionId仅两个exec文件,其余为report报告
 * <p>
 * 处理/task/下的classes 和 downloadzip 目录文件
 */
public class OcsUtil {

    private static final Logger logger = LoggerFactory.getLogger(OcsUtil.class);

    private static String accessKeyId = "IYKHOpfZ915vqTreh_nfJWG8f0r9vseZn2S7JrL9";
    private static String accessKeySecret = "hk2LKzt9D1hSp9gA0Q6QVeiOB7W65v9VgfFsaHGc";
    private static String endPoint = "http://s3v2.ocs-cn-south.wanyol.com";
    private static String region = "cn-south-1";
    private static String bucketName = "code-coverage";

    /**
     * 上传文件
     *
     * @param key  : /path/fileName
     * @param file : 文件
     */
    public static boolean upload(AmazonS3 s3, String key, File file, String contentType) {
        return getS3Result(s3, 1, key, null, file, null, contentType) != null;
    }

    /**
     * 删除文件
     *
     * @param key : /path/fileName
     */
    public static void delete(String key) {
        AmazonS3 s3 = getAmazonS3();
        //Delete objects from a bucket
        getS3Result(s3, 2, key, null, null, null, null);
    }

    /**
     * 下载文件
     *
     * @param key      : /path/fileName
     * @param filePath : /path/fileName
     */
    public static void download(AmazonS3 s3, String key, String filePath) {
        //get object from a bucket
        getS3Result(s3, 3, key, filePath, null, null, null);
    }

    /**
     * 查看文件
     *
     * @param prefix : 指定目录
     * @return : 文件列表
     */
    public static List<String> query(String prefix) {
        AmazonS3 s3 = getAmazonS3();

        ListObjectsV2Request req = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(prefix)
                .withMaxKeys(1000);
        ListObjectsV2Result res;
        List<String> result = new LinkedList<>();

        do {
            res = (ListObjectsV2Result) getS3Result(s3, 4, null, null, null, req, null);

            if (res == null) {
                logger.error("query res is null !");
                return result;
            }

            for (S3ObjectSummary objectSummary : res.getObjectSummaries()) {
                result.add(objectSummary.getKey());
            }
            // If there are more than maxKeys keys in the bucket, get a continuation token
            // and list the next objects.
            String token = res.getNextContinuationToken();
            req.setContinuationToken(token);
        } while (res.isTruncated());

        return result;
    }


    private static Object getS3Result(AmazonS3 s3, int type,
                                      String key, String filePath,
                                      File file,
                                      ListObjectsV2Request req,
                                      String contentType) {
        try {
            switch (type) {
                case 1:
                    PutObjectRequest request = new PutObjectRequest(bucketName, key, file);
                    if (!StringUtils.isEmpty(contentType)) {
                        ObjectMetadata metadata = new ObjectMetadata();
                        metadata.setContentType("text/html");
                        request.setMetadata(metadata);
                    }
                    return s3.putObject(new PutObjectRequest(bucketName, key, file));
                case 2:
                    //特殊:delete 没有返回值
                    s3.deleteObject(bucketName, key);
                    return null;
                case 3:
                    return s3.getObject(new GetObjectRequest(bucketName, key), new File(filePath));
                case 4:
                    return s3.listObjectsV2(req);
                default:
                    return null;
            }
        } catch (AmazonS3Exception exception) {
            int statusCode = (exception.getStatusCode());
            if (statusCode / 100 == 4) {
                logger.error("type : {} , an error occurred in client : {}", type, exception.getErrorResponseXml());
            } else if (statusCode / 100 == 5) {
                logger.error("type : {} , an error occurred in ocs : {}", type, exception.getErrorResponseXml());
            }
        } catch (SdkClientException exception) {
            logger.error("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            logger.error("Type is {} , Error Message: {}", type, exception.getMessage());
        }
        return null;
    }


    public static AmazonS3 getAmazonS3() {
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, accessKeySecret);
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
        ClientConfiguration clientConfiguration = new ClientConfiguration()
                .withProtocol(Protocol.HTTP);
        return AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider)
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, region))
                .build();
    }

    public static void main(String[] args) {

        List<String> fileList = query("test/test");

        System.out.println(fileList);
        AmazonS3 s3 = getAmazonS3();

        for (String fileKey : fileList) {

            String[] tempString = fileKey.split("/");
            String fileName = tempString[tempString.length - 1];

            download(s3, fileKey, "F:\\业务场景\\play27\\" + fileName);
//            delete(fileKey);
        }
    }


}

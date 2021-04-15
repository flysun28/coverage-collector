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
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class OcsDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(OcsDemo.class);

    /**
     * 下面四项属性按照自己ocs信息进行填写
     */
    private final String accessKeyId = "AK";
    private final String accessKeySecret = "SK";
    private final String endPoint = "EndPoint";
    private final String region = "Region";

    private final AWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, accessKeySecret);
    private final AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
    private final ClientConfiguration clientConfiguration = new ClientConfiguration()
            .withProtocol(Protocol.HTTP)
            .withSignerOverride("S3SignerType");
    private final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider)
            .withPathStyleAccessEnabled(true)
            .withClientConfiguration(clientConfiguration)
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, region))
            .build();

    public PutObjectResult putObject(File file, String bucketName, String key) {
        try {
            return s3.putObject(new PutObjectRequest(bucketName, key, file));
        } catch (AmazonS3Exception exception) {
            int statusCode = (exception.getStatusCode());
            if (statusCode / 100 == 4) {
                LOGGER.error("an error occurred in client");
                LOGGER.error(exception.getErrorResponseXml());
            } else if (statusCode / 100 == 5) {
                LOGGER.error("an error occurred in ocs");
                LOGGER.error(exception.getErrorResponseXml());
            }
        } catch (SdkClientException exception) {
            LOGGER.error("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            LOGGER.error("Error Message: " + exception.getMessage());
        }
        return null;
    }

    public ObjectMetadata getObject(String bucketName, String key, String filePath) {
        try {
            return s3.getObject(new GetObjectRequest(bucketName, key), new File(filePath));
        } catch (AmazonS3Exception exception) {
            int statusCode = (exception.getStatusCode());
            if (statusCode / 100 == 4) {
                LOGGER.error("an error occurred in client");
                LOGGER.error(exception.getErrorResponseXml());
            } else if (statusCode / 100 == 5) {
                LOGGER.error("an error occurred in ocs");
                LOGGER.error(exception.getErrorResponseXml());
            }
        } catch (SdkClientException exception) {
            LOGGER.error("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            LOGGER.error("Error Message: " + exception.getMessage());
        }
        return null;
    }

    public void deleteObject(String bucketName, String key) {
        try {
            s3.deleteObject(bucketName, key);
        } catch (AmazonS3Exception exception) {
            int statusCode = (exception.getStatusCode());
            if (statusCode/100 == 4){
                LOGGER.error("an error occurred in client");
                LOGGER.error(exception.getErrorResponseXml());
            } else if (statusCode/100 == 5) {
                LOGGER.error("an error occurred in ocs");
                LOGGER.error(exception.getErrorResponseXml());
            }
        } catch (SdkClientException exception) {
            LOGGER.error("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            LOGGER.error("Error Message: " + exception.getMessage());
        }
    }
}



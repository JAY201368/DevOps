package com.example.tomatomall.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
//import java.util.UUID;

@Component
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties("aliyun.oss")
public class OssUtil {
    //    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    //    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    //    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    //    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    /**
     * 实现上传图片到OSS
     */
    public String upload(String objectName, InputStream inputStream) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
        try {
            ossClient.putObject(putObjectRequest);
        }finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return ossClient.generatePresignedUrl(bucketName, objectName, new Date()).toString().split("\\?Expires")[0];
    }
}
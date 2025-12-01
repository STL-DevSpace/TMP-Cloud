package org.dromara.data.utils;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.CannedAccessControlList;  // 添加这个导入
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;  // 添加这个导入
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Component
public class CosUtils {
    @Value("${oss.accessKey}")
    private String secretId;
    @Value("${oss.secretKey}")
    private String secretKey;
    @Value("${oss.region}")
    private String region;
    @Value("${oss.bucketName}")
    private String bucket;
    @Value("${oss.prefix}")
    private String prefix;

    private COSClient getClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(cred, clientConfig);
    }

    // 上传文件
    public String uploadFile(InputStream inputStream, String fileName, long contentLength) {
        COSClient cosClient = getClient();
        try {
            String key = prefix + fileName;

            ObjectMetadata metadata = new ObjectMetadata();
            // 使用传入的准确大小
            metadata.setContentLength(contentLength);

            PutObjectRequest request = new PutObjectRequest(bucket, key, inputStream, metadata);
            request.setCannedAcl(CannedAccessControlList.PublicRead);

            cosClient.putObject(request);
            return key;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败", e);
        } finally {
            cosClient.shutdown();
        }
    }

    // 生成临时访问链接（私有读时用）
    public String getPresignedUrl(String key, long expireSeconds) {
        COSClient cosClient = getClient();
        try {
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000L);
            GeneratePresignedUrlRequest request =
                new GeneratePresignedUrlRequest(bucket, key);
            URL url = cosClient.generatePresignedUrl(request.withExpiration(expiration));
            return url.toString();
        } finally {
            cosClient.shutdown();
        }
    }

    /**
     * 获取对象的公有读URL
     * @param objectKey 对象键
     * @return 公有访问URL
     */
    public String getPublicUrl(String objectKey) {
        // 腾讯云COS公有读URL格式：
        // https://<BucketName-APPID>.cos.<Region>.myqcloud.com/<ObjectKey>
        return String.format("https://%s.cos.%s.myqcloud.com/%s",
            bucket,  // 你的桶名称-APPID
            region,  // 你的区域，如：ap-guangzhou
            objectKey
        );
    }
}

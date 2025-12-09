package org.dromara.data.utils;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
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

    // 分块上传的阈值，大于此值使用分块上传（默认100MB）
    private static final long MULTIPART_UPLOAD_THRESHOLD = 100 * 1024 * 1024L;
    // 每个分块的大小（默认50MB）
    private static final long PART_SIZE = 50 * 1024 * 1024L;

    private COSClient getClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(cred, clientConfig);
    }

    /**
     * 智能上传：根据文件大小自动选择普通上传或分块上传
     */
    public String uploadFile(InputStream inputStream, String fileName, long contentLength) {
        if (contentLength > MULTIPART_UPLOAD_THRESHOLD) {
            log.info("文件大小 {} 字节，使用分块上传", contentLength);
            return uploadFileMultipart(inputStream, fileName, contentLength);
        } else {
            log.info("文件大小 {} 字节，使用普通上传", contentLength);
            return uploadFileNormal(inputStream, fileName, contentLength);
        }
    }

    /**
     * 普通上传（小文件）
     */
    private String uploadFileNormal(InputStream inputStream, String fileName, long contentLength) {
        COSClient cosClient = getClient();
        try {
            String key = prefix + fileName;

            ObjectMetadata metadata = new ObjectMetadata();
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

    /**
     * 分块上传（大文件）
     */
    private String uploadFileMultipart(InputStream inputStream, String fileName, long contentLength) {
        COSClient cosClient = getClient();
        String key = prefix + fileName;
        String uploadId = null;

        try {
            // 1. 初始化分块上传
            InitiateMultipartUploadRequest initRequest =
                new InitiateMultipartUploadRequest(bucket, key);
            initRequest.setCannedACL(CannedAccessControlList.PublicRead);

            InitiateMultipartUploadResult initResponse = cosClient.initiateMultipartUpload(initRequest);
            uploadId = initResponse.getUploadId();
            log.info("初始化分块上传，UploadId: {}", uploadId);

            // 2. 上传分块
            List<PartETag> partETags = new ArrayList<>();
            long uploadedBytes = 0;
            int partNumber = 1;
            byte[] buffer = new byte[(int) PART_SIZE];

            while (uploadedBytes < contentLength) {
                // 计算当前分块大小
                long currentPartSize = Math.min(PART_SIZE, contentLength - uploadedBytes);

                // 读取分块数据
                int bytesRead = 0;
                int totalRead = 0;
                while (totalRead < currentPartSize &&
                    (bytesRead = inputStream.read(buffer, totalRead,
                        (int)(currentPartSize - totalRead))) != -1) {
                    totalRead += bytesRead;
                }

                if (totalRead == 0) {
                    break;
                }

                // 创建分块输入流
                java.io.ByteArrayInputStream partInputStream =
                    new java.io.ByteArrayInputStream(buffer, 0, totalRead);

                // 上传分块
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucket);
                uploadPartRequest.setKey(key);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setPartNumber(partNumber);
                uploadPartRequest.setInputStream(partInputStream);
                uploadPartRequest.setPartSize(totalRead);

                UploadPartResult uploadPartResult = cosClient.uploadPart(uploadPartRequest);
                partETags.add(uploadPartResult.getPartETag());

                uploadedBytes += totalRead;
                log.info("上传分块 {}/{}，已上传 {} 字节 / {} 字节 ({} %)",
                    partNumber,
                    (contentLength + PART_SIZE - 1) / PART_SIZE,
                    uploadedBytes,
                    contentLength,
                    String.format("%.2f", uploadedBytes * 100.0 / contentLength));

                partNumber++;
            }

            // 3. 完成分块上传
            CompleteMultipartUploadRequest completeRequest =
                new CompleteMultipartUploadRequest(bucket, key, uploadId, partETags);
            cosClient.completeMultipartUpload(completeRequest);

            log.info("分块上传完成，文件: {}", key);
            return key;

        } catch (Exception e) {
            // 上传失败，取消分块上传
            if (uploadId != null) {
                try {
                    AbortMultipartUploadRequest abortRequest =
                        new AbortMultipartUploadRequest(bucket, key, uploadId);
                    cosClient.abortMultipartUpload(abortRequest);
                    log.warn("分块上传失败，已取消上传任务，UploadId: {}", uploadId);
                } catch (Exception abortException) {
                    log.error("取消分块上传失败", abortException);
                }
            }
            throw new RuntimeException("分块上传失败", e);
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                log.error("关闭输入流失败", e);
            }
            cosClient.shutdown();
        }
    }

    /**
     * 生成临时访问链接（私有读时用）
     */
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
     */
    public String getPublicUrl(String objectKey) {
        return String.format("https://%s.cos.%s.myqcloud.com/%s",
            bucket,
            region,
            objectKey
        );
    }
    /**
     * 支持边读 InputStream 边分块上传 COS（带进度）
     */
    public String uploadStreamWithProgress(
        InputStream input,
        String fileName,
        long totalSize,
        java.util.function.LongConsumer progressCallback
    ) {
        COSClient cosClient = getClient();

        String key = prefix + fileName;
        String uploadId = null;

        try {
            // 1. 初始化分块上传
            InitiateMultipartUploadRequest initRequest =
                new InitiateMultipartUploadRequest(bucket, key);
            initRequest.setCannedACL(CannedAccessControlList.PublicRead);

            InitiateMultipartUploadResult initResponse =
                cosClient.initiateMultipartUpload(initRequest);
            uploadId = initResponse.getUploadId();
            log.info("初始化分块上传，UploadId: {}", uploadId);

            List<PartETag> partETags = new ArrayList<>();

            byte[] buffer = new byte[(int) PART_SIZE]; // 使用你配置的50MB分片
            long uploadedBytes = 0;
            int partNumber = 1;

            while (true) {

                // 从 inputStream 读一片到 buffer
                int bytesRead = input.read(buffer);
                if (bytesRead == -1) break;   // EOF

                // 2. 上传该分片
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucket);
                uploadPartRequest.setKey(key);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setPartNumber(partNumber);
                uploadPartRequest.setInputStream(
                    new java.io.ByteArrayInputStream(buffer, 0, bytesRead)
                );
                uploadPartRequest.setPartSize(bytesRead);

                UploadPartResult uploadPartResult =
                    cosClient.uploadPart(uploadPartRequest);

                partETags.add(uploadPartResult.getPartETag());

                uploadedBytes += bytesRead;

                // 进度回调
                if (progressCallback != null && totalSize > 0) {
                    int percent = (int) (uploadedBytes * 100.0 / totalSize);
                    progressCallback.accept(Math.min(percent, 100));
                }

                log.info("上传分片 {} 成功，已上传 {} / {} ({}%)",
                    partNumber,
                    uploadedBytes,
                    totalSize,
                    String.format("%.2f", uploadedBytes * 100.0 / totalSize)
                );

                partNumber++;
            }

            // 3. 完成分片上传
            CompleteMultipartUploadRequest completeRequest =
                new CompleteMultipartUploadRequest(bucket, key, uploadId, partETags);
            cosClient.completeMultipartUpload(completeRequest);

            log.info("COS 分片上传完成：{}", key);
            return key;

        } catch (Exception e) {
            log.error("COS 分片上传失败", e);

            // 上传失败要 Abort
            if (uploadId != null) {
                try {
                    cosClient.abortMultipartUpload(
                        new AbortMultipartUploadRequest(bucket, key, uploadId)
                    );
                } catch (Exception ex) {
                    log.error("Abort 分片失败", ex);
                }
            }

            throw new RuntimeException("COS 分片上传失败", e);

        } finally {
            try {
                input.close();
            } catch (Exception ignore) {}
            cosClient.shutdown();
        }
    }


}

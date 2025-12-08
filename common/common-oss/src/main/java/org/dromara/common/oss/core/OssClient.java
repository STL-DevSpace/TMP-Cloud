package org.dromara.common.oss.core;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.file.FileUtils;
import org.dromara.common.oss.constant.OssConstant;
import org.dromara.common.oss.entity.UploadResult;
import org.dromara.common.oss.enums.AccessPolicyType;
import org.dromara.common.oss.exception.OssException;
import org.dromara.common.oss.properties.OssProperties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.*;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.*;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.dromara.common.oss.core.IMultipartUploader;
/**
 * S3 存储协议 所有兼容S3协议的云厂商均支持
 * 阿里云 腾讯云 七牛云 minio
 *
 * @author AprilWind
 */
@Slf4j
public class OssClient implements IMultipartUploader{

    /**
     * 服务商
     */
    private final String configKey;

    /**
     * 配置属性
     */
    private final OssProperties properties;

    /**
     * Amazon S3 异步客户端
     */
    private final S3AsyncClient client;

    /**
     * 用于管理 S3 数据传输的高级工具
     */
    private final S3TransferManager transferManager;

    /**
     * AWS S3 预签名 URL 的生成器
     */
    private final S3Presigner presigner;

    /**
     * 构造方法
     *
     * @param configKey     配置键
     * @param ossProperties Oss配置属性
     */
    public OssClient(String configKey, OssProperties ossProperties) {
        this.configKey = configKey;
        this.properties = ossProperties;
        try {
            // 创建 AWS 认证信息
            StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey()));

            // MinIO 使用 HTTPS 限制使用域名访问，站点填域名。需要启用路径样式访问
            boolean isStyle = !StringUtils.containsAny(properties.getEndpoint(), OssConstant.CLOUD_SERVICE);

            // 创建AWS基于 Netty 的 S3 客户端
            this.client = S3AsyncClient.builder()
                .credentialsProvider(credentialsProvider)
                .endpointOverride(URI.create(getEndpoint()))
                .region(of())
                .forcePathStyle(isStyle)
                .httpClient(NettyNioAsyncHttpClient.builder()
                    .connectionTimeout(Duration.ofSeconds(60)).build())
                .build();

            //AWS基于 CRT 的 S3 AsyncClient 实例用作 S3 传输管理器的底层客户端
            this.transferManager = S3TransferManager.builder().s3Client(this.client).build();

            // 创建 S3 配置对象
            S3Configuration config = S3Configuration.builder().chunkedEncodingEnabled(false)
                .pathStyleAccessEnabled(isStyle).build();

            // 创建 预签名 URL 的生成器 实例，用于生成 S3 预签名 URL
            this.presigner = S3Presigner.builder()
                .region(of())
                .credentialsProvider(credentialsProvider)
                .endpointOverride(URI.create(getDomain()))
                .serviceConfiguration(config)
                .build();

        } catch (Exception e) {
            if (e instanceof OssException) {
                throw e;
            }
            throw new OssException("配置错误! 请检查系统配置:[" + e.getMessage() + "]");
        }
    }

    /**
     * 上传文件到 Amazon S3，并返回上传结果
     *
     * @param filePath    本地文件路径
     * @param key         在 Amazon S3 中的对象键
     * @param md5Digest   本地文件的 MD5 哈希值（可选）
     * @param contentType 文件内容类型
     * @return UploadResult 包含上传后的文件信息
     * @throws OssException 如果上传失败，抛出自定义异常
     */
    public UploadResult upload(Path filePath, String key, String md5Digest, String contentType) {
        try {
            // 构建上传请求对象
            FileUpload fileUpload = transferManager.uploadFile(
                x -> x.putObjectRequest(
                        y -> y.bucket(properties.getBucketName())
                            .key(key)
                            .contentMD5(StringUtils.isNotEmpty(md5Digest) ? md5Digest : null)
                            .contentType(contentType)
                            // 用于设置对象的访问控制列表（ACL）。不同云厂商对ACL的支持和实现方式有所不同，
                            // 因此根据具体的云服务提供商，你可能需要进行不同的配置（自行开启，阿里云有acl权限配置，腾讯云没有acl权限配置）
                            //.acl(getAccessPolicy().getObjectCannedACL())
                            .build())
                    .addTransferListener(LoggingTransferListener.create())
                    .source(filePath).build());

            // 等待上传完成并获取上传结果
            CompletedFileUpload uploadResult = fileUpload.completionFuture().join();
            String eTag = uploadResult.response().eTag();

            // 提取上传结果中的 ETag，并构建一个自定义的 UploadResult 对象
            return UploadResult.builder().url(getUrl() + StringUtils.SLASH + key).filename(key).eTag(eTag).build();
        } catch (Exception e) {
            // 捕获异常并抛出自定义异常
            throw new OssException("上传文件失败，请检查配置信息:[" + e.getMessage() + "]");
        } finally {
            // 无论上传是否成功，最终都会删除临时文件
            FileUtils.del(filePath);
        }
    }

    /**
     * 上传 InputStream 到 Amazon S3
     *
     * @param inputStream 要上传的输入流
     * @param key         在 Amazon S3 中的对象键
     * @param length      输入流的长度
     * @param contentType 文件内容类型
     * @return UploadResult 包含上传后的文件信息
     * @throws OssException 如果上传失败，抛出自定义异常
     */
    public UploadResult upload(InputStream inputStream, String key, Long length, String contentType) {
        // 如果输入流不是 ByteArrayInputStream，则将其读取为字节数组再创建 ByteArrayInputStream
        if (!(inputStream instanceof ByteArrayInputStream)) {
            inputStream = new ByteArrayInputStream(IoUtil.readBytes(inputStream));
        }
        try {
            // 创建异步请求体（length如果为空会报错）
            BlockingInputStreamAsyncRequestBody body = BlockingInputStreamAsyncRequestBody.builder()
                .contentLength(length)
                .subscribeTimeout(Duration.ofSeconds(120))
                .build();

            // 使用 transferManager 进行上传
            Upload upload = transferManager.upload(
                x -> x.requestBody(body).addTransferListener(LoggingTransferListener.create())
                    .putObjectRequest(
                        y -> y.bucket(properties.getBucketName())
                            .key(key)
                            .contentType(contentType)
                            // 用于设置对象的访问控制列表（ACL）。不同云厂商对ACL的支持和实现方式有所不同，
                            // 因此根据具体的云服务提供商，你可能需要进行不同的配置（自行开启，阿里云有acl权限配置，腾讯云没有acl权限配置）
                            //.acl(getAccessPolicy().getObjectCannedACL())
                            .build())
                    .build());

            // 将输入流写入请求体
            body.writeInputStream(inputStream);

            // 等待文件上传操作完成
            CompletedUpload uploadResult = upload.completionFuture().join();
            String eTag = uploadResult.response().eTag();

            // 提取上传结果中的 ETag，并构建一个自定义的 UploadResult 对象
            return UploadResult.builder().url(getUrl() + StringUtils.SLASH + key).filename(key).eTag(eTag).build();
        } catch (Exception e) {
            throw new OssException("上传文件失败，请检查配置信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 下载文件从 Amazon S3 到临时目录
     *
     * @param path 文件在 Amazon S3 中的对象键
     * @return 下载后的文件在本地的临时路径
     * @throws OssException 如果下载失败，抛出自定义异常
     */
    public Path fileDownload(String path) {
        // 构建临时文件
        Path tempFilePath = FileUtils.createTempFile().toPath();
        // 使用 S3TransferManager 下载文件
        FileDownload downloadFile = transferManager.downloadFile(
            x -> x.getObjectRequest(
                    y -> y.bucket(properties.getBucketName())
                        .key(removeBaseUrl(path))
                        .build())
                .addTransferListener(LoggingTransferListener.create())
                .destination(tempFilePath)
                .build());
        // 等待文件下载操作完成
        downloadFile.completionFuture().join();
        return tempFilePath;
    }

    /**
     * 下载文件从 Amazon S3 到 输出流
     *
     * @param key 文件在 Amazon S3 中的对象键
     * @param out 输出流
     * @param consumer 自定义处理逻辑
     * @throws OssException 如果下载失败，抛出自定义异常
     */
    public void download(String key, OutputStream out, Consumer<Long> consumer) {
        try {
            this.download(key, consumer).writeTo(out);
        } catch (Exception e) {
            throw new OssException("文件下载失败，错误信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 下载文件从 Amazon S3 到 输出流
     *
     * @param key 文件在 Amazon S3 中的对象键
     * @param contentLengthConsumer 文件大小消费者函数
     * @return 写出订阅器
     * @throws OssException 如果下载失败，抛出自定义异常
     */
    public WriteOutSubscriber<OutputStream> download(String key, Consumer<Long> contentLengthConsumer) {
        try {
            // 构建下载请求
            DownloadRequest<ResponsePublisher<GetObjectResponse>> publisherDownloadRequest = DownloadRequest.builder()
                // 文件对象
                .getObjectRequest(y -> y.bucket(properties.getBucketName())
                    .key(key)
                    .build())
                .addTransferListener(LoggingTransferListener.create())
                // 使用发布订阅转换器
                .responseTransformer(AsyncResponseTransformer.toPublisher())
                .build();

            // 使用 S3TransferManager 下载文件
            Download<ResponsePublisher<GetObjectResponse>> publisherDownload = transferManager.download(publisherDownloadRequest);
            // 获取下载发布订阅转换器
            ResponsePublisher<GetObjectResponse> publisher = publisherDownload.completionFuture().join().result();
            // 执行文件大小消费者函数
            Optional.ofNullable(contentLengthConsumer)
                .ifPresent(lengthConsumer -> lengthConsumer.accept(publisher.response().contentLength()));

            // 构建写出订阅器对象
            return out -> {
                // 创建可写入的字节通道
                try(WritableByteChannel channel = Channels.newChannel(out)){
                    // 订阅数据
                    publisher.subscribe(byteBuffer -> {
                        while (byteBuffer.hasRemaining()) {
                            try {
                                channel.write(byteBuffer);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }).join();
                }
            };
        } catch (Exception e) {
            throw new OssException("文件下载失败，错误信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 删除云存储服务中指定路径下文件
     *
     * @param path 指定路径
     */
    public void delete(String path) {
        try {
            client.deleteObject(
                x -> x.bucket(properties.getBucketName())
                    .key(removeBaseUrl(path))
                    .build());
        } catch (Exception e) {
            throw new OssException("删除文件失败，请检查配置信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 获取私有URL链接
     *
     * @param objectKey   对象KEY
     * @param expiredTime 链接授权到期时间
     */
    public String getPrivateUrl(String objectKey, Duration expiredTime) {
        // 使用 AWS S3 预签名 URL 的生成器 获取对象的预签名 URL
        URL url = presigner.presignGetObject(
                x -> x.signatureDuration(expiredTime)
                    .getObjectRequest(
                        y -> y.bucket(properties.getBucketName())
                            .key(objectKey)
                            .build())
                    .build())
            .url();
        return url.toString();
    }

    /**
     * 上传 byte[] 数据到 Amazon S3，使用指定的后缀构造对象键。
     *
     * @param data   要上传的 byte[] 数据
     * @param suffix 对象键的后缀
     * @return UploadResult 包含上传后的文件信息
     * @throws OssException 如果上传失败，抛出自定义异常
     */
    public UploadResult uploadSuffix(byte[] data, String suffix, String contentType) {
        return upload(new ByteArrayInputStream(data), getPath(properties.getPrefix(), suffix), Long.valueOf(data.length), contentType);
    }

    /**
     * 上传 InputStream 到 Amazon S3，使用指定的后缀构造对象键。
     *
     * @param inputStream 要上传的输入流
     * @param suffix      对象键的后缀
     * @param length      输入流的长度
     * @return UploadResult 包含上传后的文件信息
     * @throws OssException 如果上传失败，抛出自定义异常
     */
    public UploadResult uploadSuffix(InputStream inputStream, String suffix, Long length, String contentType) {
        return upload(inputStream, getPath(properties.getPrefix(), suffix), length, contentType);
    }

    /**
     * 上传文件到 Amazon S3，使用指定的后缀构造对象键
     *
     * @param file   要上传的文件
     * @param suffix 对象键的后缀
     * @return UploadResult 包含上传后的文件信息
     * @throws OssException 如果上传失败，抛出自定义异常
     */
    public UploadResult uploadSuffix(File file, String suffix) {
        return upload(file.toPath(), getPath(properties.getPrefix(), suffix), null, FileUtils.getMimeType(suffix));
    }

    /**
     * 获取文件输入流
     *
     * @param path 完整文件路径
     * @return 输入流
     */
    public InputStream getObjectContent(String path) throws IOException {
        // 下载文件到临时目录
        Path tempFilePath = fileDownload(path);
        // 创建输入流
        InputStream inputStream = Files.newInputStream(tempFilePath);
        // 删除临时文件
        FileUtils.del(tempFilePath);
        // 返回对象内容的输入流
        return inputStream;
    }

    /**
     * 获取 S3 客户端的终端点 URL
     *
     * @return 终端点 URL
     */
    public String getEndpoint() {
        // 根据配置文件中的是否使用 HTTPS，设置协议头部
        String header = getIsHttps();
        // 拼接协议头部和终端点，得到完整的终端点 URL
        return header + properties.getEndpoint();
    }

    /**
     * 获取 S3 客户端的终端点 URL（自定义域名）
     *
     * @return 终端点 URL
     */
    public String getDomain() {
        // 从配置中获取域名、终端点、是否使用 HTTPS 等信息
        String domain = properties.getDomain();
        String endpoint = properties.getEndpoint();
        String header = getIsHttps();

        // 如果是云服务商，直接返回域名或终端点
        if (StringUtils.containsAny(endpoint, OssConstant.CLOUD_SERVICE)) {
            return StringUtils.isNotEmpty(domain) ? header + domain : header + endpoint;
        }

        // 如果是 MinIO，处理域名并返回
        if (StringUtils.isNotEmpty(domain)) {
            return domain.startsWith(Constants.HTTPS) || domain.startsWith(Constants.HTTP) ? domain : header + domain;
        }

        // 返回终端点
        return header + endpoint;
    }

    /**
     * 根据传入的 region 参数返回相应的 AWS 区域
     * 如果 region 参数非空，使用 Region.of 方法创建并返回对应的 AWS 区域对象
     * 如果 region 参数为空，返回一个默认的 AWS 区域（例如，us-east-1），作为广泛支持的区域
     *
     * @return 对应的 AWS 区域对象，或者默认的广泛支持的区域（us-east-1）
     */
    public Region of() {
        //AWS 区域字符串
        String region = properties.getRegion();
        // 如果 region 参数非空，使用 Region.of 方法创建对应的 AWS 区域对象，否则返回默认区域
        return StringUtils.isNotEmpty(region) ? Region.of(region) : Region.US_EAST_1;
    }

    /**
     * 获取云存储服务的URL
     *
     * @return 文件路径
     */
    public String getUrl() {
        String domain = properties.getDomain();
        String endpoint = properties.getEndpoint();
        String header = getIsHttps();
        // 云服务商直接返回
        if (StringUtils.containsAny(endpoint, OssConstant.CLOUD_SERVICE)) {
            return header + (StringUtils.isNotEmpty(domain) ? domain : properties.getBucketName() + "." + endpoint);
        }
        // MinIO 单独处理
        if (StringUtils.isNotEmpty(domain)) {
            // 如果 domain 以 "https://" 或 "http://" 开头
            return (domain.startsWith(Constants.HTTPS) || domain.startsWith(Constants.HTTP)) ?
                domain + StringUtils.SLASH + properties.getBucketName() : header + domain + StringUtils.SLASH + properties.getBucketName();
        }
        return header + endpoint + StringUtils.SLASH + properties.getBucketName();
    }

    /**
     * 生成一个符合特定规则的、唯一的文件路径。通过使用日期、UUID、前缀和后缀等元素的组合，确保了文件路径的独一无二性
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 文件路径
     */
    public String getPath(String prefix, String suffix) {
        // 生成uuid
        String uuid = IdUtil.fastSimpleUUID();
        // 生成日期路径
        String datePath = DateUtils.datePath();
        // 拼接路径
        String path = StringUtils.isNotEmpty(prefix) ?
            prefix + StringUtils.SLASH + datePath + StringUtils.SLASH + uuid : datePath + StringUtils.SLASH + uuid;
        return path + suffix;
    }

    /**
     * 移除路径中的基础URL部分，得到相对路径
     *
     * @param path 完整的路径，包括基础URL和相对路径
     * @return 去除基础URL后的相对路径
     */
    public String removeBaseUrl(String path) {
        return path.replace(getUrl() + StringUtils.SLASH, "");
    }

    /**
     * 服务商
     */
    public String getConfigKey() {
        return configKey;
    }

    /**
     * 获取是否使用 HTTPS 的配置，并返回相应的协议头部。
     *
     * @return 协议头部，根据是否使用 HTTPS 返回 "https://" 或 "http://"
     */
    public String getIsHttps() {
        return OssConstant.IS_HTTPS.equals(properties.getIsHttps()) ? Constants.HTTPS : Constants.HTTP;
    }

    /**
     * 检查配置是否相同
     */
    public boolean checkPropertiesSame(OssProperties properties) {
        return this.properties.equals(properties);
    }

    /**
     * 获取当前桶权限类型
     *
     * @return 当前桶权限类型code
     */
    public AccessPolicyType getAccessPolicy() {
        return AccessPolicyType.getByType(properties.getAccessPolicy());
    }

    /**
     * 1. 启动分块上传任务
     */
    @Override
    public String startMultipartUpload(String objectName, String contentType) throws OssException {
        // ⚠️ 实际代码：在此处根据 properties.getType() 路由到具体的云服务商 SDK 逻辑
        //TODO 根据 properties.getType() 获取对应的云服务商 SDK
        log.info("Starting multipart upload for object: {} using service: {}", objectName, "tencent");

        // 示例：这里需要根据 properties.getType() 调用 Aliyun/Tencent/AWS SDK 的 initiateMultipartUpload 方法
        // 由于没有具体的 SDK 代码，这里仅返回一个模拟的 UploadId
        if ("aliyun".equalsIgnoreCase("tencent")) {
            // return aliyunClient.initiateMultipartUpload(objectName, contentType);
            return "aliyun-" + IdUtil.fastSimpleUUID();
        } else if ("tencent".equalsIgnoreCase("tencent") ){
            // return tencentClient.createMultipartUpload(objectName, contentType);
            return "tencent-" + IdUtil.fastSimpleUUID();
        }

        throw new OssException("当前配置[" + "tencent" + "]不支持分块上传");
    }

    /**
     * 2. 上传文件分块
     */
    @Override
    public Map<String, Object> uploadPart(String uploadId, int partNumber, InputStream content, long contentLength) throws OssException {
        log.info("Uploading part {} with size {} for uploadId: {}", partNumber, contentLength, uploadId);

        // ⚠️ 实际代码：在此处根据 uploadId (或 properties.getType()) 路由到具体的 SDK 逻辑
        // 示例：调用 SDK 的 uploadPart API
        if (uploadId.startsWith("aliyun")) {
            // PartETag partEtag = aliyunClient.uploadPart(uploadId, partNumber, content, contentLength);

            Map<String, Object> partInfo = new HashMap<>(); // 🚀 需要 Map 包装 PartNumber 和 ETag
            partInfo.put("PartNumber", partNumber);
            partInfo.put("ETag", "ETag-" + IdUtil.fastSimpleUUID()); // 实际是 SDK 返回的 ETag
            partInfo.put("size", contentLength); // 方便上层计算总大小
            return partInfo;
        }

        // ... 其他云服务商逻辑 ...

        throw new OssException("不支持的分块上传任务ID: " + uploadId);
    }

    /**
     * 3. 完成分块上传任务
     */
    @Override
    public UploadResult completeMultipartUpload(String uploadId, String objectName, List<Map<String, Object>> partsList) throws OssException {
        log.info("Completing multipart upload for object: {} with {} parts", objectName, partsList.size());

        // ⚠️ 实际代码：在此处根据 uploadId 路由到具体的 SDK 逻辑，并执行合并/完成操作
        if (uploadId.startsWith("aliyun")) {
            // CompleteMultipartUploadResult result = aliyunClient.completeMultipartUpload(uploadId, objectName, partsList);

            // 构造 UploadResult 返回给上层
            UploadResult result = new UploadResult();
            result.setUrl(getUrl() + "/" + objectName); // 假设 getUrl() 存在
            result.setFilename(objectName);
            return result;
        }

        // ... 其他云服务商逻辑 ...

        throw new OssException("不支持的分块上传任务ID: " + uploadId);
    }

    /**
     * 4. 取消分块上传
     */
    @Override
    public void abortMultipartUpload(String uploadId) throws OssException {
        log.warn("Aborting multipart upload for ID: {}", uploadId);

        // ⚠️ 实际代码：在此处根据 uploadId 路由到具体的 SDK 逻辑，并执行中止操作
        if (uploadId.startsWith("aliyun")) {
            // aliyunClient.abortMultipartUpload(uploadId);
            return;
        }
        if (uploadId.startsWith("tencent")) {
            // tencentClient.abortMultipartUpload(uploadId);
            return;
        }

        // ... 其他云服务商逻辑 ...
    }
}

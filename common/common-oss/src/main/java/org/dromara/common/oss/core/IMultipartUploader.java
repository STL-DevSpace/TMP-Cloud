package org.dromara.common.oss.core;

import org.dromara.common.oss.entity.UploadResult;
import org.dromara.common.oss.exception.OssException;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 大文件分块上传能力接口
 * 供 OssClient 的具体实现类选择性实现，以避免修改 OssClient 核心接口。
 */
public interface IMultipartUploader {

    /**
     * 1. 启动分块上传任务
     *
     * @param objectName 存储桶中的对象名/路径
     * @param contentType 文件类型
     * @return 分块上传的唯一标识 uploadId
     * @throws OssException OSS操作异常
     */
    String startMultipartUpload(String objectName, String contentType) throws OssException;

    /**
     * 2. 上传文件分块
     *
     * @param uploadId 上传任务ID
     * @param partNumber 分块编号（通常从 1 开始）
     * @param content 分块数据流
     * @param contentLength 分块数据的长度
     * @return 返回该分块的 ETag/PartNumber 信息 (Map, 包含 PartNumber 和 ETag)
     * @throws OssException OSS操作异常
     */
    Map<String, Object> uploadPart(String uploadId, int partNumber, InputStream content, long contentLength) throws OssException;

    /**
     * 3. 完成分块上传任务
     *
     * @param uploadId 上传任务ID
     * @param objectName 存储桶中的对象名/路径
     * @param partsList 所有分块的 ETag/PartNumber 列表
     * @return 最终的文件上传结果 UploadResult (包含 URL 和文件名)
     * @throws OssException OSS操作异常
     */
    UploadResult completeMultipartUpload(String uploadId, String objectName, List<Map<String, Object>> partsList) throws OssException;

    /**
     * 4. 取消分块上传
     * 在发生错误时调用，清理对象存储上的残留分块。
     *
     * @param uploadId 上传任务ID
     * @throws OssException OSS操作异常
     */
    void abortMultipartUpload(String uploadId) throws OssException;
}

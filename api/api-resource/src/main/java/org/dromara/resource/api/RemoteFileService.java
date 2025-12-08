package org.dromara.resource.api;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.resource.api.domain.RemoteFile;

import java.util.List;
import java.util.Map;

/**
 * 文件服务
 *
 * @author Lion Li
 */
public interface RemoteFileService {

    /**
     * 上传文件
     *
     * @param file 文件信息
     * @return 结果
     */
    RemoteFile upload(String name, String originalFilename, String contentType, byte[] file) throws ServiceException;

    /**
     * 通过ossId查询对应的url
     *
     * @param ossIds ossId串逗号分隔
     * @return url串逗号分隔
     */
    String selectUrlByIds(String ossIds);

    /**
     * 通过ossId查询列表
     *
     * @param ossIds ossId串逗号分隔
     * @return 列表
     */
    List<RemoteFile> selectByIds(String ossIds);

    // ----------------------------------------------------
    //  🚀 新增：大文件分块上传方法
    // ----------------------------------------------------

    /**
     * 1. 启动分块上传任务
     * 在 OSS/COS 中初始化分片上传，返回一个唯一的 uploadId。
     *
     * @param originalFilename 原始文件名
     * @param objectName 存储桶中的对象名/路径
     * @param contentType 文件类型
     * @return 分块上传的唯一标识 uploadId
     * @throws ServiceException 上传服务异常
     */
    String startMultipartUpload(String originalFilename, String objectName, String contentType) throws ServiceException;


    /**
     * 2. 上传文件分块
     * 负责上传文件的一个数据块。
     *
     * @param uploadId 上传任务ID
     * @param partNumber 分块编号（通常从 1 开始）
     * @param fileChunkData 分块数据（不超过 Dubbo payload 限制，如 5MB）
     * @return 返回该分块的 ETag 或其他验证信息 (Map, 包含 PartNumber 和 ETag)
     * @throws ServiceException 上传服务异常
     */
    Map<String, Object> uploadChunk(String uploadId, int partNumber, byte[] fileChunkData) throws ServiceException;


    /**
     * 3. 完成分块上传任务
     * 将所有已上传的分块合并成一个完整的对象。
     *
     * @param uploadId 上传任务ID
     * @param objectName 存储桶中的对象名/路径
     * @param partsList 所有分块的 ETag/PartNumber 列表，用于合并
     * @param originalFilename 原始文件名
     * @return 最终的文件对象 RemoteFile (包含 URL 和 ID)
     * @throws ServiceException 上传服务异常
     */
    RemoteFile completeMultipartUpload(String uploadId, String objectName, List<Map<String, Object>> partsList, String originalFilename) throws ServiceException;


    /**
     * (可选但推荐) 4. 取消分块上传
     * 在发生错误时调用，清理对象存储上的残留分块。
     *
     * @param uploadId 上传任务ID
     * @throws ServiceException 上传服务异常
     */
    void abortMultipartUpload(String uploadId) throws ServiceException;
}

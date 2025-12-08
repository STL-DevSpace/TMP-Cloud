package org.dromara.resource.dubbo;

import cn.hutool.core.convert.Convert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.oss.core.IMultipartUploader;
import org.dromara.common.oss.core.OssClient;
import org.dromara.common.oss.entity.UploadResult;
import org.dromara.common.oss.factory.OssFactory;
import org.dromara.resource.api.RemoteFileService;
import org.dromara.resource.api.domain.RemoteFile;
import org.dromara.resource.domain.SysOssExt;
import org.dromara.resource.domain.bo.SysOssBo;
import org.dromara.resource.domain.vo.SysOssVo;
import org.dromara.resource.service.ISysOssService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 文件请求处理
 *
 * @author Lion Li
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DubboService
public class RemoteFileServiceImpl implements RemoteFileService {

    private final ISysOssService sysOssService;

    /**
     * 文件上传请求
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public RemoteFile upload(String name, String originalFilename, String contentType, byte[] file) throws ServiceException {
        try {
            String suffix = StringUtils.substring(originalFilename, originalFilename.lastIndexOf("."), originalFilename.length());
            OssClient storage = OssFactory.instance();
            UploadResult uploadResult = storage.uploadSuffix(file, suffix, contentType);
            // 保存文件信息
            SysOssBo oss = new SysOssBo();
            oss.setUrl(uploadResult.getUrl());
            oss.setFileSuffix(suffix);
            oss.setFileName(uploadResult.getFilename());
            oss.setOriginalName(originalFilename);
            oss.setService(storage.getConfigKey());
            SysOssExt ext1 = new SysOssExt();
            ext1.setFileSize((long) file.length);
            String extStr = JsonUtils.toJsonString(ext1);
            oss.setExt1(extStr);
            sysOssService.insertByBo(oss);
            RemoteFile sysFile = new RemoteFile();
            sysFile.setOssId(oss.getOssId());
            sysFile.setName(uploadResult.getFilename());
            sysFile.setUrl(uploadResult.getUrl());
            sysFile.setOriginalName(originalFilename);
            sysFile.setFileSuffix(suffix);
            sysFile.setExt1(extStr);
            return sysFile;
        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new ServiceException("上传文件失败");
        }
    }
    /**
     * 检查 OssClient 实例是否支持分块上传
     */
    private IMultipartUploader checkUploader(OssClient storage) {
        if (storage instanceof IMultipartUploader) {
            return (IMultipartUploader) storage;
        }
        log.error("当前文件存储服务不支持大文件分块上传: {}", storage.getConfigKey());
        throw new ServiceException("当前文件存储服务不支持大文件分块上传 (请检查配置或客户端实现)");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startMultipartUpload(String originalFilename, String objectName, String contentType) throws ServiceException {
        OssClient storage = OssFactory.instance();
        IMultipartUploader uploader = checkUploader(storage);
        try {
            String uploadId = uploader.startMultipartUpload(objectName, contentType);
            // ... (可选：可以在此记录分块任务信息到数据库)
            return uploadId;
        } catch (Exception e) {
            log.error("启动分块上传任务失败", e);
            throw new ServiceException("启动分块上传任务失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> uploadChunk(String uploadId, int partNumber, byte[] fileChunkData) throws ServiceException {
        OssClient storage = OssFactory.instance();
        IMultipartUploader uploader = checkUploader(storage);
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(fileChunkData);

            // 调用 IMultipartUploader 上的 uploadPart 方法
            return uploader.uploadPart(uploadId, partNumber, bais, fileChunkData.length);
        } catch (Exception e) {
            log.error("上传文件分块失败 (Part: {})", partNumber, e);
            throw new ServiceException("上传文件分块失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RemoteFile completeMultipartUpload(String uploadId, String objectName, List<Map<String, Object>> partsList, String originalFilename) throws ServiceException {
        OssClient storage = OssFactory.instance();
        IMultipartUploader uploader = checkUploader(storage);
        try {
            // 1. 完成分块上传，获取最终结果
            UploadResult uploadResult = uploader.completeMultipartUpload(uploadId, objectName, partsList);

            // 2. 数据库记录 (复用原 upload 方法中的数据库插入逻辑)
            // ... (省略 SysOssBo, SysOssExt 构造和 sysOssService.insertByBo(oss) 的逻辑)

            // 3. 构造并返回 RemoteFile
            RemoteFile sysFile = new RemoteFile(); // 实际应从数据库或构造中获取完整对象
            sysFile.setUrl(uploadResult.getUrl());
            sysFile.setOriginalName(originalFilename);
            // ... 填充其他字段 ...

            return sysFile;
        } catch (Exception e) {
            log.error("完成分块上传任务失败", e);
            throw new ServiceException("完成分块上传任务失败: " + e.getMessage());
        }
    }

    @Override
    public void abortMultipartUpload(String uploadId) throws ServiceException {
        OssClient storage = OssFactory.instance();

        // 允许不检查 uploader，如果不支持分片，则无需中止
        if (storage instanceof IMultipartUploader) {
            IMultipartUploader uploader = (IMultipartUploader) storage;
            try {
                uploader.abortMultipartUpload(uploadId);
            } catch (Exception e) {
                log.warn("取消分块上传任务失败 (UploadId: {})，请手动检查OSS控制台。", uploadId, e);
            }
        }
    }

    /**
     * 通过ossId查询对应的url
     *
     * @param ossIds ossId串逗号分隔
     * @return url串逗号分隔
     */
    @Override
    public String selectUrlByIds(String ossIds) {
        return sysOssService.selectUrlByIds(ossIds);
    }

    /**
     * 通过ossId查询列表
     *
     * @param ossIds ossId串逗号分隔
     * @return 列表
     */
    @Override
    public List<RemoteFile> selectByIds(String ossIds){
        List<SysOssVo> sysOssVos = sysOssService.listByIds(StringUtils.splitTo(ossIds, Convert::toLong));
        return MapstructUtils.convert(sysOssVos, RemoteFile.class);
    }
}

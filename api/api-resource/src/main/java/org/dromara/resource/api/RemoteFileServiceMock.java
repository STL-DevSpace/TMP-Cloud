package org.dromara.resource.api;

import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.resource.api.domain.RemoteFile;

import java.util.List;
import java.util.Map;

/**
 * 文件服务(降级处理)
 *
 * @author Lion Li
 */
@Slf4j
public class RemoteFileServiceMock implements RemoteFileService {

    /**
     * 上传文件
     *
     * @param file 文件信息
     * @return 结果
     */
    @Override
    public RemoteFile upload(String name, String originalFilename, String contentType, byte[] file) {
        log.warn("服务调用异常 -> 降级处理");
        return null;
    }

    @Override
    public String startMultipartUpload(String originalFilename, String objectName, String contentType) throws ServiceException {
        return "";
    }

    @Override
    public Map<String, Object> uploadChunk(String uploadId, int partNumber, byte[] fileChunkData) throws ServiceException {
        return Map.of();
    }

    @Override
    public RemoteFile completeMultipartUpload(String uploadId, String objectName, List<Map<String, Object>> partsList, String originalFilename) throws ServiceException {
        return null;
    }

    @Override
    public void abortMultipartUpload(String uploadId) throws ServiceException {

    }

    /**
     * 通过ossId查询对应的url
     *
     * @param ossIds ossId串逗号分隔
     * @return url串逗号分隔
     */
    @Override
    public String selectUrlByIds(String ossIds) {
        log.warn("服务调用异常 -> 降级处理");
        return StringUtils.EMPTY;
    }

    /**
     * 通过ossId查询列表
     *
     * @param ossIds ossId串逗号分隔
     * @return 列表
     */
    @Override
    public List<RemoteFile> selectByIds(String ossIds) {
        log.warn("服务调用异常 -> 降级处理");
        return List.of();
    }

}

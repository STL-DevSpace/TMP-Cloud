package org.dromara.data.controller;


import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.domain.R;
import org.dromara.data.service.TestService;
import org.dromara.resource.api.RemoteFileService;
import org.dromara.resource.api.domain.RemoteFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 作为引用对象存储并存进数据库的参考
 */
@RestController
@RequestMapping("/api/test")
public class TestOssController {

    @DubboReference
    private RemoteFileService remoteFileService;

    @Resource
    private TestService testService;

    @PostMapping("/test/save_data")
    public R<RemoteFile> test(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String objectName = UUID.randomUUID().toString() + extension;
        RemoteFile uploadFile = remoteFileService.upload(originalFileName, originalFileName, file.getContentType(),
                file.getBytes());
        testService.save_data(uploadFile);
        return R.ok(uploadFile);

    }
}

package org.dromara.data.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.domain.R;
import org.dromara.data.utils.CosUtils;
import org.dromara.resource.api.RemoteFileService;
import org.dromara.resource.api.domain.RemoteFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequestMapping("/api/cos")
@RestController
public class COSController {

    @Resource
    private CosUtils cosUtils;

    /**
     * 使用 @DubboReference 注入远程服务
     */
    @DubboReference(check = false, timeout = 30000, retries = 0)
    private RemoteFileService remoteOssService;

    /**
     * 方式1: 使用自定义 CosUtils 上传（不保存数据库）
     */
    @PostMapping("/upload")
    public R<Map<String, String>> uploadFile(MultipartFile file) {
        log.info("文件上传（CosUtils）：{}", file.getOriginalFilename());
        try {
            String fileName = file.getOriginalFilename();
            String extension = fileName.substring(fileName.lastIndexOf("."));
            String objectName = UUID.randomUUID().toString() + extension;

            String filePath = cosUtils.uploadFile(
                file.getInputStream(),
                objectName,
                file.getSize()
            );

            Map<String, String> result = new HashMap<>();
            result.put("filepath", filePath);
            result.put("url", cosUtils.getPublicUrl(filePath));
            return R.ok(result);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return R.fail("上传失败: " + e.getMessage());
        }
    }


    /**
     * 根据 OSS ID 获取文件信息
     */
    @GetMapping("/{ossId}")
    public R<String> getById(@PathVariable String ossId) {
        return R.ok(remoteOssService.selectUrlByIds(ossId));
    }

    /**
     * 批量获取 URL（通过 Dubbo 从数据库查询）
     */
    @PostMapping("/batch-urls")
    public List<RemoteFile> getBatchUrls(@RequestBody String ossIds) {
        return remoteOssService.selectByIds(ossIds);
    }

    /**
     * 批量获取公有读 URL（直接拼接，不查数据库）
     */
    @PostMapping("/batch-public-urls")
    public R<Map<String, String>> getBatchPublicUrls(@RequestBody String[] keys) {
        Map<String, String> result = new HashMap<>();
        for (String key : keys) {
            result.put(key, cosUtils.getPublicUrl(key));
        }
        return R.ok(result);
    }

    @GetMapping("/hi")
    public String hi() {
        return "hi from dubbo service";
    }
}

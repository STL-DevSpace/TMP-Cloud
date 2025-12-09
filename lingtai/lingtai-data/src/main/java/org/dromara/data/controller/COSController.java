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
import java.util.*;

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
     * 【已修改】方式1: 使用自定义 CosUtils 上传，支持多文件/文件夹上传
     * 前端使用 files[] 字段，因此后端需要 @RequestParam("files[]") List<MultipartFile>
     */
    @PostMapping("/upload")
    public R<Map<String, Object>> uploadFiles(@RequestParam("files[]") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return R.fail("未接收到任何文件");
        }

        log.info("文件上传（CosUtils），共接收到 {} 个文件", files.size());

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> uploadedFiles = new ArrayList<>();

        // 【修正 1】引入一个变量来存储本次上传的唯一目录名
        String uniqueDir = UUID.randomUUID().toString();
        // 定义 COS 存储的根路径前缀，例如: "model/f9207432-dc2c-4065-8105-32c8bee21658"
        String baseKeyPrefix = "model/" + uniqueDir;

        // 用于存储最终返回的 Primary URL（整个文件夹的根 URL）
        String primaryFileUrl = null;
        long totalSize = 0;

        try {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (file.isEmpty()) continue;

                String originalFilename = file.getOriginalFilename();

                // 生成 COS Key (uniqueDir + 文件名)
                // objectKey 示例: model/f9207432-dc2c-4065-8105-32c8bee21658/config.json
                String objectKey = baseKeyPrefix + "/" + originalFilename;

                totalSize += file.getSize();

                // 上传文件（你的方法返回的是 key）
                String key = cosUtils.uploadFile(
                    file.getInputStream(),
                    objectKey,
                    file.getSize()
                );

                // 转换为可访问 URL （传入的是完整文件路径 key）
                String url = cosUtils.getPublicUrl(key);

                log.info("文件 {} 上传成功，访问地址：{}", originalFilename, url);

                // 保存每个文件的上传结果
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("name", originalFilename);
                fileInfo.put("url", url);
                fileInfo.put("size", file.getSize());
                fileInfo.put("key", key); // 完整的 COS Key

                uploadedFiles.add(fileInfo);
            }

            if (uploadedFiles.isEmpty()) {
                return R.fail("文件上传成功，但上传文件列表为空！");
            }

            // 【修正 2】构建整个上传目录的根 URL 作为 PrimaryUrl
            // 1. 构造文件夹的 COS Key ( 必须以 '/' 结尾 )
            // folderObjectKey 示例: "model/f9207432-dc2c-4065-8105-32c8bee21658/"
            String folderObjectKey = baseKeyPrefix + "/";

            // 2. 使用 getPublicUrl 获取文件夹的 URL
            primaryFileUrl = cosUtils.getPublicUrl(folderObjectKey);

            // 返回给前端的数据结构
            result.put("primaryUrl", primaryFileUrl);
            result.put("files", uploadedFiles);
            result.put("totalSize", totalSize);
            result.put("count", uploadedFiles.size());

            return R.ok(result);

        } catch (Exception e) {
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

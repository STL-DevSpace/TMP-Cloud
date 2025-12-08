package org.dromara.data.service.impl;

import org.apache.dubbo.config.annotation.DubboReference;
import lombok.RequiredArgsConstructor;
import org.dromara.data.entity.Models; // 假设的模型实体类
import org.dromara.data.mapper.DataMapper;
import org.dromara.data.service.IHubImportTaskService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 假设对象存储服务的接口和实体类
import org.dromara.resource.api.RemoteFileService;
import org.dromara.resource.api.domain.RemoteFile;

import java.io.File;
import java.io.FileOutputStream; // 用于模拟创建文件
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Hub 模型导入任务服务实现
 */
@Service
@RequiredArgsConstructor
public class HubImportTaskService implements IHubImportTaskService {

    private final DataMapper modelsMapper;

    // 🚀 Dubbo 引用对象存储服务 (RemoteFileService)
    @DubboReference
    private RemoteFileService remoteFileService;

    /**
     * 异步执行模型导入任务
     * 使用 @Async 确保该方法在一个独立线程中运行，不阻塞主线程。
     */
    @Override
    @Async("taskExecutor") // 使用 AsyncImportConfig 中定义的线程池
    @Transactional(rollbackFor = Exception.class)
    public void startImport(Integer modelId, String hubUrl) {
        System.out.println("[ASYNC TASK] Starting import for Model ID: " + modelId + " from Hub: " + hubUrl);

        Models modelUpdate = new Models();
        modelUpdate.setId(modelId);

        File tempLocalFile = null;

        try {
            // =========================================================
            //  第一步：下载到本地临时文件 (Download)
            // =========================================================
            System.out.println("[ASYNC TASK] 1. Downloading model from Hub...");
            tempLocalFile = downloadModelFromHub(hubUrl);
            long fileSize = tempLocalFile.length();

            // =========================================================
            //  第二步：上传到对象存储 (Upload via Dubbo Service)
            // =========================================================
            System.out.println("[ASYNC TASK] 2. Uploading file to Object Storage...");

            // 1. 读取本地文件数据
            byte[] fileBytes = Files.readAllBytes(tempLocalFile.toPath());

            // 2. 构造上传所需的元数据
            String originalFileName = tempLocalFile.getName();
            String contentType = "application/octet-stream";
            String objectName = generateObjectName(hubUrl, originalFileName); // 生成唯一的对象存储路径

            // 3. 调用 Dubbo 远程服务进行上传
            RemoteFile uploadResult = remoteFileService.upload(
                originalFileName,
                objectName,
                contentType,
                fileBytes
            );

            if (uploadResult == null || uploadResult.getUrl() == null) {
                throw new Exception("Object Storage upload failed, returned null or incomplete result.");
            }

            // =========================================================
            //  第三步：更新数据库状态 (Update DB Record)
            // =========================================================

            modelUpdate.setFilePath(uploadResult.getUrl());     // 🚀 使用对象存储返回的 URL
            modelUpdate.setSize(fileSize);          // 设置文件大小
            modelUpdate.setStatus("Active");
            modelUpdate.setUpdatedTime(Timestamp.valueOf(LocalDateTime.now()));
            modelsMapper.updateById(modelUpdate);

            System.out.println("[ASYNC TASK] Model ID " + modelId + " imported successfully. URL: " + uploadResult.getUrl());

        } catch (Exception e) {
            // =========================================================
            //  导入失败处理
            // =========================================================
            modelUpdate.setStatus("Error");
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error during import.";
            modelUpdate.setUpdatedTime(Timestamp.valueOf(LocalDateTime.now()));
            modelsMapper.updateById(modelUpdate);

            System.err.println("[ASYNC TASK] Import failed for Model ID " + modelId + ": " + e.getMessage());
        } finally {
            // =========================================================
            //  第四步：清理本地临时文件 (Cleanup)
            // =========================================================
            if (tempLocalFile != null && tempLocalFile.exists()) {
                try {
                    Files.deleteIfExists(tempLocalFile.toPath());
                    System.out.println("[ASYNC TASK] Cleaned up temporary file: " + tempLocalFile.getAbsolutePath());
                } catch (Exception cleanupE) {
                    System.err.println("[ASYNC TASK] Failed to clean up temp file: " + cleanupE.getMessage());
                }
            }
        }
    }

    // =========================================================
    //  私有辅助方法
    // =========================================================

    /**
     * 生成对象存储的唯一路径/文件名
     */
    private String generateObjectName(String hubUrl, String originalFileName) {
        // 目标格式：models/hf/openai-whisper-large/model-uuid.extension
        String safeHubName = hubUrl.replace("/", "-").toLowerCase();
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
        }
        return "models/hf/" + safeHubName + "/" + UUID.randomUUID().toString() + extension;
    }

    /**
     * 从 Hub 下载模型到本地临时文件 (这里是模拟实现，请替换为您的实际下载逻辑)
     */
    private File downloadModelFromHub(String hubUrl) throws Exception {
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = hubUrl.replace("/", "_") + "_" + System.currentTimeMillis() + ".pt"; // 模拟模型文件
        File tempFile = new File(tempDir, fileName);

        System.out.println("[ASYNC TASK] Downloading " + hubUrl + " to " + tempFile.getAbsolutePath() + "...");

        // 模拟下载和文件创建 (重要：确保文件不为空)
        if (!tempFile.exists()) {
            tempFile.createNewFile();
            // 写入少量数据模拟文件内容，确保文件大小 > 0
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                // 模拟写入 10MB 的数据
                fos.write(new byte[1024 * 1024 * 10]);
            }
        }

        Thread.sleep(8000); // 模拟耗时下载
        return tempFile;
    }

    /**
     * 将字节数格式化为易读的字符串 (例如: "10.0 MB")
     */
    private String formatSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        // 确保不会越界，虽然通常不会
        if (digitGroups >= units.length) {
            digitGroups = units.length - 1;
        }
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}

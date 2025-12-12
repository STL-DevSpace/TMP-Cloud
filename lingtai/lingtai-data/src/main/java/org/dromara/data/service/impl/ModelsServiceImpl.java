package org.dromara.data.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.data.entity.ModelFiles;
import org.dromara.data.entity.Models;
import org.dromara.data.entity.dto.FileInfoDTO;
import org.dromara.data.entity.dto.ModelsDTO;
import org.dromara.data.mapper.DataMapper;
import org.dromara.data.mapper.FileMapper;
import org.dromara.data.progress.ProgressStore;
import org.dromara.data.service.IModelsService;
import org.dromara.data.utils.CosUtils; // 导入 CosUtils
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;

/**
 * Models 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelsServiceImpl implements IModelsService {


    private final DataMapper dataMapper;
    private final FileMapper fileMapper;
    @Resource
    private final CosUtils cosUtils; // 注入 CosUtils
    @Resource
    private final ProgressStore progressStore;

    // 移除 IHubImportTaskService 相关的注入
    // @Resource
    // private IHubImportTaskService hubImportTaskService;

    @Override
    public List<ModelsDTO> getAllModels() {
        LambdaQueryWrapper<Models> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Models::getUpdatedTime);
        return dataMapper.selectVoList(queryWrapper);
    }

    @Override
    public IPage<ModelsDTO> getModelsPage(Integer pageNum, Integer pageSize, String name, String status) {
        Page<Models> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Models> queryWrapper = new LambdaQueryWrapper<>();
        String loginStr = StpUtil.getLoginId().toString();
        String loginId = loginStr.substring(loginStr.indexOf(":")+1);
        Long userId = Long.valueOf(loginId);
        queryWrapper.eq(Models::getUserId, userId);
        // 条件查询
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like(Models::getName, name);
        }
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq(Models::getStatus, status);
        }

        queryWrapper.orderByDesc(Models::getUpdatedTime);
        return dataMapper.selectVoPage(page, queryWrapper);
    }

    @Override
    public ModelsDTO getModelById(Integer id) {
        if (id == null) {
            log.warn("查询模型失败: ID为空");
            return null;
        }
        return dataMapper.selectVoById(id);
    }

    @Override
    public List<ModelsDTO> getModelsByUserId(Long userId) {
        if (userId == null) {
            log.warn("查询模型失败: 用户ID为空");
            return List.of();
        }
        LambdaQueryWrapper<Models> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Models::getUserId, userId)
            .orderByDesc(Models::getUpdatedTime);
        return dataMapper.selectVoList(queryWrapper);
    }

    @Override
    public List<ModelsDTO> getModelsByProjectId(Integer projectId) {
        if (projectId == null) {
            log.warn("查询模型失败: 项目ID为空");
            return List.of();
        }
        LambdaQueryWrapper<Models> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Models::getProjectId, projectId)
            .orderByDesc(Models::getUpdatedTime);
        return dataMapper.selectVoList(queryWrapper);
    }

// 假设你的 Service 类中已经注入了 FileMapper
// @Autowired
// private FileMapper fileMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelsDTO createModel(ModelsDTO dto) {
        if (dto == null) {
            log.error("创建模型失败: DTO为空");
            return null;
        }

        // 检查模型名称是否已存在
        if (isModelNameExists(dto.getName(), dto.getUserId())) {
            log.warn("创建模型失败: 模型名称已存在 - {}", dto.getName());
            return null;
        }

        // 1. 获取用户ID
        String loginStr = StpUtil.getLoginId().toString();
        String loginId = loginStr.substring(loginStr.indexOf(":")+1);
        Long userId = Long.valueOf(loginId);

        // 2. 准备 Models 主表数据
        Models model = new Models();
        model.setUserId(userId);
        model.setName(dto.getName());
        model.setDescription(dto.getDescription());
        model.setFilePath(dto.getFilePath()); // 文件夹路径
        model.setVersion(dto.getVersion());
        model.setSize(dto.getSize());
        model.setStatus(dto.getStatus() != null ? dto.getStatus() : "Inactive");
        // TODO: 补充设置 type 字段 (ModelsDTO 中有但你没有设置)

        Timestamp now = new Timestamp(System.currentTimeMillis());
        model.setCreatedTime(now);
        model.setUpdatedTime(now);

        // 3. 【关键步骤 A】保存 Models 主表数据
        int result = dataMapper.insert(model);

        if (result > 0 && model.getId() != null) {
            log.info("主表 Models 创建成功: ID={}, Name={}", model.getId(), model.getName());

            // =======================================================
            // 4. 【关键修改区域】处理 ModelFiles 数组
            ModelFiles[] modelFilesArray = dto.getModelFiles();

            if (modelFilesArray != null && modelFilesArray.length > 0) {
                int successCount = 0;
                int  modelId = model.getId(); // 获取新生成的模型ID

                // 循环遍历数组中的每一个 ModelFiles 对象
                for (ModelFiles file : modelFilesArray) {
                    if (file == null) continue;

                    // 设置外键关联ID
                    file.setModelId(modelId);
                    // 设置创建时间
                    file.setCreatedTime(now);

                    // 【可选】设置其他默认值 (isPrimary应该由前端发送，这里作为保险)
                    if (file.getIsPrimary() == null) {
                        // 如果前端没有标记，默认为非主文件 (0)
                        file.setIsPrimary(0);
                    }

                    // 调用 FileMapper 存储单条文件详情
                    if (fileMapper.insert(file) > 0) {
                        successCount++;
                    }
                }

                log.info("子表 ModelFiles 批量存储完成: Model ID={}, 成功插入 {} / {} 条记录",
                    modelId, successCount, modelFilesArray.length);
            }
            // =======================================================

        } else {
            log.error("创建模型失败: 插入 Models 表失败");
            return null;
        }

        // 5. 组装返回的 DTO
        ModelsDTO modelUpdate = new ModelsDTO();
        BeanUtils.copyProperties(model, modelUpdate);
        // 将 ModelFiles 数组复制到返回的 DTO 中
        modelUpdate.setModelFiles(dto.getModelFiles());

        return modelUpdate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateModel(ModelsDTO dto) {
        if (dto == null || dto.getId() == null) {
            log.error("更新模型失败: DTO或ID为空");
            return false;
        }

        Models model = dataMapper.selectById(dto.getId());
        if (model == null) {
            log.warn("更新模型失败: 模型不存在 - ID={}", dto.getId());
            return false;
        }

        // 如果更新模型名称，检查是否与其他模型重复
        if (dto.getName() != null && !dto.getName().equals(model.getName())) {
            if (isModelNameExists(dto.getName(), model.getUserId())) {
                log.warn("更新模型失败: 模型名称已存在 - {}", dto.getName());
                return false;
            }
        }

        if (dto.getName() != null) {
            model.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            model.setDescription(dto.getDescription());
        }
        if (dto.getFilePath() != null) {
            model.setFilePath(dto.getFilePath());
        }
        if (dto.getVersion() != null) {
            model.setVersion(dto.getVersion());
        }
        if (dto.getSize() != null) {
            model.setSize(dto.getSize());
        }
        if (dto.getStatus() != null) {
            model.setStatus(dto.getStatus());
        }


        model.setUpdatedTime(new Timestamp(System.currentTimeMillis()));

        int result = dataMapper.updateById(model);
        log.info("更新模型{}: ID={}", result > 0 ? "成功" : "失败", dto.getId());
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteModel(Integer id) {
        if (id == null) {
            log.error("删除模型失败: ID为空");
            return false;
        }

        int result = dataMapper.deleteById(id);
        log.info("删除模型{}: ID={}", result > 0 ? "成功" : "失败", id);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteModels(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            log.error("批量删除模型失败: ID列表为空");
            return false;
        }

        int result = dataMapper.deleteBatchIds(ids);
        log.info("批量删除模型{}: 删除数量={}", result > 0 ? "成功" : "失败", result);
        return result > 0;
    }

    @Override
    public ModelsStatsDTO getModelStats() {
        LambdaQueryWrapper<Models> queryWrapper = new LambdaQueryWrapper<>();

        // 总数
        Long total = dataMapper.selectCount(queryWrapper);

        // 激活状态的模型数
        LambdaQueryWrapper<Models> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(Models::getStatus, "Active");
        Long active = dataMapper.selectCount(activeWrapper);

        // 非激活状态的模型数
        LambdaQueryWrapper<Models> inactiveWrapper = new LambdaQueryWrapper<>();
        inactiveWrapper.eq(Models::getStatus, "Inactive");
        Long inactive = dataMapper.selectCount(inactiveWrapper);

        // 错误状态的模型数
        LambdaQueryWrapper<Models> errorWrapper = new LambdaQueryWrapper<>();
        errorWrapper.eq(Models::getStatus, "Error");
        Long error = dataMapper.selectCount(errorWrapper);

        // 计算总存储大小
        List<Models> allModels = dataMapper.selectList(queryWrapper);
        Long totalSize = allModels.stream()
            .mapToLong(m -> m.getSize() != null ? m.getSize() : 0L)
            .sum();

        // 版本数量（去重）
        Long versions = allModels.stream()
            .map(Models::getVersion)
            .filter(v -> v != null && !v.isEmpty())
            .distinct()
            .count();

        ModelsStatsDTO stats = new ModelsStatsDTO();
        stats.setTotal(total.intValue());
        stats.setActive(active.intValue());
        stats.setInactive(inactive.intValue());
        stats.setError(error.intValue());
        stats.setVersions(versions.intValue());
        stats.setStorageUsed(totalSize);
        stats.setStorageUsedFormatted(formatFileSize(totalSize));

        return stats;
    }

    @Override
    public ModelsStatsDTO getModelStatsByUserId(Integer userId) {
        if (userId == null) {
            log.warn("查询用户模型统计失败: 用户ID为空");
            return new ModelsStatsDTO();
        }

        LambdaQueryWrapper<Models> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Models::getUserId, userId);

        // 总数
        Long total = dataMapper.selectCount(queryWrapper);

        // 激活状态的模型数
        LambdaQueryWrapper<Models> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(Models::getUserId, userId).eq(Models::getStatus, "Active");
        Long active = dataMapper.selectCount(activeWrapper);

        // 非激活状态的模型数
        LambdaQueryWrapper<Models> inactiveWrapper = new LambdaQueryWrapper<>();
        inactiveWrapper.eq(Models::getUserId, userId).eq(Models::getStatus, "Inactive");
        Long inactive = dataMapper.selectCount(inactiveWrapper);

        // 错误状态的模型数
        LambdaQueryWrapper<Models> errorWrapper = new LambdaQueryWrapper<>();
        errorWrapper.eq(Models::getUserId, userId).eq(Models::getStatus, "Error");
        Long error = dataMapper.selectCount(errorWrapper);

        // 计算总存储大小
        List<Models> userModels = dataMapper.selectList(queryWrapper);
        Long totalSize = userModels.stream()
            .mapToLong(m -> m.getSize() != null ? m.getSize() : 0L)
            .sum();

        // 版本数量（去重）
        Long versions = userModels.stream()
            .map(Models::getVersion)
            .filter(v -> v != null && !v.isEmpty())
            .distinct()
            .count();

        ModelsStatsDTO stats = new ModelsStatsDTO();
        stats.setTotal(total.intValue());
        stats.setActive(active.intValue());
        stats.setInactive(inactive.intValue());
        stats.setError(error.intValue());
        stats.setVersions(versions.intValue());
        stats.setStorageUsed(totalSize);
        stats.setStorageUsedFormatted(formatFileSize(totalSize));

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deployModel(Integer id) {
        if (id == null) {
            log.error("部署模型失败: ID为空");
            return false;
        }

        Models model = dataMapper.selectById(id);
        if (model == null) {
            log.warn("部署模型失败: 模型不存在 - ID={}", id);
            return false;
        }

        model.setStatus("Active");
        model.setUpdatedTime(new Timestamp(System.currentTimeMillis()));

        int result = dataMapper.updateById(model);
        log.info("部署模型{}: ID={}", result > 0 ? "成功" : "失败", id);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deactivateModel(Integer id) {
        if (id == null) {
            log.error("停用模型失败: ID为空");
            return false;
        }

        Models model = dataMapper.selectById(id);
        if (model == null) {
            log.warn("停用模型失败: 模型不存在 - ID={}", id);
            return false;
        }

        model.setStatus("Inactive");
        model.setUpdatedTime(new Timestamp(System.currentTimeMillis()));

        int result = dataMapper.updateById(model);
        log.info("停用模型{}: ID={}", result > 0 ? "成功" : "失败", id);
        return result > 0;
    }

    @Override
    public boolean isModelNameExists(String name, Long userId) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        LambdaQueryWrapper<Models> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Models::getName, name);

        if (userId != null) {
            queryWrapper.eq(Models::getUserId, userId);
        }

        Long count = dataMapper.selectCount(queryWrapper);
        return count != null && count > 0;
    }

    @Override
    public List<ModelsDTO> getModelsByVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            log.warn("查询模型失败: 版本号为空");
            return List.of();
        }

        LambdaQueryWrapper<Models> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Models::getVersion, version)
            .orderByDesc(Models::getUpdatedTime);
        return dataMapper.selectVoList(queryWrapper);
    }

    @Override
    public boolean importModelFromHub(ModelsDTO dto) {
        return false;
    }

    /**
     * 真正执行下载并上传的方法（同步执行），接收外部传入的 taskId 用于上报进度
     * <p>
     * 事务注意：本方法会在出现异常时尝试将 DB 状态设为 Error，
     * 由于是跨 IO（可能分片上传很久），需要谨慎考虑事务边界。
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean importModelFromHubWithProgress(ModelsDTO dto, String taskId) {
        if (dto == null || dto.getHubUrl() == null || dto.getHubUrl().isEmpty()) {
            progressStore.fail(taskId, "Hub URL 为空");
            return false;
        }

        // 1) 创建初始数据库记录（Importing）
        Models model = new Models();
        // 设置登录用户 ID（从 Sa-Token 中解析）
        try {
            String loginStr = StpUtil.getLoginId().toString();
            String loginId = loginStr.contains(":") ? loginStr.substring(loginStr.indexOf(":")+1) : loginStr;
            Long userId = Long.valueOf(loginId);
            model.setUserId(userId);
        } catch (Exception e) {
            log.warn("无法解析登录用户ID，使用 null: {}", e.getMessage());
        }

        model.setName(dto.getName());
        model.setDescription(dto.getDescription());
        model.setVersion(dto.getVersion());
        model.setHubUrl(dto.getHubUrl());
        model.setStatus("Importing");
        model.setCreatedTime(new Timestamp(System.currentTimeMillis()));
        model.setUpdatedTime(new Timestamp(System.currentTimeMillis()));

        int insertResult = dataMapper.insert(model);
        if (insertResult != 1) {
            progressStore.fail(taskId, "创建模型记录失败");
            throw new RuntimeException("创建模型记录失败");
        }
        log.info("创建初始模型记录成功，id={}", model.getId());
        progressStore.updatePercent(taskId, 1, "已创建数据库记录");

        // 2) 准备文件名（安全化）
        String safeName = (dto.getName() == null || dto.getName().trim().isEmpty())
            ? ("model_" + model.getId())
            : dto.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = model.getId() + "_" + safeName;

        HttpURLConnection conn = null;
        InputStream input = null;
        try {
            // 3) 打开 Hub URL 流（支持 http/https）
            URL url = new URL(dto.getHubUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15_000);
            conn.setReadTimeout(120_000);
            conn.setRequestProperty("User-Agent", "EdgeAI-Model-Importer/1.0");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode >= 400) {
                String msg = "下载失败，HTTP 响应码: " + responseCode;
                log.error(msg);
                progressStore.fail(taskId, msg);
                // 将模型标记为 Error
                markModelError(model.getId(), "下载失败 HTTP " + responseCode);
                return false;
            }

            long totalSize = conn.getContentLengthLong();
            if (totalSize <= 0) {
                log.warn("远端未返回 Content-Length（或为0），将按未知大小处理");
                // 若 totalSize <= 0，进度条将以已上传字节数为基准展示（可能无法做百分比）
            }

            input = conn.getInputStream();

            progressStore.updatePercent(taskId, 5, "开始下载并上传到 COS");

            // 4) 定义回调：cosUtils 会在每次分片上传后回调已上传字节数。
            LongConsumer progressCallback = uploadedBytes -> {
                try {
                    int percent;
                    if (totalSize > 0) {
                        percent = (int) Math.min(100, (uploadedBytes * 100.0) / totalSize);
                    } else {
                        // 当 totalSize 未知时，无法精确计算百分比，改为根据字节数做粗略展示（最多到99）
                        long kb = uploadedBytes / 1024;
                        percent = (int) Math.min(99, 1 + kb / 1024); // 每 MB 增 1%
                    }
                    progressStore.updatePercent(taskId, percent, "上传中 " + percent + "%");
                } catch (Exception e) {
                    log.warn("进度回调处理异常", e);
                }
            };

            // 5) 调用 CosUtils 的流式上传（分块上传），传入 InputStream 和 progressCallback
            String cosKey = cosUtils.uploadStreamWithProgress(input, fileName, totalSize, progressCallback);

            // 6) 上传成功，更新 DB
            Models update = new Models();
            update.setId(model.getId());
            update.setFilePath(cosKey);
            update.setSize(totalSize > 0 ? totalSize : null);
            update.setStatus("Active");
            update.setUpdatedTime(new java.sql.Timestamp(System.currentTimeMillis()));
            dataMapper.updateById(update);

            progressStore.success(taskId, "上传完成，模型已激活");
            log.info("任务成功，modelId={}, cosKey={}", model.getId(), cosKey);
            return true;

        } catch (Exception e) {
            log.error("导入任务失败，modelId={}, url={}", model.getId(), dto.getHubUrl(), e);
            progressStore.fail(taskId, "任务失败：" + e.getMessage());

            // 标记 DB 为 Error（不回滚 insert，保持记录用于排查）
            markModelError(model.getId(), e.getMessage());
            return false;

        } finally {
            // 7) 关闭资源
            try {
                if (input != null) input.close();
            } catch (Exception ignored) {}
            if (conn != null) conn.disconnect();
        }
    }
    /**
     * 从 HuggingFace 下载模型文件（支持大文件）。
     * @param hubUrl 如：https://huggingface.co/xxxx/xxx/resolve/main/model.safetensors
     * @return 下载后的本地临时文件 File
     */
    public File downloadModelFromHub(String hubUrl) throws IOException {

        log.info("[IMPORT] 开始下载 HuggingFace 模型: {}", hubUrl);

        // 为下载的模型创建临时文件（自动随机命名）
        String fileName = hubUrl.substring(hubUrl.lastIndexOf("/") + 1);
        File tempFile = File.createTempFile("hf_", "_" + fileName);

        // 重试次数（防止网络抖动）
        int maxRetry = 3;

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            try {
                return doDownload(hubUrl, tempFile);
            } catch (Exception e) {
                log.warn("[IMPORT] 下载失败 attempt={}/{}: {}", attempt, maxRetry, e.getMessage());

                if (attempt == maxRetry) {
                    throw new IOException("下载模型失败（重试次数耗尽）: " + e.getMessage());
                }

                try { Thread.sleep(1000); } catch (Exception ignored) {}
            }
        }

        return tempFile; // 理论不会到这里
    }


    /**
     * 执行一次实际下载
     */
    private File doDownload(String hubUrl, File destFile) throws IOException {

        URL url = new URL(hubUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(true);
        conn.addRequestProperty("User-Agent", "Mozilla/5.0");

        int status = conn.getResponseCode();

        // HuggingFace 有时会 302 跳转到真实原始文件地址
        if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
            status == HttpURLConnection.HTTP_MOVED_PERM ||
            status == HttpURLConnection.HTTP_SEE_OTHER) {

            String newUrl = conn.getHeaderField("Location");
            log.info("[IMPORT] HuggingFace 302 跳转至真实地址: {}", newUrl);
            return doDownload(newUrl, destFile); // 递归处理
        }

        if (status != 200) {
            throw new IOException("下载失败，HTTP 状态码: " + status);
        }

        long contentLength = conn.getContentLengthLong();
        log.info("[IMPORT] 文件大小: {} MB", contentLength / 1024 / 1024);

        try (
            InputStream input = conn.getInputStream();
            FileOutputStream output = new FileOutputStream(String.valueOf(destFile))
        ) {

            byte[] buffer = new byte[1024 * 1024]; // 1MB 缓冲
            int bytesRead;
            long totalRead = 0;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                totalRead += bytesRead;

                // 这里如果你需要更新下载进度，可以添加回调
                // 但现在不需要，因为前端只关心上传进度
            }

            output.flush();
        }

        log.info("[IMPORT] 模型文件已下载到: {}", destFile.getAbsolutePath());

        return destFile;
    }


    @Override
    public String startImportAsync(ModelsDTO dto) {

        // 生成 taskId
        String taskId = "task-" + System.currentTimeMillis();

        // 初始化任务状态
        progressStore.start(taskId, "任务已开始，准备下载模型文件...");

        // 异步执行任务
        CompletableFuture.runAsync(() -> {

            File tempLocalFile = null;

            try {
                // ==========================================================
                // 1. 下载 HuggingFace 模型
                // ==========================================================
                progressStore.updatePercent(taskId, 5, "正在从 HuggingFace 下载模型...");


                // ----------------------------------------------------------
                // 【核心修改】自动构造完整的下载 URL
                // ----------------------------------------------------------
                String hubUrl = dto.getHubUrl();

                if (hubUrl != null && !hubUrl.trim().isEmpty() && !hubUrl.toLowerCase().startsWith("http")) {
                    // 假设约定：主模型文件在 main 分支下，文件名为 model.safetensors
                    String fullDownloadUrl = "https://huggingface.co/" + hubUrl + "/resolve/main/model.safetensors";
                    dto.setHubUrl(fullDownloadUrl); // 将修正后的 URL 设回 DTO
                    log.info("[IMPORT] 自动构造下载 URL: {}", fullDownloadUrl);
                }

                tempLocalFile = downloadModelFromHub(dto.getHubUrl());
                long fileSize = tempLocalFile.length();
                progressStore.updatePercent(taskId, 30, "模型下载完成，准备上传至对象存储...");

                // ==========================================================
                // 2. 上传 COS（带进度）
                // ==========================================================
                String fileName = tempLocalFile.getName();
                InputStream fileStream = new FileInputStream(tempLocalFile);

                progressStore.updatePercent(taskId, 35, "开始上传模型文件至对象存储...");

                String cosKey = cosUtils.uploadStreamWithProgress(
                    fileStream,
                    fileName,
                    fileSize,
                    percent -> {
                        // 强制将结果转换为 int，解决潜在的 long -> int 赋值问题
                        int mapped = 30 + (int) ((percent * 60.0) / 100.0);

                        // 或者，如果您确定 percent 是 int，并且 percent * 60 不会溢出 int，可以使用：
                        // int mapped = 30 + (percent * 60) / 100;

                        progressStore.updatePercent(taskId, mapped, "正在上传模型文件...");
                    }
                );

                String fileUrl = cosUtils.getPublicUrl(cosKey);
                progressStore.updatePercent(taskId, 95, "上传完成，正在写入数据库...");


                // ==========================================================
                // 3. 写入数据库
                // ==========================================================
                Models model = new Models();
                model.setName(dto.getName());
                model.setDescription(dto.getDescription());
                model.setHubUrl(dto.getHubUrl());
                model.setFilePath(fileUrl);
                model.setVersion(dto.getVersion());
                model.setSize(fileSize);
                model.setUserId(dto.getUserId());
                model.setStatus("Active");
                model.setCreatedTime(Timestamp.valueOf(LocalDateTime.now()));
                model.setUpdatedTime(Timestamp.valueOf(LocalDateTime.now()));

                dataMapper.insert(model);


                // ==========================================================
                // 4. 标记成功
                // ==========================================================
                progressStore.success(taskId, "模型导入成功！");

            } catch (Exception e) {

                log.error("[IMPORT] 模型导入失败: {}", e.getMessage(), e);
                progressStore.fail(taskId, "导入失败：" + e.getMessage());

            } finally {

                if (tempLocalFile != null && tempLocalFile.exists()) {
                    try { tempLocalFile.delete(); } catch (Exception ignored) {}
                }
            }

        });

        // 返回 taskId 给前端
        return taskId;
    }

    @Override
    public List<FileInfoDTO> getFileInfo(Integer id) {
        if (id == null) {
            return List.of(); // 返回空列表
        }

        // 1. 构造查询条件
        // LambdaQueryWrapper 提供了类型安全的方式来引用字段 (ModelFiles::getModelId)
        LambdaQueryWrapper<ModelFiles> wrapper = new LambdaQueryWrapper<>();

        // 查询条件：model_id = id
        // 注意：ModelFiles.modelId 是 Long 类型，这里将 Integer 类型的 id 转换为 Long
        wrapper.eq(ModelFiles::getModelId, id.longValue());

        // 2. 执行查询
        // selectList 将返回满足条件的 ModelFiles 实体列表
        List<ModelFiles> fileList = fileMapper.selectList(wrapper);

        // 3. 结果转换 (将 ModelFiles 列表转换为 FileInfoDTO 列表)
        if (fileList == null || fileList.isEmpty()) {
            return List.of();
        }

        List<FileInfoDTO> dtoList = fileList.stream()
            .map(file -> {
                FileInfoDTO dto = new FileInfoDTO();
                // 假设 FileInfoDTO 和 ModelFiles 字段结构相似，使用 BeanUtils 复制属性
                BeanUtils.copyProperties(file, dto);
                return dto;
            })
            .collect(Collectors.toList());

        return dtoList;
    }


    /**
     * 将指定 modelId 的状态标记为 Error，并写入错误信息（更新 updatedTime）
     */
    private void markModelError(Integer modelId, String errMsg) {
        try {
            Models m = new Models();
            m.setId(modelId);
            m.setStatus("Error");
            m.setUpdatedTime(new Timestamp(System.currentTimeMillis()));
            // 若你的 Models 表有字段存错误信息，可以设置，例如 m.setErrorMessage(errMsg);
            dataMapper.updateById(m);
        } catch (Exception e) {
            log.error("标记模型 Error 状态失败，id={}", modelId, e);
        }
    }




    /**
     * 格式化文件大小
     * @param size 文件大小（字节）
     * @return 格式化后的字符串
     */
    private String formatFileSize(Long size) {
        if (size == null || size == 0) {
            return "0 B";
        }

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size.doubleValue();

        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }

        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(fileSize) + " " + units[unitIndex];
    }
}

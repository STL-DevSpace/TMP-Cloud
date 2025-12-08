package org.dromara.data.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.data.entity.Models;
import org.dromara.data.entity.dto.ModelsDTO;
import org.dromara.data.mapper.DataMapper;
import org.dromara.data.service.IHubImportTaskService;
import org.dromara.data.service.IModelsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Models 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelsServiceImpl implements IModelsService {


    private final DataMapper dataMapper;

    @Resource
    private IHubImportTaskService hubImportTaskService;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createModel(ModelsDTO dto) {
        if (dto == null) {
            log.error("创建模型失败: DTO为空");
            return false;
        }

        // 检查模型名称是否已存在
        if (isModelNameExists(dto.getName(), dto.getUserId())) {
            log.warn("创建模型失败: 模型名称已存在 - {}", dto.getName());
            return false;
        }
        String loginStr = StpUtil.getLoginId().toString();
        String loginId = loginStr.substring(loginStr.indexOf(":")+1);
        Long userId = Long.valueOf(loginId);
        Models model = new Models();
        model.setUserId(userId);
        model.setName(dto.getName());
        model.setDescription(dto.getDescription());
        model.setFilePath(dto.getFilePath());
        model.setVersion(dto.getVersion());
        model.setSize(dto.getSize());
        model.setStatus(dto.getStatus() != null ? dto.getStatus() : "Inactive");

        Timestamp now = new Timestamp(System.currentTimeMillis());
        model.setCreatedTime(now);
        model.setUpdatedTime(now);

        int result = dataMapper.insert(model);
        log.info("创建模型{}: ID={}, Name={}", result > 0 ? "成功" : "失败", model.getId(), model.getName());
        return result > 0;
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

    /**
     * 🚀 实现 IModelsService 接口中的 Hub 模型导入方法
     * 职责：1. 在数据库中创建初始模型记录（状态为 'Importing'）。
     * 2. 启动异步下载任务。
     * @param dto 包含 hubUrl, name, description 的 DTO
     * @return 任务是否成功启动
     */
    @Override
    @Transactional // 确保数据库操作是原子的
    public boolean importModelFromHub(ModelsDTO dto) {
        if (dto.getHubUrl() == null || dto.getHubUrl().isEmpty()) {
            return false;
        }

        try {
            // 1. 构造模型实体并设置初始状态
            Models model = BeanUtil.copyProperties(dto, Models.class);

            // 必须设置的关键字段：
            model.setStatus("Importing"); // 初始状态：正在导入中
            model.setHubUrl(dto.getHubUrl()); // 保存 Hub URL
            model.setCreatedTime(new Timestamp(System.currentTimeMillis()));
            model.setUpdatedTime(Timestamp.valueOf(LocalDateTime.now()));
            String loginStr = StpUtil.getLoginId().toString();
            String loginId = loginStr.substring(loginStr.indexOf(":")+1);
            Long userId = Long.valueOf(loginId);
            model.setUserId(userId);
            // 2. 将初始记录存入数据库
            int result = dataMapper.insert(model);
            if (result != 1) {
                // 如果插入失败，抛出异常以回滚事务
                throw new RuntimeException("Failed to create initial model record.");
            }

            // 3. 启动异步导入任务
            // 将刚创建的数据库记录ID和Hub URL传递给后台任务
            hubImportTaskService.startImport(model.getId(), dto.getHubUrl());

            return true;
        } catch (Exception e) {
            // 记录错误日志
            System.err.println("Failed to start Hub import task for " + dto.getHubUrl() + ": " + e.getMessage());
            // 抛出运行时异常，确保 @Transactional 可以回滚（如果数据库插入成功但任务启动失败）
            throw new RuntimeException("模型导入任务启动失败", e);
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

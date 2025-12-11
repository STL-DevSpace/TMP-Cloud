package org.dromara.data.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.linpeilie.Converter;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.data.entity.DataSets;
import org.dromara.data.entity.DataSetsFiles;
import org.dromara.data.entity.ModelFiles;
import org.dromara.data.entity.dto.DataSetFileInfoDTO;
import org.dromara.data.entity.dto.DataSetsDTO;
import org.dromara.data.entity.dto.FileInfoDTO;
import org.dromara.data.mapper.DataSetsFileMapper;
import org.dromara.data.mapper.DataSetsMapper; // 使用您提供的 Mapper
import org.dromara.data.progress.ProgressStore;
import org.dromara.data.service.IDataSetsService;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * DataSets 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSetsServiceImpl implements IDataSetsService {

    private final DataSetsMapper dataSetsMapper;
    private final DataSetsFileMapper fileMapper;
    private final Converter converter;
    @Resource
    private ProgressStore progressStore; // 用于处理异步任务的进度存储

    // =========================== 基础 CRUD ===========================

    @Override
    public List<DataSetsDTO> getAllDataSets() {
        List<DataSets> list = dataSetsMapper.selectList();
        return converter.convert(list, DataSetsDTO.class);
    }

    @Override
    public IPage<DataSetsDTO> pageDataSets(Integer pageNum, Integer pageSize, String name, String status) {
        LambdaQueryWrapper<DataSets> wrapper = new LambdaQueryWrapper<>();

        if (name != null && !name.isEmpty()) {
            wrapper.like(DataSets::getName, name);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(DataSets::getStatus, status);
        }
        // 按照创建时间倒序
        wrapper.orderByDesc(DataSets::getCreatedTime);

        Page<DataSets> page = dataSetsMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return page.convert(e -> converter.convert(e, DataSetsDTO.class));
    }

    @Override
    public DataSetsDTO getDataSetById(Integer id) {
        DataSets dataSet = dataSetsMapper.selectById(id);
        return converter.convert(dataSet, DataSetsDTO.class);
    }

    @Override
    public List<DataSetsDTO> getDataSetsByUserId(Long userId) {
        return List.of();
    }

    @Override
    public List<DataSetsDTO> getDataSetsByProjectId(Integer projectId) {
        return List.of();
    }

    // 省略 getDataSetsByUserId 和 getDataSetsByProjectId 的实现 (与 Models 类似)

    @Override
    @Transactional(rollbackFor = Exception.class) // 建议明确rollbackFor以确保文件插入失败时主表回滚
    public DataSetsDTO createDataSet(DataSetsDTO dto) {
        if (dto == null) {
            log.error("创建数据集失败: DTO为空");
            return null;
        }

        // --- 1. 准备主表数据 ---
        DataSets dataSet = converter.convert(dto, DataSets.class);

        String loginStr = StpUtil.getLoginId().toString();
        String loginId = loginStr.substring(loginStr.indexOf(":")+1);
        Long userId = Long.valueOf(loginId);
        if (userId == null) {
            log.error("创建数据集失败: 无法获取登录用户ID");
            // 抛出异常比返回 null 更能明确错误来源，并触发事务回滚
            throw new RuntimeException("Authentication error: User ID not found.");
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());

        dataSet.setUserId(userId);
        // 设置初始状态和时间
        dataSet.setStatus("pending"); // 保持原有逻辑
        dataSet.setCreatedTime(now);
        dataSet.setUpdatedTime(now);

        // 假设您还需要进行名称校验，但此处未提供 isDataSetNameExists 方法
        // if (isDataSetNameExists(dto.getName(), userId)) { ... }

        // --- 2. 插入 DataSets 主表 ---
        int result = dataSetsMapper.insert(dataSet);

        if (result <= 0 || dataSet.getId() == null) {
            log.error("创建数据集失败: 插入 DataSets 表失败");
            return null;
        }

        int dataSetId = dataSet.getId();
        log.info("主表 DataSets 创建成功: ID={}, Name={}", dataSetId, dataSet.getName());

        // =======================================================
        // --- 3. 【新增功能】处理并批量插入 DataSetsFiles 子表 ---

        // 假设 DTO 中包含文件信息的列表/数组
        // 需要根据您的 DataSetsDTO 结构调整这一行，这里假设有一个 getFiles() 方法返回 DTO 列表
        List<DataSetFileInfoDTO> fileInfoDTOList = dto.getFiles();

        List<DataSetsFiles> filesToInsert = new ArrayList<>();

        if (fileInfoDTOList != null && !fileInfoDTOList.isEmpty()) {

            // 循环将 DTO 转换为实体，并设置外键
            for (DataSetFileInfoDTO fileDTO : fileInfoDTOList) {
                if (fileDTO == null) continue;

                // 使用 AutoMapper 或手动转换 DTO -> 实体
                // 注意: ModelFiles.class 应改为 DataSetsFiles.class
                DataSetsFiles fileEntity = converter.convert(fileDTO, DataSetsFiles.class);

                // 设置外键关联ID (datasetId)
                fileEntity.setDataSetId(dataSetId);
                // 设置创建时间
                fileEntity.setCreatedTime(now);

                // 【可选】设置其他默认值 (isPrimary)
                if (fileEntity.getIsPrimary() == null) {
                    fileEntity.setIsPrimary(0);
                }

                filesToInsert.add(fileEntity);
            }

            // 执行批量插入
            if (!filesToInsert.isEmpty()) {
                // 假设 dataSetsFilesMapper 有一个 insertBatch 方法，返回 boolean
                boolean success = fileMapper.insertBatch(filesToInsert);

                if (success) {
                    // 如果返回 true，通常意味着批量操作在数据库层面执行成功。
                    log.info("子表 DataSetsFiles 批量存储成功: DataSet ID={}, 尝试插入 {} 条记录",
                        dataSetId, filesToInsert.size());
                } else {
                    // 如果返回 false，但没有抛出异常，这表明操作失败。
                    // 因为方法有 @Transactional，理论上返回 false 应该被视为失败并导致回滚。
                    // 但为保险起见，可以在这里抛出异常确保回滚。
                    log.error("子表 DataSetsFiles 批量存储失败: DataSet ID={}, 尝试插入 {} 条记录",
                        dataSetId, filesToInsert.size());
                    // 确保事务回滚：抛出 RuntimeException
                    throw new RuntimeException("Failed to batch insert DataSetsFiles.");
                }
            }
        }
        // =======================================================

        // --- 4. 组装返回的 DTO ---
        DataSetsDTO dataSetUpdate = converter.convert(dataSet, DataSetsDTO.class);

        // 将处理后（带有外键和时间）的文件列表设置到返回的 DTO 中
        // 需要将 DataSetsFiles 实体列表转换回 DataSetFileInfoDTO 列表
        List<DataSetFileInfoDTO> insertedFileDTOs = converter.convert(filesToInsert, DataSetFileInfoDTO.class);
        // 假设 DataSetsDTO 有 setFiles(List<DataSetFileInfoDTO>) 方法
        dataSetUpdate.setFiles(insertedFileDTOs);

        return dataSetUpdate;
    }

    @Override
    @Transactional
    public boolean updateDataSet(DataSetsDTO dto) {
        DataSets dataSet = converter.convert(dto, DataSets.class);
        // 确保只更新非空字段
        return dataSetsMapper.updateById(dataSet) > 0;
    }

    @Override
    @Transactional
    public boolean deleteDataSets(List<Integer> ids) {
        // 实际应用中需要增加权限和状态校验
        return dataSetsMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public boolean isDataSetNameExists(String name, Long userId) {
        return dataSetsMapper.exists(new LambdaQueryWrapper<DataSets>()
            .eq(DataSets::getName, name)
            .eq(DataSets::getUserId, userId));
    }

    // =========================== 业务逻辑：Hub 导入 ===========================

    @Override
    public String startImportAsync(DataSetsDTO dto) {
        String taskId = UUID.randomUUID().toString();

        // 1. 在数据库中创建数据集记录，状态设置为 'importing'
        dto.setStatus("importing");
        DataSetsDTO newDataSet = createDataSet(dto);
        Integer newDataSetId = newDataSet.getId();

        //TODO import from hub

        // 3. 异步执行导入操作
        CompletableFuture.runAsync(() -> {
            // 确保 DTO 中包含新创建的 ID
            newDataSet.setHubUrl(dto.getHubUrl());
            newDataSet.setSource("hub_import");

            boolean success = importDataSetFromHubWithProgress(newDataSet, taskId);

            if (success) {
                // 导入成功，更新最终状态
                updateDataSetStatus(newDataSetId, "ready", BigDecimal.valueOf(100.00));
            } else {
                // 导入失败，更新错误状态
                markDataSetError(newDataSetId, "Import failed during processing.");
            }
        });

        return taskId;
    }

    @Async
    @Override
    @Transactional(noRollbackFor = Exception.class) // 导入失败不回滚，只更新状态为 Error
    public boolean importDataSetFromHubWithProgress(DataSetsDTO dto, String taskId) {
        //TODO 从 Hub 导入数据集
        log.info("开始导入数据集，ID: {}, Hub URL: {}", dto.getId(), dto.getHubUrl());

        try {
            // 模拟实际的导入过程，例如下载文件、解压、解析、写入元数据等
            // =======================================================
            Thread.sleep(1000); // 模拟准备工作

            // 假设导入分为 5 个阶段，每个阶段更新一次进度
            for (int i = 1; i <= 5; i++) {
                // 模拟耗时操作
                Thread.sleep(2000);

                int percentage = i * 20;
                String message = "Processing step " + i + "/5";


                // 实时更新数据库进度 (可选，但通常推荐)
                updateDataSetProgress(dto.getId(), BigDecimal.valueOf(percentage));
            }
            // =======================================================

            // 假设导入成功
            log.info("数据集导入完成，ID: {}", dto.getId());
            return true;

        } catch (Exception e) {
            log.error("数据集导入失败，ID: {}", dto.getId(), e);
            markDataSetError(dto.getId(), e.getMessage());
            return false;
        } finally {
            progressStore.remove(taskId); // 无论成功失败，任务完成后移除进度记录
        }
    }

    @Override
    public List<FileInfoDTO> getFileInfo(Integer id) {
        if (id == null) {
            return List.of(); // 返回空列表
        }

        // 1. 构造查询条件
        // LambdaQueryWrapper 提供了类型安全的方式来引用字段 (ModelFiles::getModelId)
        LambdaQueryWrapper<DataSetsFiles> wrapper = new LambdaQueryWrapper<>();

        // 查询条件：model_id = id
        // 注意：ModelFiles.modelId 是 Long 类型，这里将 Integer 类型的 id 转换为 Long
        wrapper.eq(DataSetsFiles::getDataSetId, id.longValue());

        // 2. 执行查询
        // selectList 将返回满足条件的 ModelFiles 实体列表
        List<DataSetsFiles> fileList = fileMapper.selectList(wrapper);

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

    // =========================== 辅助私有方法 ===========================

    /**
     * 更新数据集状态和进度
     */
    private void updateDataSetStatus(Integer dataSetId, String status, BigDecimal progress) {
        DataSets ds = new DataSets();
        ds.setId(dataSetId);
        ds.setStatus(status);
        ds.setProgress(progress);
        ds.setUpdatedTime(new Timestamp(System.currentTimeMillis()));
        dataSetsMapper.updateById(ds);
    }

    /**
     * 更新数据集进度
     */
    private void updateDataSetProgress(Integer dataSetId, BigDecimal progress) {
        DataSets ds = new DataSets();
        ds.setId(dataSetId);
        ds.setProgress(progress);
        ds.setUpdatedTime(new Timestamp(System.currentTimeMillis()));
        dataSetsMapper.updateById(ds);
    }

    /**
     * 将指定 dataSetId 的状态标记为 Error，并写入错误信息
     */
    private void markDataSetError(Integer dataSetId, String errMsg) {
        try {
            DataSets ds = new DataSets();
            ds.setId(dataSetId);
            ds.setStatus("error");
            ds.setUpdatedTime(new Timestamp(System.currentTimeMillis()));
            // 如果 DataSets 表有 error_message 字段，可以在此处设置
            dataSetsMapper.updateById(ds);
            log.warn("数据集已标记为 Error 状态，ID={}，错误信息: {}", dataSetId, errMsg);
        } catch (Exception e) {
            log.error("标记数据集 Error 状态失败，id={}", dataSetId, e);
        }
    }

    // =========================== 统计信息 ===========================

    @Override
    public DataSetsStatsDTO getDataSetStatsByUserId(Long userId) {
        // 实际实现应使用 dataSetsMapper 进行 COUNT() 和 GROUP BY status 的聚合查询
        // 这里仅返回一个模拟结果
        DataSetsStatsDTO stats = new DataSetsStatsDTO();
        stats.setTotal(10);
        stats.setReady(8);
        stats.setProcessing(1);
        stats.setError(1);
        return stats;
    }
}

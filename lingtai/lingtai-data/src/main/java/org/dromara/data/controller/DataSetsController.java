package org.dromara.data.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.data.entity.DataSets;
import org.dromara.data.entity.DataSetsFiles;
import org.dromara.data.entity.dto.DataSetsDTO;
import org.dromara.data.entity.dto.FileInfoDTO;
import org.dromara.data.progress.ProgressStore;
import org.dromara.data.service.IDataSetsService; // 假设对应的数据集服务接口
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DataSets 控制器
 * 负责处理数据集相关的 HTTP 请求
 */
@RestController
@RequestMapping("/api/edgeai/datasets") // 对应前端 DataSet.vue 中可能调用的 /api/edgeai/datasets
@RequiredArgsConstructor
public class DataSetsController {

    private final IDataSetsService dataSetsService;
    @Resource
    private ProgressStore progressStore; // 用于处理异步任务的进度存储

    /**
     * 查询所有数据集
     * @return 数据集列表
     */
    @GetMapping("/list")
    public R<List<DataSetsDTO>> getAllDataSets() {
        // 通常会添加用户ID或项目ID的过滤
        List<DataSetsDTO> datasets = dataSetsService.getAllDataSets();
        return R.ok(datasets);
    }

    /**
     * 分页查询数据集
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 数据集名称（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<IPage<DataSetsDTO>> pageDataSets(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String dataType
    ) {
        IPage<DataSetsDTO> page = dataSetsService.pageDataSets(pageNum, pageSize, name, status, dataType);
        return R.ok(page);
    }

    /**
     * 根据ID获取数据集详情
     * @param id 数据集ID
     * @return 数据集详情
     */
    @GetMapping("/{id}")
    public R<DataSetsDTO> getDataSetById(@PathVariable Integer id) {
        DataSetsDTO dataset = dataSetsService.getDataSetById(id);
        if (dataset == null) {
            return R.fail("数据集不存在");
        }
        return R.ok(dataset);
    }

    /**
     * 新增数据集 (通常用于元数据创建，实际文件上传可能在其他接口)
     * @param dto 数据集 DTO
     * @return 结果
     */
    @PostMapping("/create")
    public R<DataSetsDTO> addDataSet(@Validated @RequestBody DataSetsDTO dto) {
        DataSetsDTO dataset = dataSetsService.createDataSet(dto);
        if (dataset != null) {
            return R.ok(dataset);
        }
        return R.fail("数据集创建失败");
    }

    /**
     * 修改数据集信息
     * @param dto 数据集 DTO
     * @return 结果
     */
    @PutMapping("/update")
    public R<Void> updateDataSet(@Validated @RequestBody DataSetsDTO dto) {
        // 权限校验或数据校验
        boolean success = dataSetsService.updateDataSet(dto);
        if (success) {
            return R.ok("数据集更新成功");
        }
        return R.fail("数据集更新失败");
    }

    /**
     * 批量删除数据集
     * @param ids ID列表
     * @return 结果
     */
    @DeleteMapping("/delete")
    public R<Void> deleteDataSets(@RequestBody List<Integer> ids) {
        boolean success = dataSetsService.deleteDataSets(ids);
        if (success) {
            return R.ok("数据集删除成功");
        }
        return R.fail("数据集删除失败");
    }

    /**
     * 从 Hub 导入数据集 (对应 DataSet.vue 的导入功能)
     * @param dto 包含 hubUrl, name, description 等信息的 DTO
     * @return 异步任务ID
     */
    @PostMapping("/import")
    public R<String> importDataSetFromHub(@Validated @RequestBody DataSetsDTO dto) {

        if (dto.getHubUrl() == null || dto.getHubUrl().isEmpty()) {
            return R.fail("Hub URL 不能为空");
        }

        // 异步启动导入任务
        String taskId = dataSetsService.startImportAsync(dto);

        return R.ok(taskId);
    }

    /**
     * 查询导入/处理进度
     * @param taskId 任务ID
     * @return 完整 Progress 对象
     */
    @GetMapping("/import/progress/{taskId}")
    public R<ProgressStore.Progress> getImportProgress(@PathVariable String taskId) {
        ProgressStore.Progress progress = progressStore.get(taskId);
        if (progress == null) {
            return R.fail("任务不存在或已完成");
        }
        return R.ok(progress);
    }
    /**
     * 用模型id获取对应文件信息
     */
    @GetMapping("/file/{id}")
    public R<List<FileInfoDTO>> getFileInfo(@PathVariable Integer id) {
        List<FileInfoDTO> fileInfo = dataSetsService.getFileInfo(id);
        if (fileInfo == null) {
            return R.fail("模型不存在");
        }
        return R.ok(fileInfo);
    }

    // 可以根据需要添加其他功能，如：
    // @PostMapping("/{id}/label") 用于数据集标注
    // @GetMapping("/{id}/files") 用于获取数据集文件列表
}

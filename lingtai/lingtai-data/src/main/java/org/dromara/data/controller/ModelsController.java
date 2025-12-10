package org.dromara.data.controller;



import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;

import org.dromara.data.entity.Models;
import org.dromara.data.entity.dto.FileInfoDTO;
import org.dromara.data.entity.dto.ModelsDTO;
import org.dromara.data.progress.ProgressStore;
import org.dromara.data.service.IModelsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Models 控制器
 */
@RestController
@RequestMapping("/api/edgeai/models")
@RequiredArgsConstructor
public class ModelsController {

    private final IModelsService modelsService;
    @Resource
    private ProgressStore progressStore;

    /**
     * 查询所有模型
     * @return 模型列表
     */
    @GetMapping("/list")
    public R<List<ModelsDTO>> getAllModels() {
        List<ModelsDTO> models = modelsService.getAllModels();
        return R.ok(models);
    }

    /**
     * 分页查询模型
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 模型名称（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<IPage<ModelsDTO>> getModelsPage(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String status) {
        IPage<ModelsDTO> page = modelsService.getModelsPage(pageNum, pageSize, name, status);
        return R.ok(page);
    }

    /**
     * 根据ID查询模型
     * @param id 模型ID
     * @return 模型信息
     */
    @GetMapping("/{id}")
    public R<ModelsDTO> getModelById(@PathVariable Integer id) {
        ModelsDTO model = modelsService.getModelById(id);
        if (model == null) {
            return R.fail("模型不存在");
        }
        return R.ok(model);
    }

    /**
     * 根据用户ID查询模型列表
     * @return 模型列表
     */
    @GetMapping("/user")
    public R<List<ModelsDTO>> getModelsByUserId() {
        String loginStr = StpUtil.getLoginId().toString();
        String loginId = loginStr.substring(loginStr.indexOf(":")+1);
        Long userId = Long.valueOf(loginId);
        List<ModelsDTO> models = modelsService.getModelsByUserId(userId);
        return R.ok(models);
    }

    /**
     * 创建模型
     * @param dto 模型DTO
     * @return 创建结果
     */
    @PostMapping
    public R<ModelsDTO> createModel(@Validated @RequestBody ModelsDTO dto) {
        ModelsDTO model = modelsService.createModel(dto);
        if (model != null) {
            return R.ok(model);
        }
        return R.fail("模型创建失败");
    }

    /**
     * 更新模型信息
     * @param id 模型ID
     * @param dto 模型DTO
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public R<Void> updateModel(@PathVariable Integer id, @Validated @RequestBody ModelsDTO dto) {
        dto.setId(id);
        boolean success = modelsService.updateModel(dto);
        if (success) {
            return R.ok("模型更新成功");
        }
        return R.fail("模型更新失败或模型不存在");
    }

    /**
     * 删除模型
     * @param id 模型ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteModel(@PathVariable Integer id) {
        boolean success = modelsService.deleteModel(id);
        if (success) {
            return R.ok("模型删除成功");
        }
        return R.fail("模型删除失败或模型不存在");
    }

    /**
     * 批量删除模型
     * @param ids 模型ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public R<Void> deleteModels(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return R.fail("请提供要删除的模型ID");
        }
        boolean success = modelsService.deleteModels(ids);
        if (success) {
            return R.ok("模型批量删除成功");
        }
        return R.fail("模型批量删除失败");
    }
    @PostMapping("/import")
    public R<String> importModelFromHub(@Validated @RequestBody ModelsDTO dto) {

        if (dto.getHubUrl() == null || dto.getHubUrl().isEmpty()) {
            return R.fail("Hub URL 不能为空");
        }

        String taskId = modelsService.startImportAsync(dto);

        return R.ok(taskId);
    }

    /**
     * 查询导入进度（返回完整 Progress 对象）
     */
    @GetMapping("/import/progress/{taskId}")
    public R<ProgressStore.Progress> getImportProgress(@PathVariable String taskId) {
        ProgressStore.Progress progress = progressStore.get(taskId);
        if (progress == null) {
            return R.fail("任务不存在");
        }
        return R.ok(progress);
    }



    /**
     * 部署模型（更新状态为Active）
     * @param id 模型ID
     * @return 部署结果
     */
    @PostMapping("/{id}/deploy")
    public R<Void> deployModel(@PathVariable Integer id) {
        ModelsDTO dto = new ModelsDTO();
        dto.setId(id);
        dto.setStatus("Active");
        boolean success = modelsService.updateModel(dto);
        if (success) {
            return R.ok("模型部署成功");
        }
        return R.fail("模型部署失败或模型不存在");
    }
    /**
     * 用模型id获取对应文件信息
     */
    @GetMapping("/file/{id}")
    public R<List<FileInfoDTO>> getFileInfo(@PathVariable Integer id) {
        List<FileInfoDTO> fileInfo = modelsService.getFileInfo(id);
        if (fileInfo == null) {
            return R.fail("模型不存在");
        }
        return R.ok(fileInfo);
    }
}

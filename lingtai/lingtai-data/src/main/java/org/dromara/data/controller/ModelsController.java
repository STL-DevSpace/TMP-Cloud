package org.dromara.data.controller;



import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;

import org.dromara.data.entity.dto.ModelsDTO;
import org.dromara.data.service.IModelsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Models 控制器
 */
@RestController
@RequestMapping("/api/edgeai/models")
@RequiredArgsConstructor
public class ModelsController {

    private final IModelsService modelsService;

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
    public R<Void> createModel(@Validated @RequestBody ModelsDTO dto) {
        boolean success = modelsService.createModel(dto);
        if (success) {
            return R.ok("模型创建成功");
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
    /**
     * 从 HuggingFace Hub 导入模型（启动异步导入任务）
     * @param dto 包含 Hub URL、本地名称和描述的 DTO。
     * (假设 ModelsDTO 中已包含 hubUrl, name, description 字段)
     * @return 导入任务启动结果
     */
    @PostMapping("/import")
    public R<Void> importModelFromHub(@Validated @RequestBody ModelsDTO dto) {
        // 检查关键的 Hub 标识符（假设 ModelsDTO 中有一个 getHubUrl() 方法）
        // 如果您的 DTO 中没有 hubUrl 字段，请根据实际字段名进行调整，例如 getSourceUrl()
        if (dto.getHubUrl() == null || dto.getHubUrl().isEmpty()) {
            return R.fail("Hub ID (URL) 不能为空");
        }

        // 核心逻辑：调用 Service 层启动异步导入任务
        // 假设 modelsService 中有一个 importModelFromHub 方法
        boolean success = modelsService.importModelFromHub(dto);

        if (success) {
            // 返回 200 OK，表示导入任务已成功排队或启动
            return R.ok("模型导入任务已成功启动");
        }
        // 如果 Service 层返回 false，表示启动失败
        return R.fail("模型导入任务启动失败，请检查 Hub ID 或系统配置");
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
}

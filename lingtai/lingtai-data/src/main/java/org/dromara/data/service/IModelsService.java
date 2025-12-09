package org.dromara.data.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import org.dromara.data.entity.dto.FileInfoDTO;
import org.dromara.data.entity.dto.ModelsDTO;


import java.util.List;

/**
 * Models 服务接口
 */
public interface IModelsService {

    /**
     * 查询所有模型
     * @return 模型列表
     */
    List<ModelsDTO> getAllModels();

    /**
     * 分页查询模型
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 模型名称（可选，模糊查询）
     * @param status 状态（可选）
     * @return 分页结果
     */
    IPage<ModelsDTO> getModelsPage(Integer pageNum, Integer pageSize, String name, String status);

    /**
     * 根据ID查询模型
     * @param id 模型ID
     * @return 模型信息
     */
    ModelsDTO getModelById(Integer id);

    /**
     * 根据用户ID查询模型列表
     * @param userId 用户ID
     * @return 模型列表
     */
    List<ModelsDTO> getModelsByUserId(Long userId);

    /**
     * 根据项目ID查询模型列表
     * @param projectId 项目ID
     * @return 模型列表
     */
    List<ModelsDTO> getModelsByProjectId(Integer projectId);

    /**
     * 创建模型
     * @param dto 模型DTO
     * @return 创建结果
     */
    ModelsDTO createModel(ModelsDTO dto);

    /**
     * 更新模型信息
     * @param dto 模型DTO
     * @return 更新结果
     */
    boolean updateModel(ModelsDTO dto);

    /**
     * 删除模型
     * @param id 模型ID
     * @return 删除结果
     */
    boolean deleteModel(Integer id);

    /**
     * 批量删除模型
     * @param ids 模型ID列表
     * @return 删除结果
     */
    boolean deleteModels(List<Integer> ids);

    /**
     * 获取模型统计信息
     * @return 统计信息
     */
    ModelsStatsDTO getModelStats();

    /**
     * 根据用户ID获取模型统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    ModelsStatsDTO getModelStatsByUserId(Integer userId);

    /**
     * 部署模型（更新状态为Active）
     * @param id 模型ID
     * @return 部署结果
     */
    boolean deployModel(Integer id);

    /**
     * 停用模型（更新状态为Inactive）
     * @param id 模型ID
     * @return 停用结果
     */
    boolean deactivateModel(Integer id);

    /**
     * 检查模型名称是否已存在
     * @param name 模型名称
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean isModelNameExists(String name, Long userId);

    /**
     * 根据版本号查询模型列表
     * @param version 版本号
     * @return 模型列表
     */
    List<ModelsDTO> getModelsByVersion(String version);

    boolean importModelFromHub(ModelsDTO dto);

    boolean importModelFromHubWithProgress(ModelsDTO dto, String taskId);

    String startImportAsync(ModelsDTO dto);

    List<FileInfoDTO> getFileInfo(Integer id);

    /**
     * 模型统计信息DTO
     */
    @lombok.Data
    class ModelsStatsDTO {
        /**
         * 总数量
         */
        private Integer total;

        /**
         * 激活状态的模型数
         */
        private Integer active;

        /**
         * 非激活状态的模型数
         */
        private Integer inactive;

        /**
         * 错误状态的模型数
         */
        private Integer error;

        /**
         * 版本数量
         */
        private Integer versions;

        /**
         * 总存储大小（字节）
         */
        private Long storageUsed;

        /**
         * 总存储大小（人类可读格式）
         */
        private String storageUsedFormatted;
    }
}

package org.dromara.data.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.dromara.data.entity.dto.DataSetsDTO;
import org.dromara.data.entity.dto.FileInfoDTO;

import java.util.List;

/**
 * DataSets 服务接口
 * 模仿 IModelsService.java 结构设计
 */
public interface IDataSetsService {

    /**
     * 查询所有数据集
     * @return 数据集列表
     */
    List<DataSetsDTO> getAllDataSets();

    /**
     * 分页查询数据集
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 数据集名称（可选，模糊查询）
     * @param status 状态（可选）
     * @return 分页结果
     */
    IPage<DataSetsDTO> pageDataSets(Integer pageNum, Integer pageSize, String name, String status, String dataType);

    /**
     * 根据ID查询数据集
     * @param id 数据集ID
     * @return 数据集信息
     */
    DataSetsDTO getDataSetById(Integer id);

    /**
     * 根据用户ID查询数据集列表
     * @param userId 用户ID
     * @return 数据集列表
     */
    List<DataSetsDTO> getDataSetsByUserId(Long userId);

    /**
     * 根据项目ID查询数据集列表
     * @param projectId 项目ID
     * @return 数据集列表
     */
    List<DataSetsDTO> getDataSetsByProjectId(Integer projectId);

    /**
     * 创建数据集
     * @param dto 数据集 DTO
     * @return 创建结果（返回带ID的数据集DTO）
     */
    DataSetsDTO createDataSet(DataSetsDTO dto);

    /**
     * 更新数据集信息
     * @param dto 数据集DTO
     * @return 更新结果
     */
    boolean updateDataSet(DataSetsDTO dto);

    /**
     * 批量删除数据集
     * @param ids ID列表
     * @return 删除结果
     */
    boolean deleteDataSets(List<Integer> ids);

    /**
     * 检查数据集名称是否已存在
     * @param name 数据集名称
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean isDataSetNameExists(String name, Long userId);

    /**
     * 从 Hub 异步开始导入数据集
     * @param dto 包含 Hub URL 的 DTO
     * @return 异步任务ID
     */
    String startImportAsync(DataSetsDTO dto);

    /**
     * 执行 Hub 导入的实际逻辑（通常在异步线程中调用）
     * @param dto 数据集 DTO
     * @param taskId 任务ID
     * @return 导入结果
     */
    boolean importDataSetFromHubWithProgress(DataSetsDTO dto, String taskId);

    List<FileInfoDTO> getFileInfo(Integer id);

    /**
     * 数据集统计信息DTO
     */
    @lombok.Data
    class DataSetsStatsDTO {
        /**
         * 总数量
         */
        private Integer total;

        /**
         * Ready 状态的数据集数
         */
        private Integer ready;

        /**
         * Processing 状态的数据集数
         */
        private Integer processing;

        /**
         * Error 状态的数据集数
         */
        private Integer error;
    }

    /**
     * 根据用户ID获取数据集统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    DataSetsStatsDTO getDataSetStatsByUserId(Long userId);
}

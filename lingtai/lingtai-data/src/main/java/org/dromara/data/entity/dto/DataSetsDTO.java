package org.dromara.data.entity.dto;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.data.entity.DataSets; // 引入刚刚创建的数据集实体类
import org.dromara.data.entity.DataSetsFiles;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * DataSets DTO
 * 用于数据集数据传输，包含创建和查询所需的字段
 */
@Data
@AutoMapper(target = DataSets.class) // 关联到 DataSets 实体类
public class DataSetsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 数据集名称
     */
    private String name;

    /**
     * 数据集描述
     */
    private String description;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 版本号
     */
    private String version;

    /**
     * 文件总大小（字节）
     */
    private Long size;

    /**
     * 数据类型：例如 Image, Text, Audio, Other (对应 DataSet.vue 的 typeFilter)
     */
    private String dataType;

    /**
     * 数据集中包含的文件或样本总数
     */
    private Integer fileCount;

    /**
     * 数据集的状态：例如 processing, ready, error
     */
    private String status;

    /**
     * 数据集来源：例如 local_upload, hub_import
     */
    private String source;

    /**
     * HuggingFace Hub url 或其他来源仓库地址
     */
    private String hubUrl;

    /**
     * 创建时间
     */
    private Timestamp createdTime;

    /**
     * 更新时间
     */
    private Timestamp updatedTime;

    /**
     * 模型文件数组
     */
    private List<DataSetFileInfoDTO> files;
    // 注意：
    // 1. 移除了 ModelsDTO 中包含的模型文件数组（ModelFiles[]），因为在 DTO 层面，我们通常只传输顶层信息。
    // 2. 移除了 ModelsDTO 中没有的 progress, labelConfig 字段，遵循 ModelsDTO 简洁的原则。

}

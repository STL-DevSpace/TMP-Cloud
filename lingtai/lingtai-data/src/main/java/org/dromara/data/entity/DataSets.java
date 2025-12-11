package org.dromara.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 数据集实体类 (对应数据库表 datasets)
 * 模仿 Models.java 结构设计
 */
@Data
@TableName("datasets") // 对应数据库中的 datasets 表
@Accessors(chain = true)
public class DataSets implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long userId;

    private Integer projectId;

    private String name;

    private String description;

    private String filePath;

    private String version;

    private Long size; // 数据集总大小

    // 以下是数据集特有的字段

    /**
     * 数据类型：例如 Image, Text, Audio, Other
     */
    private String dataType;

    /**
     * 数据集中包含的文件或样本总数
     */
    private Integer fileCount;

    /**
     * 数据集的标签、类别或标注格式配置 (JSON 字段)
     */
    private String labelConfig;

    private String status;

    private BigDecimal progress;

    /**
     * 导入来源：例如 local_upload, hub_import
     */
    private String source;

    // 以下字段保持与 Models.java 一致，用于追踪来源和时间

    private String hubUrl; // 数据集仓库地址，例如 HuggingFace Hub URL

    private Timestamp createdTime;

    private Timestamp updatedTime;

    // 移除了 Models 中的 loss 和 accuracy 字段
}

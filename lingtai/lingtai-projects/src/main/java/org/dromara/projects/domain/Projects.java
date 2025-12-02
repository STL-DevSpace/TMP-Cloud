package org.dromara.projects.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("projects")
public class Projects {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //  用户
    private Long userId;
    // 项目名称
    private String name;
    // 描述
    private String description;
    // 训练算法
    private String trainingAlg;
    // 联邦算法
    private String fedAlg;
    // 私密聚合
    private Boolean secureAggregation;
    // 总轮数
    private Integer totalEpochs;
    // 轮数
    private Integer numRounds;
    // 批次
    private Integer batchSize;
    // 学习率
    private BigDecimal lr;
    // 机器数
    private Integer numComputers;
    // 阈值
    private Integer threshold;
    // 客户数
    private Integer numClients;
    // 样本客户数
    private Integer sampleClients;
    // 迭代
    private Integer maxSteps;
    // 模型
    private String modelNameOrPath;
    // 数据集
    private String datasetName;
    // 样本
    private String datasetSample;
    // 状态
    private String status;
    // 进度
    private BigDecimal progress;
    private String taskId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}

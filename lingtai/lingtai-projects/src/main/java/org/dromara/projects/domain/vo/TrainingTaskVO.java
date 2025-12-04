package org.dromara.projects.domain.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.projects.domain.TrainingTask;
import org.dromara.projects.enums.TrainingTaskStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 训练任务视图对象
 */
@Data
@AutoMapper(target = TrainingTask.class)
public class TrainingTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    // 任务名称
    private String name;
    // 描述
    private String description;
    // 训练算法
    private String trainingAlg;
    // 联邦算法
    private String fedAlg;
    // 训练状态：PENDING, RUNNING, PAUSED, STOPPED, COMPLETED, FAILED
    private TrainingTaskStatus status;
    // 进度
    private BigDecimal progress;
    /**
     * 最终损失值
     */
    private Float finalLoss;
    /**
     * 日志路径
     */
    private String logPath;
    /**
     * 当前损失值
     */
    private Float currentLoss;
    // 私密聚合
    private Boolean secureAggregation;
    // 总轮数
    private Integer totalEpochs;
    /**
     * 已完成的轮数
     */
    private Integer rounds;
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
    // 步长
    private Integer maxSteps;
    // 模型
    private String modelNameOrPath;
    // 数据集
    private String datasetName;
    // 样本
    private String datasetSample;
    // 备注
    private String remark;

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}

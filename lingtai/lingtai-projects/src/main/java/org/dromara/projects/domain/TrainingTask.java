package org.dromara.projects.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 训练任务对象 training_task
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("training_task")
public class TrainingTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 训练轮数
     */
    private Integer epochs;

    /**
     * 批次大小
     */
    private Integer batchSize;

    /**
     * 学习率
     */
    private Float lr;

    /**
     * 最终损失值
     */
    private Float finalLoss;

    /**
     * 训练状态：PENDING, RUNNING, PAUSED, STOPPED, COMPLETED, FAILED
     */
    private String status;

    /**
     * 已完成的轮数
     */
    private Integer rounds;

    /**
     * 日志路径
     */
    private String logPath;

    /**
     * 当前进度 (0-100)
     */
    private Float progress;

    /**
     * 当前损失值
     */
    private Float currentLoss;

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

    /**
     * 备注
     */
    private String remark;
}

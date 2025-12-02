package org.dromara.projects.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 训练任务视图对象
 */
@Data
public class TrainingTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
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
     * 训练状态
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
     * 当前进度
     */
    private Float progress;

    /**
     * 当前损失值
     */
    private Float currentLoss;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;
}

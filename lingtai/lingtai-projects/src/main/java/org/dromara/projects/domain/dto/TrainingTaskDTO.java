package org.dromara.projects.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serializable;

/**
 * 训练任务请求对象
 */
@Data
public class TrainingTaskDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "名称不能为空",groups = {AddGroup.class})
    private String name;

    @NotNull(message = "训练轮数不能为空",groups = {AddGroup.class, EditGroup.class})
    @Min(value = 1, message = "训练轮数不能小于1")
    private Integer totalEpochs;

    @NotNull(message = "批次大小不能为空")
    @Min(value = 1, message = "批次大小不能小于1")
    private Integer batchSize;

    @NotNull(message = "学习率不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "学习率必须大于0")
    private Float lr;

    private String remark;
}

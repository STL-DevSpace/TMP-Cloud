package org.dromara.nodes.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@TableName("rays")
public class Cluster {
    private int id;
    @NotBlank
    private String name;
    private Long userId;
    private int projectId;
    private String createdTime;
    private String lastUpdatedTime;
}

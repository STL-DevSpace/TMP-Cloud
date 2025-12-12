package org.dromara.nodes.entity.dto; // 建议放在entity包下的dto子包

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 节点信息数据传输对象 (DTO)
 */
@Data
@Accessors(chain = true)
public class NodesDto implements Serializable {

    // 基础信息
    private Long id;
    private Long userId;
    private Long projectId;
    private Long rayId;
    private String name;
    private String role;

    // 连接与状态
    private String pathIpv4;
    private String progress;
    private String state;
    private Date heartbeat;

    // 资源配置
    private String cpu;
    private String gpu;
    private String memory;

    // 资源使用情况 (通常用于展示)
    private Double cpuUsage; // 对应实体类中的 cpu_usage
    private Double memoryUsage; // 对应实体类中的 memory_usage
    private Double diskUsage; // 对应实体类中的 disk_usage

    // 网络流量 (通常用于展示)
    private Integer sent; // 对应实体类中的 sent (使用Integer以保持与int的兼容性)
    private Integer received; // 对应实体类中的 received

    // 时间戳（如果前端需要展示创建或更新时间）
    // private Date createdTime;
    // private Date lastUpdatedTime;
}

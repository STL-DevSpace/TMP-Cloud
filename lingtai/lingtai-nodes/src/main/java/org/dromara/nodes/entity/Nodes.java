package org.dromara.nodes.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bouncycastle.asn1.cms.Time;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("nodes")
@Accessors(chain = true)
public class Nodes implements Serializable {
    private Long id;
    private Long userId;
    private Long projectId;
    private Long rayId;
    private String name;
    private String pathIpv4;
    private String progress;
    private String state;
    private String role;
    private Double cpu_usage;
    private Double memory_usage;
    private Double disk_usage;
    private int sent;
    private int received;
    private Date heartbeat;
    private String cpu;
    private String gpu;
    private String memory;
    private Date createdTime;
    private Date lastUpdatedTime;
}

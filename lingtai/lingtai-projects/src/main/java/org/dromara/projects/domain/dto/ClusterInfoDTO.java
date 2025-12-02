package org.dromara.projects.domain.dto;

import lombok.Data;

@Data
public class ClusterInfoDTO {
    private String id;
    private String name;
    private String userId;
    private String projectId;
    private String createdTime;
    private String lastUpdatedTime;
}

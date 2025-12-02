package org.dromara.projects.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProjectsCreateDTO {
    private Integer id;

    private Integer userId;
    private String name;
    private String description;
    private String trainingAlg;
    private String fedAlg;
    private Boolean secureAggregation;
    private Integer totalEpochs;
    private Integer numRounds;
    private Integer batchSize;
    private BigDecimal lr;
    private Integer numComputers;
    private Integer threshold;
    private Integer numClients;
    private Integer sampleClients;
    private Integer maxSteps;
    private String modelNameOrPath;
    private String datasetName;
    private String datasetSample;
    private String status;
    private BigDecimal progress;
    private String taskId;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}

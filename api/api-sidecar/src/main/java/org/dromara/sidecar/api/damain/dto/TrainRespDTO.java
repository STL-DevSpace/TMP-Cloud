package org.dromara.sidecar.api.damain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TrainRespDTO {
    private String projectId;
    private Float finalLoss;
    private String status;
    private Integer rounds;
}

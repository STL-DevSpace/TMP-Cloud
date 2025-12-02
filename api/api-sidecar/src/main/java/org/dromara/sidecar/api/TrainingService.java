package org.dromara.sidecar.api;

import com.edgeai.training.api.PiTrainReply;
import com.edgeai.training.api.PiTrainRequest;

public interface TrainingService {
    PiTrainReply piTrain(PiTrainRequest request);
}

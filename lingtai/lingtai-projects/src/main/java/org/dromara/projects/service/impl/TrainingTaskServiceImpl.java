package org.dromara.projects.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edgeai.training.api.*;
import com.edgeai.training.api.TrainingServiceGrpc;
import org.dromara.projects.domain.TrainingTask;
import org.dromara.projects.domain.dto.TrainingTaskDTO;
import org.dromara.projects.domain.vo.TrainingTaskVO;
import org.dromara.projects.mapper.TrainingTaskMapper;
import org.dromara.projects.service.ITrainingTaskService;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 训练任务Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingTaskServiceImpl extends ServiceImpl<TrainingTaskMapper, TrainingTask>
    implements ITrainingTaskService {

    private final TrainingServiceGrpc.TrainingServiceBlockingStub trainingServiceStub;

    @Override
    public List<TrainingTaskVO> selectTaskList(TrainingTaskDTO dto) {
        LambdaQueryWrapper<TrainingTask> wrapper = new LambdaQueryWrapper<>();

        if (dto.getProjectId() != null) {
            wrapper.eq(TrainingTask::getProjectId, dto.getProjectId());
        }

        wrapper.orderByDesc(TrainingTask::getCreateTime);

        List<TrainingTask> list = this.list(wrapper);
        return list.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public TrainingTaskVO selectTaskById(Long id) {
        TrainingTask task = this.getById(id);
        if (task == null) {
            throw new RuntimeException("训练任务不存在");
        }
        return convertToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createTask(TrainingTaskDTO dto) {
        // 1. 保存到数据库
        TrainingTask task = new TrainingTask();
        BeanUtil.copyProperties(dto, task);
        task.setStatus("PENDING");
        return this.save(task);
    }

    @Override
    public TrainingTaskVO startTask(TrainingTaskDTO dto) {

        String projectId = dto.getProjectId();
        LambdaQueryWrapper<TrainingTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingTask::getProjectId, projectId);
        TrainingTask task = this.getOne(wrapper);
        if (task == null ) {
            throw new RuntimeException("训练任务不存在");
        }
        if (!task.getStatus().equals("PENDING")) {
            throw new RuntimeException("训练任务已启动");
        }

        try {
            // 2. 调用 gRPC 启动训练
            PiTrainRequest request = PiTrainRequest.newBuilder()
                .setProjectId(projectId)
                .setEpochs(dto.getEpochs())
                .setBatchSize(dto.getBatchSize())
                .setLr(dto.getLr())
                .build();

            log.info("Starting training task via gRPC: {}", projectId);
            PiTrainReply reply = trainingServiceStub.piTrain(request);

            // 3. 更新训练结果
            task.setFinalLoss(reply.getFinalLoss());
            task.setStatus(reply.getStatus());
            task.setRounds(reply.getRounds());
            task.setLogPath(reply.getLogPath());
            this.updateById(task);

            log.info("Training task started successfully: {}", projectId);
            return convertToVO(task);

        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed: {}", e.getMessage(), e);
            task.setStatus("FAILED");
            task.setRemark("gRPC调用失败: " + e.getMessage());
            this.updateById(task);
            throw new RuntimeException("启动训练失败: " + e.getMessage());
        }
    }

    @Override
    public Boolean pauseTask(String projectId) {
        try {
            PauseRequest request = PauseRequest.newBuilder()
                .setProjectId(projectId)
                .build();

            log.info("Pausing training task: {}", projectId);
            PauseReply reply = trainingServiceStub.pauseTrain(request);

            if (reply.getSuccess()) {
                // 更新数据库状态
                LambdaQueryWrapper<TrainingTask> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(TrainingTask::getProjectId, projectId);
                TrainingTask task = this.getOne(wrapper);
                if (task != null) {
                    task.setStatus("PAUSED");
                    this.updateById(task);
                }
            }

            return reply.getSuccess();
        } catch (StatusRuntimeException e) {
            log.error("Failed to pause training task: {}", e.getMessage(), e);
            throw new RuntimeException("暂停训练失败: " + e.getMessage());
        }
    }

    @Override
    public Boolean stopTask(String projectId) {
        try {
            StopRequest request = StopRequest.newBuilder()
                .setProjectId(projectId)
                .build();

            log.info("Stopping training task: {}", projectId);
            StopReply reply = trainingServiceStub.stopTrain(request);

            if (reply.getSuccess()) {
                // 更新数据库状态
                LambdaQueryWrapper<TrainingTask> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(TrainingTask::getProjectId, projectId);
                TrainingTask task = this.getOne(wrapper);
                if (task != null) {
                    task.setStatus("STOPPED");
                    this.updateById(task);
                }
            }

            return reply.getSuccess();
        } catch (StatusRuntimeException e) {
            log.error("Failed to stop training task: {}", e.getMessage(), e);
            throw new RuntimeException("停止训练失败: " + e.getMessage());
        }
    }

    @Override
    public TrainingTaskVO getTaskProgress(String projectId) {
        try {
            ProgressRequest request = ProgressRequest.newBuilder()
                .setProjectId(projectId)
                .build();

            log.info("Getting training progress: {}", projectId);
            ProgressReply reply = trainingServiceStub.getProgress(request);

            // 更新数据库中的进度信息
            LambdaQueryWrapper<TrainingTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TrainingTask::getProjectId, projectId);
            TrainingTask task = this.getOne(wrapper);

            if (task != null) {
                task.setProgress(reply.getProgress());
                task.setRounds(reply.getRound());
                task.setCurrentLoss(reply.getLoss());
                this.updateById(task);
                return convertToVO(task);
            }

            throw new RuntimeException("训练任务不存在");
        } catch (StatusRuntimeException e) {
            log.error("Failed to get training progress: {}", e.getMessage(), e);
            throw new RuntimeException("获取训练进度失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteTask(Long id) {
        TrainingTask task = this.getById(id);
        if (task == null) {
            throw new RuntimeException("训练任务不存在");
        }

        // 如果任务正在运行，先停止
        if ("RUNNING".equals(task.getStatus())) {
            stopTask(task.getProjectId());
        }

        return this.removeById(id);
    }

    /**
     * 实体转VO
     */
    private TrainingTaskVO convertToVO(TrainingTask task) {
        TrainingTaskVO vo = new TrainingTaskVO();
        BeanUtil.copyProperties(task, vo);
        return vo;
    }
}

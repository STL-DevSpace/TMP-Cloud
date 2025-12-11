package org.dromara.projects.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edgeai.training.api.*;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.projects.domain.TrainingTask;
import org.dromara.projects.domain.dto.TrainingTaskDTO;
import org.dromara.projects.domain.dto.TrainingTaskUpdateDTO;
import org.dromara.projects.domain.vo.TrainingTaskVO;
import org.dromara.projects.enums.TrainingTaskStatus;
import org.dromara.projects.mapper.TrainingTaskMapper;
import org.dromara.projects.service.ITrainingTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 训练任务Service实现
 *
 * @author 86185
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingTaskServiceImpl implements ITrainingTaskService {

    private final TrainingServiceGrpc.TrainingServiceBlockingStub trainingServiceStub;

    private final TrainingTaskMapper baseMapper;

    @Override
    public List<TrainingTaskVO> selectTaskList(TrainingTaskDTO dto) {
        LambdaQueryWrapper<TrainingTask> wrapper = new LambdaQueryWrapper<>();
        Long userId = LoginHelper.getUserId();
        wrapper.eq(TrainingTask::getUserId, userId);

        wrapper.orderByDesc(TrainingTask::getCreateTime);

        return baseMapper.selectVoList(wrapper);
    }

    @Override
    public TrainingTaskVO selectTaskById(Long id) {
        TrainingTaskVO task = baseMapper.selectVoById(id);
        if (task == null) {
            throw new RuntimeException("训练任务不存在");
        }
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createTask(TrainingTaskDTO dto) {
        // 1. 保存到数据库
        TrainingTask task = new TrainingTask();
        BeanUtil.copyProperties(dto, task);
        task.setUserId(LoginHelper.getUserId());
        task.setStatus(TrainingTaskStatus.PENDING);
        return baseMapper.insert(task) > 0;
    }

    @Override
    public TrainingTaskVO startTask(TrainingTaskDTO dto) {

        Long taskId = dto.getId();
        TrainingTask task = baseMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("训练任务不存在");
        }
        if (!task.getStatus().equals(TrainingTaskStatus.PENDING)) {
            throw new RuntimeException("训练任务已启动");
        }

        try {
            // 2. 调用 gRPC 启动训练
            PiTrainRequest request = PiTrainRequest.newBuilder()
                .setProjectId(taskId.toString())
                .setEpochs(dto.getTotalEpochs())
                .setBatchSize(dto.getBatchSize())
                .setLr(dto.getLr())
                .build();

            log.info("Starting training task via gRPC: {}", taskId);
            PiTrainReply reply = trainingServiceStub.piTrain(request);

            // 3. 更新训练结果
            task.setFinalLoss(reply.getFinalLoss());
            task.setStatus(TrainingTaskStatus.valueOf(reply.getStatus()));
            task.setRounds(reply.getRounds());
            task.setLogPath(reply.getLogPath());
            baseMapper.updateById(task);

            log.info("Training task started successfully: {}", taskId);
            return convertToVO(task);

        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed: {}", e.getMessage(), e);
            task.setStatus(TrainingTaskStatus.FAILED);
            task.setRemark("gRPC调用失败: " + e.getMessage());
            baseMapper.updateById(task);
            throw new RuntimeException("启动训练失败: " + e.getMessage());
        }
    }

    @Override
    public Boolean pauseTask(Long taskId) {
        try {
            PauseRequest request = PauseRequest.newBuilder()
                .setProjectId(taskId.toString())
                .build();

            log.info("Pausing training task: {}", taskId);
            PauseReply reply = trainingServiceStub.pauseTrain(request);

            if (reply.getSuccess()) {
                // 更新数据库状态
                TrainingTask task = baseMapper.selectById(taskId);
                if (task != null) {
                    task.setStatus(TrainingTaskStatus.PAUSED);
                    baseMapper.updateById(task);
                }
            }

            return reply.getSuccess();
        } catch (StatusRuntimeException e) {
            log.error("Failed to pause training task: {}", e.getMessage(), e);
            throw new RuntimeException("暂停训练失败: " + e.getMessage());
        }
    }

    @Override
    public Boolean stopTask(Long taskId) {
        try {
            StopRequest request = StopRequest.newBuilder()
                .setProjectId(taskId.toString())
                .build();

            log.info("Stopping training task: {}", taskId);
            StopReply reply = trainingServiceStub.stopTrain(request);

            if (reply.getSuccess()) {
                // 更新数据库状态
                TrainingTask task = baseMapper.selectById(taskId);
                if (task != null) {
                    task.setStatus(TrainingTaskStatus.STOPPED);
                    baseMapper.updateById(task);
                }
            }

            return reply.getSuccess();
        } catch (StatusRuntimeException e) {
            log.error("Failed to stop training task: {}", e.getMessage(), e);
            throw new RuntimeException("停止训练失败: " + e.getMessage());
        }
    }

    @Override
    public TrainingTaskVO getTaskProgress(Long taskId) {
        try {
            ProgressRequest request = ProgressRequest.newBuilder()
                .setProjectId(taskId.toString())
                .build();

            log.info("Getting training progress: {}", taskId);
            ProgressReply reply = trainingServiceStub.getProgress(request);

            // 更新数据库中的进度信息
            TrainingTask task = baseMapper.selectById(taskId);

            if (task != null) {
                task.setProgress(BigDecimal.valueOf(reply.getProgress()));
                task.setRounds(reply.getRound());
                task.setCurrentLoss(reply.getLoss());
                baseMapper.updateById(task);
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
        TrainingTask task = baseMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("训练任务不存在");
        }

        // 如果任务正在运行，先停止
        if (TrainingTaskStatus.RUNNING.equals(task.getStatus())) {
            stopTask(task.getId());
        }

        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public Boolean updateTask(Long id, TrainingTaskUpdateDTO dto) {
        //根据前端传回来的有的数据进行更新
        TrainingTask task = baseMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("训练任务不存在");
        }
        task.setName(dto.getName());
        return baseMapper.updateById(task) > 0;
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

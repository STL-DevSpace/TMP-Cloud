package org.dromara.projects.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.dromara.projects.domain.TrainingTask;
import org.dromara.projects.domain.dto.TrainingTaskDTO;
import org.dromara.projects.domain.dto.TrainingTaskUpdateDTO;
import org.dromara.projects.domain.vo.TrainingTaskVO;
import java.util.List;

/**
 * 训练任务Service接口
 */
public interface ITrainingTaskService {

    /**
     * 查询训练任务列表
     */
    List<TrainingTaskVO> selectTaskList(TrainingTaskDTO dto);

    /**
     * 查询训练任务详情
     */
    TrainingTaskVO selectTaskById(Long id);

    /**
     * 创建训练任务
     */
    Boolean createTask(TrainingTaskDTO dto);

    /**
     * 启动训练
     */
    TrainingTaskVO startTask(TrainingTaskDTO dto);

    /**
     * 暂停训练任务
     */
    Boolean pauseTask(Long projectId);

    /**
     * 停止训练任务
     */
    Boolean stopTask(Long projectId);

    /**
     * 查询训练进度
     */
    TrainingTaskVO getTaskProgress(Long projectId);

    /**
     * 删除训练任务
     */
    Boolean deleteTask(Long id);

    Boolean updateTask(Long id, TrainingTaskUpdateDTO dto);
}

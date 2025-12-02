package org.dromara.projects.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.projects.domain.TrainingTask;

/**
 * 训练任务Mapper接口
 */
@Mapper
public interface TrainingTaskMapper extends BaseMapper<TrainingTask> {

}

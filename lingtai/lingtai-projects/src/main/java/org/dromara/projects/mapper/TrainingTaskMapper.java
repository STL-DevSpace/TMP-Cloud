package org.dromara.projects.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.projects.domain.TrainingTask;
import org.dromara.projects.domain.vo.TrainingTaskVO;

/**
 * 训练任务Mapper接口
 * @author 86185
 */
@Mapper
public interface TrainingTaskMapper extends BaseMapperPlus<TrainingTask, TrainingTaskVO> {

}

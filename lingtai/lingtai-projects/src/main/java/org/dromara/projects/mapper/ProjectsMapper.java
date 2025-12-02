package org.dromara.projects.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.projects.domain.Projects;

@Mapper
public interface ProjectsMapper extends BaseMapper<Projects> {
}

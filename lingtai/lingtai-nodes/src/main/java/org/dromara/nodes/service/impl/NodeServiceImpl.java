package org.dromara.nodes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.nodes.entity.Nodes;
import org.dromara.nodes.mapper.NodesMapper;
import org.dromara.nodes.service.NodesService;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class NodeServiceImpl implements NodesService {

    private final NodesMapper baseMapper;

    @Override
    public IPage<Nodes> list(Integer pageNum, Integer pageSize) {
        Page<Nodes> page = new Page<>(pageNum, pageSize);

        //构建查询条件
        LambdaQueryWrapper<Nodes> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Nodes::getCreatedTime);
        return baseMapper.selectPage(page, queryWrapper);
    }
}

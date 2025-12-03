package org.dromara.nodes.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.nodes.entity.Cluster;
import org.dromara.nodes.entity.Nodes;
import org.dromara.nodes.mapper.NodesMapper;
import org.dromara.nodes.service.ClustersService;
import org.dromara.nodes.service.NodesService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NodeServiceImpl implements NodesService {

    private final NodesMapper baseMapper;
    private final ClustersService clustersService;

    @Override
    public IPage<Nodes> list(Integer pageNum, Integer pageSize) {
        Page<Nodes> page = new Page<>(pageNum, pageSize);

        //构建查询条件
        LambdaQueryWrapper<Nodes> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Nodes::getCreatedTime);
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Nodes addByClusterId(Long clusterId, Nodes  node) {
        // 从当前Token获取 userId
        String loginStr = StpUtil.getLoginId().toString();
        String loginId = loginStr.substring(loginStr.indexOf(":")+1);
        Long userId = Long.valueOf(loginId);
        node.setUserId(userId);
        node.setRayId(clusterId);
        Cluster cluster = clustersService.getDetail(clusterId);
        node.setProjectId(cluster.getProjectId());
        return node;
    }

}

package org.dromara.nodes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.nodes.entity.Cluster;
import org.dromara.nodes.entity.Nodes;
import org.dromara.nodes.mapper.ClustersMapper;
import org.dromara.nodes.service.ClustersService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.Date;

import static cn.dev33.satoken.json.SaJsonTemplateForJackson.DATE_TIME_FORMATTER;

@RequiredArgsConstructor
@Service
public class ClustersServiceImpl implements ClustersService {

    private final ClustersMapper baseMapper;
    @Override
    public IPage<Cluster> list(Integer pageNum, Integer pageSize) {
        Page<Cluster> page = new Page<>(pageNum, pageSize);

        //构建查询条件
        LambdaQueryWrapper<Cluster> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Cluster::getCreatedTime);
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Cluster add(@RequestBody @Validated Cluster cluster) {
        LocalDateTime now = LocalDateTime.now();
        // 2. 格式化为字符串（匹配数据库常用格式）
        String nowStr = now.format(DATE_TIME_FORMATTER);
        // 3. 设置到对象中（createdTime 也建议同步设置）
        cluster.setLastUpdatedTime(nowStr);
        baseMapper.insert(cluster);
        return cluster;
    }

    @Override
    public Cluster getDetail(Long clusterId) {
        LambdaQueryWrapper<Cluster> queryWrapper = new LambdaQueryWrapper<>();
        if(clusterId > 0){
            queryWrapper.eq(Cluster::getId, clusterId);
        }
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void delete(Long clusterId) {
        boolean result = baseMapper.deleteById(clusterId) > 0;
        if(!result){
            // 删除失败
            throw new RuntimeException("删除失败");
        }
    }
}

package org.dromara.nodes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.dromara.nodes.entity.Nodes;
import org.dromara.nodes.mapper.NodesMapper;
import org.dromara.nodes.service.NodesService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 节点信息 服务实现类
 */
@Service
public class NodesServiceImpl extends ServiceImpl<NodesMapper, Nodes> implements NodesService {

    // ServiceImpl 已经自动注入了 baseMapper，但我们也可以选择显式注入 Mapper
    @Resource
    private NodesMapper nodesMapper;

    // IService<Nodes> 接口中的 save, getById, updateById, removeById 等方法
    // 已经由 ServiceImpl<NodesMapper, Nodes> 自动实现，无需在此处重复编写。


    /**
     * 【自定义方法实现】分页查询节点信息
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 节点实体分页对象
     */
    @Override
    public IPage<Nodes> list(Integer pageNum, Integer pageSize) {
        // 1. 创建 MyBatis-Plus 分页对象
        Page<Nodes> page = new Page<>(pageNum, pageSize);

        // 2. 构造查询条件 (此处使用LambdaQueryWrapper进行无条件查询并按时间倒序)
        LambdaQueryWrapper<Nodes> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Nodes::getCreatedTime);

        // 3. 执行分页查询 (调用 baseMapper.selectPage)
        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 【自定义方法实现】根据项目ID查询用于可视化的节点列表
     * @param projectid 项目ID
     * @return 节点实体列表
     */
    @Override
    public List<Nodes> visulazation(Long projectid) {
        // 1. 构造查询条件
        LambdaQueryWrapper<Nodes> wrapper = new LambdaQueryWrapper<>();
        // 过滤条件：projectId 必须等于传入的 projectid
        wrapper.eq(Nodes::getProjectId, projectid);
        // 可以添加排序或其他过滤条件，例如：
        wrapper.eq(Nodes::getState, "RUNNING"); // 仅查询状态为 RUNNING 的节点
        wrapper.orderByAsc(Nodes::getName);

        // 2. 执行列表查询 (调用 baseMapper.selectList)
        return baseMapper.selectList(wrapper);
    }
}

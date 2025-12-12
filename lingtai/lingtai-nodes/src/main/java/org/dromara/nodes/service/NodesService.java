package org.dromara.nodes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.dromara.nodes.entity.Nodes;
import org.dromara.nodes.entity.dto.NodesDto; // DTO 可以在 service 层用于更复杂的业务逻辑，但此处保持简单，只返回 Entity

import java.util.List;

/**
 * 节点信息 服务接口
 * 继承 IService<Nodes> 以获得 MyBatis-Plus 提供的基础 CRUD 方法
 */
public interface NodesService extends IService<Nodes> {

    /**
     * 【对应Controller中的分页查询】
     * 分页查询节点信息。
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 节点实体分页对象 (IPage<Nodes>)
     */
    IPage<Nodes> list(Integer pageNum, Integer pageSize);

    /**
     * 【对应Controller中的可视化查询】
     * 根据项目ID查询用于可视化的节点列表。
     * @param projectid 项目ID
     * @return 节点实体列表
     */
    List<Nodes> visulazation(Long projectid);
}

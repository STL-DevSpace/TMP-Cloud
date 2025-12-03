package org.dromara.nodes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.dromara.nodes.entity.Cluster;
import org.dromara.nodes.entity.Nodes;

public interface ClustersService {
    IPage<Cluster> list(Integer pageNum, Integer pageSize);

    Cluster add(Cluster cluster);

    Cluster getDetail(Long clusterId);

    void delete(Long clusterId);
}

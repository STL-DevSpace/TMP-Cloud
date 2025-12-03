package org.dromara.nodes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.dromara.nodes.entity.Nodes;

import java.util.List;

public interface NodesService {
    IPage<Nodes> list(Integer pageNum, Integer pageSize);
}

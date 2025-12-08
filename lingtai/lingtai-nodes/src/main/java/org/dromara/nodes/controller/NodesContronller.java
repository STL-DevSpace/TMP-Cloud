package org.dromara.nodes.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.dromara.common.core.domain.R;
import org.dromara.nodes.entity.Nodes;
import org.dromara.nodes.service.NodesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/edgeai/nodes")
public class NodesContronller {

    @Resource
    private NodesService nodesService;
    @RequestMapping("hello")
    public String hello() {
        return "hello world";
    }

    /**
     * 分页查询节点信息
     * @return
     */
    @GetMapping
    public R<List<Nodes>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        IPage<Nodes> list = nodesService.list(pageNum, pageSize);
        return R.ok(list.getRecords());
    }
    @GetMapping("/visualization/{projectid}")
    public R<List<Nodes>> visulazation(@PathVariable("projectid") Long projectid) {
        List<Nodes> list = nodesService.visulazation(projectid);
        return R.ok(list);
    }
}

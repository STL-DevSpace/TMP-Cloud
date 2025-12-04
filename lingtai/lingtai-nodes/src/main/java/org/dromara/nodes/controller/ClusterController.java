package org.dromara.nodes.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.dromara.common.core.domain.R;
import org.dromara.nodes.entity.Cluster;
import org.dromara.nodes.entity.Nodes;
import org.dromara.nodes.service.ClustersService;
import org.dromara.nodes.service.NodesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/edgeai/clusters")
@RestController
public class ClusterController {
    @Resource
    private ClustersService clustersService;

    @Resource
    private NodesService nodesService;
    @RequestMapping("/hello")
    public String hello() {
        return "hello world";
    }

    /**
     * 分页查询集群信息
     * @return
     */
    @GetMapping
    public R<List<Cluster>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        IPage<Cluster> list = clustersService.list(pageNum, pageSize);
        return R.ok(list.getRecords());
    }
    @PostMapping
    public R<Cluster> add(@RequestBody Cluster cluster) {
        // 从当前Token获取 userId
        String loginStr = StpUtil.getLoginId().toString();
        String loginId = loginStr.substring(loginStr.indexOf(":")+1);
        Long userId = Long.valueOf(loginId);
        cluster.setUserId(userId);
        Cluster result = clustersService.add(cluster);
        return R.ok(result);
    }
    @GetMapping("/{id}")
    public R<Cluster> getDetail(@PathVariable("id") Long clusterId) {
        Cluster result = clustersService.getDetail(clusterId);
        return R.ok(result);
    }
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable("id") Long clusterId) {
        clustersService.delete(clusterId);
        return R.ok("删除成功");
    }
    @PostMapping("/{id}/nodes/")
    public R<Nodes> addNode(@PathVariable("id") Long clusterId, @RequestBody Nodes node) {
        Nodes result = nodesService.addByClusterId(clusterId, node);
        return R.ok(result);
    }

}

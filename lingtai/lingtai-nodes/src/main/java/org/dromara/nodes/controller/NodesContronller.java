package org.dromara.nodes.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.dromara.common.core.domain.R;
import org.dromara.nodes.entity.Nodes; // 实体类
import org.dromara.nodes.entity.dto.NodesDto; // DTO 类
import org.dromara.nodes.service.NodesService;
import org.springframework.beans.BeanUtils; // 用于对象属性拷贝
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/edgeai/nodes")
public class NodesContronller {

    @Resource
    private NodesService nodesService;

    // 示例方法：保持不变
    @RequestMapping("hello")
    public String hello() {
        return "hello world";
    }

    /**
     * 1. 【新增】节点信息
     * POST /api/edgeai/nodes
     * @param nodesDto 节点数据传输对象
     * @return 成功或失败的响应
     */
    @PostMapping("/{id}")
    public R<Void> add(
        @PathVariable Long id,
        @RequestBody NodesDto nodesDto) {
        Nodes nodes = new Nodes();
        String loginStr = StpUtil.getLoginId().toString();
        String loginId = loginStr.substring(loginStr.indexOf(":")+1);
        Long userId = Long.valueOf(loginId);
        // 将 DTO 属性拷贝到实体类，用于存储
        BeanUtils.copyProperties(nodesDto, nodes);
        nodes.setUserId(userId);
        nodes.setProjectId(id);
        nodes.setRayId(101L);
        nodesService.save(nodes);
        return R.ok();
    }

    /**
     * 2. 【查询-详情】根据 ID 查询单个节点信息
     * GET /api/edgeai/nodes/{id}
     * @param id 节点 ID
     * @return 包含节点详情的响应
     */
    @GetMapping("/{id}")
    public R<NodesDto> getDetail(@PathVariable("id") Long id) {
        Nodes nodes = nodesService.getById(id);
        if (nodes == null) {
            return R.fail("节点不存在");
        }
        NodesDto nodesDto = new NodesDto();
        // 将实体类属性拷贝到 DTO，返回给前端
        BeanUtils.copyProperties(nodes, nodesDto);
        return R.ok(nodesDto);
    }

    /**
     * 3. 【修改】更新节点信息
     * PUT /api/edgeai/nodes
     * @param nodesDto 节点数据传输对象
     * @return 成功或失败的响应
     */
    @PutMapping
    public R<Void> edit(@RequestBody NodesDto nodesDto) {
        if (nodesDto.getId() == null) {
            return R.fail("节点 ID 不能为空");
        }
        Nodes nodes = new Nodes();
        // 将 DTO 属性拷贝到实体类，用于更新
        BeanUtils.copyProperties(nodesDto, nodes);
        nodesService.updateById(nodes);
        return R.ok();
    }

    /**
     * 4. 【删除】根据 ID 删除节点信息
     * DELETE /api/edgeai/nodes/{id}
     * @param id 节点 ID
     * @return 成功或失败的响应
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable("id") Long id) {
        nodesService.removeById(id);
        return R.ok();
    }

    /**
     * 5. 【查询-分页列表】分页查询节点信息（使用 DTO 优化返回结果）
     * GET /api/edgeai/nodes?pageNum=1&pageSize=10
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 节点列表的响应
     */
    @GetMapping
    public R<List<NodesDto>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        IPage<Nodes> page = nodesService.list(pageNum, pageSize);

        // 将查询到的 Nodes 实体列表转换为 NodesDto 列表
        List<NodesDto> dtoList = page.getRecords().stream().map(nodes -> {
            NodesDto dto = new NodesDto();
            BeanUtils.copyProperties(nodes, dto);
            return dto;
        }).collect(Collectors.toList());

        // 注意：实际项目中 R<T> 应该封装分页信息 (总数/总页数)
        // 此处为了简洁，仅返回列表
        return R.ok(dtoList);
    }

    /**
     * 6. 【查询-可视化数据】根据项目 ID 查询节点信息（使用 DTO 优化返回结果）
     * GET /api/edgeai/nodes/visualization/{projectid}
     * @param projectid 项目 ID
     * @return 节点列表的响应
     */
    @GetMapping("/visualization/{projectid}")
    public R<List<NodesDto>> visulazation(@PathVariable("projectid") Long projectid) {
        List<Nodes> nodesList = nodesService.visulazation(projectid);

        // 将实体列表转换为 DTO 列表
        List<NodesDto> dtoList = nodesList.stream().map(nodes -> {
            NodesDto dto = new NodesDto();
            BeanUtils.copyProperties(nodes, dto);
            return dto;
        }).collect(Collectors.toList());

        return R.ok(dtoList);
    }
}

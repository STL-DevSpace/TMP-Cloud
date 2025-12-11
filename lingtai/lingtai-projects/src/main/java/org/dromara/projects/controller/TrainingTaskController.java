package org.dromara.projects.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.projects.domain.dto.ClusterInfoDTO;
import org.dromara.projects.domain.dto.TrainingTaskDTO;
import org.dromara.projects.domain.dto.TrainingTaskUpdateDTO;
import org.dromara.projects.domain.vo.TrainingTaskVO;
import org.dromara.projects.service.ITrainingTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 训练任务控制器
 *
 * @author YangYi
 */
@RestController
@RequestMapping("/training/task")
@RequiredArgsConstructor
public class TrainingTaskController {

    private final ITrainingTaskService trainingTaskService;

    /**
     * 查询训练任务列表
     */
    @GetMapping("/list")
    public R<List<TrainingTaskVO>> list(TrainingTaskDTO dto) {
        List<TrainingTaskVO> list = trainingTaskService.selectTaskList(dto);
        return R.ok(list);
    }

    /**
     * 获取训练任务详情
     */
    @GetMapping("/{id}")
    public R<TrainingTaskVO> getInfo(@PathVariable Long id) {
        TrainingTaskVO vo = trainingTaskService.selectTaskById(id);
        return R.ok(vo);
    }

    /**
     * 创建训练任务
     */
    @PostMapping("/add")
    public R<Boolean> add(@Validated @RequestBody TrainingTaskDTO dto) {
        Boolean result = trainingTaskService.createTask(dto);
        return R.ok(result);
    }

    /**
     * 启动训练任务
     */
    @PostMapping("/start")
    public R<TrainingTaskVO> start(@Validated @RequestBody TrainingTaskDTO dto) {
        TrainingTaskVO vo = trainingTaskService.startTask(dto);
        return R.ok(vo);
    }

    /**
     * 暂停训练任务
     */
    @PostMapping("/{projectId}/pause")
    public R<Boolean> pause(@PathVariable Long projectId) {
        Boolean result = trainingTaskService.pauseTask(projectId);
        return R.ok(result);
    }

    /**
     * 停止训练任务
     */
    @PostMapping("/{projectId}/stop")
    public R<Boolean> stop(@PathVariable Long projectId) {
        Boolean result = trainingTaskService.stopTask(projectId);
        return R.ok(result);
    }

    /**
     * 获取训练进度
     */
    @GetMapping("/{projectId}/progress")
    public R<TrainingTaskVO> getProgress(@PathVariable Long projectId) {
        TrainingTaskVO vo = trainingTaskService.getTaskProgress(projectId);
        return R.ok(vo);
    }

    /**
     * 删除训练任务
     */
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        trainingTaskService.deleteTask(id);
        return R.ok();
    }
    /**
     * 更新训练任务
     */
    @PostMapping("/update/{id}")
    public R<Boolean> update(@PathVariable Long id, @Validated @RequestBody TrainingTaskUpdateDTO dto) {
        Boolean result = trainingTaskService.updateTask(id, dto);
        return R.ok(result);
    }
    /**
     * TODO 临时集群列表
     */
    @GetMapping("/clusters")
    public R<List<ClusterInfoDTO>> clustersList() {
        List<ClusterInfoDTO> clusters = Arrays.asList(new ClusterInfoDTO() {{
            setId("1");
            setName("Cluster 1 (db)");
            setUserId("1");
            setProjectId("5");
            setCreatedTime("2025-10-27T10:02:57");
            setLastUpdatedTime("2025-11-12T02:52:33");
        }}, new ClusterInfoDTO() {{
            setId("2");
            setName("Cluster 2 (db)");
            setUserId("1");
            setProjectId("3");
            setCreatedTime("2025-10-27T10:02:57");
            setLastUpdatedTime("2025-11-06T03:13:08");
        }}, new ClusterInfoDTO() {{
            setId("3");
            setName("Demo 3 (db)");
            setUserId("1");
            setProjectId("3");
            setCreatedTime("2025-10-27T10:02:57");
            setLastUpdatedTime("2025-11-06T03:13:08");
        }}, new ClusterInfoDTO() {{
            setId("4");
            setName("Test 4 (db)");
            setUserId("1");
            setProjectId("3");
            setCreatedTime("2025-10-27T10:02:57");
            setLastUpdatedTime("2025-11-06T03:13:08");
        }});

        return R.ok(clusters);
    }

    /**
     * 获取模型列表模拟数据
     */
    @GetMapping("/mock/models")
    public R<List<Map<String, Object>>> getModelList() {
        List<Map<String, Object>> models = Arrays.asList(
            new HashMap<String, Object>() {{
                put("id", 201);
                put("userId", 1);
                put("projectId", 101);
                put("name", "resnet18-cifar10-r50");
                put("description", "第 50 轮全局模型");
                put("filePath", "/models/p101/resnet18_round50.pth");
                put("version", "v1.0.50");
                put("size", 44851280);
                put("classConfig", "{\"num_classes\": 10}");
                put("status", "completed");
                put("progress", 100.00);
                put("loss", 0.0485);
                put("accuracy", 0.8654);
            }},
            new HashMap<String, Object>() {{
                put("id", 202);
                put("userId", 2);
                put("projectId", 102);
                put("name", "bert-imdb-r30");
                put("description", "第 30 轮全局模型");
                put("filePath", "/models/p102/bert_round30.pth");
                put("version", "v1.0.30");
                put("size", 438401088);
                put("classConfig", "{\"num_classes\": 2}");
                put("status", "completed");
                put("progress", 100.00);
                put("loss", 0.1823);
                put("accuracy", 0.9127);
            }},
            new HashMap<String, Object>() {{
                put("id", 203);
                put("userId", 3);
                put("projectId", 103);
                put("name", "mlp-credit-r0");
                put("description", "初始模型");
                put("filePath", "/models/p103/mlp_init.pth");
                put("version", "v0.0.0");
                put("size", 12345678);
                put("classConfig", "{\"num_classes\": 2}");
                put("status", "training");
                put("progress", 12.50);
                put("loss", 0.2937);
                put("accuracy", 0.7234);
            }}
        );

        return R.ok(models);
    }

    /**
     * 获取Ray集群列表模拟数据
     */
    @GetMapping("/mock/rays")
    public R<List<Map<String, Object>>> getRayClusters() {
        List<Map<String, Object>> rays = Arrays.asList(
            new HashMap<String, Object>() {{
                put("id", 301);
                put("name", "ray-cifar10-cluster");
                put("userId", 1);
                put("projectId", 101);
            }},
            new HashMap<String, Object>() {{
                put("id", 302);
                put("name", "ray-imdb-cluster");
                put("userId", 2);
                put("projectId", 102);
            }},
            new HashMap<String, Object>() {{
                put("id", 303);
                put("name", "ray-credit-cluster");
                put("userId", 3);
                put("projectId", 103);
            }}
        );

        return R.ok(rays);
    }

    /**
     * 获取节点列表模拟数据
     */
    @GetMapping("/mock/nodes")
    public R<List<Map<String, Object>>> getNodes() {
        List<Map<String, Object>> nodes = Arrays.asList(
            new HashMap<String, Object>() {{
                put("id", 401);
                put("userId", 1);
                put("projectId", 101);
                put("rayId", 301);
                put("name", "node-01-gpu");
                put("pathIpv4", "192.168.1.11");
                put("progress", 38.20);
                put("state", "running");
                put("role", "worker");
                put("cpuUsage", 42.30);
                put("memoryUsage", 68.50);
                put("diskUsage", 25.10);
                put("sent", 9876543);
                put("received", 8765432);
                put("heartbeat", "2025-11-12T02:52:33");
                put("cpu", "16 vCPU");
                put("gpu", "RTX-3090-24G");
                put("memory", "64GB");
            }},
            new HashMap<String, Object>() {{
                put("id", 402);
                put("userId", 1);
                put("projectId", 101);
                put("rayId", 301);
                put("name", "node-02-gpu");
                put("pathIpv4", "192.168.1.12");
                put("progress", 35.40);
                put("state", "running");
                put("role", "worker");
                put("cpuUsage", 38.60);
                put("memoryUsage", 71.20);
                put("diskUsage", 28.00);
                put("sent", 8765432);
                put("received", 7654321);
                put("heartbeat", "2025-11-12T02:52:33");
                put("cpu", "16 vCPU");
                put("gpu", "RTX-3090-24G");
                put("memory", "64GB");
            }},
            new HashMap<String, Object>() {{
                put("id", 403);
                put("userId", 1);
                put("projectId", 101);
                put("rayId", 301);
                put("name", "node-head");
                put("pathIpv4", "192.168.1.10");
                put("progress", 36.50);
                put("state", "running");
                put("role", "head");
                put("cpuUsage", 25.00);
                put("memoryUsage", 45.80);
                put("diskUsage", 20.30);
                put("sent", 12345678);
                put("received", 11234567);
                put("heartbeat", "2025-11-12T02:52:33");
                put("cpu", "32 vCPU");
                put("gpu", "");
                put("memory", "128GB");
            }},
            new HashMap<String, Object>() {{
                put("id", 404);
                put("userId", 2);
                put("projectId", 102);
                put("rayId", 302);
                put("name", "node-cpu-01");
                put("pathIpv4", "192.168.2.21");
                put("progress", 100.00);
                put("state", "completed");
                put("role", "worker");
                put("cpuUsage", 12.50);
                put("memoryUsage", 30.20);
                put("diskUsage", 15.60);
                put("sent", 11223344);
                put("received", 9988776);
                put("heartbeat", "2025-11-12T01:52:33");
                put("cpu", "8 vCPU");
                put("gpu", "");
                put("memory", "32GB");
            }},
            new HashMap<String, Object>() {{
                put("id", 405);
                put("userId", 3);
                put("projectId", 103);
                put("rayId", 303);
                put("name", "node-credit-01");
                put("pathIpv4", "192.168.3.31");
                put("progress", 12.50);
                put("state", "running");
                put("role", "worker");
                put("cpuUsage", 55.10);
                put("memoryUsage", 82.40);
                put("diskUsage", 40.20);
                put("sent", 5566778);
                put("received", 4455667);
                put("heartbeat", "2025-11-12T02:52:33");
                put("cpu", "16 vCPU");
                put("gpu", "");
                put("memory", "64GB");
            }}
        );

        return R.ok(nodes);
    }
}

package org.dromara.projects.controller;

import org.dromara.common.core.domain.R;
import org.dromara.projects.domain.dto.ClusterInfoDTO;
import org.dromara.projects.domain.dto.TrainingTaskDTO;
import org.dromara.projects.domain.vo.TrainingTaskVO;
import org.dromara.projects.service.ITrainingTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 训练任务控制器
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
    public R<Boolean> pause(@PathVariable String projectId) {
        Boolean result = trainingTaskService.pauseTask(projectId);
        return R.ok(result);
    }

    /**
     * 停止训练任务
     */
    @PostMapping("/{projectId}/stop")
    public R<Boolean> stop(@PathVariable String projectId) {
        Boolean result = trainingTaskService.stopTask(projectId);
        return R.ok(result);
    }

    /**
     * 获取训练进度
     */
    @GetMapping("/{projectId}/progress")
    public R<TrainingTaskVO> getProgress(@PathVariable String projectId) {
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
     * TODO 临时集群列表
     */
    @GetMapping("/clusters")
    public R<List<ClusterInfoDTO>> clustersList() {
        List<ClusterInfoDTO> clusters = Arrays.asList(
            new ClusterInfoDTO() {{
                setId("1");
                setName("Cluster 1 (db)");
                setUserId("1");
                setProjectId("5");
                setCreatedTime("2025-10-27T10:02:57");
                setLastUpdatedTime("2025-11-12T02:52:33");
            }},
            new ClusterInfoDTO() {{
                setId("2");
                setName("Cluster 2 (db)");
                setUserId("1");
                setProjectId("3");
                setCreatedTime("2025-10-27T10:02:57");
                setLastUpdatedTime("2025-11-06T03:13:08");
            }},
            new ClusterInfoDTO() {{
                setId("3");
                setName("Demo 3 (db)");
                setUserId("1");
                setProjectId("3");
                setCreatedTime("2025-10-27T10:02:57");
                setLastUpdatedTime("2025-11-06T03:13:08");
            }},
            new ClusterInfoDTO() {{
                setId("4");
                setName("Test 4 (db)");
                setUserId("1");
                setProjectId("3");
                setCreatedTime("2025-10-27T10:02:57");
                setLastUpdatedTime("2025-11-06T03:13:08");
            }}
             );

        return R.ok(clusters);
    }
}

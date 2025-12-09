package org.dromara.data.progress;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务进度存储（线程安全）
 * key：taskId
 */
@Component
public class ProgressStore {

    private final Map<String, Progress> progressMap = new ConcurrentHashMap<>();

    public void start(String taskId, String message) {
        Progress p = new Progress();
        p.setPercent(0);
        p.setStatus(Status.RUNNING.name());
        p.setMessage(message);
        progressMap.put(taskId, p);
    }

    public void updatePercent(String taskId, int percent, String message) {
        Progress p = progressMap.get(taskId);
        if (p != null) {
            p.setPercent(Math.max(0, Math.min(100, percent)));
            if (message != null) p.setMessage(message);
        } else {
            // 若不存在则创建（防止空指针）
            start(taskId, message != null ? message : "");
            updatePercent(taskId, percent, message);
        }
    }

    public void success(String taskId, String message) {
        Progress p = progressMap.get(taskId);
        if (p != null) {
            p.setPercent(100);
            p.setStatus(Status.SUCCESS.name());
            p.setMessage(message);
        } else {
            Progress np = new Progress();
            np.setPercent(100);
            np.setStatus(Status.SUCCESS.name());
            np.setMessage(message);
            progressMap.put(taskId, np);
        }
    }

    public void fail(String taskId, String message) {
        Progress p = progressMap.get(taskId);
        if (p != null) {
            p.setStatus(Status.FAILED.name());
            p.setMessage(message);
        } else {
            Progress np = new Progress();
            np.setPercent(0);
            np.setStatus(Status.FAILED.name());
            np.setMessage(message);
            progressMap.put(taskId, np);
        }
    }

    public Progress get(String taskId) {
        return progressMap.get(taskId);
    }

    public void remove(String taskId) {
        progressMap.remove(taskId);
    }

    @Data
    public static class Progress {
        private int percent;   // 0 - 100
        private String status; // RUNNING / SUCCESS / FAILED
        private String message;
    }

    private enum Status {
        RUNNING, SUCCESS, FAILED
    }
}

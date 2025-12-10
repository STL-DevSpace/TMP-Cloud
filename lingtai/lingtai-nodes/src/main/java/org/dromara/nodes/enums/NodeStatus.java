package org.dromara.nodes.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 训练任务状态枚举
 */
@Getter
public enum NodeStatus {
    /**
     * 待处理
     */
    PENDING,

    /**
     * 运行中
     */
    RUNNING,

    /**
     * 已暂停
     */
    PAUSED,

    /**
     * 已停止
     */
    STOPPED,

    /**
     * 已完成
     */
    COMPLETED,

    /**
     * 已启动
     */
    STARTED,

    /**
     * 失败
     */
    FAILED;

    // 添加状态码映射（可根据实际需求调整）
    private static final Map<String, NodeStatus> NAME_TO_ENUM_MAP =
        Arrays.stream(values()).collect(Collectors.toMap(Enum::name, e -> e));

    /**
     * 根据名称获取枚举值
     *
     * @param name 状态名称
     * @return 对应的枚举值，找不到返回null
     */
    public static NodeStatus fromName(String name) {
        return NAME_TO_ENUM_MAP.get(name);
    }

    /**
     * 判断是否为运行状态（RUNNING或PAUSED）
     *
     * @return 是否为运行状态
     */
    public boolean isRunningState() {
        return this == RUNNING || this == PAUSED;
    }

    /**
     * 判断是否为终止状态（COMPLETED、STOPPED或FAILED）
     *
     * @return 是否为终止状态
     */
    public boolean isTerminalState() {
        return this == COMPLETED || this == STOPPED || this == FAILED;
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述文本
     */
    public String getDescription() {
        return switch (this) {
            case PENDING -> "待处理";
            case RUNNING -> "运行中";
            case PAUSED -> "已暂停";
            case STOPPED -> "已停止";
            case COMPLETED -> "已完成";
            case STARTED -> "已启动";
            case FAILED -> "失败";
            default -> "未知状态";
        };
    }
}

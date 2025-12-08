package org.dromara.data.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置类
 * 专用于模型导入等长耗时任务的线程池配置
 */
@Configuration
@EnableAsync // 🚀 启用 Spring 对 @Async 注解的支持
public class AsyncImportConfig {

    /**
     * 定义 Hub 模型导入任务专用的线程池
     * Bean 名称 'taskExecutor' 必须与 HubImportTaskService 中的 @Async("taskExecutor") 一致。
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 获取 CPU 核心数
        int cpuCores = Runtime.getRuntime().availableProcessors();

        // 核心线程数：IO 密集型任务（如网络下载）可以设置得比 CPU 核心数高
        int corePoolSize = cpuCores * 2;

        // 设置线程池参数
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize * 2);
        executor.setQueueCapacity(200); // 任务队列大小
        executor.setThreadNamePrefix("HubImport-"); // 线程名称前缀，便于日志追踪

        // 配置拒绝策略：当线程池满且队列满时，由调用线程执行任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}

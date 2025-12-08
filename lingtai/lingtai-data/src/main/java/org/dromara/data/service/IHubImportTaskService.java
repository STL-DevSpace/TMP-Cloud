package org.dromara.data.service;


/**
 * Hub 模型导入任务服务接口
 * 负责启动和执行从 Hugging Face Hub 导入模型的异步任务
 */
public interface IHubImportTaskService {

    /**
     * 启动异步模型导入任务。
     * 该方法应在独立线程中执行，不阻塞调用它的主业务线程。
     *
     * @param modelId 数据库中已创建的模型记录 ID
     * @param hubUrl  Hugging Face Hub 的模型 ID (例如: openai/whisper-large)
     */
    void startImport(Integer modelId, String hubUrl);
}

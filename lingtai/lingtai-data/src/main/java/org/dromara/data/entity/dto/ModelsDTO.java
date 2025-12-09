package org.dromara.data.entity.dto;


import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.data.entity.ModelFiles;
import org.dromara.data.entity.Models;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Models DTO
 * 用于数据传输，包含创建和查询所需的字段
 */
@Data
@AutoMapper(target = Models.class)
public class ModelsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 版本号
     */
    private String version;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 状态：Active, Inactive, Error
     */
    private String status;

    /**
     * HuggingFace url
     */
    private String hubUrl;
    /**
     * 创建时间
     */
    private Timestamp createdTime;

    /**
     * 更新时间
     */
    private Timestamp updatedTime;

    /**
     * 模型文件类
     */
    private ModelFiles[] modelFiles;
}

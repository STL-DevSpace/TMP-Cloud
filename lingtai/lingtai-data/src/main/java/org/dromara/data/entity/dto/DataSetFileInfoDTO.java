package org.dromara.data.entity.dto;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.data.entity.DataSetsFiles;
import org.dromara.data.entity.ModelFiles;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@AutoMapper(target = DataSetsFiles.class)
public class DataSetFileInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件ID (主键)
     */
    private Integer id;

    /**
     * 【外键】关联的模型ID，指向 models.id
     */
    private Integer datasetId;

    /**
     * 文件在COS中的存储路径 (含 bucket)
     */
    private String fileUrl;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件大小 (字节)
     */
    private Long fileSize;

    /**
     * 文件类型/扩展名 (例如: bin, json, yaml)
     */
    private String fileType;

    /**
     * 是否是主文件 (1: 是, 0: 否)。用于快速识别模型的主要文件。
     */
    private Integer isPrimary;

    /**
     * 文件上传时间
     */
    private Timestamp createdTime;
    /**
     * 文件类型
     */
    private String dataType;

    // 您可以根据需要添加更多字段，例如：
    // private String hashMd5; // 文件的MD5校验码
}

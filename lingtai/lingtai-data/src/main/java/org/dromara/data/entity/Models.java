package org.dromara.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@TableName("models")
@Accessors(chain = true)
public class Models implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long userId;

    private Integer projectId;

    private String name;

    private String description;

    private String filePath;

    private String version;

    private Long size;

    private String classConfig; // JSON 字段，可以使用 String 或 JSONObject

    private String status;

    private BigDecimal progress;

    private BigDecimal loss;

    private BigDecimal accuracy;

    private Timestamp createdTime;

    private Timestamp updatedTime;

    private String hubUrl;
}


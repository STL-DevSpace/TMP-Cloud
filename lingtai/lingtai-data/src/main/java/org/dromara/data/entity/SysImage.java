package org.dromara.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 图片存储记录表实体类
 * 对应表：sys_image
 *
 * @author your-name
 * @date 2025-11-28
 */
@Data
@TableName("sys_image") // 映射数据库表名
//允许链式调用
@Accessors(chain = true)
public class SysImage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO) // 主键自增（匹配数据库AUTO_INCREMENT）
    private Long id;

    /**
     * 图片原始名称
     */
    private String imageName;

    /**
     * 图片大小（字节）
     */
    private Long imageSize;

    /**
     * 图片类型（如image/jpeg、image/png）
     */
    private String imageType;

    /**
     * 图片后缀（如jpg、png，不含点）
     */
    private String imageSuffix;

    /**
     * 对象存储路径（如：bucket/2025/11/28/xxx.jpg）
     */
    private String ossPath;

    /**
     * 图片访问URL
     */
    private String accessUrl;

    /**
     * 图片MD5（防重复上传）
     */
    private String md5;

    /**
     * 上传时间
     */
    private Date createTime;

    /**
     * 状态（1：正常 0：删除）
     */
    private Integer status;
    /**
     * 租户ID
     */
    private Long tenantId;
}

package org.dromara.common.oss.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传返回体
 *
 * @author Lion Li
 */
@Data
@Builder
@AllArgsConstructor // 生成 public 全参构造器
@NoArgsConstructor  // 生成 public 无参构造器（可选，按需添加）
public class UploadResult {

    /**
     * 文件路径
     */
    private String url;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 已上传对象的实体标记（用来校验文件）
     */
    private String eTag;

}

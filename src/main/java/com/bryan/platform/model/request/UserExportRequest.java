package com.bryan.platform.model.request;

import lombok.Getter;

import java.util.List;

/**
 * 用户导出请求对象
 *
 * @author Bryan Long
 * @since 2025/6/28 - 21:10
 * @version 1.0
 */
@Getter
public class UserExportRequest {
    /**
     * 要导出的字段列表
     */
    private List<String> fields;

    /**
     * 导出文件名（可选，默认为"用户数据"）
     */
    private String fileName;

    /**
     * 状态过滤（可选）
     * 0: 正常, 1: 封禁
     */
    private Integer status;
}

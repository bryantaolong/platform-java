package com.bryan.platform.model.request;

import lombok.Data;

import java.util.List;

/**
 * ClassName: UserExportRequest
 * Package: com.bryan.platform.model.request
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/28 - 21:10
 * Version: v1.0
 */
@Data
public class UserExportRequest {
    /**
     * 要导出的字段列表
     * 可选值：id, username, email, roles, status, createTime, updateTime
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

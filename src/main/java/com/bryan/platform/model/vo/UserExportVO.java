package com.bryan.platform.model.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户导出视图对象
 *
 * @author Bryan Long
 * @since 2025/6/28 - 21:12
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserExportVO {
    @ExcelProperty("用户ID")
    private Long id;

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("角色")
    private String roles;

    @ExcelProperty("状态")
    private String statusText; // 显示文本而不是数字

    @ExcelProperty("创建时间")
    private String createTime;

    @ExcelProperty("更新时间")
    private String updateTime;

    // 原始状态值（用于过滤，不导出）
    @ExcelIgnore
    private Integer status;
}
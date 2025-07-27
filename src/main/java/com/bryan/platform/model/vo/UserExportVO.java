package com.bryan.platform.model.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * UserExportVO 用户导出 VO
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/7/26
 */
@Data
public class UserExportVO {

    @ExcelProperty("用户ID")
    @ColumnWidth(10)
    private Long id;

    @ExcelProperty("用户名")
    @ColumnWidth(20)
    private String username;

    @ExcelProperty("手机号")
    @ColumnWidth(15)
    private String phoneNumber;

    @ExcelProperty("邮箱")
    @ColumnWidth(25)
    private String email;

    @ExcelProperty("性别")
    @ColumnWidth(10)
    private String gender;

    @ExcelProperty("状态")
    @ColumnWidth(10)
    private String status;

    @ExcelProperty("角色")
    @ColumnWidth(20)
    private String roles;

    @ExcelProperty("最后登陆时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

    @ExcelProperty("最后登录IP")
    @ColumnWidth(20)
    private LocalDateTime loginIp;

    @ExcelProperty("密码重置时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime passwordResetTime;

    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ExcelProperty("创建人")
    @ColumnWidth(15)
    private String createBy;

    @ExcelProperty("更新时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ExcelProperty("更新人")
    @ColumnWidth(15)
    private String updateBy;

    // 表头样式设置
    @HeadStyle(
            fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND,
            fillForegroundColor = 42,  // 浅绿色背景
            horizontalAlignment = HorizontalAlignmentEnum.CENTER  // 居中对齐
    )
    // 内容样式设置
    @ContentStyle(
            horizontalAlignment = HorizontalAlignmentEnum.CENTER  // 居中对齐
    )
    private String styledField; // 这个字段仅用于样式设置，实际不使用
}

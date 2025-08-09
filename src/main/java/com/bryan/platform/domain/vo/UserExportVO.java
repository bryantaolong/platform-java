package com.bryan.platform.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * UserExportVO 用户导出 VO
 *
 * @author Bryan Long
 */
@Data
@Builder
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
    private String loginIp;

    @ExcelProperty("密码重置时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime passwordResetTime;

    @ExcelProperty("登录失败次数")
    @ColumnWidth(15)
    private Integer loginFailCount;

    @ExcelProperty("账户锁定时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime accountLockTime;

    @ExcelProperty("删除状态")
    @ColumnWidth(12)
    private String deleted;

    @ExcelProperty("版本号")
    @ColumnWidth(10)
    private Integer version;

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

    /* =====================
       样式占位字段，不会被导出
       ===================== */
    @HeadStyle(
            fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND,
            fillForegroundColor = 42,
            horizontalAlignment = HorizontalAlignmentEnum.CENTER
    )
    @ContentStyle(
            horizontalAlignment = HorizontalAlignmentEnum.CENTER
    )
    private String styledField;

    /* ============================================================
       静态工具：根据 @ExcelProperty 自动生成 “字段名 -> 中文标题”
       ============================================================ */
    public static Map<String, String> getExportableFieldsByAnnotation() {
        Map<String, String> map = new LinkedHashMap<>();
        Field[] fields = UserExportVO.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ExcelProperty.class)) {
                ExcelProperty anno = field.getAnnotation(ExcelProperty.class);
                // 只取第一个标题
                map.put(field.getName(), anno.value()[0]);
            }
        }
        return map;
    }
}

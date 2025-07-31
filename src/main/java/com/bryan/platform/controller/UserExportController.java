package com.bryan.platform.controller;

import com.bryan.platform.model.request.UserExportRequest;
import com.bryan.platform.service.UserExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * UserExportController
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/7/26
 */
@RestController
@RequestMapping("/api/user/export")
@RequiredArgsConstructor
public class UserExportController {

    private final UserExportService userExportService;

    /**
     * 导出所有用户数据为 Excel 文件。
     * <p>仅管理员可操作。</p>
     *
     * @param response HttpServletResponse
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportAllUsers(HttpServletResponse response) {
        userExportService.exportAllFields(new UserExportRequest(), response);
    }

    /**
     * 根据指定条件导出用户数据为 Excel 文件。
     * <p>管理员可以通过请求体中的 UserExportRequest 对象指定需要导出的字段和筛选条件。</p>
     *
     * @param exportRequest UserExportRequest 包含导出字段和筛选条件的请求体
     * @param response HttpServletResponse 用于将 Excel 文件写入 HTTP 响应
     */
    @PostMapping("/fields")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportUsersByCondition(@RequestBody UserExportRequest exportRequest,
                                       HttpServletResponse response) {
        userExportService.exportUsersByFields(exportRequest, response);
    }

    /**
     * 获取所有可供导出的用户字段及其对应的中文描述。
     * <p>此接口返回一个Map，键为字段的英文名，值为对应的中文描述，方便前端展示和用户选择。</p>
     *
     * @return Map<String, String> 包含所有可导出字段的英文名和中文描述的映射
     */
    @GetMapping("/fields")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> getExportableFields() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("id", "用户ID");
        fields.put("username", "用户名");
        fields.put("phoneNumber", "手机号");
        fields.put("email", "邮箱");
        fields.put("gender", "性别");
        fields.put("status", "状态");
        fields.put("roles", "角色");
        fields.put("loginTime", "最后登陆时间");
        fields.put("loginIp", "最后登录IP");
        fields.put("passwordResetTime", "密码重置时间");
        fields.put("createTime", "创建时间");
        fields.put("createBy", "创建人");
        fields.put("updateTime", "更新时间");
        fields.put("updateBy", "更新人");
        return fields;
    }
}

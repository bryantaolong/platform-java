package com.bryan.platform.controller;

import com.bryan.platform.model.request.UserExportRequest;
import com.bryan.platform.model.response.Result;
import com.bryan.platform.service.UserExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * UserExportController
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/7/18
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
public class UserExportController {

    private final UserExportService userExportService;


    /**
     * 导出用户数据，支持字段选择。
     * <p>仅管理员可执行。</p>
     *
     * @param response HTTP 响应对象，用于写出导出文件
     * @param request  导出请求体，包含导出字段和筛选条件
     */
    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportUsers(HttpServletResponse response,
                            @RequestBody UserExportRequest request) {
        // 1. 调用服务执行导出
        userExportService.exportUsers(response, request);
    }

    /**
     * 导出所有用户数据，包含所有字段。
     * <p>仅管理员可执行。</p>
     *
     * @param response HTTP 响应对象
     * @param status   用户状态筛选，非必填
     * @param fileName 导出文件名，默认 "用户数据"
     */
    @GetMapping("/export/all")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportAllUsers(HttpServletResponse response,
                               @RequestParam(required = false) Integer status,
                               @RequestParam(required = false, defaultValue = "用户数据") String fileName) {
        // 1. 调用服务执行导出
        userExportService.exportAllUsers(response, status, fileName);
    }

    /**
     * 获取可供导出的字段列表，供前端动态选择。
     * <p>仅管理员可访问。</p>
     *
     * @return 字段名与中文描述的映射表
     */
    @GetMapping("/export/fields")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, String>> getExportFields() {
        // 1. 准备字段映射
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("id", "用户ID");
        fields.put("username", "用户名");
        fields.put("email", "邮箱");
        fields.put("roles", "角色");
        fields.put("statusText", "状态");
        fields.put("createTime", "创建时间");
        fields.put("updateTime", "更新时间");

        // 2. 返回字段映射结果
        return Result.success(fields);
    }
}


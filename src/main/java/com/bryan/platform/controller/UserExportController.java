package com.bryan.platform.controller;

import com.bryan.platform.model.request.UserExportRequest;
import com.bryan.platform.model.vo.UserExportVO;
import com.bryan.platform.service.user.UserExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
        return UserExportVO.getExportableFieldsByAnnotation();
    }
}

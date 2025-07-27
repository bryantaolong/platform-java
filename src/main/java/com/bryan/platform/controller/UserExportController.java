package com.bryan.platform.controller;

import com.bryan.platform.model.request.UserSearchRequest;
import com.bryan.platform.service.UserExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * UserExportController
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/7/18
 */
@RestController
@RequestMapping("/api/user/export")
@RequiredArgsConstructor
class UserExportController {

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
        userExportService.exportAllUsers(response);
    }

    @PostMapping("/condition")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportUsersByCondition(@RequestBody UserSearchRequest searchRequest,
                                       HttpServletResponse response) {
        userExportService.exportUsersByCondition(searchRequest, response);
    }
}

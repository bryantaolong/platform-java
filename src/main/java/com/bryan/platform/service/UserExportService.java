package com.bryan.platform.service;

import com.bryan.platform.dao.mapper.UserMapper;
import com.bryan.platform.model.request.UserExportRequest;
import com.bryan.platform.model.vo.UserExportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * UserExportService
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/7/18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserExportService {

    private final UserMapper userMapper;
    private final ExcelExportService excelExportService;

    private static final LinkedHashMap<String, String> EXPORT_FIELDS;

    static {
        EXPORT_FIELDS = new LinkedHashMap<>();
        EXPORT_FIELDS.put("id", "用户ID");
        EXPORT_FIELDS.put("username", "用户名");
        EXPORT_FIELDS.put("email", "邮箱");
        EXPORT_FIELDS.put("roles", "角色");
        EXPORT_FIELDS.put("statusText", "状态");
        EXPORT_FIELDS.put("createTime", "创建时间");
        EXPORT_FIELDS.put("updateTime", "更新时间");
    }


    /**
     * 导出用户数据（支持动态字段选择）。
     *
     * @param response HTTP 响应对象
     * @param request  导出请求参数（包含状态和字段）
     */
    public void exportUsers(HttpServletResponse response, UserExportRequest request) {
        // 1. 查询符合条件的用户数据
        List<Map<String, Object>> userData = userMapper.selectUsersForExport(request.getStatus());

        // 2. 转换状态字段为文字描述
        for (Map<String, Object> row : userData) {
            Integer status = (Integer) row.get("status");
            row.put("statusText", status != null && status == 0 ? "正常" : "封禁");
        }

        // 3. 过滤导出字段
        LinkedHashMap<String, String> selectedFields = new LinkedHashMap<>();
        if (request.getFields() == null || request.getFields().isEmpty()) {
            selectedFields.putAll(EXPORT_FIELDS);
        } else {
            for (String key : EXPORT_FIELDS.keySet()) {
                if (request.getFields().contains(key)) {
                    selectedFields.put(key, EXPORT_FIELDS.get(key));
                }
            }
        }

        // 4. 构建导出数据
        List<Map<String, Object>> filteredData = new ArrayList<>();
        for (Map<String, Object> row : userData) {
            Map<String, Object> filteredRow = new LinkedHashMap<>();
            for (String key : selectedFields.keySet()) {
                filteredRow.put(key, row.get(key));
            }
            filteredData.add(filteredRow);
        }

        // 5. 执行导出
        String fileName = (request.getFileName() == null || request.getFileName().isEmpty()) ? "用户数据" : request.getFileName();
        excelExportService.exportDynamicExcel(response, filteredData, selectedFields, fileName);
        log.info("导出用户数据完成，导出条数：{}", filteredData.size());
    }

    /**
     * 导出所有用户数据（字段固定，使用实体类）。
     *
     * @param response HTTP 响应对象
     * @param status   用户状态过滤条件
     * @param fileName 导出文件名
     */
    public void exportAllUsers(HttpServletResponse response, Integer status, String fileName) {
        // 1. 查询数据并封装为 VO
        List<UserExportVO> users = userMapper.selectUsersForExport(status).stream().map(map -> {
            UserExportVO vo = new UserExportVO();
            vo.setId(Long.valueOf(map.get("id").toString()));
            vo.setUsername((String) map.get("username"));
            vo.setEmail((String) map.get("email"));
            vo.setRoles((String) map.get("roles"));

            Integer s = (Integer) map.get("status");
            vo.setStatus(s);
            vo.setStatusText(s != null && s == 0 ? "正常" : "封禁");

            vo.setCreateTime((String) map.get("createTime"));
            vo.setUpdateTime((String) map.get("updateTime"));
            return vo;
        }).toList();

        // 2. 设置导出文件名
        String finalFileName = (fileName == null || fileName.isEmpty()) ? "用户数据" : fileName;

        // 3. 执行导出
        excelExportService.exportToExcel(response, users, UserExportVO.class, finalFileName);
        log.info("导出所有用户数据完成，条数：{}", users.size());
    }
}
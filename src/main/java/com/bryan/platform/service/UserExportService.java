package com.bryan.platform.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.dao.mapper.UserMapper;
import com.bryan.platform.model.converter.UserConverter;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.model.request.PageRequest;
import com.bryan.platform.model.request.UserSearchRequest;
import com.bryan.platform.model.vo.UserExportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

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
    private final UserService userService;

    /**
     * 导出所有用户数据
     */
    public void exportAllUsers(HttpServletResponse response) {
        try {
            // 设置响应头
            setupResponse(response, "用户数据导出");

            // 使用EasyExcel导出
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), UserExportVO.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("用户列表").build();

            // 分批查询写入
            int pageNum = 1;
            int pageSize = 10;
            int totalPages = 0; // 总页数
            int totalRecords = 0; // 总记录数
            List<User> users;

            // 先获取总页数
            Page<User> firstPage = new Page<>(1, pageSize);
            Page<User> firstResult = userMapper.selectPage(firstPage, new QueryWrapper<>());
            totalPages = (int) firstResult.getPages();

            do {
                log.info("正在处理第 {} 页/共 {} 页数据...", pageNum, totalPages);
                Page<User> page = new Page<>(pageNum, pageSize);
                Page<User> resultPage = userMapper.selectPage(page, new QueryWrapper<>());

                users = resultPage.getRecords();

                if (CollUtil.isNotEmpty(users)) {
                    List<UserExportVO> exportData = users.stream()
                            .map(UserConverter::toExportVO)
                            .collect(Collectors.toList());

                    excelWriter.write(exportData, writeSheet);
                    totalRecords += users.size();
                    log.info("已处理 {} 条数据", totalRecords);
                }

                pageNum++;

                // 明确终止条件：已处理完所有页
                if (pageNum > totalPages) {
                    break;
                }
            } while (true); // 改为无限循环，由内部break控制

            excelWriter.finish();
            log.info("导出完成，共导出 {} 条数据", totalRecords);
        } catch (IOException e) {
            log.error("导出用户数据失败", e);
            throw new RuntimeException("导出用户数据失败", e);
        }
    }

    /**
     * 按条件导出用户数据
     */
    public void exportUsersByCondition(UserSearchRequest searchRequest, HttpServletResponse response) {
        try {
            // 设置响应头
            setupResponse(response, "用户数据条件导出");

            // 使用EasyExcel导出
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), UserExportVO.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("用户列表").build();

            // 分批查询写入
            int pageNum = 1;
            int pageSize = 10;
            int totalRecords = 0;
            List<User> users;

            // 先获取总页数
            Page<User> firstPage = new Page<>(1, pageSize);
            Page<User> firstResult = userService.searchUsers(searchRequest,
                    new PageRequest(1, pageSize));
            int totalPages = (int) firstResult.getPages();

            do {
                log.info("正在处理条件导出的第 {} 页/共 {} 页...", pageNum, totalPages);
                users = userService.searchUsers(searchRequest,
                        new PageRequest(pageNum, pageSize)).getRecords();

                if (CollUtil.isNotEmpty(users)) {
                    List<UserExportVO> exportData = users.stream()
                            .map(UserConverter::toExportVO)
                            .collect(Collectors.toList());

                    excelWriter.write(exportData, writeSheet);
                    totalRecords += users.size();
                }

                pageNum++;

                if (pageNum > totalPages) {
                    break;
                }
            } while (true);

            excelWriter.finish();
            log.info("条件导出完成，共导出 {} 条数据", totalRecords);
        } catch (IOException e) {
            log.error("导出用户数据失败", e);
            throw new RuntimeException("导出用户数据失败", e);
        }
    }

    private void setupResponse(HttpServletResponse response, String fileName) throws IOException {
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
    }
}

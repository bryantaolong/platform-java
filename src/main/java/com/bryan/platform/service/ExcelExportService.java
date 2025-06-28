package com.bryan.platform.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: ExcelService
 * Package: com.bryan.platform.service
 * Description: EasyExcel 通用导出服务
 * Author: Bryan Long
 * Create: 2025/6/28 - 21:05
 * Version: v1.0
 */
@Service
public class ExcelExportService {

    /**
     * 导出数据到 Excel 文件（下载）
     *
     * @param response HTTP响应对象
     * @param data 要导出的数据列表
     * @param clazz 数据对应的实体类
     * @param fileName 文件名（不包含扩展名）
     * @param <T> 数据类型
     */
    public <T> void exportToExcel(HttpServletResponse response, List<T> data,
                                  Class<T> clazz, String fileName) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);

            // 使用 EasyExcel 写入数据
            EasyExcel.write(response.getOutputStream(), clazz)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 自动调整列宽
                    .sheet(fileName)
                    .doWrite(data);
        } catch (IOException e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    /**
     * 导出数据到本地 Excel 文件
     *
     * @param data 要导出的数据列表
     * @param clazz 数据对应的实体类
     * @param filePath 文件完整路径（包含文件名和扩展名）
     * @param <T> 数据类型
     */
    public <T> void exportToFile(List<T> data, Class<T> clazz, String filePath) {
        EasyExcel.write(filePath, clazz)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("Sheet1")
                .doWrite(data);
    }

    /**
     * 动态导出（不依赖实体类注解）
     *
     * @param response HTTP响应对象
     * @param data 要导出的数据列表
     * @param headers 表头映射（字段名 -> 显示名称）
     * @param fileName 文件名
     */
    public void exportDynamicExcel(HttpServletResponse response, List<Map<String, Object>> data,
                                   LinkedHashMap<String, String> headers, String fileName) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);

            // 构建表头
            List<List<String>> headList = new ArrayList<>();
            for (String displayName : headers.values()) {
                List<String> head = new ArrayList<>();
                head.add(displayName);
                headList.add(head);
            }

            // 构建数据
            List<List<Object>> dataList = new ArrayList<>();
            for (Map<String, Object> row : data) {
                List<Object> rowData = new ArrayList<>();
                for (String fieldName : headers.keySet()) {
                    rowData.add(row.get(fieldName));
                }
                dataList.add(rowData);
            }

            EasyExcel.write(response.getOutputStream())
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .head(headList)
                    .sheet(fileName)
                    .doWrite(dataList);
        } catch (IOException e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    /**
     * 从数据库查询并导出（需要配合具体的 DAO 使用）
     * 这里提供一个通用的模板方法
     */
    public interface DataProvider<T> {
        List<T> getData();
    }

    public <T> void exportFromDatabase(HttpServletResponse response, DataProvider<T> dataProvider,
                                       Class<T> clazz, String fileName) {
        List<T> data = dataProvider.getData();
        exportToExcel(response, data, clazz, fileName);
    }
}

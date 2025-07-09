package com.bryan.platform.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ExcelExportService 通用 Excel 导出服务
 * 基于 EasyExcel 实现多种场景下的 Excel 文件导出功能
 * 包括导出到 HTTP 响应（下载）、导出到本地文件及动态表头导出
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/6/28
 */
@Service
public class ExcelExportService {

    /**
     * 导出数据到 Excel 文件（通过 HTTP 响应实现文件下载）
     *
     * @param response HTTP 响应对象，用于输出 Excel 文件流
     * @param data 要导出的数据列表
     * @param clazz 数据对应的实体类类型
     * @param fileName 导出文件名（不含扩展名）
     * @param <T> 泛型数据类型
     * @throws RuntimeException 导出过程中发生 IO 异常时抛出
     */
    public <T> void exportToExcel(HttpServletResponse response, List<T> data,
                                  Class<T> clazz, String fileName) {
        try {
            // 1. 设置响应头，告知浏览器文件类型和编码格式，支持中文文件名
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);

            // 2. 使用 EasyExcel 写入数据，自动根据内容调整列宽
            EasyExcel.write(response.getOutputStream(), clazz)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet(fileName)
                    .doWrite(data);
        } catch (IOException e) {
            // 3. 发生异常时包装成运行时异常抛出，便于统一处理
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    /**
     * 导出数据到本地 Excel 文件
     *
     * @param data 要导出的数据列表
     * @param clazz 数据对应的实体类类型
     * @param filePath 本地文件完整路径，包含文件名和扩展名
     * @param <T> 泛型数据类型
     */
    public <T> void exportToFile(List<T> data, Class<T> clazz, String filePath) {
        // 1. 直接使用 EasyExcel 写入本地文件，自动调整列宽，默认 sheet 名称为 Sheet1
        EasyExcel.write(filePath, clazz)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("Sheet1")
                .doWrite(data);
    }

    /**
     * 动态导出 Excel（不依赖实体类注解，适合动态字段）
     *
     * @param response HTTP 响应对象，用于输出 Excel 文件流
     * @param data 要导出的数据列表，每条数据为字段名到值的映射
     * @param headers 表头映射，key 为字段名，value 为对应显示名称，保证有序
     * @param fileName 导出文件名（不含扩展名）
     * @throws RuntimeException 导出过程中发生 IO 异常时抛出
     */
    public void exportDynamicExcel(HttpServletResponse response, List<Map<String, Object>> data,
                                   LinkedHashMap<String, String> headers, String fileName) {
        try {
            // 1. 设置响应头，支持中文文件名下载
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);

            // 2. 构建表头列表，格式为 List<List<String>>，每个内层 List 代表一列的多级表头
            List<List<String>> headList = new ArrayList<>();
            for (String displayName : headers.values()) {
                List<String> head = new ArrayList<>();
                head.add(displayName);
                headList.add(head);
            }

            // 3. 构建数据列表，保持与表头字段顺序一致
            List<List<Object>> dataList = new ArrayList<>();
            for (Map<String, Object> row : data) {
                List<Object> rowData = new ArrayList<>();
                for (String fieldName : headers.keySet()) {
                    rowData.add(row.get(fieldName));
                }
                dataList.add(rowData);
            }

            // 4. 使用 EasyExcel 写入数据，自动列宽，动态表头导出
            EasyExcel.write(response.getOutputStream())
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .head(headList)
                    .sheet(fileName)
                    .doWrite(dataList);
        } catch (IOException e) {
            // 5. 发生异常时包装为运行时异常抛出
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    /**
     * 数据提供者接口，用于从数据库或其他数据源获取导出数据
     *
     * @param <T> 泛型数据类型
     */
    public interface DataProvider<T> {
        /**
         * 获取导出所需数据列表
         *
         * @return 数据列表
         */
        List<T> getData();
    }

    /**
     * 从数据库或其它数据源查询数据并导出 Excel
     *
     * @param response HTTP 响应对象，用于输出 Excel 文件流
     * @param dataProvider 数据提供者，实现数据查询逻辑
     * @param clazz 数据对应的实体类类型
     * @param fileName 导出文件名（不含扩展名）
     * @param <T> 泛型数据类型
     */
    public <T> void exportFromDatabase(HttpServletResponse response, DataProvider<T> dataProvider,
                                       Class<T> clazz, String fileName) {
        // 1. 通过数据提供者获取数据
        List<T> data = dataProvider.getData();

        // 2. 调用通用导出方法，将数据写入响应输出流
        exportToExcel(response, data, clazz, fileName);
    }
}

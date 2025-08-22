package com.bryan.platform.service.user;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.bryan.platform.domain.entity.user.SysUser;
import com.bryan.platform.exception.BusinessException;
import com.bryan.platform.domain.request.user.UserExportRequest;
import com.bryan.platform.domain.vo.UserExportVO;
import com.bryan.platform.domain.converter.UserConverter;
import com.bryan.platform.mapper.UserMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * UserExportService
 *
 * @author Bryan Long
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserExportService {

    private final UserMapper userMapper;

    /**
     * 导出所有字段（全量导出）
     *
     * @param exportRequest 可包含fileName和status过滤条件
     */
    public void exportAllFields(UserExportRequest exportRequest, HttpServletResponse response) {
        try {
            // 1. 设置响应头
            String fileName = Optional.ofNullable(exportRequest.getFileName())
                    .orElse("用户数据全量导出");
            setupResponse(response, fileName);

            // 2. 构建Excel写入器（不使用字段过滤）
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream())
                    .head(UserExportVO.class) // 自动包含所有@ExcelProperty字段
                    .registerWriteHandler(new CustomCellWriteHandler()) // 保持样式
                    .build();

            // 3. 执行导出（复用相同分批逻辑）
            WriteSheet writeSheet = EasyExcel.writerSheet("用户列表").build();
            executeBatchExport(excelWriter, writeSheet, exportRequest);

        } catch (IOException e) {
            throw new BusinessException("全量导出失败，请检查系统资源");
        }
    }

    /**
     * 按字段导出用户数据
     */
    public void exportUsersByFields(UserExportRequest exportRequest, HttpServletResponse response) {
        try {
            // 1. 校验字段名
            validateFieldNames(exportRequest.getFields());

            // 2. 设置响应头
            String fileName = Optional.ofNullable(exportRequest.getFileName())
                    .orElse("用户数据导出");
            setupResponse(response, fileName);

            // 3. 构建Excel写入器（包含动态字段和样式处理）
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream())
                    .head(UserExportVO.class)
                    .includeColumnFieldNames(exportRequest.getFields())
                    .registerWriteHandler(new CustomCellWriteHandler())
                    .build();

            // 4. 分批查询写入
            WriteSheet writeSheet = EasyExcel.writerSheet("用户列表").build();
            executeBatchExport(excelWriter, writeSheet, exportRequest);

        } catch (IOException e) {
            throw new BusinessException("用户数据导出失败，请稍后重试");
        }
    }

    /**
     * 配套方法1：字段名校验
     */
    private void validateFieldNames(List<String> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            throw new IllegalArgumentException("导出字段列表不能为空");
        }

        // 获取VO中所有有效字段
        Set<String> validFields = Arrays.stream(UserExportVO.class.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(ExcelProperty.class))
                .map(Field::getName)
                .collect(Collectors.toSet());

        // 校验请求字段
        fields.forEach(field -> {
            if (!validFields.contains(field)) {
                throw new IllegalArgumentException(
                        String.format("无效导出字段: %s (可用字段: %s)",
                                field, validFields));
            }
        });
    }

    /**
     * 配套方法2：自定义样式处理器
     */
    private static class CustomCellWriteHandler implements CellWriteHandler {
        @Override
        public void afterCellCreate(WriteSheetHolder writeSheetHolder,
                                    WriteTableHolder writeTableHolder,
                                    Cell cell,
                                    Head head,
                                    Integer relativeRowIndex,
                                    Boolean isHead) {
            if (isHead) {
                // 表头样式（保持与VO中@HeadStyle一致）
                CellStyle style = cell.getSheet().getWorkbook().createCellStyle();
                style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                style.setAlignment(HorizontalAlignment.CENTER);
                cell.setCellStyle(style);
            } else {
                // 内容样式（保持与VO中@ContentStyle一致）
                CellStyle style = cell.getSheet().getWorkbook().createCellStyle();
                style.setAlignment(HorizontalAlignment.CENTER);
                cell.setCellStyle(style);
            }
        }
    }

    // -------------------- 以下为内部辅助方法 --------------------

    /**
     * 执行分批导出
     */
    private void executeBatchExport(ExcelWriter excelWriter,
                                    WriteSheet writeSheet,
                                    UserExportRequest exportRequest) {
        int pageNum = 1;
        int pageSize = 1000;
        int totalExported = 0;

        while (true) {
            int offset = (pageNum - 1) * pageSize;
            List<SysUser> records = userMapper.selectPage(offset,
                    pageSize,
                    null,
                    exportRequest);
            if (CollectionUtils.isEmpty(records)) {
                break;
            }
            List<UserExportVO> vos = records.stream()
                    .map(UserConverter::toExportVO)
                    .toList();
            excelWriter.write(vos, writeSheet);
            totalExported += vos.size();
            log.info("已导出 {} 条数据", totalExported);
            pageNum++;
        }
        excelWriter.finish();
        log.info("导出完成，总计 {} 条数据", totalExported);
    }

    /**
     * 实体转换
     */
    private List<UserExportVO> convertToVO(List<SysUser> sysUsers) {
        return sysUsers.stream()
                .map(UserConverter::toExportVO)
                .collect(Collectors.toList());
    }

    /**
     * 响应头设置
     */
    private void setupResponse(HttpServletResponse response, String fileName) throws IOException {
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
    }
}

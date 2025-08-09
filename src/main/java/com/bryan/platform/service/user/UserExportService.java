package com.bryan.platform.service.user;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.bryan.platform.exception.BusinessException;
import com.bryan.platform.model.entity.user.User;
import com.bryan.platform.model.request.user.UserExportRequest;
import com.bryan.platform.model.vo.UserExportVO;
import com.bryan.platform.model.converter.UserConverter;
import com.bryan.platform.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

    private final UserRepository userRepository;

    /* ======== 导出所有字段 ======== */
    /**
     * 导出所有字段（全量导出）
     *
     * @param exportRequest 可包含fileName和status过滤条件
     */
    public void exportAllFields(UserExportRequest exportRequest, HttpServletResponse response) {
        export(exportRequest, response, null);
    }

    /* ======== 按指定字段导出 ======== */
    /**
     * 按字段导出用户数据
     */
    public void exportUsersByFields(UserExportRequest exportRequest,
                                    HttpServletResponse response) {
        validateFieldNames(exportRequest.getFields());
        export(exportRequest, response, exportRequest.getFields());
    }

    /* ======== 通用导出实现 ======== */
    private void export(UserExportRequest req,
                        HttpServletResponse resp,
                        List<String> includeFields) {

        try {
            String fileName = Optional.ofNullable(req.getFileName())
                    .orElse("用户数据导出");
            setupResponse(resp, fileName);

            ExcelWriter writer = EasyExcel.write(resp.getOutputStream(), UserExportVO.class)
                    .includeColumnFieldNames(includeFields)
                    .registerWriteHandler(new CustomCellWriteHandler())
                    .build();

            WriteSheet sheet = EasyExcel.writerSheet("用户列表").build();

            int pageSize = 500;
            int pageNumber = 0;
            long totalExported = 0;

            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                Slice<User> slice = userRepository.findAll(new UserRepository.UserExportSpec(req), pageable);

                List<UserExportVO> vos = convertToVO(slice.getContent());
                if (CollectionUtils.isEmpty(vos)) {
                    break;
                }

                writer.write(vos, sheet);
                totalExported += vos.size();
                log.info("已导出 {} 条数据", totalExported);

                if (!slice.hasNext()) {
                    break;
                }
                pageNumber++;
            }

            writer.finish();
            log.info("导出完成，总计 {} 条数据", totalExported);

        } catch (IOException e) {
            throw new BusinessException("用户数据导出失败，请稍后重试");
        }
    }

    /* ---------- 字段名校验 ---------- */
    /**
     * 配套方法：字段名校验
     */
    private void validateFieldNames(List<String> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            throw new IllegalArgumentException("导出字段列表不能为空");
        }
        Set<String> validFields = Arrays.stream(UserExportVO.class.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(
                        com.alibaba.excel.annotation.ExcelProperty.class))
                .map(Field::getName)
                .collect(Collectors.toSet());
        fields.forEach(f -> {
            if (!validFields.contains(f)) {
                throw new IllegalArgumentException(
                        String.format("无效导出字段: %s (可用字段: %s)", f, validFields));
            }
        });
    }

    /* ---------- VO 转换 ---------- */
    private List<UserExportVO> convertToVO(List<User> users) {
        return users.stream()
                .map(UserConverter::toExportVO)
                .collect(Collectors.toList());
    }

    /* ---------- 响应头设置 ---------- */
    private void setupResponse(HttpServletResponse resp, String fileName) throws IOException {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setCharacterEncoding("utf-8");
        resp.setHeader("Content-disposition",
                "attachment;filename*=utf-8''" + encoded + ".xlsx");
    }

    /* ---------- 样式处理器 ---------- */
    /**
     * 配套方法：自定义样式处理器
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
}

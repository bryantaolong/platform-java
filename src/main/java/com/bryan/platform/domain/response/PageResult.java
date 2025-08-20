package com.bryan.platform.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * PageResult 通用分页返回对象
 *
 * @author Bryan Long
 * @param <T> 数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 当前页数据 */
    private List<T> rows = Collections.emptyList();

    /** 总记录数 */
    private long total = 0;

    /** 当前页码 */
    private long pageNum = 1;

    /** 每页条数 */
    private long pageSize = 10;

    /** 总页数 */
    public long getPages() {
        if (total == 0 || pageSize == 0) {
            return 0;
        }
        return (total + pageSize - 1) / pageSize;
    }

    /** 是否为空页 */
    public boolean isEmpty() {
        return rows == null || rows.isEmpty();
    }

    /** 快速构造 */
    public static <T> PageResult<T> of(List<T> rows, long total, long pageNum, long pageSize) {
        return PageResult.<T>builder()
                .rows(rows)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }
}

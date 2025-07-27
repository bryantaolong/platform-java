package com.bryan.platform.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PageRequest
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/7/27
 */
@Getter
@AllArgsConstructor
public class PageRequest {
    private Integer pageNum = 1;

    private Integer pageSize = 10;
}

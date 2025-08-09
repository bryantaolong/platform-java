package com.bryan.platform.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PageRequest
 *
 * @author Bryan Long
 */
@Getter
@AllArgsConstructor
public class PageRequest {
    private Long pageNum;

    private Long pageSize;
}

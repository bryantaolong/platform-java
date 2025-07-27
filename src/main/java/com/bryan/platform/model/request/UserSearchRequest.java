package com.bryan.platform.model.request;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * UserSearchRequest
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/7/27
 */
@Getter
public class UserSearchRequest {
    private String username;

    private String phoneNumber;

    private String email;

    private Integer gender;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime createTimeStart;

    private LocalDateTime createTimeEnd;

    private LocalDateTime updateTime;

    private LocalDateTime updateTimeStart;

    private LocalDateTime updateTimeEnd;
}

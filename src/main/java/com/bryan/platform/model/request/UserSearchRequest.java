package com.bryan.platform.model.request;

import jakarta.validation.constraints.*;
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
    @Size(min = 2, max = 20, message = "用户名长度应在2-20个字符之间")
    private String username;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Min(value = 0, message = "性别不合法")
    @Max(value = 2, message = "性别不合法")
    private Integer gender;

    @Min(value = 0, message = "状态不合法")
    @Max(value = 1, message = "状态不合法")
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime createTimeStart;

    private LocalDateTime createTimeEnd;

    private LocalDateTime updateTime;

    private LocalDateTime updateTimeStart;

    private LocalDateTime updateTimeEnd;

    @AssertTrue(message = "创建时间范围不合法")
    public boolean isCreateTimeValid() {
        return createTimeStart == null || createTimeEnd == null || !createTimeEnd.isBefore(createTimeStart);
    }

    @AssertTrue(message = "更新时间范围不合法")
    public boolean isUpdateTimeValid() {
        return updateTimeStart == null || updateTimeEnd == null || !updateTimeEnd.isBefore(updateTimeStart);
    }
}
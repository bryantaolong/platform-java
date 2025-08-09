package com.bryan.platform.domain.request.user;

import com.bryan.platform.domain.enums.UserStatusEnum;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * UserSearchRequest
 *
 * @author Bryan Long
 */
@Getter
public class UserSearchRequest {
    // 用户名校验
    @Size(min = 2, max = 20, message = "用户名长度应在2-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", message = "用户名只能包含中文、字母、数字和下划线")
    private String username;

    // 手机号校验（更精确的中国手机号校验）
    @Pattern(regexp = "^(1[3-9]\\d{9})?$", message = "手机号格式不正确") // 允许空值
    private String phoneNumber;

    // 邮箱校验（更宽松的格式）
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @NotNull(message = "状态不能为空")
    private UserStatusEnum status;

    // 角色校验
    @Pattern(regexp = "^[a-zA-Z0-9_,-]*$", message = "角色格式不正确") // 允许字母、数字、逗号和横线
    private String roles;

    // 时间范围校验
    @PastOrPresent(message = "登录时间不能是未来时间")
    private LocalDateTime loginTime;

    // IP地址校验
    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$", message = "IP地址格式不正确")
    private String loginIp;

    @PastOrPresent(message = "密码重置时间不能是未来时间")
    private LocalDateTime passwordResetTime;

    // 登录失败次数校验
    @Min(value = 0, message = "登录失败次数不能为负数")
    @Max(value = 10, message = "登录失败次数超过最大值")
    private Integer loginFailCount;

    // 账户锁定时间校验
    @PastOrPresent(message = "账户锁定时间不能是未来时间")
    private LocalDateTime accountLockTime;

    // 删除标记校验
    @Min(value = 0, message = "删除标记不合法")
    @Max(value = 1, message = "删除标记不合法")
    private Integer deleted;

    // 乐观锁版本号校验
    @Min(value = 0, message = "版本号不能为负数")
    private Integer version;

    @PastOrPresent(message = "创建时间不能是未来时间")
    private LocalDateTime createTime;

    @PastOrPresent(message = "开始时间不能是未来时间")
    private LocalDateTime createTimeStart;

    @PastOrPresent(message = "结束时间不能是未来时间")
    private LocalDateTime createTimeEnd;

    @Size(max = 50, message = "创建人名称过长")
    private String createBy;

    @PastOrPresent(message = "更新时间不能是未来时间")
    private LocalDateTime updateTime;

    @PastOrPresent(message = "开始时间不能是未来时间")
    private LocalDateTime updateTimeStart;

    @PastOrPresent(message = "结束时间不能是未来时间")
    private LocalDateTime updateTimeEnd;

    @Size(max = 50, message = "更新人名称过长")
    private String updateBy;

    // 自定义校验方法
    @AssertTrue(message = "创建时间范围不合法")
    public boolean isCreateTimeValid() {
        if (createTimeStart == null || createTimeEnd == null) {
            return true;
        }
        return !createTimeEnd.isBefore(createTimeStart)
                && !createTimeEnd.isBefore(LocalDateTime.now().minusYears(10)) // 结束时间不能早于10年前
                && !createTimeStart.isAfter(LocalDateTime.now().plusDays(1)); // 开始时间不能晚于明天
    }

    @AssertTrue(message = "更新时间范围不合法")
    public boolean isUpdateTimeValid() {
        if (updateTimeStart == null || updateTimeEnd == null) {
            return true;
        }
        return !updateTimeEnd.isBefore(updateTimeStart)
                && !updateTimeEnd.isBefore(LocalDateTime.now().minusYears(10))
                && !updateTimeStart.isAfter(LocalDateTime.now().plusDays(1));
    }

    // 新增校验：如果指定了createTimeEnd，则必须指定createTimeStart
    @AssertTrue(message = "必须同时指定开始和结束时间")
    public boolean isCreateTimeRangeComplete() {
        return !(createTimeStart == null ^ createTimeEnd == null);
    }

    // 新增校验：如果指定了updateTimeEnd，则必须指定updateTimeStart
    @AssertTrue(message = "必须同时指定开始和结束时间")
    public boolean isUpdateTimeRangeComplete() {
        return !(updateTimeStart == null ^ updateTimeEnd == null);
    }
}

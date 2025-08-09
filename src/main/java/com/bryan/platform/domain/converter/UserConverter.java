package com.bryan.platform.domain.converter;

import com.bryan.platform.domain.enums.UserStatusEnum;
import com.bryan.platform.domain.entity.user.User;
import com.bryan.platform.domain.vo.UserExportVO;

/**
 * UserConvert
 *
 * @author Bryan Long
 */
public class UserConverter {

    public static UserExportVO toExportVO(User user) {
        if (user == null) {
            return null;
        }

        return UserExportVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .status(convertStatus(user.getStatus()))
                .roles(user.getRoles())
                .loginTime(user.getLoginTime())
                .loginIp(user.getLoginIp())
                .passwordResetTime(user.getPasswordResetTime())
                .loginFailCount(user.getLoginFailCount())
                .accountLockTime(user.getAccountLockTime())
                .deleted(convertDeletedStatus(user.getDeleted()))
                .createTime(user.getCreateTime())
                .createBy(user.getCreateBy())
                .updateTime(user.getUpdateTime())
                .updateBy(user.getUpdateBy())
                .build();
    }

    private static String convertStatus(UserStatusEnum status) {
        if (status == null) return "";
        return switch (status) {
            case NORMAL -> "正常";
            case BANNED -> "封禁";
            case LOCKED -> "锁定";
            // 如果以后再加枚举，保留 default 分支
            default -> "未知";
        };
    }

    private static String convertDeletedStatus(Integer deleted) {
        if (deleted == null) return "";
        return deleted == 0 ? "未删除" : "已删除";
    }
}

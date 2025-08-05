package com.bryan.platform.model.converter;

import com.bryan.platform.model.entity.user.User;
import com.bryan.platform.model.vo.UserExportVO;

/**
 * UserConvert
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/7/27
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

    private static String convertGender(Integer gender) {
        if (gender == null) return "";
        return gender == 1 ? "男" : "女";
    }

    private static String convertStatus(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "正常";
            case 1 -> "封禁";
            case 2 -> "锁定";
            default -> "未知";
        };
    }

    private static String convertDeletedStatus(Integer deleted) {
        if (deleted == null) return "";
        return deleted == 0 ? "未删除" : "已删除";
    }
}

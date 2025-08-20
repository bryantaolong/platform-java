package com.bryan.platform.domain.converter;

import com.bryan.platform.domain.entity.user.SysUser;
import com.bryan.platform.domain.enums.UserStatusEnum;
import com.bryan.platform.domain.vo.UserExportVO;

/**
 * UserConvert
 *
 * @author Bryan Long
 */
public class UserConverter {

    public static UserExportVO toExportVO(SysUser user) {
        if (user == null) {
            return null;
        }

        return UserExportVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .status(convertStatus(user.getStatus()))
                .roles(user.getRoles())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .passwordResetAt(user.getPasswordResetAt())
                .loginFailCount(user.getLoginFailCount())
                .lockedAt(user.getLockedAt())
                .deleted(convertDeletedStatus(user.getDeleted()))
                .createdAt(user.getCreatedAt())
                .createdBy(user.getCreatedBy())
                .updatedAt(user.getUpdatedAt())
                .updatedBy(user.getUpdatedBy())
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

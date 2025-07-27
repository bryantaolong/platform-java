package com.bryan.platform.model.converter;

import com.bryan.platform.model.entity.User;
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

        UserExportVO vo = new UserExportVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhoneNumber(user.getPhoneNumber());
        vo.setEmail(user.getEmail());
        vo.setGender(convertGender(user.getGender()));
        vo.setStatus(convertStatus(user.getStatus()));
        vo.setRoles(user.getRoles());
        vo.setLoginTime(user.getLoginTime());
        vo.setLoginIp(user.getLoginIp());
        vo.setPasswordResetTime(user.getPasswordResetTime());
        vo.setCreateTime(user.getCreateTime());
        vo.setCreateBy(user.getCreateBy());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setUpdateBy(user.getUpdateBy());

        return vo;
    }

    private static String convertGender(Integer gender) {
        if (gender == null) return "";
        return gender == 1 ? "男" : "女";
    }

    private static String convertStatus(Integer status) {
        if (status == null) return "";
        return status == 0 ? "正常" : "禁用";
    }
}

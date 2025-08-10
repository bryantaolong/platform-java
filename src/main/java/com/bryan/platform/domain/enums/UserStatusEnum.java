package com.bryan.platform.domain.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * UserStatusEnum 用户状态枚举
 *
 * @author Bryan Long
 */
@Getter
@AllArgsConstructor
public enum UserStatusEnum implements IEnum<Integer> {
    NORMAL(0, "正常"),
    BANNED(1, "封禁"),
    LOCKED(2, "锁定");

    private final Integer code;
    private final String desc;

    public static UserStatusEnum of(Integer code) {
        for (UserStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    @Override
    public Integer getValue() {
        return code;
    }
}

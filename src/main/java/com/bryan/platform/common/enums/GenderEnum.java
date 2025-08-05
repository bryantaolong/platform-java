package com.bryan.platform.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * GenderEnum 性别枚举
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/8/5
 */
@Getter
@AllArgsConstructor
public enum GenderEnum implements IEnum<Integer> {
    FEMALE(0, "女"),
    MALE(1, "男");

    private final Integer code;
    private final String desc;

    public static GenderEnum of(Integer code) {
        for (GenderEnum e : values()) {
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
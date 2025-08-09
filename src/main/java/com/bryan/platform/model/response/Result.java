package com.bryan.platform.model.response;

import com.bryan.platform.model.enums.HttpStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应封装
 *
 * @author Bryan Long
 */
@Data
@NoArgsConstructor
public class Result<T> {
    private int code;

    private String message;

    private T data;

    // 成功响应
    public static <T> Result<T> success(T data) {
        return new Result<>(HttpStatus.SUCCESS.getCode(), HttpStatus.SUCCESS.getMsg(), data);
    }

    // 错误响应（使用错误码枚举）
    public static <T> Result<T> error(HttpStatus httpStatus) {
        return new Result<>(httpStatus.getCode(), httpStatus.getMsg(), null);
    }

    // 错误响应（自定义消息）
    public static <T> Result<T> error(HttpStatus httpStatus, String message) {
        return new Result<>(httpStatus.getCode(), message, null);
    }

    // 全参构造（私有化，强制使用静态方法）
    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}

package com.bryan.platform.model.response;

import com.bryan.platform.common.enums.ErrorCode;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: Result
 * Package: com.bryan.platform.common
 * Description: 统一响应封装
 * Author: Bryan Long
 * Create: 2025/6/19 - 19:51
 * Version: v1.0
 */
@Data
@NoArgsConstructor
public class Result<T> {

    private int code;

    private String message;

    private T data;

    // 成功响应
    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg(), data);
    }

    // 错误响应（使用错误码枚举）
    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMsg(), null);
    }

    // 错误响应（自定义消息）
    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null);
    }

    // 全参构造（私有化，强制使用静态方法）
    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
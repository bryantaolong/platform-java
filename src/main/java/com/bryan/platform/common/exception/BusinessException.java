package com.bryan.platform.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ClassName: BusinessException
 * Package: com.bryan.platform.common.exception
 * Description: 通用业务异常类。
 * 用于封装和抛出在业务逻辑处理过程中发生的特定业务错误。
 * 通常对应 HTTP 状态码 500 (Internal Server Error) 或根据具体业务规则映射到其他状态码。
 * 例如，订单状态冲突、数据校验失败等。
 * Author: Bryan Long
 * Create: 2025/6/19 - 20:30
 * Version: v1.0
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class BusinessException extends RuntimeException {

    /**
     * 构造一个新的 BusinessException 实例，并附带详细的错误信息。
     *
     * @param message 异常的详细信息（通常是用户友好的错误描述）。
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * 构造一个新的 BusinessException 实例，附带详细的错误信息和导致此异常的根本原因。
     *
     * @param message 异常的详细信息。
     * @param cause 导致此异常的 Throwable 对象。
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

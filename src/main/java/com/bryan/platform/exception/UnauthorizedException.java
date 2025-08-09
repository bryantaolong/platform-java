package com.bryan.platform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * UnauthorizedException 未授权/未认证异常类。
 * 用于封装和抛出当用户尝试访问需要认证的资源，但其未提供有效认证凭据（例如，Token缺失、无效或过期）时发生的异常。
 * 通过 {@code @ResponseStatus(HttpStatus.UNAUTHORIZED)} 直接绑定 HTTP 状态码 401 (Unauthorized)。
 *
 * @author Bryan Long
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED) // 将此异常直接映射到 HTTP 401 状态码
public class UnauthorizedException extends RuntimeException {

    /**
     * 构造一个新的 UnauthorizedException 实例，并附带详细的错误信息。
     *
     * @param message 异常的详细信息（通常描述认证失败的原因）。
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * 构造一个新的 UnauthorizedException 实例，附带详细的错误信息和导致此异常的根本原因。
     *
     * @param message 异常的详细信息。
     * @param cause   导致此异常的 Throwable 对象。
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}

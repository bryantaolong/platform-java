package com.bryan.platform.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * HttpStatus
 * 统一错误码枚举，定义了系统中常见的业务错误代码和对应的消息。
 *
 * @author Bryan Long
 */
@Getter
@AllArgsConstructor
public enum HttpStatus {
    /**
     * 操作成功。
     */
    SUCCESS(200, "成功"),

    /**
     * 请求参数错误或无效。
     */
    BAD_REQUEST(400, "参数错误"),

    /**
     * 未经授权的访问，通常指用户未登录或Token无效。
     */
    UNAUTHORIZED(401, "未授权"),

    /**
     * 禁止访问，用户已认证但无权限执行此操作。
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 请求的资源不存在。
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 服务器内部错误或未知异常。
     */
    INTERNAL_ERROR(500, "服务异常");

    /**
     * 错误码。
     */
    private final int code;

    /**
     * 错误消息。
     */
    private final String msg;
}

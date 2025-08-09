package com.bryan.platform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ResourceNotFoundException 资源不存在异常类。
 * 用于封装和抛出当请求的资源（例如用户、博文、评论等）在系统中不存在时发生的异常。
 * 此异常通常会被全局异常处理器捕获，并映射为 HTTP 状态码 404 (Not Found)。
 *
 * @author Bryan Long
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 构造一个新的 ResourceNotFoundException 实例，并附带详细的错误信息。
     *
     * @param message 异常的详细信息（通常描述哪个资源不存在）。
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 构造一个新的 ResourceNotFoundException 实例，附带详细的错误信息和导致此异常的根本原因。
     *
     * @param message 异常的详细信息。
     * @param cause 导致此异常的 Throwable 对象。
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

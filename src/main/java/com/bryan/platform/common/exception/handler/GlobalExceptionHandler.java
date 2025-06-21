package com.bryan.platform.common.exception.handler;

import com.bryan.platform.common.exception.BusinessException;
import com.bryan.platform.common.exception.ResourceNotFoundException;
import com.bryan.platform.common.exception.UnauthorizedException;
import com.bryan.platform.model.Result;
import com.bryan.platform.common.constant.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * ClassName: GlobalExceptionHandler
 * Package: com.bryan.platform.common.exception
 * Description: 全局异常处理器（替代 WebMvcConfig 中的异常处理逻辑）
 * Author: Bryan Long
 * Create: 2025/6/19 - 20:02
 * Version: v1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(HttpServletRequest request, RuntimeException e) {
        log.error("请求URL: {}, 业务异常: {}", request.getRequestURL(), e.getMessage(), e);
        return Result.error(ErrorCode.INTERNAL_ERROR, "服务繁忙，请稍后重试");
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", errorMsg);
        return Result.error(ErrorCode.BAD_REQUEST, errorMsg);
    }

    /**
     * 处理404异常（需配合Spring Boot的ErrorController）
     */
    @ExceptionHandler(ResourceNotFoundException.class) // 自定义异常示例
    public Result<String> handleNotFoundException(ResourceNotFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        return Result.error(ErrorCode.NOT_FOUND);
    }

    /**
     * 处理业务异常（HTTP 500）
     */
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return Result.error(ErrorCode.INTERNAL_ERROR, e.getMessage());
    }

    /**
     * 处理未授权异常（HTTP 401）
     * 注意：由于类上有@ResponseStatus，实际会优先使用注解的HTTP状态码
     */
    @ExceptionHandler(UnauthorizedException.class)
    public Result<String> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("未授权访问: {}", e.getMessage());
        return Result.error(ErrorCode.UNAUTHORIZED, e.getMessage());
    }
}
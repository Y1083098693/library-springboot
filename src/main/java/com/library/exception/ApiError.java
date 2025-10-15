package com.library.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * 自定义API错误异常类
 * 用于封装API接口的错误信息，包含状态码和详细描述
 */
@Data
public class ApiError extends RuntimeException {
    // 错误状态码（对应HTTP状态码，如400、404、500等）
    private int status;
    // 错误详细信息（用于前端展示或问题排查）
    private String message;

    /**
     * 无参构造器
     * 保留以兼容可能的默认实例化场景
     */
    public ApiError() {
        super();
    }

    /**
     * 带状态码和消息的构造器
     * 核心构造器，用于快速创建包含错误信息的异常实例
     * @param status 错误状态码
     * @param message 错误详细信息
     */
    public ApiError(int status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }

    /**
     * 带状态码、消息和根因异常的构造器
     * 用于异常链追踪，保留原始异常信息
     * @param status 错误状态码
     * @param message 错误详细信息
     * @param cause 根因异常
     */
    public ApiError(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.message = message;
    }

    /**
     * 基于HTTP状态枚举的构造器
     * 方便直接使用HttpStatus枚举创建异常
     * @param status HTTP状态枚举
     * @param message 错误详细信息
     */
    public ApiError(HttpStatus status, String message) {
        this(status.value(), message);
    }
}
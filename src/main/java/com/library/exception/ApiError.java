package com.library.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * 自定义API错误异常类
 * 补充带参构造器，支持传入状态码和错误信息
 */
@Data // Lombok注解，自动生成getter/setter
public class ApiError extends RuntimeException {
    // 错误状态码（如404、400、500）
    private int status;
    // 错误详细信息
    private String message;

    // 1. 无参构造器（保留，兼容原有可能的调用）
    public ApiError() {
        super();
    }

    // 2. 带参构造器（核心修复：支持传入状态码和错误信息）
    public ApiError(int status, String message) {
        super(message); // 调用父类RuntimeException的带参构造器，传递错误信息
        this.status = status; // 赋值状态码
        this.message = message; // 赋值错误信息
    }

    // 3. 可选：支持传入状态码、错误信息、根因异常（用于异常链追踪）
    public ApiError(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.message = message;
    }
}
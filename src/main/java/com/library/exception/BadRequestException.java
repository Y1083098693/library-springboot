package com.library.exception;

/**
 * 请求参数错误异常（如邮箱已占用、生日格式错误）
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
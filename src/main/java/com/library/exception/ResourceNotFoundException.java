package com.library.exception;

/**
 * 资源不存在异常（如用户不存在、地址不存在）
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
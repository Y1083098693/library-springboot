package com.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一捕获并处理ApiError等自定义异常
 */
@ControllerAdvice // 全局生效
public class GlobalExceptionHandler {

    // 专门处理ApiError异常
    @ExceptionHandler(ApiError.class)
    public ResponseEntity<Map<String, Object>> handleApiError(ApiError ex) {
        // 构建结构化错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString()); // 错误发生时间
        errorResponse.put("status", ex.getStatus()); // 错误状态码
        errorResponse.put("error", HttpStatus.valueOf(ex.getStatus()).getReasonPhrase()); // 状态码描述（如Not Found）
        errorResponse.put("message", ex.getMessage()); // 自定义错误信息
        errorResponse.put("path", "/api/books"); // 可通过request获取真实请求路径，此处为示例

        // 返回对应HTTP状态码的响应
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getStatus()));
    }
}
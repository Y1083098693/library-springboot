package com.library.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应格式封装类
 * 用于标准化所有接口的响应格式，包含状态、消息和数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    // 操作是否成功（true/false）
    private boolean success;

    // 响应消息（成功/错误描述）
    private String message;

    // 响应数据（成功时返回的业务数据，失败时可为null）
    private T data;

    // 快捷构造成功响应（带数据）
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // 快捷构造成功响应（无数据）
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    // 快捷构造失败响应（带错误消息）
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
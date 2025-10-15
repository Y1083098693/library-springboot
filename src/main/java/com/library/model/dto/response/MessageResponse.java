package com.library.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageResponse {
    private boolean success;
    private String message;
    private Object data; // 新增：用于返回额外数据（如avatarUrl、ID等）

    // 原有2参数构造（兼容旧用法）
    public MessageResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null; // 无数据时默认为null
    }

    // 快捷创建：成功且无数据
    public static MessageResponse success(String message) {
        return new MessageResponse(true, message);
    }

    // 快捷创建：成功且有数据（新增，用于返回avatarUrl等）
    public static MessageResponse success(String message, Object data) {
        return new MessageResponse(true, message, data);
    }

    // 快捷创建：失败且无数据
    public static MessageResponse error(String message) {
        return new MessageResponse(false, message);
    }

    // 快捷创建：失败且有数据（可选）
    public static MessageResponse error(String message, Object data) {
        return new MessageResponse(false, message, data);
    }
}
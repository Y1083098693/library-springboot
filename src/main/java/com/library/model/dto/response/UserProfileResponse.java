package com.library.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户个人资料响应DTO
 * 字段类型与Controller传入参数严格匹配（日期统一为String）
 */
@Data
@AllArgsConstructor
public class UserProfileResponse {
    // 参数顺序需与Controller构造时的顺序完全一致
    private Long id;          // 用户ID
    private String username;  // 用户名
    private String nickname;  // 昵称
    private String email;     // 邮箱
    private String phone;     // 手机号
    private String bio;       // 个人简介
    private String avatarUrl; // 头像URL
    private String gender;    // 性别（与User实体一致：String）
    private String birthDate; // 生日（String类型，格式：yyyy-MM-dd）
    private Integer points;   // 积分
    private String createdAt; // 创建时间（String类型，格式：yyyy-MM-dd HH:mm:ss）
}
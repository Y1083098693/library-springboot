package com.library.model.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String bio;
    private String avatarUrl;
    private String gender;
    private String birthDate; // 日期字符串（yyyy-MM-dd）
    private Integer points;
    private String createdAt; // 时间字符串（yyyy-MM-dd HH:mm:ss）
}
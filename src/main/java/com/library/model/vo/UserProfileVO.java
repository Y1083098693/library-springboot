package com.library.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String bio;
    private String avatarUrl;
    private Integer gender;
    private LocalDateTime birthDate;
    private Integer points;
    private LocalDateTime createdAt;
}
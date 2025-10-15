package com.library.model.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String avatarUrl;
    private String createdAt;
}
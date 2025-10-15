package com.library.model.dto.response;

import com.library.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data

public class JwtResponse {
    private String token;
    private String refreshToken; // 新增刷新令牌字段
    private Long id;
    private String username;
    private String email;
    private Integer points;

    // 原有构造器：仅支持访问令牌
    public JwtResponse(String token, User user) {
        this.token = token;
        this.refreshToken = null;
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.points = user.getPoints();
    }

    // 新增构造器：支持访问令牌+刷新令牌+用户信息
    public JwtResponse(String token, String refreshToken, Long id, String username, String email, Integer points) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.points = points;
    }

    // 简化构造器：用于登录和刷新令牌场景
    public JwtResponse(String token, String refreshToken, Long id, String username) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = null;
        this.points = null;
    }
}
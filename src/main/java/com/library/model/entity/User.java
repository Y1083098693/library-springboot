package com.library.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 存储系统用户的基本信息、认证信息及个人资料
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username; // 用户名（登录账号）

    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // 密码哈希值

    @Column(name = "email", unique = true, length = 100)
    private String email; // 电子邮箱

    @Column(name = "phone", length = 20)
    private String phone; // 手机号码

    @Column(name = "nickname", length = 50)
    private String nickname; // 昵称

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl; // 头像URL

    @Column(name = "gender", length = 10)
    private String gender; // 性别（男/女/未知）

    @Column(name = "birth_date")
    private LocalDateTime birthDate; // 出生日期

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio; // 个人简介

    @Column(name = "points", nullable = false)
    private Integer points = 0; // 积分（默认0）

    @Column(name = "status", nullable = false)
    private String status = "active"; // 账号状态（active/inactive/locked）

    @Column(name = "role", nullable = false)
    private String role = "USER"; // 角色（USER/ADMIN）

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 创建时间

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 更新时间

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
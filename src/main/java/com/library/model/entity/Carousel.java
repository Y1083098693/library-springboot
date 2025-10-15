package com.library.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 轮播图实体类
 * 用于存储首页轮播展示的图片、链接及相关信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carousels")
public class Carousel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl; // 轮播图片URL

    @Column(name = "title", length = 100)
    private String title; // 轮播标题（可为空）

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 轮播描述（可为空）

    @Column(name = "link", length = 255)
    private String link; // 点击跳转链接（可为空）

    @Column(name = "button_text", length = 50)
    private String buttonText; // 按钮文本（可为空）

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder; // 排序序号，越小越靠前

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true; // 是否启用，默认为true

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
package com.library.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 图书分类实体类
 * 用于存储图书分类信息，支持多级分类结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, name = "name")
    private String name; // 分类名称

    @Column(unique = true, length = 100, nullable = false, name = "slug")
    private String slug; // 分类标识（URL友好的名称）

    @Column(name = "parent_id")
    private Long parentId; // 父分类ID，顶级分类为null

    @Column(length = 500, name = "description")
    private String description; // 分类描述

    @Column(name = "display_order")
    private Integer displayOrder; // 显示顺序，用于排序

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // 是否激活，默认为true

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
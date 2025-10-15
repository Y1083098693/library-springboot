package com.library.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 图书实体类
 * 存储图书的基本信息、价格、库存、分类等核心数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20, nullable = false, name = "isbn")
    private String isbn; // 国际标准书号，唯一标识

    @Column(nullable = false, length = 200, name = "title")
    private String title; // 图书标题

    @Column(length = 200, name = "subtitle")
    private String subtitle; // 图书副标题

    @Column(nullable = false, length = 100, name = "author")
    private String author; // 作者

    @Column(length = 100, name = "translator")
    private String translator; // 译者（可为空）

    @Column(nullable = false, length = 100, name = "publisher")
    private String publisher; // 出版社

    @Column(name = "publish_date")
    private LocalDateTime publishDate; // 出版日期

    @Column(length = 20, name = "language")
    private String language; // 语言版本

    @Column(name = "pages")
    private Integer pages; // 页数

    @Column(name = "category_id", nullable = false)
    private Long categoryId; // 所属分类ID

    @Column(name = "original_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal originalPrice; // 原价

    @Column(name = "selling_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal sellingPrice; // 售价

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0; // 库存数量，默认为0

    @Column(name = "sales_volume")
    private Integer salesVolume = 0; // 销售量，默认为0

    @Column(name = "cover_image", nullable = false)
    private String coverImage; // 封面图片URL

    @Column(columnDefinition = "TEXT", name = "description")
    private String description; // 图书简介

    @Column(columnDefinition = "TEXT", name = "details")
    private String details; // 详细内容

    @Column(name = "author_intro", columnDefinition = "TEXT")
    private String authorIntro; // 作者简介

    @Column(name = "is_hot")
    private Boolean isHot = false; // 是否热门图书

    @Column(name = "is_new")
    private Boolean isNew = false; // 是否新书

    @Column(name = "is_recommended")
    private Boolean isRecommended = false; // 是否推荐

    @Column(precision = 3, scale = 2, name = "rating")
    private BigDecimal rating = BigDecimal.ZERO; // 评分，默认为0

    @Column(name = "review_count")
    private Integer reviewCount = 0; // 评论数量，默认为0

    @Column(name = "popularity")
    private Integer popularity = 0; // 人气值，用于排序

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
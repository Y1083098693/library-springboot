package com.library.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // 显式导入Lombok注解

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20, nullable = false)
    private String isbn;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 200)
    private String subtitle;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(length = 100)
    private String translator;

    @Column(nullable = false, length = 100)
    private String publisher;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(length = 20)
    private String language;

    private Integer pages;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "original_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal originalPrice;

    @Column(name = "selling_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal sellingPrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "sales_volume")
    private Integer salesVolume = 0;

    @Column(name = "cover_image", nullable = false)
    private String coverImage;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "author_intro", columnDefinition = "TEXT")
    private String authorIntro;

    @Column(name = "is_hot")
    private Boolean isHot = false;

    @Column(name = "is_new")
    private Boolean isNew = false;

    @Column(name = "is_recommended")
    private Boolean isRecommended = false;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
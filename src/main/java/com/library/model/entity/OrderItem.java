package com.library.model.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项实体类
 * 补充Service调用的setter方法对应的字段
 */
@Data
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId; // 关联订单ID

    @Column(name = "book_id", nullable = false)
    private Long bookId; // 关联图书ID

    // 图书标题（匹配Service的setBookTitle调用）
    @Column(name = "book_title", nullable = false)
    private String bookTitle;

    // 图书封面（匹配Service的setBookCover调用）
    @Column(name = "book_cover")
    private String bookCover;

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // 购买数量

    // 单价（BigDecimal类型，匹配Service的setUnitPrice调用）
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
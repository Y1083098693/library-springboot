package com.library.model.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 * 记录用户收藏的图书信息
 */
@Data
@Entity
@Table(name = "wishlist", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "book_id"})
})
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 关联用户ID

    @Column(name = "book_id", nullable = false)
    private Long bookId; // 关联图书ID

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 自动设置创建时间
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
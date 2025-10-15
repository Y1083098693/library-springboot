package com.library.model.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 字段类型、方法与OrderService调用严格匹配
 */
@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", unique = true, nullable = false)
    private String orderNo; // 订单编号（如20240520123456）

    @Column(name = "user_id", nullable = false)
    private Long userId; // 关联用户ID

    @Column(name = "address_id", nullable = false)
    private Long addressId; // 关联收货地址ID

    // 金额字段用BigDecimal（匹配Service中的totalAmount类型）
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "final_amount", nullable = false)
    private BigDecimal finalAmount;

    // 订单状态：用枚举（匹配Service中"pending"等状态）
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    // 支付方式（新增字段，匹配Service的setPaymentMethod调用）
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 订单状态枚举（定义Service中用到的"pending"、"cancelled"等）
    public enum OrderStatus {
        PENDING, // 待支付
        PAID,    // 已支付
        SHIPPED, // 已发货
        COMPLETED, // 已完成
        CANCELLED // 已取消
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // 生成订单编号（示例：时间戳+随机数）
        this.orderNo = "ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
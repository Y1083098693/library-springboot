package com.library.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户地址实体类
 * 关键：规范字段命名+确保Lombok正确生成getter/setter
 */
@Data // 必须添加：Lombok注解，自动生成getter/setter/toString等
@Entity
@Table(name = "user_addresses")
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false)
    private String recipientPhone;

    @Column(name = "province", nullable = false)
    private String province;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "district", nullable = false)
    private String district;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    // 核心修复1：字段命名为isDefault（符合JavaBean规范）
    // Lombok会自动生成：setIsDefault()和isIsDefault()/getIsDefault()
    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 核心修复2：若Lombok失效，手动添加getter/setter（双保险）
    public boolean isDefault() { // boolean类型的getter规范是isXxx()
        return isDefault;
    }

    public void setDefault(boolean isDefault) { // setter规范是setXxx()
        this.isDefault = isDefault;
    }
}
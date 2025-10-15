package com.library.model.dto;

/**
 * 用户地址DTO（完全手动编写，不依赖Lombok）
 */
public class UserAddressDTO {
    // 字段定义
    private Long id;
    private String recipientName;
    private String recipientPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private boolean isDefault; // 核心字段：是否默认地址
    private String createdAt;
    private String updatedAt;

    // 手动添加所有字段的getter和setter（重点：isDefault的setter）
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    // 核心修复：手动实现isDefault的getter和setter
    public boolean isDefault() { // boolean类型的getter用isXxx()
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) { // 显式定义setIsDefault方法
        this.isDefault = isDefault;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
package com.library.model.dto;

/**
 * 用户地址DTO
 * 用于用户地址信息的传输，与前端交互时使用
 */
public class UserAddressDTO {
    private Long id;

    private String recipientName;  // 收件人姓名

    private String recipientPhone; // 收件人电话

    private String province;       // 省份

    private String city;           // 城市

    private String district;       // 区县

    private String detailAddress;  // 详细地址

    private boolean isDefault;     // 是否为默认地址

    private String createdAt;      // 创建时间（字符串格式，便于前端展示）

    private String updatedAt;      // 更新时间（字符串格式，便于前端展示）

    // 手动生成所有getter和setter
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

    public boolean isDefault() {
        return isDefault;
    }

    // 关键修复：显式定义setIsDefault方法
    public void setIsDefault(boolean isDefault) {
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
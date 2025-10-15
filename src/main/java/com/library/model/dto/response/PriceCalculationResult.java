package com.library.model.dto.response; // 注意：根据代码结构放在dto包更合适

import java.math.BigDecimal;

/**
 * 图书价格计算结果DTO，用于封装售价、原价和折扣率
 */
public class PriceCalculationResult {
    private final BigDecimal price;         // 最终售价
    private final BigDecimal originalPrice; // 原价
    private final int discount;             // 折扣率（整数，如85表示85折）

    public PriceCalculationResult(BigDecimal price, BigDecimal originalPrice, int discount) {
        this.price = price;
        this.originalPrice = originalPrice;
        this.discount = discount;
    }

    // getter方法
    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public int getDiscount() {
        return discount;
    }
}
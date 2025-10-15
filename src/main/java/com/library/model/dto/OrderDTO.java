package com.library.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String orderNo;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;
    private String paymentMethod;
    private String addressName;
    private String addressPhone;
    private String addressDetail;
    private String createdAt;
    private List<OrderItemDTO> items;
}
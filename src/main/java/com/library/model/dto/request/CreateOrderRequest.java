package com.library.model.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Long addressId;
    private String paymentMethod;
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long bookId;
        private Integer quantity;
    }
}
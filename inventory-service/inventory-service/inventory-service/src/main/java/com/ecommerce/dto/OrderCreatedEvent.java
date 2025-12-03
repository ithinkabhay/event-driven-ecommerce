package com.ecommerce.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreatedEvent {

    private Long orderId;
    private String customerUsername;
    private BigDecimal totalAmount;
    private List<OrderItemEvent> items;

    @Data
    public static class OrderItemEvent{
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
    }
}

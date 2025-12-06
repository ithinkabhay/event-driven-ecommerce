package com.ecommerce.dto;

import com.ecommerce.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderResponse {

    private Long id;

    private String customerUsername;

    private OrderStatus status;

    private BigDecimal totalAmount;

    private List<OrderItemResponse> items;

    private Instant createdAt;

    private Instant updatedAt;


    @Data
    @Builder
    public static class OrderItemResponse{
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
    }
}

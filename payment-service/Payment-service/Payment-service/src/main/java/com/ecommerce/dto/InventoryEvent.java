package com.ecommerce.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryEvent {

    private Long orderId;
    private InventoryEventType type;
    private BigDecimal amount;
    private String reason;
}

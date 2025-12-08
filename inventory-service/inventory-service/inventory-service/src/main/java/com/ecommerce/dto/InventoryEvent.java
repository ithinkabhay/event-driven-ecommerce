package com.ecommerce.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InventoryEvent {

    private Long orderId;
    private InventoryEventType type;
    private BigDecimal amount;
    private String reason;
}

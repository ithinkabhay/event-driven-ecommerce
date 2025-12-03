package com.ecommerce.dto;

import lombok.Data;

@Data
public class InventoryEvent {

    private Long orderId;
    private InventoryEventType type;
    private String reason;
}

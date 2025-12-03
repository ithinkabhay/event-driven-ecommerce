package com.ecommerce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryEvent {

    private Long orderId;
    private InventoryEventType type;
    private String reason;
}

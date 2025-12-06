package com.ecommerce.dto;

import com.ecommerce.enums.InventoryEventType;
import lombok.Data;

@Data
public class InventoryEvent {

    private Long orderId;
    private InventoryEventType type;
    private String reason;
}

package com.ecommerce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatusEvent {

    private Long orderId;
    private String status;
    private String reason;
}

package com.ecommerce.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentEvent {

    private Long orderId;
    private PaymentEventType type;
    private BigDecimal amount;
    private String reason;

}

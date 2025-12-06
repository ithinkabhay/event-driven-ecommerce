package com.ecommerce.dto;

import com.ecommerce.enums.PaymentEventType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentEvent {

    private Long orderId;
    private PaymentEventType type;
    private BigDecimal amount;
    private String reason;
}

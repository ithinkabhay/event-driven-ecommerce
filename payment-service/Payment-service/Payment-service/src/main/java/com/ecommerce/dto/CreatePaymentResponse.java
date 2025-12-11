package com.ecommerce.dto;

import com.stripe.param.PaymentIntentCreateParams;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePaymentResponse {
    private String clientSecret;
//    private Long OrderId;
}

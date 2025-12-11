package com.ecommerce.controller;

import com.ecommerce.dto.CreatePaymentRequest;
import com.ecommerce.dto.CreatePaymentResponse;
import com.ecommerce.service.PaymentService;
import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @RequestBody CreatePaymentRequest paymentRequest) throws StripeException {

        CreatePaymentResponse response = paymentService.createPaymentIntent(paymentRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        try {
            paymentService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok("received");
        } catch (Exception e) {
            log.error("Error handling Stripe webhook", e);
            return ResponseEntity.status(400).body("error");
        }
    }
}

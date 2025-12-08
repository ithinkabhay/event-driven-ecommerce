package com.ecommerce.service;

import com.ecommerce.dto.InventoryEvent;
import com.ecommerce.dto.InventoryEventType;
import com.ecommerce.dto.PaymentEvent;
import com.ecommerce.dto.PaymentEventType;
import com.ecommerce.kafka.PaymentEventProducer;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentEventProducer eventProducer;
    private final Random random = new Random();

    @Value("${stripe.currency}")
    private String currency;

    public void processInventoryEvent(InventoryEvent event) {
        log.info("Processing InventoryEvent for orderId={} type={}", event.getOrderId(), event.getType());

        if (event.getType() == InventoryEventType.INVENTORY_REJECTED) {
            // If inventory was rejected, no payment to process.
            log.info("Skipping payment for orderId={} because inventory was rejected (reason={})",
                    event.getOrderId(), event.getReason());
            return;
        }

        if (event.getAmount() == null){
            log.warn("InventoryEvent Amount is null for OrderId+{}, skipping stripe payment", event.getOrderId());
            return;
        }

        // Inventory reserved -> simulate payment
        BigDecimal amount = event.getAmount(); //

        Long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValueExact();

        boolean success = false; // ~80% success rate
        String failureReason = null;

        try {
            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            .setAmount(amountInCents)
                            .setCurrency(currency)
                            .setPaymentMethod("pm_card_visa")
                            .setConfirm(true)
                            .build();

            PaymentIntent intent = PaymentIntent.create(params);

            log.info("Stripe PaymentIntent created: id={}, status={}, amount={}",
                    intent.getId(), intent.getStatus(), intent.getAmount());

            if ("success".equals(intent.getStatus())){
                success = true;
            }else {
                failureReason = "Stripe payment status: " + intent.getStatus();
            }
        }catch (StripeException e){
            log.error("Stripe Payment fails for orderId={}, error={}", event.getOrderId(), e.getMessage(), e);
            failureReason = "Stripe error: " + e.getMessage();
        }

        PaymentEvent paymentEvent;
        if (success){
            log.info("Payment succeeded for orderId={} amount={}", event.getOrderId(), amount);

            paymentEvent = PaymentEvent.builder()
                    .orderId(event.getOrderId())
                    .type(PaymentEventType.PAYMENT_SUCCEEDED)
                    .amount(amount)
                    .reason(null)
                    .build();
        }else {
            if (failureReason == null){
                failureReason = "Unknown payment failure";
            }
            log.warn("Payment fails for orderId={} reason={}",event.getOrderId(), failureReason);
            paymentEvent = PaymentEvent.builder()
                    .orderId(event.getOrderId())
                    .type(PaymentEventType.PAYMENT_FAILED)
                    .amount(amount)
                    .reason(failureReason)
                    .build();
        }

        eventProducer.sendPaymentEvent(paymentEvent);
    }

}
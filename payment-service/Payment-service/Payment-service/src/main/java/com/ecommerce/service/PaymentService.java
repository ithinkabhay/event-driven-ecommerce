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

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentEventProducer eventProducer;

    @Value("${stripe.currency}")
    private String currency;

    public void processInventoryEvent(InventoryEvent event) {
        log.info("Processing InventoryEvent for orderId={} type={}", event.getOrderId(), event.getType());

        // 1. Skip if inventory rejected
        if (event.getType() == InventoryEventType.INVENTORY_REJECTED) {
            log.info("Skipping payment for orderId={} because inventory was rejected (reason={})",
                    event.getOrderId(), event.getReason());
            return;
        }

        // 2. Ensure amount is present
        if (event.getAmount() == null) {
            log.warn("InventoryEvent amount is null for orderId={}, skipping Stripe payment", event.getOrderId());
            return;
        }

        BigDecimal amount = event.getAmount();
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValueExact();

        boolean success = false;
        String failureReason = null;

        try {
            // 3. Build PaymentIntent params (disable redirect methods)
            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            .setAmount(amountInCents)
                            .setCurrency(currency)
                            .setAutomaticPaymentMethods(
                                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                            .setEnabled(true)
                                            .setAllowRedirects(
                                                    PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER
                                            )
                                            .build()
                            )
                            .setPaymentMethod("pm_card_visa") // Stripe test payment method
                            .setConfirm(true)
                            .build();

            // 4. Call Stripe
            PaymentIntent intent = PaymentIntent.create(params);

            log.info("Stripe PaymentIntent created: id={}, status={}, amount={}",
                    intent.getId(), intent.getStatus(), intent.getAmount());

            // ✅ THIS WAS THE BUG: Stripe uses "succeeded", not "success"
            if ("succeeded".equalsIgnoreCase(intent.getStatus())) {
                success = true;
                failureReason = null;
            } else {
                success = false;
                failureReason = "Stripe payment status: " + intent.getStatus();
            }

        } catch (StripeException e) {
            log.error("Stripe payment fails for orderId={}, error={}", event.getOrderId(), e.getMessage(), e);
            success = false;
            failureReason = "Stripe error: " + e.getMessage();
        }

        // 5. Send PaymentEvent back to Kafka
        PaymentEvent paymentEvent;
        if (success) {
            log.info("Payment succeeded for orderId={} amount={}", event.getOrderId(), amount);

            paymentEvent = PaymentEvent.builder()
                    .orderId(event.getOrderId())
                    .type(PaymentEventType.PAYMENT_SUCCEEDED)
                    .amount(amount)
                    .reason(null)
                    .build();
        } else {
            if (failureReason == null) {
                failureReason = "Unknown payment failure";
            }
            log.warn("Payment fails for orderId={} reason={}", event.getOrderId(), failureReason);

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
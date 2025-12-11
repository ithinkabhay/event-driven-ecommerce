package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.kafka.PaymentEventProducer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
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

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

//    public void processInventoryEvent(InventoryEvent event) {
//        log.info("Processing InventoryEvent for orderId={} type={}", event.getOrderId(), event.getType());
//
//        // 1. Skip if inventory rejected
//        if (event.getType() == InventoryEventType.INVENTORY_REJECTED) {
//            log.info("Skipping payment for orderId={} because inventory was rejected (reason={})",
//                    event.getOrderId(), event.getReason());
//            return;
//        }
//
//        // 2. Ensure amount is present
//        if (event.getAmount() == null) {
//            log.warn("InventoryEvent amount is null for orderId={}, skipping Stripe payment", event.getOrderId());
//            return;
//        }
//
//        BigDecimal amount = event.getAmount();
//        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValueExact();
//
//        boolean success = false;
//        String failureReason = null;
//
//        try {
//            // 3. Build PaymentIntent params (disable redirect methods)
//            PaymentIntentCreateParams params =
//                    PaymentIntentCreateParams.builder()
//                            .setAmount(amountInCents)
//                            .setCurrency(currency)
//                            .setAutomaticPaymentMethods(
//                                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
//                                            .setEnabled(true)
//                                            .setAllowRedirects(
//                                                    PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER
//                                            )
//                                            .build()
//                            )
//                            .setPaymentMethod("pm_card_visa") // Stripe test payment method
//                            .setConfirm(true)
//                            .build();
//
//            // 4. Call Stripe
//            PaymentIntent intent = PaymentIntent.create(params);
//
//            log.info("Stripe PaymentIntent created: id={}, status={}, amount={}",
//                    intent.getId(), intent.getStatus(), intent.getAmount());
//
//            // ✅ THIS WAS THE BUG: Stripe uses "succeeded", not "success"
//            if ("succeeded".equalsIgnoreCase(intent.getStatus())) {
//                success = true;
//                failureReason = null;
//            } else {
//                success = false;
//                failureReason = "Stripe payment status: " + intent.getStatus();
//            }
//
//        } catch (StripeException e) {
//            log.error("Stripe payment fails for orderId={}, error={}", event.getOrderId(), e.getMessage(), e);
//            success = false;
//            failureReason = "Stripe error: " + e.getMessage();
//        }
//
//        // 5. Send PaymentEvent back to Kafka
//        PaymentEvent paymentEvent;
//        if (success) {
//            log.info("Payment succeeded for orderId={} amount={}", event.getOrderId(), amount);
//
//            paymentEvent = PaymentEvent.builder()
//                    .orderId(event.getOrderId())
//                    .type(PaymentEventType.PAYMENT_SUCCEEDED)
//                    .amount(amount)
//                    .reason(null)
//                    .build();
//        } else {
//            if (failureReason == null) {
//                failureReason = "Unknown payment failure";
//            }
//            log.warn("Payment fails for orderId={} reason={}", event.getOrderId(), failureReason);
//
//            paymentEvent = PaymentEvent.builder()
//                    .orderId(event.getOrderId())
//                    .type(PaymentEventType.PAYMENT_FAILED)
//                    .amount(amount)
//                    .reason(failureReason)
//                    .build();
//        }
//
//        eventProducer.sendPaymentEvent(paymentEvent);
//    }

//    public CreatePaymentResponse processPaymentEvent(CreatePaymentRequest request) throws  StripeException {
//
//        BigDecimal amount = request.getAmount();
//        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValueExact();
//
//        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
//                .setAmount(amountInCents)
//                .setCurrency(currency)
//                .setAutomaticPaymentMethods(
//                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
//                                .setEnabled(true)
//                                .setAllowRedirects(
//                                        PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER
//                                ).build()
//                ).build();
//
//        PaymentIntent intent = PaymentIntent.create(params);
//        log.info("Created PaymentIntent form: id={} clientSecret={}", intent.getId(), intent.getClientSecret() );
//
//        return new CreatePaymentResponse(intent.getClientSecret());
//
//    }

    public CreatePaymentResponse createPaymentIntent(CreatePaymentRequest request) throws  StripeException {

        BigDecimal amount = request.getAmount();
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValueExact();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .putMetadata("orderId", request.getOrderId().toString())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        log.info("Created PaymentIntent form: id={} clientSecret={}", intent.getId(), intent.getClientSecret() );
        return new CreatePaymentResponse(intent.getClientSecret());
    }

    // Handle stripe webhook payload and signature

    public void handleWebhook(String payload, String sigHeader) throws Exception {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.info("Invalid Stripe webhook signature", e);
            throw e;
        }

        String eventType = event.getType();
        log.info("Received stripe event type: {}", eventType);

        switch (eventType) {
            case "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event);
            case "payment_intent.payment_failed" -> handlePaymentIntentFailed(event);
            default -> log.info("Unhandled Stripe event type: {}", eventType);
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentInfo info = extractPaymentInfo(event, "payment_intent.succeeded");
        if (info == null) {
            return;
        }

        log.info("Payment succeeded for orderId={}, amount={}, intentId={}",
                info.orderId, info.amount, info.intentId);

        PaymentEvent paymentEvent = PaymentEvent.builder()
                .orderId(info.orderId)
                .type(PaymentEventType.PAYMENT_SUCCEEDED)
                .amount(info.amount)
                .reason(null)
                .build();

        eventProducer.sendPaymentEvent(paymentEvent);
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentInfo info = extractPaymentInfo(event, "payment_intent.payment_failed");
        if (info == null) {
            return;
        }

        log.info("Payment failed for orderId={}, intentId={}", info.orderId, info.intentId);

        PaymentEvent failedEvent = PaymentEvent.builder()
                .orderId(info.orderId)
                .type(PaymentEventType.PAYMENT_FAILED)
                .amount(info.amount)
                .reason("payment_intent.payment_failed")
                .build();

        eventProducer.sendPaymentEvent(failedEvent);
    }

    /**
     * Extract the minimal fields we need from the Stripe Event:
     * - orderId (from metadata.orderId)
     * - amount
     * - intentId
     */
    private PaymentInfo extractPaymentInfo(Event event, String expectedType) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        // 1) Try normal Stripe deserialization first
        if (deserializer.getObject().isPresent() && deserializer.getObject().get() instanceof PaymentIntent pi) {
            Long orderId = null;
            String orderIdStr = pi.getMetadata().get("orderId");
            if (orderIdStr != null && !orderIdStr.isBlank()) {
                orderId = Long.parseLong(orderIdStr);
            }

            BigDecimal amount = BigDecimal.valueOf(pi.getAmount()).divide(BigDecimal.valueOf(100));
            return new PaymentInfo(orderId, amount, pi.getId());
        }

        // 2) Fallback to raw JSON + Jackson
        String rawJson = deserializer.getRawJson();
        log.warn("{} received but data object null or not PaymentIntent. Raw data: {}",
                expectedType, rawJson);
        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawJson);

            String orderIdStr = root.path("metadata").path("orderId").asText(null);
            Long orderId = (orderIdStr != null && !orderIdStr.isBlank())
                    ? Long.parseLong(orderIdStr)
                    : null;

            long amountLong = root.path("amount").asLong();
            BigDecimal amount = BigDecimal.valueOf(amountLong).divide(BigDecimal.valueOf(100));

            String intentId = root.path("id").asText(null);

            return new PaymentInfo(orderId, amount, intentId);
        } catch (Exception ex) {
            log.error("Failed to parse PaymentIntent from raw JSON", ex);
            return null;
        }
    }

    // small internal helper record
    private record PaymentInfo(Long orderId, BigDecimal amount, String intentId) {}
}
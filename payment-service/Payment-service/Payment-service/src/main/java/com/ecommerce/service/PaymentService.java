package com.ecommerce.service;

import com.ecommerce.dto.InventoryEvent;
import com.ecommerce.dto.InventoryEventType;
import com.ecommerce.dto.PaymentEvent;
import com.ecommerce.dto.PaymentEventType;
import com.ecommerce.kafka.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentEventProducer eventProducer;
    private final Random random = new Random();

    public void processInventoryEvent(InventoryEvent event) {
        log.info("Processing InventoryEvent for orderId={} type={}", event.getOrderId(), event.getType());

        if (event.getType() == InventoryEventType.INVENTORY_REJECTED) {
            // If inventory was rejected, no payment to process.
            log.info("Skipping payment for orderId={} because inventory was rejected (reason={})",
                    event.getOrderId(), event.getReason());
            return;
        }

        // Inventory reserved -> simulate payment
        BigDecimal amount = BigDecimal.valueOf(100); // For demo, a fixed amount or later derive from order

        boolean success = random.nextDouble() > 0.2; // ~80% success rate

        PaymentEvent paymentEvent;
        if (success) {
            log.info("Payment succeeded for orderId={}", event.getOrderId());
            paymentEvent = PaymentEvent.builder()
                    .orderId(event.getOrderId())
                    .type(PaymentEventType.PAYMENT_SUCCEEDED)
                    .amount(amount)
                    .reason(null)
                    .build();
        } else {
            String reason = "Payment gateway error";
            log.warn("Payment failed for orderId={} reason={}", event.getOrderId(), reason);
            paymentEvent = PaymentEvent.builder()
                    .orderId(event.getOrderId())
                    .type(PaymentEventType.PAYMENT_FAILED)
                    .amount(amount)
                    .reason(reason)
                    .build();
        }

        eventProducer.sendPaymentEvent(paymentEvent);
    }
}
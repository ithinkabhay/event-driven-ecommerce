package com.ecommerce.kafka;

import com.ecommerce.dto.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "payments";

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentEvent(PaymentEvent event){

        log.info("Sending PaymentEvent to topic {} for orderId={} type={}", TOPIC, event.getOrderId(), event.getType());
        kafkaTemplate.send(TOPIC, event.getOrderId().toString(), event);
    }
}

package com.ecommerce.kafka;

import com.ecommerce.dto.OrderStatusEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderStatusEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderStatusEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private static final String TOPIC = "notifications";

    public void sendOrderStatusEvent(OrderStatusEvent event){

        log.info("Sending order Status Event to topic {} for orderId={} status={}"
        ,TOPIC, event.getOrderId(), event.getStatus());
        kafkaTemplate.send(TOPIC, event.getOrderId().toString(), event);
    }
}

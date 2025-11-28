package com.ecommerce.kafka;

import com.ecommerce.dto.OrderCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private static final String Topic = "orders";

    public void sendOrderCreatedEvent(OrderCreatedEvent event){

        kafkaTemplate.send(Topic, event.getOrderId().toString(), event);
    }
}

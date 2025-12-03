package com.ecommerce.kafka;

import com.ecommerce.dto.InventoryEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private static final String TOPIC = "Inventory";

    public void sendInventoryEvent(InventoryEvent event){
        kafkaTemplate.send(TOPIC, event.getOrderId().toString(), event);
    }
}

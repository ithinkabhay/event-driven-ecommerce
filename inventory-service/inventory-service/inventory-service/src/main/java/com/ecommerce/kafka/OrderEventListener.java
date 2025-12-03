package com.ecommerce.kafka;

import com.ecommerce.dto.OrderCreatedEvent;
import com.ecommerce.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventListener {

    private final InventoryService inventoryService;

    public OrderEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "orders", groupId = "inventory-service")
    public void handleOrderCreated(OrderCreatedEvent event){
        log.info("Received OrderCreatedEvent from kafka: orderId={}", event.getOrderId());
        inventoryService.processOrderCreated(event);
    }
}

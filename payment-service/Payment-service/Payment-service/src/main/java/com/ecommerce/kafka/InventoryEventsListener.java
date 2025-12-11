package com.ecommerce.kafka;

import com.ecommerce.service.PaymentService;
import com.ecommerce.dto.InventoryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryEventsListener {

    private final PaymentService paymentService;

    public InventoryEventsListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "inventory", groupId = "paymentService")
    public void handleInventoryEvent(InventoryEvent inventoryEvent){

        log.info("Received InventoryEvent from kafka: orderID={} type={}",
                inventoryEvent.getOrderId(), inventoryEvent.getType());
//        paymentService.processInventoryEvent(inventoryEvent);

    }
}

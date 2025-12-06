package com.ecommerce.kafka;

import com.ecommerce.dto.InventoryEvent;
import com.ecommerce.dto.PaymentEvent;
import com.ecommerce.service.OrderWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventListener {

    private final OrderWorkflowService orderWorkflowService;

    public OrderEventListener(OrderWorkflowService orderWorkflowService) {
        this.orderWorkflowService = orderWorkflowService;
    }

    @KafkaListener(topics = "inventory", groupId = "order-service-inventory")
    public void handleInventoryEvent(InventoryEvent event){
        log.info("Order service received InventoryEvent: orderId={} type={}", event.getOrderId()
        , event.getType());

        orderWorkflowService.handleEnventoryEvent(event);
    }

    @KafkaListener(topics = "payments", groupId = "order-service-payments")
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Order-service received PaymentEvent: orderId={} type={}",
                event.getOrderId(), event.getType());
        orderWorkflowService.handlePaymentEvent(event);
    }
}

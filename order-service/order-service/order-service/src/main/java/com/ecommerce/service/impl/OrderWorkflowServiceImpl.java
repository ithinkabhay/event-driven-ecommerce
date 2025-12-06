package com.ecommerce.service.impl;

import com.ecommerce.dto.InventoryEvent;
import com.ecommerce.dto.OrderStatusEvent;
import com.ecommerce.dto.PaymentEvent;
import com.ecommerce.entity.Order;
import com.ecommerce.enums.InventoryEventType;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentEventType;
import com.ecommerce.kafka.OrderStatusEventProducer;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.service.OrderWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class OrderWorkflowServiceImpl implements OrderWorkflowService {

    private final OrderRepository orderRepository;
    private final OrderStatusEventProducer statusEventProducer;

    public OrderWorkflowServiceImpl(OrderRepository orderRepository, OrderStatusEventProducer statusEventProducer) {
        this.orderRepository = orderRepository;
        this.statusEventProducer = statusEventProducer;
    }

    @Override
    public void handleEnventoryEvent(InventoryEvent inventoryEvent) {

        Order order = orderRepository.findById(inventoryEvent.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("order not found: " + inventoryEvent.getOrderId()));

        if (inventoryEvent.getType() == InventoryEventType.INVENTORY_REJECTED){
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);

            OrderStatusEvent statusEvent = OrderStatusEvent.builder()
                    .orderId(order.getId())
                    .status(OrderStatus.CANCELED.name())
                    .reason("Inventory rejected:" + inventoryEvent.getReason())
                    .build();

            statusEventProducer.sendOrderStatusEvent(statusEvent);
        } else if (inventoryEvent.getType() == InventoryEventType.INVENTORY_RESERVED) {

            try {
                OrderStatus.valueOf("RESERVED"); // check if exists
                order.setStatus(OrderStatus.valueOf("RESERVED"));
                orderRepository.save(order);
            } catch (IllegalArgumentException ignored) {
                // RESERVED not present, keep as PENDING
            }

        }
    }

    @Transactional
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Handling PaymentEvent for orderId={} type={}", event.getOrderId(), event.getType());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

        if (event.getType() == PaymentEventType.PAYMENT_SUCCEEDED) {
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);

            OrderStatusEvent statusEvent = OrderStatusEvent.builder()
                    .orderId(order.getId())
                    .status(OrderStatus.COMPLETED.name())
                    .reason(null)
                    .build();

            statusEventProducer.sendOrderStatusEvent(statusEvent);

        } else if (event.getType() == PaymentEventType.PAYMENT_FAILED) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            OrderStatusEvent statusEvent = OrderStatusEvent.builder()
                    .orderId(order.getId())
                    .status(OrderStatus.CANCELLED.name())
                    .reason("Payment failed: " + event.getReason())
                    .build();

            statusEventProducer.sendOrderStatusEvent(statusEvent);
        }
    }
}

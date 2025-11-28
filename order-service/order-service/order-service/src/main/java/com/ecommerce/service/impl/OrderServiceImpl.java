package com.ecommerce.service.impl;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderCreatedEvent;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.kafka.OrderEventProducer;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.OrderItem;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.service.OrderService;
import jakarta.transaction.Transactional;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;

    public OrderServiceImpl(OrderRepository orderRepository, OrderEventProducer eventProducer) {
        this.orderRepository = orderRepository;
        this.eventProducer = eventProducer;
    }


    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Authentication auth) {

        String username = auth.getName();

        // calculate total
        BigDecimal total = request.getItems().stream().map(i -> i.getPrice()
                .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setCustomerUsername(username);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(total);

        var items = request.getItems().stream().map(i -> OrderItem.builder()
                .productId(i.getProductId())
                .quantity(i.getQuantity())
                .price(i.getPrice())
                .order(order)
                .build()

        ).toList();

        order.setItems(items);

        Order saved = orderRepository.save(order);

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(saved.getId())
                .customerUsername(saved.getCustomerUsername())
                .totalAmount(saved.getTotalAmount())
                .items(
                        items.stream().map(oi -> OrderCreatedEvent.OrderItemEvent.builder()
                                .productId(oi.getProductId())
                                .quantity(oi.getQuantity())
                                .price(oi.getPrice())
                                .build()
                        ).collect(Collectors.toList())
                ).build();

        eventProducer.sendOrderCreatedEvent(event);

        return mapToResponse(saved);
    }

    @Override
    public OrderResponse getOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerUsername(order.getCustomerUsername())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(
                        order.getItems().stream()
                                .map(oi -> OrderResponse.OrderItemResponse.builder()
                                        .productId(oi.getProductId())
                                        .quantity(oi.getQuantity())
                                        .price(oi.getPrice())
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }
}

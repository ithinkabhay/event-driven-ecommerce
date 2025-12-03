package com.ecommerce.service.impl;

import com.ecommerce.dto.InventoryEvent;
import com.ecommerce.dto.InventoryEventType;
import com.ecommerce.dto.OrderCreatedEvent;
import com.ecommerce.entity.Product;
import com.ecommerce.kafka.InventoryEventProducer;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.InventoryService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final InventoryEventProducer eventProducer;

    public InventoryServiceImpl(ProductRepository productRepository, InventoryEventProducer eventProducer) {
        this.productRepository = productRepository;
        this.eventProducer = eventProducer;
    }


    @Override
    @Transactional
    public void processOrderCreated(OrderCreatedEvent event) {

        log.info("Processing OrderCreatedEvent for orderId={}, items={}",
                event.getOrderId(), event.getItems().size());

        for (OrderCreatedEvent.OrderItemEvent item : event.getItems()){
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProductId()));


            if (product.getAvailableQuantity() < item.getQuantity()){

                String reason = "Not enough stock for product: " + item.getProductId();
                log.warn("Inventory rejected for orderId={}, reason={}",event.getOrderId(), reason);

                InventoryEvent invEvent = InventoryEvent.builder()
                        .orderId(event.getOrderId())
                        .type(InventoryEventType.INVENTORY_REJECTED)
                        .reason(reason)
                        .build();

                eventProducer.sendInventoryEvent(invEvent);
                return;
            }

        }

        for (OrderCreatedEvent.OrderItemEvent item : event.getItems()){

            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProductId()));


            product.setAvailableQuantity(product.getAvailableQuantity() - item.getQuantity());
            productRepository.save(product);

        }

        log.info("Inventory reserved for orderId={}", event.getOrderId());

        InventoryEvent invEvent = InventoryEvent.builder()
                .orderId(event.getOrderId())
                .type(InventoryEventType.INVENTORY_RESERVED)
                .reason(null)
                .build();

        eventProducer.sendInventoryEvent(invEvent);



    }
}

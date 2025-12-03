package com.ecommerce.service;

import com.ecommerce.dto.OrderCreatedEvent;

public interface InventoryService {

    public void processOrderCreated(OrderCreatedEvent event);
}

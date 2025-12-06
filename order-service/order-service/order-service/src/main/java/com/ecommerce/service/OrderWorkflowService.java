package com.ecommerce.service;

import com.ecommerce.dto.InventoryEvent;
import com.ecommerce.dto.PaymentEvent;

public interface OrderWorkflowService {

    public void handleEnventoryEvent(InventoryEvent inventoryEvent);
    public void handlePaymentEvent(PaymentEvent event);
}

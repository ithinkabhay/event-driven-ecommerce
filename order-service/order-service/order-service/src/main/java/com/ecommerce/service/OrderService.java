package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import org.springframework.security.core.Authentication;

public interface OrderService {

    public OrderResponse createOrder(CreateOrderRequest request, Authentication auth);

    public OrderResponse getOrder(Long id);

}

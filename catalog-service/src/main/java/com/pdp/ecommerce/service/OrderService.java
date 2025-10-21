package com.pdp.ecommerce.service;

import com.pdp.ecommerce.dto.OrderRequest;
import com.pdp.ecommerce.dto.OrderResponse;

public interface OrderService {

	OrderResponse getOrder(Long orderId);
	OrderResponse placeOrder(OrderRequest request);
}
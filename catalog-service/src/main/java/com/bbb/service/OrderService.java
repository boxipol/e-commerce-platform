package com.bbb.service;

import com.bbb.dto.OrderRequest;
import com.bbb.dto.OrderResponse;

public interface OrderService {

	OrderResponse getOrder(Long orderId);
	OrderResponse placeOrder(OrderRequest request);
}
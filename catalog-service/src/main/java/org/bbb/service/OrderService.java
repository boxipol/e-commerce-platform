package org.bbb.service;

import org.bbb.dto.OrderRequest;
import org.bbb.dto.OrderResponse;

public interface OrderService {

	OrderResponse getOrder(Long orderId);
	OrderResponse placeOrder(OrderRequest request);
}
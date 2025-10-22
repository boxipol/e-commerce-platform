package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.OrderRequest;
import com.pd.ecommerce.dto.OrderResponse;

public interface OrderService {

	OrderResponse getOrder(Long orderId);
	OrderResponse placeOrder(OrderRequest request);
}
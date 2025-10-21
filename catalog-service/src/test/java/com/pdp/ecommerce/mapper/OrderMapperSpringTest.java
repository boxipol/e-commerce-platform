package com.pdp.ecommerce.mapper;

import com.pdp.ecommerce.dto.OrderItemRequest;
import com.pdp.ecommerce.dto.OrderRequest;
import com.pdp.ecommerce.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderMapperSpringTest {

	@Autowired
	private OrderMapper orderMapper;


	@Test
	void testMappingFromOrderRequestToOrder() {
		var item1 = new OrderItemRequest(101L, 2);
		var item2 = new OrderItemRequest(202L, 3);
		var orderRequest = new OrderRequest(55L, List.of(item1, item2));
		var order = orderMapper.toOrder(orderRequest);

		assertEquals(55L, order.getUserId());
		assertEquals(OrderStatus.PENDING, order.getStatus());
		assertNotNull(order.getCreatedAt());
		assertEquals(2, order.getItems().size());

		order.getItems().forEach(item -> {
//			assertEquals(order, item.getOrder());
			assertTrue(item.getProductId() == 101L || item.getProductId() == 202L);
			assertNull(item.getId());
		});
	}
}
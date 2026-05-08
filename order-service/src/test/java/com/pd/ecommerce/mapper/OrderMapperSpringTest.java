package com.pd.ecommerce.mapper;

//@SpringBootTest(properties = "spring.cloud.openfeign.enabled=false")
class OrderMapperSpringTest {

//	@Autowired
//	private OrderMapper orderMapper;

//	private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);
//
//	@Test
//	void testMappingFromOrderRequestToOrder() {
//		var item1 = new OrderItemRequest(101L, 2);
//		var item2 = new OrderItemRequest(202L, 3);
//		var orderRequest = new OrderRequest(55L, List.of(item1, item2));
//		var order = orderMapper.toOrder(orderRequest);
//
//		assertEquals(55L, order.getUserId());
//		assertEquals(OrderStatus.PENDING, order.getStatus());
//		assertNotNull(order.getCreatedAt());
//		assertEquals(2, order.getItems().size());
//
//		order.getItems().forEach(item -> {
////			assertEquals(order, item.getOrder());
//			assertTrue(item.getProductId() == 101L || item.getProductId() == 202L);
//			assertNull(item.getId());
//		});
//	}
}
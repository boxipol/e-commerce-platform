package com.pd.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.ecommerce.client.ProductServiceClient;
import com.pd.ecommerce.dto.CreateOrderRequest;
import com.pd.ecommerce.dto.OrderItemRequest;
import com.pd.ecommerce.dto.OrderItemResponse;
import com.pd.ecommerce.dto.OrderResponse;
import com.pd.ecommerce.dto.ProductSnapshot;
import com.pd.ecommerce.entity.Order;
import com.pd.ecommerce.entity.OrderItem;
import com.pd.ecommerce.entity.OrderStatus;
import com.pd.ecommerce.entity.OutboxEvent;
import com.pd.ecommerce.exception.OrderNotFoundException;
import com.pd.ecommerce.mapper.OrderMapper;
import com.pd.ecommerce.repository.OrderItemRepository;
import com.pd.ecommerce.repository.OrderRepository;
import com.pd.ecommerce.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl")
class OrderServiceImplTest {

	@Mock
	private ProductServiceClient productServiceClient;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderItemRepository orderItemRepository;

	@Mock
	private OutboxEventRepository outboxEventRepository;

	@Mock
	private OrderMapper mapper;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private OrderServiceImpl orderService;

	private static final UUID ORDER_ID = UUID.randomUUID();
	private static final UUID USER_ID = UUID.randomUUID();
	private static final UUID PRODUCT_ID = UUID.randomUUID();
	private static final String PUBLIC_ORDER_ID = "ORD-ABCD1234";
	private static final String USER_MAIL = "user@example.com";

	private Order savedOrder;
	private OrderItem savedItem;
	private OrderResponse orderResponse;


	@BeforeEach
	void setUp() {
		savedOrder = Order.builder().id(ORDER_ID).publicOrderId(PUBLIC_ORDER_ID).userId(USER_ID).userMail(USER_MAIL).status(OrderStatus.CREATED).totalAmount(new BigDecimal("199.98")).createdAt(Instant.now()).build();
		savedItem = OrderItem.builder().id(UUID.randomUUID()).orderId(ORDER_ID).productId(PRODUCT_ID).sku("SKU-001").quantity(2).unitPrice(new BigDecimal("99.99")).subtotal(new BigDecimal("199.98")).build();
		orderResponse = OrderResponse.builder().publicOrderId(PUBLIC_ORDER_ID).userMail(USER_MAIL).status(OrderStatus.CREATED).totalAmount(new BigDecimal("199.98")).createdAt(savedOrder.getCreatedAt()).items(List.of(new OrderItemResponse("SKU-001", 2, new BigDecimal("99.99"), new BigDecimal("199.98")))).build();
	}
	// ── getOrder ──────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("getOrder")
	class GetOrder {

		@Test
		@DisplayName("returns order with items when found by publicOrderId")
		void returnsOrderWithItems() {
			when(orderRepository.findByPublicOrderId(PUBLIC_ORDER_ID)).thenReturn(Mono.just(savedOrder));
			when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(Flux.just(savedItem));
			when(mapper.toItemResponse(savedItem)).thenReturn(new OrderItemResponse("SKU-001", 2, new BigDecimal("99.99"), new BigDecimal("199.98")));
			StepVerifier.create(orderService.getOrder(PUBLIC_ORDER_ID)).assertNext(response -> {
				assertThat(response.publicOrderId()).isEqualTo(PUBLIC_ORDER_ID);
				assertThat(response.userMail()).isEqualTo(USER_MAIL);
				assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
				assertThat(response.totalAmount()).isEqualByComparingTo("199.98");
				assertThat(response.items()).hasSize(1);
				assertThat(response.items().get(0).sku()).isEqualTo("SKU-001");
			}).verifyComplete();
		}

		@Test
		@DisplayName("returns order with empty items list when order has no items")
		void returnsOrderWithNoItems() {
			when(orderRepository.findByPublicOrderId(PUBLIC_ORDER_ID)).thenReturn(Mono.just(savedOrder));
			when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(Flux.empty());
			StepVerifier.create(orderService.getOrder(PUBLIC_ORDER_ID)).assertNext(response -> assertThat(response.items()).isEmpty()).verifyComplete();
		}

		@Test
		@DisplayName("throws OrderNotFoundException when publicOrderId not found")
		void throwsOrderNotFoundWhenMissing() {
			when(orderRepository.findByPublicOrderId(PUBLIC_ORDER_ID)).thenReturn(Mono.empty());
			StepVerifier.create(orderService.getOrder(PUBLIC_ORDER_ID)).expectError(OrderNotFoundException.class).verify();
			verify(orderItemRepository, never()).findByOrderId(any(UUID.class));
		}
	}
	// ── createOrder ───────────────────────────────────────────────────────────

	@Nested
	@DisplayName("createOrder")
	@MockitoSettings(strictness = Strictness.LENIENT)
	class CreateOrder {

		private CreateOrderRequest request;
		private ProductSnapshot product;

		@BeforeEach
		void setUp() throws Exception {
			request = new CreateOrderRequest(List.of(new OrderItemRequest("SKU-001", 2)));
			product = new ProductSnapshot(PRODUCT_ID, "SKU-001", new BigDecimal("99.99"));
			when(productServiceClient.getProducts(List.of("SKU-001"))).thenReturn(Mono.just(List.of(product)));
			when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));
			when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.just(savedItem));
			when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(Mono.just(new OutboxEvent()));
			when(objectMapper.writeValueAsString(any())).thenReturn("{\"orderId\":\"test\"}");
			when(mapper.toOrderCreatedEvent(any(Order.class), anyList())).thenReturn(com.pd.ecommerce.event.OrderCreatedEvent.builder().orderId(ORDER_ID).publicOrderId(PUBLIC_ORDER_ID).userId(USER_ID).userMail(USER_MAIL).totalPrice(new BigDecimal("199.98")).items(List.of()).build());
			when(mapper.toResponse(anyString(), any(Order.class), anyList())).thenReturn(orderResponse);
		}

		@Test
		@DisplayName("creates order, saves items and outbox event, returns response")
		void createsOrderSuccessfully() {
			StepVerifier.create(orderService.createOrder(USER_ID, USER_MAIL, request)).assertNext(response -> {
				assertThat(response.publicOrderId()).isEqualTo(PUBLIC_ORDER_ID);
				assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
				assertThat(response.totalAmount()).isEqualByComparingTo("199.98");
			}).verifyComplete();
			verify(orderRepository).save(any(Order.class));
			verify(orderItemRepository).saveAll(anyList());
			verify(outboxEventRepository).save(any(OutboxEvent.class));
		}

		@Test
		@DisplayName("saves order with CREATED status and correct user fields")
		void savesOrderWithCorrectFields() {
			StepVerifier.create(orderService.createOrder(USER_ID, USER_MAIL, request)).expectNextCount(1).verifyComplete();
			ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
			verify(orderRepository).save(orderCaptor.capture());
			Order captured = orderCaptor.getValue();
			assertThat(captured.getUserId()).isEqualTo(USER_ID);
			assertThat(captured.getUserMail()).isEqualTo(USER_MAIL);
			assertThat(captured.getStatus()).isEqualTo(OrderStatus.CREATED);
			assertThat(captured.getPublicOrderId()).startsWith("ORD-");
			assertThat(captured.getTotalAmount()).isEqualByComparingTo("199.98");
			assertThat(captured.getCreatedAt()).isNotNull();
		}

		@Test
		@DisplayName("calculates total as sum of (unitPrice × quantity) for all items")
		void calculatesTotalCorrectly() {
			// Two items: SKU-001 × 2 @ 99.99 = 199.98, SKU-002 × 1 @ 49.99 = 49.99 → total 249.97
			UUID product2Id = UUID.randomUUID();
			ProductSnapshot product2 = new ProductSnapshot(product2Id, "SKU-002", new BigDecimal("49.99"));
			CreateOrderRequest multiItemRequest = new CreateOrderRequest(List.of(new OrderItemRequest("SKU-001", 2), new OrderItemRequest("SKU-002", 1)));
			when(productServiceClient.getProducts(List.of("SKU-001", "SKU-002"))).thenReturn(Mono.just(List.of(product, product2)));
			StepVerifier.create(orderService.createOrder(USER_ID, USER_MAIL, multiItemRequest)).expectNextCount(1).verifyComplete();
			ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
			verify(orderRepository).save(orderCaptor.capture());
			assertThat(orderCaptor.getValue().getTotalAmount()).isEqualByComparingTo("249.97");
		}

		@Test
		@DisplayName("saves outbox event with ORDER_CREATED type and PENDING status")
		void savesOutboxEventWithCorrectFields() {
			StepVerifier.create(orderService.createOrder(USER_ID, USER_MAIL, request)).expectNextCount(1).verifyComplete();
			ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
			verify(outboxEventRepository).save(outboxCaptor.capture());
			OutboxEvent outbox = outboxCaptor.getValue();
			assertThat(outbox.getAggregateType()).isEqualTo("ORDER");
			assertThat(outbox.getAggregateId()).isEqualTo(ORDER_ID);
			assertThat(outbox.getEventType()).isEqualTo("ORDER_CREATED");
			assertThat(outbox.getStatus()).isEqualTo(com.pd.ecommerce.entity.OutboxEventStatus.PENDING);
			assertThat(outbox.getPayload()).isNotNull();
		}

		@Test
		@DisplayName("assigns saved order ID to each order item before persisting")
		void assignsOrderIdToItems() {
			StepVerifier.create(orderService.createOrder(USER_ID, USER_MAIL, request)).expectNextCount(1).verifyComplete();
			@SuppressWarnings("unchecked") ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
			verify(orderItemRepository).saveAll(itemsCaptor.capture());
			List<OrderItem> items = itemsCaptor.getValue();
			assertThat(items).allMatch(item -> ORDER_ID.equals(item.getOrderId()));
		}

		@Test
		@DisplayName("propagates error when product not found in product service response")
		void throwsWhenProductSkuMissing() {
			// Request contains SKU-MISSING which is not in the product service response
			CreateOrderRequest badRequest = new CreateOrderRequest(List.of(new OrderItemRequest("SKU-MISSING", 1)));
			when(productServiceClient.getProducts(List.of("SKU-MISSING"))).thenReturn(Mono.just(List.of())); // empty — SKU not found
			StepVerifier.create(orderService.createOrder(USER_ID, USER_MAIL, badRequest)).expectError(RuntimeException.class).verify();
			verify(orderRepository, never()).save(any());
		}

		@Test
		@DisplayName("propagates error when product service call fails")
		void propagatesProductServiceError() {
			when(productServiceClient.getProducts(anyList())).thenReturn(Mono.error(new RuntimeException("Product service unavailable")));
			StepVerifier.create(orderService.createOrder(USER_ID, USER_MAIL, request)).expectErrorMessage("Product service unavailable").verify();
			verify(orderRepository, never()).save(any());
		}

		@Test
		@DisplayName("propagates error when order repository save fails")
		void propagatesOrderSaveError() {
			when(orderRepository.save(any(Order.class))).thenReturn(Mono.error(new RuntimeException("DB unavailable")));
			StepVerifier.create(orderService.createOrder(USER_ID, USER_MAIL, request)).expectErrorMessage("DB unavailable").verify();
			verify(orderItemRepository, never()).saveAll(anyList());
			verify(outboxEventRepository, never()).save(any());
		}
	}
	// ── getOrdersByUser ───────────────────────────────────────────────────────

	@Nested
	@DisplayName("getOrdersByUser")
	class GetOrdersByUser {

		@Test
		@DisplayName("returns all orders with items for the given user")
		void returnsOrdersForUser() {
			when(orderRepository.findByUserId(USER_ID)).thenReturn(Flux.just(savedOrder));
			when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(Flux.just(savedItem));
			when(mapper.toItemResponse(savedItem)).thenReturn(new OrderItemResponse("SKU-001", 2, new BigDecimal("99.99"), new BigDecimal("199.98")));

			StepVerifier.create(orderService.getOrdersByUser(USER_ID))
				.assertNext(response -> {
					assertThat(response.publicOrderId()).isEqualTo(PUBLIC_ORDER_ID);
					assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
					assertThat(response.items()).hasSize(1);
					assertThat(response.items().get(0).sku()).isEqualTo("SKU-001");
				})
				.verifyComplete();
		}

		@Test
		@DisplayName("returns empty flux when user has no orders")
		void returnsEmptyForUserWithNoOrders() {
			when(orderRepository.findByUserId(USER_ID)).thenReturn(Flux.empty());

			StepVerifier.create(orderService.getOrdersByUser(USER_ID))
				.verifyComplete();

			verify(orderItemRepository, never()).findByOrderId(any(UUID.class));
		}

		@Test
		@DisplayName("returns multiple orders each with their own items")
		void returnsMultipleOrdersWithItems() {
			UUID order2Id = UUID.randomUUID();
			Order order2 = Order.builder().id(order2Id).publicOrderId("ORD-XYZ99999").userId(USER_ID)
				.userMail(USER_MAIL).status(OrderStatus.PAID).totalAmount(new BigDecimal("49.99"))
				.createdAt(java.time.Instant.now()).build();
			OrderItem item2 = OrderItem.builder().id(UUID.randomUUID()).orderId(order2Id)
				.productId(UUID.randomUUID()).sku("SKU-002").quantity(1)
				.unitPrice(new BigDecimal("49.99")).subtotal(new BigDecimal("49.99")).build();

			when(orderRepository.findByUserId(USER_ID)).thenReturn(Flux.just(savedOrder, order2));
			when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(Flux.just(savedItem));
			when(orderItemRepository.findByOrderId(order2Id)).thenReturn(Flux.just(item2));
			when(mapper.toItemResponse(savedItem)).thenReturn(new OrderItemResponse("SKU-001", 2, new BigDecimal("99.99"), new BigDecimal("199.98")));
			when(mapper.toItemResponse(item2)).thenReturn(new OrderItemResponse("SKU-002", 1, new BigDecimal("49.99"), new BigDecimal("49.99")));

			StepVerifier.create(orderService.getOrdersByUser(USER_ID))
				.assertNext(r -> assertThat(r.publicOrderId()).isEqualTo(PUBLIC_ORDER_ID))
				.assertNext(r -> {
					assertThat(r.publicOrderId()).isEqualTo("ORD-XYZ99999");
					assertThat(r.status()).isEqualTo(OrderStatus.PAID);
				})
				.verifyComplete();
		}
	}

	// ── markAsPaid ────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("markAsPaid")
	class MarkAsPaid {

		@Test
		@DisplayName("updates order status to PAID and marks outbox event as processed")
		void marksOrderAsPaid() {
			when(orderRepository.markAsPaid(eq(ORDER_ID), any(Instant.class))).thenReturn(Mono.just(1));
			when(outboxEventRepository.markProcessed(eq(ORDER_ID), any(Instant.class))).thenReturn(Mono.just(1));
			StepVerifier.create(orderService.markAsPaid(ORDER_ID)).verifyComplete();
			verify(orderRepository).markAsPaid(eq(ORDER_ID), any(Instant.class));
			verify(outboxEventRepository).markProcessed(eq(ORDER_ID), any(Instant.class));
		}

		@Test
		@DisplayName("propagates error when order update fails")
		void propagatesErrorOnOrderUpdateFailure() {
			when(orderRepository.markAsPaid(eq(ORDER_ID), any(Instant.class))).thenReturn(Mono.error(new RuntimeException("DB error")));
			StepVerifier.create(orderService.markAsPaid(ORDER_ID)).expectError(RuntimeException.class).verify();
			verify(outboxEventRepository, never()).markProcessed(any(UUID.class), any(Instant.class));
		}
	}
	// ── markAsFailed ──────────────────────────────────────────────────────────

	@Nested
	@DisplayName("markAsFailed")
	class MarkAsFailed {

		@Test
		@DisplayName("cancels order and marks outbox event as failed")
		void marksOrderAsFailed() {
			when(orderRepository.markAsCanceled(eq(ORDER_ID), any(Instant.class))).thenReturn(Mono.just(1));
			when(outboxEventRepository.markAsFailed(eq(ORDER_ID), any(Instant.class))).thenReturn(Mono.just(1));
			StepVerifier.create(orderService.markAsFailed(ORDER_ID)).verifyComplete();
			verify(orderRepository).markAsCanceled(eq(ORDER_ID), any(Instant.class));
			verify(outboxEventRepository).markAsFailed(eq(ORDER_ID), any(Instant.class));
		}

		@Test
		@DisplayName("propagates error when order cancellation fails")
		void propagatesErrorOnCancellationFailure() {
			when(orderRepository.markAsCanceled(eq(ORDER_ID), any(Instant.class))).thenReturn(Mono.error(new RuntimeException("DB error")));
			StepVerifier.create(orderService.markAsFailed(ORDER_ID)).expectError(RuntimeException.class).verify();
			verify(outboxEventRepository, never()).markAsFailed(any(UUID.class), any(Instant.class));
		}
	}
}
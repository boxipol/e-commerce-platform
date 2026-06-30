package com.pd.ecommerce.integration;

import com.pd.ecommerce.entity.Order;
import com.pd.ecommerce.entity.OrderItem;
import com.pd.ecommerce.entity.OrderStatus;
import com.pd.ecommerce.repository.OrderItemRepository;
import com.pd.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link OrderRepository} and {@link OrderItemRepository} against a real
 * PostgreSQL instance (Testcontainers). Verifies persistence, the {@code findByPublicOrderId}
 * derived query, the order ↔ order_items relationship, and the status-guarded
 * {@code markAsPaid} / {@code markAsCanceled} state transitions.
 */
@DataR2dbcTest
@DisplayName("OrderRepository Integration Tests")
class OrderRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OrderItemRepository orderItemRepository;


	@BeforeEach
	void setUp() {
		orderItemRepository.deleteAll()
			.then(orderRepository.deleteAll())
			.block();
	}

	private Order newOrder(String publicOrderId, OrderStatus status) {
		return Order.builder()
			.publicOrderId(publicOrderId)
			.userId(UUID.randomUUID())
			.userMail("buyer@example.com")
			.status(status)
			.totalAmount(new BigDecimal("199.99"))
			.createdAt(Instant.now())
			.build();
	}

	private Order newOrderForUser(String publicOrderId, UUID userId, OrderStatus status) {
		return Order.builder()
			.publicOrderId(publicOrderId)
			.userId(userId)
			.userMail("buyer@example.com")
			.status(status)
			.totalAmount(new BigDecimal("199.99"))
			.createdAt(Instant.now())
			.build();
	}

	private OrderItem newItem(UUID orderId) {
		return OrderItem.builder()
			.orderId(orderId)
			.productId(UUID.randomUUID())
			.sku("SKU-1")
			.quantity(2)
			.unitPrice(new BigDecimal("99.99"))
			.subtotal(new BigDecimal("199.98"))
			.build();
	}

	@Test
	@DisplayName("save - should persist an order and assign a generated id")
	void testSaveAssignsId() {
		StepVerifier.create(orderRepository.save(newOrder("ORD-1", OrderStatus.CREATED)))
			.assertNext(saved -> {
				assertThat(saved.getId()).isNotNull();
				assertThat(saved.getStatus()).isEqualTo(OrderStatus.CREATED);
			})
			.verifyComplete();
	}

	@Test
	@DisplayName("findByPublicOrderId - should return the matching order")
	void testFindByPublicOrderId() {
		orderRepository.save(newOrder("ORD-2", OrderStatus.CREATED)).block();

		StepVerifier.create(orderRepository.findByPublicOrderId("ORD-2"))
			.assertNext(order -> assertThat(order.getPublicOrderId()).isEqualTo("ORD-2"))
			.verifyComplete();
	}

	@Test
	@DisplayName("findByPublicOrderId - should be empty when no order matches")
	void testFindByPublicOrderIdMissing() {
		StepVerifier.create(orderRepository.findByPublicOrderId("missing"))
			.verifyComplete();
	}

	@Test
	@DisplayName("order items - should be linked to their order and retrievable by order id")
	void testOrderItemsRelationship() {
		Order saved = orderRepository.save(newOrder("ORD-3", OrderStatus.CREATED)).block();
		orderItemRepository.save(newItem(saved.getId())).block();
		orderItemRepository.save(newItem(saved.getId())).block();

		StepVerifier.create(orderItemRepository.findByOrderId(saved.getId()))
			.expectNextCount(2)
			.verifyComplete();
	}

	@Test
	@DisplayName("markAsPaid - should move a CREATED order to PAID and report one row updated")
	void testMarkAsPaidFromCreated() {
		Order saved = orderRepository.save(newOrder("ORD-4", OrderStatus.CREATED)).block();

		StepVerifier.create(orderRepository.markAsPaid(saved.getId(), Instant.now()))
			.expectNext(1)
			.verifyComplete();

		StepVerifier.create(orderRepository.findById(saved.getId()))
			.assertNext(order -> assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID))
			.verifyComplete();
	}

	@Test
	@DisplayName("markAsPaid - should be a no-op when the order is not in CREATED state")
	void testMarkAsPaidGuardsState() {
		Order saved = orderRepository.save(newOrder("ORD-5", OrderStatus.CANCELLED)).block();

		StepVerifier.create(orderRepository.markAsPaid(saved.getId(), Instant.now()))
			.expectNext(0)
			.verifyComplete();

		StepVerifier.create(orderRepository.findById(saved.getId()))
			.assertNext(order -> assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED))
			.verifyComplete();
	}

	@Test
	@DisplayName("markAsCanceled - should move a CREATED order to CANCELLED")
	void testMarkAsCanceledFromCreated() {
		Order saved = orderRepository.save(newOrder("ORD-6", OrderStatus.CREATED)).block();

		StepVerifier.create(orderRepository.markAsCanceled(saved.getId(), Instant.now()))
			.expectNext(1)
			.verifyComplete();

		StepVerifier.create(orderRepository.findById(saved.getId()))
			.assertNext(order -> assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED))
			.verifyComplete();
	}

	@Test
	@DisplayName("findByUserId - returns all orders belonging to the given user")
	void testFindByUserId() {
		UUID userId = UUID.randomUUID();
		orderRepository.save(newOrderForUser("ORD-UA1", userId, OrderStatus.CREATED)).block();
		orderRepository.save(newOrderForUser("ORD-UA2", userId, OrderStatus.PAID)).block();

		StepVerifier.create(orderRepository.findByUserId(userId).collectList())
			.assertNext(orders -> {
				assertThat(orders).hasSize(2);
				assertThat(orders).extracting(Order::getUserId).containsOnly(userId);
			})
			.verifyComplete();
	}

	@Test
	@DisplayName("findByUserId - returns empty flux when user has no orders")
	void testFindByUserIdEmpty() {
		StepVerifier.create(orderRepository.findByUserId(UUID.randomUUID()))
			.verifyComplete();
	}

	@Test
	@DisplayName("findByUserId - does not return orders belonging to a different user")
	void testFindByUserIdIsolation() {
		UUID user1 = UUID.randomUUID();
		UUID user2 = UUID.randomUUID();
		orderRepository.save(newOrderForUser("ORD-UB1", user1, OrderStatus.CREATED)).block();
		orderRepository.save(newOrderForUser("ORD-UB2", user2, OrderStatus.CREATED)).block();

		StepVerifier.create(orderRepository.findByUserId(user1).collectList())
			.assertNext(orders -> {
				assertThat(orders).hasSize(1);
				assertThat(orders.get(0).getUserId()).isEqualTo(user1);
			})
			.verifyComplete();
	}

	@Test
	@DisplayName("markAsCanceled - should not cancel an order that is already PAID")
	void testMarkAsCanceledGuardsState() {
		Order saved = orderRepository.save(newOrder("ORD-7", OrderStatus.PAID)).block();

		StepVerifier.create(orderRepository.markAsCanceled(saved.getId(), Instant.now()))
			.expectNext(0)
			.verifyComplete();

		StepVerifier.create(orderRepository.findById(saved.getId()))
			.assertNext(order -> assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID))
			.verifyComplete();
	}
}

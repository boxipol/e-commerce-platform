package com.pd.ecommerce.service;

import com.pd.ecommerce.dto.InventoryCreateRequest;
import com.pd.ecommerce.dto.InventoryResponse;
import com.pd.ecommerce.dto.InventoryUpdateRequest;
import com.pd.ecommerce.entity.Inventory;
import com.pd.ecommerce.event.OrderEventItem;
import com.pd.ecommerce.event.PaymentCompletedEvent;
import com.pd.ecommerce.exception.InsufficientInventoryException;
import com.pd.ecommerce.exception.ProductAlreadyExistsException;
import com.pd.ecommerce.mapper.InventoryMapper;
import com.pd.ecommerce.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryServiceImpl Tests")
class InventoryServiceImplTest {

	@Mock
	private InventoryRepository repository;

	@Mock
	private InventoryMapper mapper;

	@InjectMocks
	private InventoryServiceImpl service;

	private UUID productId;
	private Inventory inventory;
	private InventoryResponse response;


	@BeforeEach
	void setUp() {
		productId = UUID.randomUUID();
		Instant now = Instant.now();
		inventory = Inventory.builder()
			.productId(productId)
			.quantity(10)
			.createdAt(now)
			.updatedAt(now)
			.build();
		response = InventoryResponse.builder()
			.productId(productId)
			.quantity(10)
			.createdAt(now)
			.updatedAt(now)
			.build();
	}

	@Test
	@DisplayName("get(UUID) - should return inventory when found")
	void testGetByIdSuccess() {
		when(repository.findById(productId)).thenReturn(Mono.just(inventory));
		when(mapper.toResponse(inventory)).thenReturn(response);

		StepVerifier.create(service.get(productId))
			.assertNext(r -> assertThat(r.productId()).isEqualTo(productId))
			.verifyComplete();
	}

	@Test
	@DisplayName("get(UUID) - should error when not found")
	void testGetByIdNotFound() {
		when(repository.findById(productId)).thenReturn(Mono.empty());

		StepVerifier.create(service.get(productId))
			.expectError(RuntimeException.class)
			.verify();
	}

	@Test
	@DisplayName("get(List) - should map every found inventory")
	void testGetProducts() {
		UUID secondId = UUID.randomUUID();
		Inventory second = Inventory.builder().productId(secondId).quantity(5).build();
		InventoryResponse secondResponse = InventoryResponse.builder().productId(secondId).quantity(5).build();

		when(repository.findById(productId)).thenReturn(Mono.just(inventory));
		when(repository.findById(secondId)).thenReturn(Mono.just(second));
		when(mapper.toResponse(inventory)).thenReturn(response);
		when(mapper.toResponse(second)).thenReturn(secondResponse);

		StepVerifier.create(service.get(List.of(productId, secondId)))
			.expectNextCount(2)
			.verifyComplete();
	}

	@Test
	@DisplayName("create - should insert when product does not yet exist")
	void testCreateSuccess() {
		InventoryCreateRequest request = new InventoryCreateRequest(productId, 10);
		when(repository.findById(productId)).thenReturn(Mono.empty());
		when(repository.insert(eq(productId), eq(10), any(Instant.class), any(Instant.class)))
			.thenReturn(Mono.just(inventory));
		when(mapper.toResponse(inventory)).thenReturn(response);

		StepVerifier.create(service.create(request))
			.assertNext(r -> assertThat(r.quantity()).isEqualTo(10))
			.verifyComplete();

		verify(repository).insert(eq(productId), eq(10), any(Instant.class), any(Instant.class));
	}

	@Test
	@DisplayName("create - should error when product already exists")
	void testCreateAlreadyExists() {
		InventoryCreateRequest request = new InventoryCreateRequest(productId, 10);
		when(repository.findById(productId)).thenReturn(Mono.just(inventory));

		StepVerifier.create(service.create(request))
			.expectError(ProductAlreadyExistsException.class)
			.verify();

		verify(repository, never()).insert(any(), any(), any(), any());
	}

	@Test
	@DisplayName("update - should update quantity when found")
	void testUpdateSuccess() {
		InventoryUpdateRequest request = new InventoryUpdateRequest(25);
		when(repository.findById(productId)).thenReturn(Mono.just(inventory));
		when(repository.update(eq(productId), eq(25), any(Instant.class))).thenReturn(Mono.just(inventory));
		when(mapper.toResponse(inventory)).thenReturn(response);

		StepVerifier.create(service.update(productId, request))
			.expectNextCount(1)
			.verifyComplete();

		verify(repository).update(eq(productId), eq(25), any(Instant.class));
	}

	@Test
	@DisplayName("update - should error when not found")
	void testUpdateNotFound() {
		InventoryUpdateRequest request = new InventoryUpdateRequest(25);
		when(repository.findById(productId)).thenReturn(Mono.empty());

		StepVerifier.create(service.update(productId, request))
			.expectError(RuntimeException.class)
			.verify();

		verify(repository, never()).update(any(), any(), any());
	}

	@Test
	@DisplayName("delete - should delete when found")
	void testDeleteSuccess() {
		when(repository.findById(productId)).thenReturn(Mono.just(inventory));
		when(repository.delete(inventory)).thenReturn(Mono.empty());

		StepVerifier.create(service.delete(productId)).verifyComplete();

		verify(repository).delete(inventory);
	}

	@Test
	@DisplayName("delete - should error when not found")
	void testDeleteNotFound() {
		when(repository.findById(productId)).thenReturn(Mono.empty());

		StepVerifier.create(service.delete(productId))
			.expectError(RuntimeException.class)
			.verify();

		verify(repository, never()).delete(any(Inventory.class));
	}

	@Test
	@DisplayName("reserveInventory - should complete when all items have stock")
	void testReserveInventorySuccess() {
		OrderEventItem item = new OrderEventItem(productId, "SKU-1", 2);
		PaymentCompletedEvent event = buildEvent(List.of(item));
		when(repository.decreaseStock(eq(productId), eq(2), any(Instant.class))).thenReturn(Mono.just(1));

		StepVerifier.create(service.reserveInventory(event)).verifyComplete();

		verify(repository).decreaseStock(eq(productId), eq(2), any(Instant.class));
	}

	@Test
	@DisplayName("reserveInventory - should error when stock is insufficient")
	void testReserveInventoryInsufficient() {
		OrderEventItem item = new OrderEventItem(productId, "SKU-1", 99);
		PaymentCompletedEvent event = buildEvent(List.of(item));
		when(repository.decreaseStock(eq(productId), eq(99), any(Instant.class))).thenReturn(Mono.just(0));

		StepVerifier.create(service.reserveInventory(event))
			.expectError(InsufficientInventoryException.class)
			.verify();
	}

	private PaymentCompletedEvent buildEvent(List<OrderEventItem> items) {
		return PaymentCompletedEvent.builder()
			.paymentId(UUID.randomUUID())
			.userMail("buyer@example.com")
			.orderId(UUID.randomUUID())
			.publicOrderId("ORD-1")
			.items(items)
			.amount(BigDecimal.TEN)
			.build();
	}
}

package com.pd.ecommerce.repository;

import com.pd.ecommerce.entity.Payment;
import com.pd.ecommerce.entity.PaymentStatus;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.UUID;

public interface PaymentRepository extends ReactiveCrudRepository<Payment, UUID> {
	@Modifying
	@Query("""
		    UPDATE payments
		    SET provider_payment_id = :providerPaymentId,
		        payment_url = :paymentUrl,
		        updated_at = :updatedAt
		    WHERE id = :paymentId
		""")
	Mono<Integer> updateProviderData(UUID paymentId, String providerPaymentId, String paymentUrl, Instant updatedAt);

	@Modifying
	@Query("""
		UPDATE payments
		SET status = :status,
		    updated_at = :updatedAt
		WHERE id = :paymentId
		""")
	Mono<Integer> updateStatus(UUID paymentId, PaymentStatus status, Instant updatedAt);
}
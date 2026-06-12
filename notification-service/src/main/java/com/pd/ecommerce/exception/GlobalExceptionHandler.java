package com.pd.ecommerce.exception;

import com.pd.ecommerce.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public final class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public Mono<ResponseEntity<ErrorResponse>> handleGeneric(Exception ex) {
		log.error("Unhandled exception", ex);

		ErrorResponse response = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
			.error(HttpStatus.INTERNAL_SERVER_ERROR
				.getReasonPhrase())
			.message("Internal server error")
			.build();

		return Mono.just(
			ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(response)
		);
	}
}
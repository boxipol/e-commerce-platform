package com.pd.ecommerce.exception;

import com.pd.ecommerce.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public Mono<ResponseEntity<ErrorResponse>> handleEmailExists(EmailAlreadyExistsException ex) {
		log.error("EmailAlreadyExistsException", ex);

		ErrorResponse response = ErrorResponse.builder()
			.timestamp(Instant.now())
			.status(HttpStatus.CONFLICT.value())
			.error(HttpStatus.CONFLICT.getReasonPhrase())
			.message(ex.getMessage())
			.build();

		return Mono.just(
			ResponseEntity.status(HttpStatus.CONFLICT)
				.body(response));
	}

	@ExceptionHandler(ResponseStatusException.class)
	public Mono<ResponseEntity<ErrorResponse>> handleResponseStatus(ResponseStatusException ex) {
		ErrorResponse response = ErrorResponse.builder()
			.timestamp(Instant.now())
			.status(ex.getStatusCode().value())
			.error(ex.getStatusCode().toString())
			.message(ex.getReason())
			.build();

		return Mono.just(
			ResponseEntity
				.status(ex.getStatusCode())
				.body(response)
		);
	}

	@ExceptionHandler(Exception.class)
	public Mono<ResponseEntity<ErrorResponse>> handleGeneric(Exception ex) {
		log.error("Unhandled exception", ex);

		ErrorResponse response = ErrorResponse.builder()
			.timestamp(Instant.now())
			.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
			.error(HttpStatus.INTERNAL_SERVER_ERROR
				.getReasonPhrase())
			.message("Internal server error")
			.build();

		return Mono.just(
			ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(response)
		);
	}
}
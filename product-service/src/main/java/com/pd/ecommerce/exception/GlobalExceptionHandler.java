package com.pd.ecommerce.exception;

import com.pd.ecommerce.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(WebExchangeBindException.class)
	public Mono<ResponseEntity<ErrorResponse>> handleValidation(WebExchangeBindException ex) {
		String message = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(fe -> fe.getField() + " " + fe.getDefaultMessage())
			.collect(Collectors.joining(", "));

		ErrorResponse response = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(HttpStatus.BAD_REQUEST.value()).error(HttpStatus.BAD_REQUEST.getReasonPhrase()).message(message).build();

		return Mono.just(ResponseEntity.badRequest().body(response));
	}

	@ExceptionHandler(Exception.class)
	public Mono<ResponseEntity<ErrorResponse>> handleGeneric(Exception ex) {
		log.error("Unhandled exception", ex);

		ErrorResponse response = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
			.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
			.message("Internal server error").build();

		return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
	}
}
package com.pd.ecommerce.controller;

import com.pd.ecommerce.event.UserCreatedEvent;
import com.pd.ecommerce.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Notifications", description = "Internal notification triggers (primarily Kafka-driven; this endpoint is for testing)")
@RestController
@RequiredArgsConstructor
public final class NotificationController {

	private final EmailService service;


	@Operation(summary = "Send test welcome email", description = "Manually triggers a user-created welcome email — for local testing only")
	@PostMapping("/test-mail")
	public Mono<String> testMail(@RequestBody UserCreatedEvent event) {
		return Mono.fromRunnable(() -> service.sendUserCreatedEmail(event))
			.thenReturn("Mail successfully sent");
	}
}
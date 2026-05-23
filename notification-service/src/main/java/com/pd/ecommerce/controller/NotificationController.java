package com.pd.ecommerce.controller;

import com.pd.ecommerce.event.UserCreatedEvent;
import com.pd.ecommerce.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public final class NotificationController {

	private final EmailService service;


	@PostMapping("/test-mail")
	public Mono<String> testMail(@RequestBody UserCreatedEvent event) {
		return Mono.fromRunnable(() -> service.sendUserCreatedEmail(event))
			.thenReturn("Mail successfully sent");
	}
}
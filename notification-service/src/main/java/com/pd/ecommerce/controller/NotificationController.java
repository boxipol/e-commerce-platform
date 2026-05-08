package com.pd.ecommerce.controller;

import com.pd.ecommerce.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
public final class NotificationController {

	private final NotificationService notificationService;


	@GetMapping("/fetch-data")
	public Mono<String> fetchData() {
		return notificationService.getData();
	}
}
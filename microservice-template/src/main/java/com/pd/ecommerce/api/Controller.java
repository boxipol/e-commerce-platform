package com.pd.ecommerce.api;

import com.pd.ecommerce.models.Response;
import com.pd.ecommerce.service.MyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public final class Controller {

	private final MyService service;


	@GetMapping("/hello")
	public Response sayHello() {
		return service.getGreeting();
	}
}

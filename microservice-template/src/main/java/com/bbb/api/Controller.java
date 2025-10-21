package com.bbb.api;

import lombok.RequiredArgsConstructor;
import com.bbb.models.Response;
import com.bbb.service.MyService;
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

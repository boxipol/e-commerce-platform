package org.bbb.api;

import lombok.RequiredArgsConstructor;
import org.bbb.models.Response;
import org.bbb.service.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public final class Controller {

	private final Service service;


	@GetMapping("/hello")
	public Response sayHello() {
		return service.getGreeting();
	}
}

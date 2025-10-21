package com.pdp.ecommerce.service;

import com.pdp.ecommerce.models.Response;
import org.springframework.stereotype.Service;

@Service
public final class MyServiceImpl implements MyService {
	public Response getGreeting() {
		return new Response("Hello from your microservice2!");
	}
}
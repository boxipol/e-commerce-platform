package org.bbb.service;

import org.bbb.models.Response;

@org.springframework.stereotype.Service
public final class Service {
	public Response getGreeting() {
		return new Response("Hello from your microservice!");
	}
}
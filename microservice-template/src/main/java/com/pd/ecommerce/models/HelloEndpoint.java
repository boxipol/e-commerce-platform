package com.pd.ecommerce.models;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public final class HelloEndpoint {

	private static final String NAMESPACE_URI = "http://example.com/demo";


	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "HelloRequest")
	@ResponsePayload
	public HelloResponse sayHello(@RequestPayload HelloRequest request) {
		HelloResponse response = new HelloResponse();
		response.setGreeting(String.format("Hello, %s!", request.getName()));

		return response;
	}
}
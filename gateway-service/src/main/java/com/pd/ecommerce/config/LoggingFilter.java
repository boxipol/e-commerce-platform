package com.pd.ecommerce.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

	private static final String CORRELATION_ID = "X-Correlation-Id";


	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		long startTime = System.currentTimeMillis();
		String correlationId = request.getHeaders().getFirst(CORRELATION_ID);

		if (correlationId == null || correlationId.isBlank()) {
			correlationId = UUID.randomUUID().toString();
		}

		ServerWebExchange mutatedExchange = exchange.mutate()
			.request(request.mutate().header(CORRELATION_ID, correlationId)
				.build())
			.build();

		String method = request.getMethod().name();
		String path = request.getURI().getPath();
		String query = request.getURI().getQuery();
		String clientIp = request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

		log.info("➡️ Incoming request [{}] {}{} | IP={} | CID={}", method, path, query != null ? "?" + query : "", clientIp, correlationId);

		String finalCorrelationId = correlationId;

		return chain.filter(mutatedExchange).doFinally(signal -> {
			long duration = System.currentTimeMillis() - startTime;
			Integer status = exchange.getResponse().getStatusCode() != null ? exchange.getResponse().getStatusCode().value() : 0;
			log.info("⬅️ Response [{}] {} | status={} | time={}ms | CID={}", method, path, status, duration, finalCorrelationId);
		});
	}

	@Override
	public int getOrder() {
		return -2;
	}
}
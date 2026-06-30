# Gateway Service

**Port:** 8081  
**Role:** Single entry point for all client traffic.

## Responsibilities

- JWT validation (`JwtAuthenticationFilter`) — rejects requests without a valid token
- Header injection — forwards `X-User-Id`, `X-User-Email`, `X-Role` to downstream services
- Routing — Spring Cloud Gateway WebFlux routes to each service by path prefix
- API aggregation — proxies each service's `/v3/api-docs` for Swagger UI consolidation
- Distributed tracing — adds OpenTelemetry trace context to every request

## Routes

| Path | Upstream |
|---|---|
| `/api/v1/users/**` | user-service:8080 |
| `/api/v1/orders/**` | order-service:8080 |
| `/api/v1/products/**` | product-service:8080 |
| `/api/v1/payments/**` | payment-service:8080 |
| `/api/v1/inventories/**` | inventory-service:8080 |
| `/api/v1/notifications/**` | notification-service:8080 |
| `/{service}/v3/api-docs` | proxied per service |

## Public routes (no JWT required)

- `/api/v1/users/**` — registration and login
- `/api/v1/payments/webhooks/**` — provider webhook callbacks
- `/swagger-ui/**`, `**/v3/api-docs` — documentation

## Swagger UI

```
http://localhost:8081/swagger-ui.html
```

Use the service dropdown to switch between API specs.

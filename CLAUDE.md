# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build all services
mvn clean package

# Build without running tests
mvn -DskipTests clean package

# Run all tests
mvn test

# Run tests for a single service
mvn -f order-service/pom.xml test

# Start full stack (all 7 services + all infra)
docker compose up

# Start infra only (databases + Kafka) — use when running services from IDE
docker compose -f docker-compose.local.yml up -d

# Rebuild a single service image without Docker layer cache
docker compose build --no-cache <service-name>
```

## Architecture

**7-service reactive microservices platform** using Spring Boot 3.5 + Spring WebFlux (all endpoints return `Mono<T>` / `Flux<T>`).

```
Gateway (8081) → JWT validation → routes to:
├─ User Service      (8082) - Auth, PostgreSQL/R2DBC, emits user events
├─ Order Service     (8083) - Orders, PostgreSQL/R2DBC, Outbox pattern, Resilience4j
├─ Product Service   (8084) - Catalog, Cassandra + Redis cache
├─ Payment Service   (8085) - Stripe/PayPal webhooks, PostgreSQL/R2DBC, Outbox pattern
├─ Inventory Service (8087) - Stock, PostgreSQL/R2DBC
└─ Notification Svc  (8086) - Async email via Kafka consumers
```

**Maven multi-module**: Root `pom.xml` manages all dependency versions. Each service has its own `pom.xml` inheriting from root.

## Critical Data Flows

1. **Auth**: Gateway validates JWT (`JwtAuthenticationFilter`) → forwards `X-User-Id`, `X-User-Email`, `X-Role` headers to services. Public routes bypass JWT: `/api/v1/users/**` and `/api/v1/payments/webhooks/**`.

2. **Order saga**: `POST /api/v1/orders` → Order Service publishes `order.created` to Kafka → Payment Service consumes → Stripe/PayPal webhook fires → Payment Service publishes `payment.completed` → Inventory Service deducts stock → Order status updated.

3. **Products (high-read path)**: Request → Redis cache hit → response. Cache miss → Cassandra query → Redis write → response. Cassandra uses **query-first multi-table design** (separate tables for lookups by ID, category, brand, featured).

## Database Patterns

**PostgreSQL services** (User, Order, Payment, Inventory): R2DBC reactive driver, `ReactiveCrudRepository`, Flyway migrations at `src/main/resources/db/migration/V*__*.sql`.

**Cassandra** (Product Service only): `@EnableCassandraRepositories`, multi-table strategy. Think query-first, not normalize-first.

**Redis** (Product Service only): Cache layer over Cassandra for high-volume product reads.

## Kafka

Single cluster, KRaft mode. Topic naming: `<domain>.<event-type>` (e.g., `order.created`, `payment.completed`, `payment.failed`, `inventory.reserved`, `inventory.failed`).

- Producers: `acks: all`, `enable.idempotence: true`, `retries: 3` (at-least-once)
- Consumers: `group-id: ecommerce-group`, `auto-offset-reset: earliest`
- Event models live in each service's `event/` package; consumers in `kafka/` package

## Package Structure

All services share `com.pd.ecommerce` base package with consistent subdirectory layout:

```
controller/   REST endpoints, path prefix /api/v1/<resource>
service/      Interface + Impl pattern
repository/   R2DBC or Cassandra repos
entity/       Domain entities (Lombok @Data, @Builder)
dto/          Request/Response DTOs (use @Valid)
kafka/        Producers and consumers
event/        Kafka event models
config/       Security, Kafka, DB configuration
mapper/       MapStruct DTO↔Entity mappers
exception/    Custom exceptions
```

## Integration Tests

Tests use **Testcontainers** — real PostgreSQL, Cassandra, and Kafka containers spin up per test run. Each service has an abstract base test class (e.g., `AbstractPostgresIntegrationTest`, `AbstractKafkaIntegrationTest`) using `@DynamicPropertySource` to inject container connection details.

Test config in `src/test/resources/application.yaml` (or `.properties`) disables Flyway and uses test credentials; schema is bootstrapped via `schema-it.sql`.

## Kubernetes

Manifests in `k8s/`. Apply with:
```bash
kubectl apply -k k8s/
kubectl -n ecommerce get pods
kubectl port-forward -n ecommerce svc/gateway-service 8081:80
```

All services expose `/actuator/health` for readiness/liveness probes. Gateway is the only LoadBalancer service (port 80).

## Environment / Secrets

Copy `.env` for docker-compose. Required variables: `POSTGRES_USER`, `POSTGRES_PASSWORD`, `CASSANDRA_USERNAME`, `CASSANDRA_PASSWORD`, `JWT_SECRET`, `STRIPE_API_KEY`, `STRIPE_WEBHOOK_SECRET`, `PAYPAL_CLIENT_ID`, `PAYPAL_CLIENT_SECRET`, `SMTP_USER`, `SMTP_PASSWORD`.

In Docker/K8s, secrets are mounted at `/run/secrets/` at runtime.
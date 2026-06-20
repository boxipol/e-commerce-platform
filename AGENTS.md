# AI Agent Guidelines for E-Commerce Platform

## Architecture Overview

**Microservices Pattern**: 7 independent Spring Boot services orchestrated via Kubernetes with Maven multi-module structure.

```
Gateway (8081) → Routes to specialized services
├─ User Service (8082) - JWT/Auth, PostgreSQL
├─ Order Service (8083) - Order management, PostgreSQL + Resilience4j
├─ Product Service (8084) - Catalog, Cassandra + Redis cache
├─ Payment Service (8085) - Payment processing (Stripe/PayPal webhooks)
├─ Inventory Service (8087) - Stock management, PostgreSQL
└─ Notification Service (8086) - Async notifications via Kafka
```

**Critical Data Flows**:
1. **Authentication**: Client → Gateway JWT validation → Service checks
2. **Orders**: Order created → Kafka event → Payment Service → Webhook → Inventory deduction → Order completion
3. **Products**: Request → Redis cache → Cassandra (if miss) → Multiple query tables optimized by use case

## Project Structure & Conventions

### Multi-Module Maven
- **Root `pom.xml`**: Parent with dependency management (JJWT, MapStruct, Stripe, PayPal, Testcontainers BOM)
- **Service modules**: Each has own `pom.xml` inheriting from root
- **Package structure**: `com.pd.ecommerce` with subdirectories:
  - `controller/` - REST endpoints with `@RestController`, path `/api/v1/<resource>`
  - `service/` - Interfaces + implementations (Interface-Implementation pattern)
  - `repository/` - Data access (R2DBC for Postgres, Cassandra for products)
  - `entity/` - JPA/Cassandra entities
  - `dto/` - Transfer objects (DTOs for requests/responses)
  - `kafka/` - Event producers/consumers
  - `event/` - Event models (e.g., `OrderCreatedEvent`)
  - `config/` - Security, Kafka, database configs
  - `exception/` - Custom exceptions
  - `mapper/` - MapStruct mappers (for DTO ↔ Entity conversion)

### Code Style Conventions
- **Lombok**: `@RequiredArgsConstructor`, `@Data`, `@Builder` on entities/DTOs
- **REST naming**: `/api/v1/` prefix, resource plural names, standard HTTP verbs
- **Reactivity**: Controllers return `Mono<T>` or `Flux<T>`, services use reactive chains
- **Validation**: `@Valid` on request DTOs, embedded validation annotations

### Configuration Files
- **YAML over Properties**: All services use `application.yaml` + `application-local.yaml`
- **Secrets**: Loaded from `/run/secrets/` at runtime (Docker/K8s)
- **Kafka Settings**: All use `org.apache.kafka.common.serialization` with `JsonDeserializer`

## Database Patterns by Service

### PostgreSQL Services (User, Order, Payment, Inventory)
- **Driver**: R2DBC with pgpool connection pooling for reactivity
- **Config**: `spring.r2dbc.url: r2dbc:postgresql://<service>-db:5432/<db_name>`
- **Initialization**: Flyway migrations in `src/main/resources/db/migration/V*__*.sql` (enabled via `spring.flyway.enabled: true`)
- **Repository pattern**: Extend `ReactiveCrudRepository<Entity, ID>`

### Cassandra (Product Service)
- **Setup**: `@EnableCassandraRepositories` annotation
- **Multi-table strategy**: Separate tables for product lookups by ID, category, brand, featured products
- **Config**: Env vars `SPRING_CASSANDRA_*` in docker-compose
- **Query optimization**: Think "query-first", not "normalize-first" for Cassandra

### Redis (Product Service Only)
- **Purpose**: Cache layer over Cassandra for low-latency product reads (~30M/day)
- **Spring Data**: `spring-boot-starter-data-redis-reactive`

## Kafka & Event Architecture

**Single Kafka cluster** (KRaft mode) with topics:
- Topic naming convention: `<domain>.<event-type>` (e.g., `order.created`, `payment.completed`)
- **Producer config**: All set to `acks: all`, `enable.idempotence: true`, `retries: 3` (at-least-once semantics)
- **Consumer config**: `group-id: ecommerce-group`, `auto-offset-reset: earliest`

**Event Flow Pattern**:
1. Service publishes event via `OrderEventProducer` (Kafka producer bean)
2. Consumers listen in `<Service>EventConsumer` classes
3. Events model: Separate `*Event` classes in `event/` package
4. Use `@KafkaListener` for async event processing

**Critical Events**:
- `order.created` → Order created, payment needed
- `payment.completed` → Inventory should deduct stock
- `payment.failed` → Order cancellation, refund logic
- `inventory.reserved` / `inventory.failed` → Inventory state changes

## Spring Boot Specific Patterns

### Reactive WebFlux (All Services)
- Controllers return `Mono<T>` or `Flux<T>` non-blocking streams
- Chain operations with `.flatMap()`, `.map()`, `.subscribe()`
- Use `Mono.just()`, `Mono.empty()`, `Mono.error()` for creating publishers

### Security & JWT
- **Gateway**: WebFlux Security + `JwtAuthenticationFilter` (`gateway-service/src/main/java/com/pd/ecommerce/security/JwtAuthenticationFilter.java`) validates Bearer JWT on non-public routes
- **Public routes at gateway**: `/api/v1/users/**` and `/api/v1/payments/webhooks/**`
- **Services**: Consume forwarded user context headers (`X-User-Id`, `X-User-Email`, `X-Role`) from gateway
- **JWT Config**: Secret loaded from `JWT_SECRET` env var, 1-hour expiration default

### Actuator & Monitoring
- Enabled on all services for readiness/liveness probes
- OpenTelemetry auto-instrumentation via javaagent
- Jaeger for trace visualization

### Resilience4j (Order Service)
- Circuit breaker pattern for inter-service calls
- See `order-service/pom.xml` for `resilience4j-spring-boot3` dependency

## Build & Deployment

### Local Development
```bash
# Build all services
mvn clean package

# Start full stack (all services + infra)
docker compose up

# Start infra only for local IDE service runs
docker compose -f docker-compose.local.yml up -d

# Single service rebuild without cache
docker compose build --no-cache <service-name>

# Database setup
psql -U ecommerce_user -d <db_name>  # PostgreSQL
cqlsh                                  # Cassandra
```

### Kubernetes Deployment
- Each service has `k8s/deployment.yaml` with:
  - Single replica (default)
  - LoadBalancer service exposure for gateway-service
  - Pod health checks via Actuator `/actuator/health`
  - IMAGE_PLACEHOLDER for container image

### Testing
- Unit and integration tests exist under `src/test/java` in multiple services (e.g., `user-service`, `order-service`, `gateway-service`, `product-service`)
- Integration tests use Testcontainers (PostgreSQL/Kafka/Cassandra depending on service)
- Quick runs:
  - `mvn test`
  - `mvn -f <service>/pom.xml test`

## Critical Integration Points

### Cross-Service Communication
1. **Gateway routing**: Maps `/api/v1/products/**`, `/api/v1/orders/**`, `/api/v1/users/**`, `/api/v1/inventories/**`, `/api/v1/payments/**`, `/api/v1/notifications/**`
2. **Order → Payment**: Sync via HTTP, then async via Kafka for webhook responses
3. **Payment → Inventory**: One-way Kafka event (`payment.completed` triggers stock deduction)
4. **Inventory → Order**: Kafka event consumer updates order completion status

### Stripe/PayPal Integration
- **Payment Service endpoints**: `/api/v1/payments/webhooks/stripe` and `/api/v1/payments/webhooks/paypal`
- **Gateway path for Stripe tests**: `stripe listen --forward-to http://localhost:8081/api/v1/payments/webhooks/stripe`
- **Secrets**: `STRIPE_API_KEY` and `STRIPE_WEBHOOK_SECRET` in docker-compose (test keys hardcoded)

### Rate Limiting & Throttling
- Implemented in **Gateway Service** (not yet visible in code)
- Strategy: Per-IP or per-user rate limits before routing to services

## Key Files & Examples

- **Gateway JWT filter + routing**: `gateway-service/src/main/java/com/pd/ecommerce/security/JwtAuthenticationFilter.java`, `gateway-service/src/main/resources/application.yaml`
- **User auth flow**: `user-service/src/main/java/com/pd/ecommerce/service/UserService.java` and `user-service/src/main/java/com/pd/ecommerce/service/UserServiceImpl.java`
- **Order event handling**: `order-service/src/main/java/com/pd/ecommerce/kafka/OrderEventProducer.java` + consumers
- **Product caching**: `product-service/src/main/java/com/pd/ecommerce/service/` (Redis + Cassandra dual-layer)
- **Payment webhook**: `payment-service/src/main/java/com/pd/ecommerce/controller/WebhookController.java`
- **PostgreSQL migrations**: `user-service/src/main/resources/db/migration/`, `order-service/src/main/resources/db/migration/`, `payment-service/src/main/resources/db/migration/`, `inventory-service/src/main/resources/db/migration/`

## Common Developer Tasks

### Adding a New Endpoint
1. Create `DTO` class in `dto/` with validation annotations
2. Create/update `Service` interface in `service/`
3. Implement service in `ServiceImpl`
4. Add method to REST controller with `@PostMapping/@GetMapping` etc.
5. Use `@Valid` on DTO parameters

### Publishing a Kafka Event
1. Create `*Event` class in `event/` package
2. Inject event producer bean (Spring will create via `@Bean`)
3. Call `kafkaTemplate.send(topic, event)` with `Topic` constant

### Consuming Kafka Events
1. Create consumer class in `kafka/` (e.g., `PaymentEventConsumer`)
2. Use `@KafkaListener(topics = "...")` on method
3. Process event, update service state, publish new events if needed

### Database Schema Changes
- For PostgreSQL: Add a new Flyway script in `src/main/resources/db/migration/` using `V<next>__<description>.sql`
- For Cassandra: Update CQL in seed query file + product service config

## Important TODOs & Known Gaps
- Exception handling framework not centralized
- AOP for cross-cutting concerns (logging, monitoring)
- Cache invalidation on product updates
- Admin control panel
- ElasticSearch for product discovery
- RabbitMQ as alternative messaging

## Non-Functional Requirements to Keep in Mind
- **Low latency for product views** (60M reads/day) → Redis + Cassandra strategy
- **High scalability** (1M users, growing 100K/month) → Microservices + K8s + reactive
- **HA & Resilience** → Multi-replica deployments, circuit breakers, event retries
- **Secure payments** → Stripe/PayPal integration, webhook validation
- **Monitoring** → OpenTelemetry + Prometheus + Grafana
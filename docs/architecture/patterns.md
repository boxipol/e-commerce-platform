# Design Patterns

## Outbox pattern

Order Service and Payment Service both use the transactional outbox pattern to guarantee Kafka delivery without distributed transactions.

```
@Transactional
1. Save domain entity  (orders / payments table)
2. Save OutboxEvent    (outbox_events table, status=PENDING)
   └─ same local transaction → atomically consistent

Outbox scheduler (every ~1s):
3. SELECT * FROM outbox_events WHERE status = 'PENDING'
4. Publish to Kafka
5. UPDATE outbox_events SET status = 'PROCESSED'
```

If the service crashes between steps 2 and 5, the scheduler retries on restart. Consumers are idempotent.

## Resilience4j (Order → Product)

The `ProductServiceClient` in Order Service wraps HTTP calls to Product Service with a circuit breaker. If Product Service is down, orders fail fast rather than queuing indefinitely.

## MapStruct mappers

All DTO ↔ entity conversions use compile-time MapStruct mappers (`@Mapper(componentModel = "spring")`). No reflection at runtime.

## Reactive error handling

Empty `Mono<T>` from a repository is always converted to a 404 `ResponseStatusException` in the service layer — never allowed to leak as a silent 200. Example:

```java
return repository.findById(id)
    .switchIfEmpty(Mono.error(
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found: " + id)))
    .map(mapper::toResponse);
```

## Integration testing

All integration tests use **Testcontainers** — real PostgreSQL, Cassandra, and Kafka containers. There are no database mocks. Schema is bootstrapped via `schema-it.sql` with Flyway disabled for tests.

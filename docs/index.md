# SmartShop E-Commerce Platform

Reactive microservices platform for product catalog browsing, order management, and payment processing.

## System map

```
                        ┌─────────────────────────────────────┐
                        │         Gateway  :8081               │
                        │  JWT validation · routing · tracing  │
                        └────────────────┬────────────────────┘
                                         │
          ┌──────────┬──────────┬────────┼────────┬──────────┬──────────┐
          ▼          ▼          ▼        ▼        ▼          ▼          ▼
      User Svc   Order Svc  Product   Payment  Inventory Notification  (future)
       :8082      :8083      :8084     :8085     :8087      :8086
      Postgres   Postgres  Cassandra  Postgres  Postgres    Kafka
                            + Redis                        consumer
```

## Tech stack

| Layer | Technology |
|---|---|
| Runtime | Spring Boot 3.5, Spring WebFlux (reactive) |
| API | REST, WebFlux (`Mono<T>` / `Flux<T>`) |
| Messaging | Apache Kafka (KRaft, at-least-once) |
| Databases | PostgreSQL (R2DBC), Cassandra 5, Redis 7 |
| Auth | JWT (RS256), validated at gateway |
| Observability | OpenTelemetry → Grafana LGTM (Loki, Grafana, Tempo, Prometheus) |
| Packaging | Docker, Kubernetes (Docker Desktop / kind) |

## Scale targets

- 1 M users, 200 K DAU
- 60 M product reads / day → Redis cache in front of Cassandra
- 40 K orders / day
- 40 K payments / day, 100 K notifications / day

# Full Stack (Docker Compose)

Runs all 7 services plus all infrastructure in Docker.

## Start

```bash
docker compose up
```

## Rebuild a service after code changes

```bash
# Rebuild and restart one service
docker compose build --no-cache <service-name>
docker compose up -d <service-name>

# Rebuild all Java services
docker compose build --no-cache \
  gateway-service user-service order-service \
  product-service payment-service inventory-service notification-service
```

## Exposed ports

| Service / Infra | Host port |
|---|---|
| Gateway | 8081 |
| Grafana | 3000 |
| OTLP HTTP | 4318 |
| Kafka (external) | 29092 |
| PostgreSQL (users) | 5432 |
| PostgreSQL (orders) | 5433 |
| PostgreSQL (payments) | 5434 |
| PostgreSQL (inventory) | 5435 |
| Cassandra | 9042 |
| Redis | 6379 |

## Access the API

All traffic flows through the gateway. After `docker compose up`:

```
http://localhost:8081/swagger-ui.html   # Aggregated Swagger UI
http://localhost:8081/api/v1/users/register
http://localhost:8081/api/v1/products/{sku}
```

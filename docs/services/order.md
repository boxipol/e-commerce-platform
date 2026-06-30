# Order Service

**Port:** 8083  
**Database:** PostgreSQL (`orders_db`)  
**Base path:** `/api/v1/orders`

## Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/` | JWT | Place a new order |
| `GET` | `/my` | JWT | List all orders for the current user |
| `GET` | `/{publicOrderId}` | JWT | Get order by public ID (e.g. `ORD-A1B2C3D4`) |

## Order states

```
CREATED ──► PAID ──► (complete)
        └──► CANCELLED
```

Transitions are driven by Kafka events from Payment and Inventory services — the Order Service never self-transitions except on creation.

## Kafka

**Publishes:** `order.created` (via outbox table)  
**Consumes:** `payment.completed`, `payment.failed`, `inventory.reserved`, `inventory.failed`

## External dependency

Calls **Product Service** (HTTP + Resilience4j circuit breaker) to fetch current prices at order time. If Product Service is unavailable, order creation fails fast.

## Outbox

The `outbox_events` table ensures `order.created` is delivered to Kafka even if the service restarts mid-flight. The outbox scheduler polls every ~1 s.

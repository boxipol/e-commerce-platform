# Inventory Service

**Port:** 8087  
**Database:** PostgreSQL (`inventory_db`)  
**Base path:** `/api/v1/inventories`

## Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/{id}` | JWT | Get inventory record by ID |
| `GET` | `/batch?ids=` | JWT | Batch fetch by ID list |
| `POST` | `/` | JWT | Register a new product SKU with initial stock |
| `PATCH` | `/{id}` | JWT | Adjust stock level or attributes |
| `DELETE` | `/{id}` | JWT | Remove inventory record |

## Kafka

**Publishes:** `inventory.reserved`, `inventory.failed`  
**Consumes:** `payment.completed`

On `payment.completed`: deducts stock for the order items. If stock is insufficient, publishes `inventory.failed` to trigger a refund and order cancellation.

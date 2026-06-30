# Services

| Service | Base path | Port | Database |
|---|---|---|---|
| [Gateway](gateway.md) | — | 8081 | — |
| [User](user.md) | `/api/v1/users` | 8082 | PostgreSQL |
| [Order](order.md) | `/api/v1/orders` | 8083 | PostgreSQL |
| [Product](product.md) | `/api/v1/products` | 8084 | Cassandra + Redis |
| [Payment](payment.md) | `/api/v1/payments` | 8085 | PostgreSQL |
| [Inventory](inventory.md) | `/api/v1/inventories` | 8087 | PostgreSQL |
| [Notification](notification.md) | — | 8086 | — |

All services expose `/actuator/health` for Kubernetes readiness / liveness probes.

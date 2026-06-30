# Order Saga

The order flow is a choreography-based saga spread across three services, coordinated via Kafka topics.

## Happy path

```
Client
  └─ POST /api/v1/orders
       │
       ▼
  Order Service
    1. Fetches product prices from Product Service (HTTP, Resilience4j)
    2. Persists Order (status=CREATED) + OrderItems
    3. Writes OrderCreatedEvent to outbox table
    4. Outbox scheduler publishes  ──► order.created
                                         │
                                         ▼
                                   Payment Service
                                     5. Consumes order.created
                                     6. Creates payment record (PENDING)
                                     7. Calls Stripe/PayPal → gets payment URL
                                     8. Returns payment URL in OrderResponse
                                         │
                              Customer completes payment in browser
                                         │
                                         ▼
                                   Stripe / PayPal
                                     9. Fires webhook ──► POST /api/v1/payments/webhooks/stripe
                                         │
                                         ▼
                                   Payment Service
                                    10. Verifies webhook signature
                                    11. Updates payment (COMPLETED)
                                    12. Writes outbox event ──► payment.completed
                                         │
                                         ▼
                                   Inventory Service
                                    13. Consumes payment.completed
                                    14. Deducts stock
                                    15. Publishes ──► inventory.reserved
                                         │
                                         ▼
                                   Order Service
                                    16. Consumes inventory.reserved
                                    17. Updates Order → status=PAID
```

## Failure paths

| Failure | Event published | Effect |
|---|---|---|
| Payment declined | `payment.failed` | Order → CANCELLED |
| Inventory insufficient | `inventory.failed` | Payment → REFUNDING, Order → CANCELLED |

## Kafka topics

| Topic | Producer | Consumer |
|---|---|---|
| `order.created` | Order Service (outbox) | Payment Service |
| `payment.completed` | Payment Service (outbox) | Inventory Service |
| `payment.failed` | Payment Service (outbox) | Order Service |
| `inventory.reserved` | Inventory Service | Order Service |
| `inventory.failed` | Inventory Service | Payment Service, Order Service |

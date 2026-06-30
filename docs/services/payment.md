# Payment Service

**Port:** 8085  
**Database:** PostgreSQL (`payments_db`)  
**Base paths:** `/api/v1/payments`, `/api/v1/payments/webhooks`

## Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/{id}` | JWT | Get payment by payment ID |
| `GET` | `/?orderId=` | JWT | Get payment by order ID |
| `POST` | `/webhooks/stripe` | None* | Stripe webhook callback |
| `POST` | `/webhooks/paypal` | None* | PayPal webhook callback |

\* Webhook endpoints bypass JWT but verify the provider's signature header.

## Payment states

```
PENDING ──► COMPLETED
        └──► FAILED
        └──► REFUNDING
```

## Providers

The service abstracts payment providers behind `PaymentProviderService`. Currently `STRIPE` is always selected; PayPal support is implemented but not yet wired into the resolver.

## Kafka

**Publishes:** `payment.completed`, `payment.failed` (via outbox table)  
**Consumes:** `order.created`

## Outbox

Same pattern as Order Service — `payment.completed` / `payment.failed` are written to an outbox table in the same transaction as the payment status update.

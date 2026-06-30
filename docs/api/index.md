# API Reference

The gateway aggregates all service specs into a single Swagger UI. Use the service dropdown in the top-right to switch between APIs.

## Swagger UI

```
http://localhost:8081/swagger-ui.html
```

## Individual spec URLs

| Service | OpenAPI JSON |
|---|---|
| Gateway | http://localhost:8081/v3/api-docs |
| User | http://localhost:8081/user-service/v3/api-docs |
| Order | http://localhost:8081/order-service/v3/api-docs |
| Product | http://localhost:8081/product-service/v3/api-docs |
| Payment | http://localhost:8081/payment-service/v3/api-docs |
| Inventory | http://localhost:8081/inventory-service/v3/api-docs |
| Notification | http://localhost:8081/notification-service/v3/api-docs |

## Authentication

All endpoints except `/register`, `/login`, and payment webhooks require a Bearer JWT.

1. Call `POST /api/v1/users/login` → get `token`
2. In Swagger UI: click **Authorize** → paste `Bearer <token>`
3. All subsequent requests in the session will include the token

## Quick reference

### Users

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/users/register` | Create account |
| `POST` | `/api/v1/users/login` | Get JWT |
| `GET` | `/api/v1/users/me` | Current profile |
| `PATCH` | `/api/v1/users/update` | Update profile |
| `DELETE` | `/api/v1/users/delete` | Delete account |

### Orders

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/orders` | Place order |
| `GET` | `/api/v1/orders/my` | My orders |
| `GET` | `/api/v1/orders/{publicOrderId}` | Order by public ID |

### Products

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/products/{sku}` | Get by SKU |
| `GET` | `/api/v1/products/batch?skus=` | Batch by SKU |
| `GET` | `/api/v1/products/category/{cat}` | Browse by category |
| `POST` | `/api/v1/products` | Create |
| `PATCH` | `/api/v1/products/{id}` | Update |
| `DELETE` | `/api/v1/products/{id}` | Delete |

### Payments

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/payments/{id}` | Payment by ID |
| `GET` | `/api/v1/payments?orderId=` | Payment by order |
| `POST` | `/api/v1/payments/webhooks/stripe` | Stripe callback |
| `POST` | `/api/v1/payments/webhooks/paypal` | PayPal callback |

### Inventory

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/inventories/{id}` | Stock by ID |
| `GET` | `/api/v1/inventories/batch?ids=` | Batch stock |
| `POST` | `/api/v1/inventories` | Register SKU |
| `PATCH` | `/api/v1/inventories/{id}` | Adjust stock |
| `DELETE` | `/api/v1/inventories/{id}` | Remove record |

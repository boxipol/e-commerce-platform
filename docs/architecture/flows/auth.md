# Auth Flow (JWT)

All authentication is handled at the gateway. Downstream services never validate tokens — they trust the headers injected by the gateway.

## Login / registration (public routes)

```
Client
  └─ POST /api/v1/users/register   (no JWT required)
  └─ POST /api/v1/users/login      (no JWT required)
       │
       ▼
  Gateway  ──── bypasses JWT filter (path starts with /api/v1/users)
       │
       ▼
  User Service
       │  validates credentials, issues JWT
       ▼
  { "token": "eyJ..." }  ←── returned to client
```

## Authenticated requests

```
Client
  └─ GET /api/v1/orders/my
       Authorization: Bearer eyJ...
       │
       ▼
  Gateway  ──── JwtAuthenticationFilter
       │         • validates signature + expiry
       │         • extracts userId, email, role
       │         • injects headers:
       │             X-User-Id:    <uuid>
       │             X-User-Email: user@example.com
       │             X-Role:       USER
       ▼
  Order Service
       │  reads X-User-Id from request header
       │  never sees or validates the JWT
       ▼
  Response
```

## Public routes (JWT bypassed)

| Path pattern | Reason |
|---|---|
| `/api/v1/users/**` | Registration + login happen before any token exists |
| `/api/v1/payments/webhooks/**` | Stripe/PayPal callbacks arrive without a user JWT; signature-verified instead |
| `/swagger-ui/**` | Documentation UI |
| `**/v3/api-docs` | OpenAPI spec endpoints |

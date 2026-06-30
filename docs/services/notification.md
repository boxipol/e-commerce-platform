# Notification Service

**Port:** 8086  
**Database:** None  
**Transport:** Email (SMTP — iCloud Mail / `smtp.mail.me.com:587`)

## Overview

Notification Service is a pure Kafka consumer. It has no database and no user-facing REST API beyond a test endpoint. All notifications are triggered by events.

## Kafka consumers

| Topic | Action |
|---|---|
| `user.created` | Sends welcome email to the new user |

## Test endpoint

```
POST /test-mail
```

Manually triggers a welcome email for a given `UserCreatedEvent` payload. For local testing only — not exposed through the gateway in production.

## Configuration

```yaml
spring.mail:
  host: smtp.mail.me.com
  port: 587
  username: ${SMTP_USER}
  password: ${SMTP_PASSWORD}
```

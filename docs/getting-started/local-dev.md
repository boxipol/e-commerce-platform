# Local Development (IDE)

This mode starts only infrastructure in Docker. Services run from IntelliJ with hot-reload.

## 1. Start infrastructure

```bash
docker compose -f docker-compose.local.yml up -d
```

This starts: PostgreSQL ×4, Cassandra, Redis, Kafka, Grafana LGTM (otel-lgtm).

## 2. Configure run profiles

Add `SPRING_PROFILES_ACTIVE=local` to each service's IntelliJ run configuration.

The `application-local.yaml` profile in each service overrides Docker hostnames with `localhost` and sets distinct server ports:

| Service | Port |
|---|---|
| gateway-service | 8081 |
| user-service | 8082 |
| order-service | 8083 |
| product-service | 8084 |
| payment-service | 8085 |
| notification-service | 8086 |
| inventory-service | 8087 |

## 3. Environment variables

Copy `.env` and fill in secrets. The minimum set needed to start:

```
POSTGRES_USER=
POSTGRES_PASSWORD=
CASSANDRA_USERNAME=
CASSANDRA_PASSWORD=
JWT_SECRET=
STRIPE_API_KEY=
STRIPE_WEBHOOK_SECRET=
PAYPAL_CLIENT_ID=
PAYPAL_CLIENT_SECRET=
SMTP_USER=
SMTP_PASSWORD=
```

## 4. Seed Cassandra (first run)

```bash
# Connect
docker exec -it products-db cqlsh -u $CASSANDRA_USERNAME -p $CASSANDRA_PASSWORD

# Run seed files
SOURCE /path/to/full_iphone_seed_query_products_by_id.sql
SOURCE /path/to/full_iphone_seed_query_products_by_sku.sql
SOURCE /path/to/full_iphone_seed_query_products_by_category.sql
```

## 5. Stripe testing

```bash
# Forward webhooks to your local gateway
stripe listen --forward-to http://localhost:8081/api/v1/payments/webhooks/stripe

# Trigger a test event
stripe trigger payment_intent.succeeded
```

## Observability

| Dashboard | URL |
|---|---|
| Grafana | http://localhost:3000 |
| Prometheus | http://localhost:9090 |
| Tempo (traces) | http://localhost:3200 |
| Pyroscope (profiles) | http://localhost:4040 |

## Build commands

```bash
# Build all services
mvn clean package

# Build skipping tests
mvn -DskipTests clean package

# Run tests for one service
mvn -f order-service/pom.xml test

# Rebuild a single Docker image without cache
docker compose build --no-cache <service-name>
```

E-COMMERCE PLATFORM

The platform is used to trade products between customers and/or shops.

System design:

Functional requirements:
- create and manage user accounts
- store products in inventory
- provide products catalog
- create orders and manage orders
- provide secure payments
- create notifications

Non-functional requirements:
- authentication/authorisation
- monitor activity
- low latency especially viewing products
- highly scalable
- HA
- rate limiting
- caching

Back of the envelope:
- 1 million user base
- 100 000 users/month growth rate
- 100 KB metadata/user = 100 GB
- 20 % DAU = 200 000 DAU
- 1 million products 
- 1 MB per product = 1 TB product data
- 30 products by user viewed/day = 60 mil product reads
- 10 % of DAU add products = 20 000 product writes, 20 GB/day
- 20 %/DAU make 1 order = 40 000 orders/day
- 100 KB data/order = 40 GB/day
- 40 000 payments/day, 10 KB/payment = 400 MB/day
- 100 000 notifications/day(new registrations, orders creations, payment completions)

User point of view:
- singup/login
- view/add products
- organize orders
- make secure payments
- receive notifications


Tech stack:
Kubernetes
Docker
Spring Boot
Kafka
Redis
Cassandra
Postgres

Kubernetes
- orchestration
- service discovery
- LB
- pod lifecycle and scaling
- network policies & security

Docker
- containerisation

Spring Boot
- starters
- WebFlux for reactive REST
- R2DBC CRUD repos
- security


Gateway service (8081)
- entry point
- JWT validation - Spring Security OAuth2 resource server
- routing requests - /products, /orders/, /users, inventories
- rate limiting and throttling 
- logging and monitoring(OpenTelemetry), providing correlation ID, request start/end
simple flow:
- client->gateway->product->cassandra


User service (8082)
- manages user data
- CRUD users
- PostgreSQL
simple flow:
- signup->create account
- login->generates JWT
- remove->remove account


Order service (8083)
- manages customer orders
- CRUD orders
- PostgreSQL
simple flow:
- client->gateway->order->payment-inventory->kafka event


Product service (8084)
- manages the product catalog
- CRUD products
- Redis cache
- CassandraDB - optimize for product lookup by id, products by category, featured_products, products by brand, search/filter support, inventory lookups, recommendations / trending, product variants


Payment service (8085)
- handles payment processing

Order Service
↓
order.created
↓
Payment Service
↓
Create Payment Intent / Order at provider
↓
Payment record = PENDING
↓
Return payment URL

Customer completes payment

Stripe/PayPal
↓
Webhook
↓
Payment Service
↓
Update payment status = PAID
↓
Outbox Event
↓
payment.completed / payment.failed
↓
Inventory Deduct
↓
Success → Order Completed
or
Failure → Refund + Order Cancelled
↓
Order Service
↓
Order status updated


Notification service (8086)
- handles notifications
simple flow:
- kafka event->notification->mail/push


Inventory service (8087)
- handles stock amounts
- notify merchants on stock 0


Logging and monitoring
- Gateway logging filter
- Spring Boot Actuator
- OpenTelemetry auto-instrumentation -javaagent:/Users/user/IdeaProjects/opentelemetry-javaagent.jar 
    - docker run --rm -p 4317:4317 -p 4318:4318 otel/opentelemetry-collector:latest
- Jaeger (trace visualization)
- Elasticsearch (analytics)
- ClickHouse (kafka analytics)
- Prometheus (data collection)
- Grafana (dashboards)


TODOs:
- cassandra batch update with outbox for product updates
- DB Flyway migrations
- circuit breaker
- cache warmup
- cache invalidation on change
- exception handling
- AOP
- monitoring
- RabbitMQ
- admin control
- ElasticSearch for product discovery


simple flow
client logins through gateway service
gateway service checks customer service for data and logins/singup
client requests product(product service)



Postgres:
psql -U ecommerce_user -d users_db
psql -U ecommerce_user -d orders_db
psql -U ecommerce_user -d payments_db
psql -U ecommerce_user -d inventory_db

docker volume rm e-commerce-platform_users-db-data
docker volume rm e-commerce-platform_orders-db-data
docker volume rm e-commerce-platform_payments-db-data
docker volume rm e-commerce-platform_inventory-db-data

Cassandra:
cqlsh
cqlsh -f /Users/user/Downloads/full_iphone_seed_query_products_by_id.sql

Docker:
docker volume inspect cassandra_data
docker compose build some-service

docker compose -f docker-compose.local.yml up -d
docker compose build 2>&1


Stripe testing:
stripe trigger payment_intent.succeeded
stripe listen --forward-to http://localhost:8081/api/v1/payments/webhooks/stripe
stripe events list
stripe events resend evt_3ThA101MX7CZ1Cce0kHDrneo


while true; do printf "kubectl> "; read cmd; kubectl ${=cmd}; done

kubectl apply -k /Users/user/IdeaProjects/e-commerce-platform/k8s
kubectl -n ecommerce get pods
kubectl -n ecommerce get svc
kubectl -n ecommerce get jobs

kubectl exec -it users-db-0 -- psql -U ecommerce_user -d users_db
kubectl exec -it orders-db-0 -- psql -U ecommerce_user -d orders_db
kubectl exec -it payments-db-0 -- psql -U ecommerce_user -d payments_db
kubectl exec -it inventory-db-0 -- psql -U ecommerce_user -d inventory_db
kubectl exec -it products-db-0 -n ecommerce -- sh -c 'cqlsh -u "$CASSANDRA_USERNAME" -p "$CASSANDRA_PASSWORD"'

kubectl port-forward -n ecommerce svc/gateway-service 8081:80


kubectl cp product-service/src/main/resources/full_iphone_seed_query_products_by_id.sql \
ecommerce/products-db-0:/tmp/seed_by_id.cql

kubectl exec -it products-db-0 -n ecommerce -- \
sh -c 'cqlsh -u "$CASSANDRA_USERNAME" -p "$CASSANDRA_PASSWORD" -f /tmp/seed_by_id.cql'

kubectl cp product-service/src/main/resources/full_iphone_seed_query_products_by_sku.sql \
ecommerce/products-db-0:/tmp/seed_by_sku.cql

kubectl exec -it products-db-0 -n ecommerce -- \
sh -c 'cqlsh -u "$CASSANDRA_USERNAME" -p "$CASSANDRA_PASSWORD" -f /tmp/seed_by_sku.cql'

kubectl cp product-service/src/main/resources/full_iphone_seed_query_products_by_category.sql \
ecommerce/products-db-0:/tmp/seed_by_category.cql

kubectl exec -it products-db-0 -n ecommerce -- \
sh -c 'cqlsh -u "$CASSANDRA_USERNAME" -p "$CASSANDRA_PASSWORD" -f /tmp/seed_by_category.cql'

kubectl cp inventory-service/src/main/resources/full_iphone_inventory_seed.sql \
ecommerce/inventory-db-0:/tmp/seed_by_id.cql

kubectl exec -it inventory-db-0 -n ecommerce -- \
sh -c 'psql -U "$POSTGRES_USER" -d inventory_db -f /tmp/seed_by_id.cql'



K8s manifests:
- 00-namespace.yaml
- 01-infra.yaml
- 02-apps.yaml

Secrets:
- CI/CD creates `ecommerce-secrets` from GitLab variables via `deploy:k8s-secrets`.
- For local-only deployment, copy `k8s/03-secrets.template.yaml` to `k8s/01-secrets.yaml`, set values, then apply manually.

# PostgreSQL databases
kubectl port-forward svc/users-db 5432:5432 -n ecommerce
kubectl port-forward svc/orders-db 5433:5432 -n ecommerce
kubectl port-forward svc/payments-db 5434:5432 -n ecommerce
kubectl port-forward svc/inventory-db 5435:5432 -n ecommerce

# Cassandra
kubectl port-forward svc/products-db 9042:9042 -n ecommerce

# Redis
kubectl port-forward svc/redis 6379:6379 -n ecommerce

# Kafka
kubectl port-forward svc/kafka-1 29092:9092 -n ecommerce
kubectl port-forward svc/kafka-2 29092:9092 -n ecommerce
kubectl port-forward svc/kafka-3 29092:9092 -n ecommerce


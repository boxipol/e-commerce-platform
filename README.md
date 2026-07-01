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


# E-Commerce Platform

Distributed e-commerce platform built with:
- Java 17
- Spring Boot
- WebFlux
- PostgreSQL
- Cassandra
- Redis
- Kafka
- Docker
- Kubernetes
- GitLab CI/CD
- Grafana LGTM

## Architecture

![Architecture Diagram](images/arch.png)

## Services

| Service | Purpose |
|----------|----------|
| Gateway | API Gateway |
| User | Authentication and users |
| Product | Product catalog |
| Order | Order management |
| Payment | Stripe integration |
| Inventory | Stock management |
| Notification | Email notifications |

## Documentation
- docs/architecture.md
- docs/deployment.md
- docs/observability.md
- docs/security.md
- docs/development.md


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
- actuator for monitoring


Gateway service
- entry point
- JWT validation - Spring Security OAuth2 resource server
- routing requests - /products, /orders/, /users, inventories
- rate limiting and throttling 
- logging and monitoring(OpenTelemetry), providing correlation ID, request start/end


User service
- manages user data
- CRUD users
- PostgreSQL
simple flow:
- signup->create account
- login->generates JWT
- remove->remove account


Order service
- manages customer orders
- CRUD orders
- PostgreSQL


Product service
- manages the product catalog
- CRUD products
- Redis cache
- CassandraDB - optimize for product lookup by id, products by category, featured_products, products by brand, search/filter support, inventory lookups, recommendations / trending, product variants


Payment service
- handles payment processing with Stripe/PayPal webhooks


Notification service
- handles notifications


Inventory service
- handles stock amounts and reservations
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
- circuit breaker
- cache warmup
- exception handling
- AOP
- RabbitMQ
- admin control
- ElasticSearch for product discovery


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


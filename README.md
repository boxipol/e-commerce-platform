E-COMMERCE PLATFORM

The platform is used to trade products between customers and/or shops.

System design:

functional:
- create and manage user accounts
- store products in inventory
- provide products catalog
- create orders and manage orders
- provide secure payments
- create notifications

non-functional:
- authentication/authorisation
- monitor activity
- low latency especially viewing products
- highly scalable
- HA
- rate limiting
- caching

estimations:
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
- organize shopping carts
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
- reactive CRUD repos
- security


Gateway service (8081)
- entry point
- JWT validation - Spring Security OAuth2 resource server
- routing requests - /products, /orders/, /auth
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
- CassandraDB - optimise for product lookup by id, products by category, featured_products, products by brand, search/filter support, inventory lookups, recommendations / trending, product variants


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
Update payment status
↓
Outbox Event
↓
payment.completed / payment.failed
↓
Order Service
↓
Order status updated


Notification service (8086)
- handles notifications
simple flow:
- kafka event->notification->mail

Inventory service (8087)
- handles stock amounts
- notify merchants on stock 0



logging and monitoring
- Gateway logging filter
- Spring Boot Actuator
- OpenTelemetry auto-instrumentation -javaagent:/Users/user/IdeaProjects/opentelemetry-javaagent.jar 
    - docker run --rm -p 4317:4317 -p 4318:4318 otel/opentelemetry-collector:latest
- Jaeger (trace visualization)
- Elasticsearch
- Prometheus (data collection)
- Grafana (dashboards)


Consider:
- DB Flyway migrations
- circuit breaker

- error handling
- monitoring


simple flow
client logins through gateway service
gateway service checks customer service for data and logins/singup
client requests product(product service)



scripts:
brew services start postgresql
brew services start cassandra

psql -U ecommerce_user -d users_db
psql -U ecommerce_user -d orders_db
psql -U ecommerce_user -d payments_db
psql -U ecommerce_user -d inventory_db

cqlsh
cqlsh -f /Users/user/Downloads/full_iphone_seed_query.sql
docker build -t product-service -f product-service/Dockerfile .
docker volume inspect cassandra_data

stripe listen --forward-to http://localhost:8085/api/v1/webhooks/stripe
stripe trigger payment_intent.succeeded


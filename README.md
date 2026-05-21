E-COMMERCE PLATFORM

Kubernetes Orchestration
- service discovery
- LB
- pod lifecycle and scaling
- network policies & security

Docker
- containerisation

gateway service (8081)
- entry point
- JWT validation - Spring Security OAuth2 resource server
- routing requests - /products, /orders/, /customers
- rate limiting and throttling 
- logging and monitoring(OpenTelemetry), providing correlation ID, request start/end
simple flow:
- client->gateway->product->cassandra

user service (8082)
- manages user data
- CRUD users
- PostgreSQL
simple flow:
- signup->create account
- login->generates JWT
- remove->remove account

scripts:
    brew services start postgresql
    docker compose up -d
    psql -U ecommerce_user -d users_db

order service (8083)
- manages customer orders
- CRUD orders
- PostgreSQL - Flyway migrations
simple flow:
- client->gateway->order->payment-inventory->kafka event

    psql -U ecommerce_user -d orders_db

product service (8084)
- manages the product catalog
- CRUD products
- CassandraDB
  - optimise for product lookup by id, products by category, featured_products, products by brand, search/filter support, 
    inventory lookups, recommendations / trending, product variants, event-driven updates

      brew services start cassandra
      cqlsh
        cqlsh -f /Users/user/Downloads/full_iphone_seed_query.sql
    docker build -t product-service -f product-service/Dockerfile .
    docker volume inspect cassandra_data


payment service (8085)
- handles payment processing

  psql -U ecommerce_user -d payments_db

notification service (8086)
- handles notifications
simple flow:
- kafka event->notification->mail

logging and monitoring
- Gateway logging filter
- Spring Boot Actuator
- OpenTelemetry auto-instrumentation -javaagent:/Users/user/IdeaProjects/opentelemetry-javaagent.jar 
    - docker run --rm -p 4317:4317 -p 4318:4318 otel/opentelemetry-collector:latest
- Jaeger (trace visualization)
- Elasticsearch
- Prometheus (data collection)
- Grafana (dashboards)

simple flow
client logins through gateway service
gateway service checks customer service for data and logins/singup
client requests product(product service)
client creates order(order service)
order service check product service for availability
order service check customer service for data
creates and order a calls payment service
E-COMMERCE PLATFORM

Kubernetes Orchestration
- service discovery
- LB
- pod lifecycle and scaling
- network policies & security

Docker

gateway service (8081)
- entry point
- authentication and authorization
- routing requests - /products, /orders/, /customers
handles cross-cutting concerns:
- rate limiting and throttling 
- logging and monitoring

customer service (8082)
- manages customer data
- CRUD customers
- PostgreSQL

order service (8083)
- manages customer orders
- CRUD orders
- PostgreSQL

product service (8084)
- manages the product catalog
- CRUD products
- CassandraDB

payment service (8085)
- handles payment processing

notification service (8086)
- handles notifications


simple flow
client logins through gateway service
gateway service checks customer service for data and logins/singup
client requests product(product service)
client creates order(order service)
order service check product service for availability
order service check customer service for data
creates and order a calls payment service
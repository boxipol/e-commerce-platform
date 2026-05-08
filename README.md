E-COMMERCE PLATFORM

gateway service (8081)
- entry point
Spring Cloud Gateway
- route requests - /products, /orders/, /customers
handles cross-cutting concerns:
- authentication - simple spring authentication
- rate limiting
- logging

product service (8084)
- manages the product catalog
- CRUD products
- CassandraDB

order service (8082)
- manages customer orders
- CRUD orders
- PostgreSQL

customer service (8083)
- manages customer data
- CRUD customers
- PostgreSQL

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
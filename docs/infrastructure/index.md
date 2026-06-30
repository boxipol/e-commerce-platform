# Infrastructure

| Component | Technology | Services |
|---|---|---|
| Relational DB | PostgreSQL 16 | User, Order, Payment, Inventory |
| Document/Wide-column | Cassandra 5 | Product |
| Cache | Redis 7 | Product |
| Messaging | Kafka 3 (KRaft) | All except Gateway |
| Observability | Grafana LGTM | All |

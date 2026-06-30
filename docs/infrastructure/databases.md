# Databases

## PostgreSQL (R2DBC)

Used by User, Order, Payment, and Inventory services. Each service has its own database — no shared schemas.

| Database | Service | Host port (local) |
|---|---|---|
| `users_db` | user-service | 5432 |
| `orders_db` | order-service | 5433 |
| `payments_db` | payment-service | 5434 |
| `inventory_db` | inventory-service | 5435 |

Schema is managed by **Flyway** migrations at `src/main/resources/db/migration/V*__*.sql`.  
Integration tests use `schema-it.sql` with Flyway disabled.

### Connect (local Docker)

```bash
docker exec -it users-db    psql -U $POSTGRES_USER -d users_db
docker exec -it orders-db   psql -U $POSTGRES_USER -d orders_db
docker exec -it payments-db psql -U $POSTGRES_USER -d payments_db
docker exec -it inventory-db psql -U $POSTGRES_USER -d inventory_db
```

### Connect (Kubernetes)

```bash
kubectl exec -it users-db-0    -n ecommerce -- psql -U $POSTGRES_USER -d users_db
kubectl exec -it orders-db-0   -n ecommerce -- psql -U $POSTGRES_USER -d orders_db
kubectl exec -it payments-db-0 -n ecommerce -- psql -U $POSTGRES_USER -d payments_db
kubectl exec -it inventory-db-0 -n ecommerce -- psql -U $POSTGRES_USER -d inventory_db
```

---

## Cassandra

Used by Product Service. Keyspace: `ecommerce`. Datacenter: `datacenter1`.

Authentication: `PasswordAuthenticator` / `CassandraAuthorizer`.  
Heap: 1 G max, 256 M new-gen (k8s); 512 M max (local Docker).

### Connect (local Docker)

```bash
docker exec -it products-db cqlsh -u $CASSANDRA_USERNAME -p $CASSANDRA_PASSWORD
```

### Connect (Kubernetes)

```bash
kubectl exec -it products-db-0 -n ecommerce -- \
  sh -c 'cqlsh -u "$CASSANDRA_USERNAME" -p "$CASSANDRA_PASSWORD"'
```

### Seed data

```bash
# Copy seed files to the pod
kubectl cp product-service/src/main/resources/full_iphone_seed_query_products_by_id.sql \
  ecommerce/products-db-0:/tmp/seed_by_id.cql

# Execute
kubectl exec -it products-db-0 -n ecommerce -- \
  sh -c 'cqlsh -u "$CASSANDRA_USERNAME" -p "$CASSANDRA_PASSWORD" -f /tmp/seed_by_id.cql'
```

Repeat for `_by_sku.cql` and `_by_category.cql`.

---

## Redis

Cache layer in front of Cassandra for product reads. No persistence configured — cache is warm-up from Cassandra on miss.

```bash
docker exec -it redis redis-cli ping
```

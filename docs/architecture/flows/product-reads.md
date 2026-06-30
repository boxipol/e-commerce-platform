# Product Reads (Cache + Cassandra)

Product reads are the highest-throughput path (~60 M/day). Two layers of caching sit in front of the database.

## Read path

```
Client
  └─ GET /api/v1/products/{sku}
       │
       ▼
  Gateway ──► Product Service
                  │
                  ▼
              Redis cache ──── HIT ──► return immediately
                  │
                 MISS
                  │
                  ▼
             Cassandra
          products_by_sku table
                  │
                  ▼
           write to Redis
                  │
                  ▼
           return to client
```

## Cassandra table strategy (query-first)

Product data is stored in multiple denormalized tables, each optimized for a specific lookup pattern:

| Table | Partition key | Use case |
|---|---|---|
| `products` | `id` | Admin CRUD by internal ID |
| `products_by_sku` | `sku` | Single-product lookup (most common) |
| `products_by_category` | `category` | Browse by category (cursor-paginated) |

When a product is created or updated, all tables are written synchronously. When deleted, all tables are cleaned up.

## Cache key format

```
product:<sku>              # single product
products:<category>:<pageSize>:<pageState>   # category page
```

## Cache invalidation

Cache entries are evicted on `PATCH /{id}` and `DELETE /{id}`. Category pages are not proactively invalidated on product update (TTL-based expiry instead — a known limitation tracked in TODOs).

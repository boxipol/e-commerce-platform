# Product Service

**Port:** 8084  
**Databases:** Cassandra (`ecommerce` keyspace) + Redis  
**Base path:** `/api/v1/products`

## Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/{sku}` | JWT | Get product by SKU (cache → Cassandra) |
| `GET` | `/batch?skus=` | JWT | Batch fetch by SKU list |
| `GET` | `/category/{category}` | JWT | Paginated browse by category |
| `POST` | `/` | JWT | Create product (writes all tables) |
| `PATCH` | `/{id}` | JWT | Update product (evicts cache) |
| `DELETE` | `/{id}` | JWT | Delete product (all tables + cache) |

## Caching

Redis is the first read layer. Cache miss → Cassandra → write back to Redis.  
Cache keys: `product:<sku>` and `products:<category>:<pageSize>:<pageState>`.

## Cassandra tables

| Table | Lookup |
|---|---|
| `products` | By internal UUID (`id`) |
| `products_by_sku` | By SKU string |
| `products_by_category` | By category (cursor-paginated) |

All three tables are written atomically on create. On update / delete, the category projection is added/removed if the category field changes.

## Pagination

Category listing uses Cassandra's native paging token (`PagingState`) returned as an opaque `pageState` string. Pass it back as `?pageState=` to fetch the next page.

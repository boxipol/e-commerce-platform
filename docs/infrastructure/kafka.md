# Kafka

**Mode:** KRaft (no ZooKeeper)  
**docker-compose:** single broker (`kafka:9092`)  
**Kubernetes:** 3-node cluster (`kafka-1`, `kafka-2`, `kafka-3`, each on port 9092)

## Topic naming

```
<domain>.<event-type>
```

| Topic | Producer | Consumers |
|---|---|---|
| `order.created` | Order Service | Payment Service |
| `payment.completed` | Payment Service | Inventory Service |
| `payment.failed` | Payment Service | Order Service |
| `inventory.reserved` | Inventory Service | Order Service |
| `inventory.failed` | Inventory Service | Payment Service, Order Service |
| `user.created` | User Service | Notification Service |

## Producer config (all services)

```yaml
acks: all
enable.idempotence: true
retries: 3
```

## Consumer config

```yaml
group-id: ecommerce-group       # (notification-service uses notification-group)
auto-offset-reset: earliest
```

## Bootstrap servers

| Environment | Value |
|---|---|
| docker-compose | `kafka:9092` |
| Kubernetes | `kafka-1:9092,kafka-2:9092,kafka-3:9092` |
| Local IDE | `localhost:29092` |

The k8s override is applied via `SPRING_KAFKA_BOOTSTRAP_SERVERS` env var in `k8s/02-apps.yaml`. The local IDE override is in each service's `application-local.yaml`.

## Connect from host (k8s)

```bash
kubectl port-forward svc/kafka-1 29092:9092 -n ecommerce
# then: kafka-console-consumer --bootstrap-server localhost:29092 --topic order.created --from-beginning
```

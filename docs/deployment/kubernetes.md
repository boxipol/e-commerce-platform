# Kubernetes Deployment

Tested on Docker Desktop Kubernetes (`desktop-control-plane` node).

## Manifests

| File | Contents |
|---|---|
| `k8s/00-namespace.yaml` | `ecommerce` namespace |
| `k8s/01-infra.yaml` | PostgreSQL ×4, Cassandra, Redis, Kafka ×3, otel-lgtm |
| `k8s/01-secrets.yaml` | `ecommerce-secrets` Secret (copy from `03-secrets.template.yaml`) |
| `k8s/02-apps.yaml` | All 7 application Deployments + Services |
| `k8s/kustomization.yaml` | Kustomize entry point |

## Apply

```bash
kubectl apply -k k8s/
kubectl -n ecommerce get pods
```

## Secrets

CI/CD creates `ecommerce-secrets` from GitLab variables via the `deploy:k8s-secrets` job.

For local-only deployment:

```bash
cp k8s/03-secrets.template.yaml k8s/01-secrets.yaml
# edit values
kubectl apply -f k8s/01-secrets.yaml
```

## Images

Services use images from the private GitLab Container Registry. The `gitlab-registry` image pull secret must exist in the `ecommerce` namespace:

```bash
kubectl create secret docker-registry gitlab-registry \
  --docker-server=registry.gitlab.com \
  --docker-username=<user> \
  --docker-password=<token> \
  -n ecommerce
```

All Deployments reference `imagePullPolicy: IfNotPresent` — pull the image with `docker pull` on the node first, then k8s uses the local copy.

## Port forwarding

```bash
# Services
kubectl port-forward -n ecommerce svc/gateway-service 8081:80

# Databases
kubectl port-forward svc/users-db     5432:5432 -n ecommerce
kubectl port-forward svc/orders-db    5433:5432 -n ecommerce
kubectl port-forward svc/payments-db  5434:5432 -n ecommerce
kubectl port-forward svc/inventory-db 5435:5432 -n ecommerce
kubectl port-forward svc/products-db  9042:9042 -n ecommerce
kubectl port-forward svc/redis        6379:6379 -n ecommerce

# Kafka
kubectl port-forward svc/kafka-1 29092:9092 -n ecommerce

# Observability
kubectl port-forward svc/otel-lgtm 3000:3000 -n ecommerce
kubectl port-forward svc/otel-lgtm 9090:9090 -n ecommerce
kubectl port-forward svc/otel-lgtm 3200:3200 -n ecommerce
```

Use `scripts/start-port-forwards.sh` to open all of them at once.

## Cassandra seeding (k8s)

```bash
for f in by_id by_sku by_category; do
  kubectl cp product-service/src/main/resources/full_iphone_seed_query_products_${f}.sql \
    ecommerce/products-db-0:/tmp/seed_${f}.cql
  kubectl exec -it products-db-0 -n ecommerce -- \
    sh -c "cqlsh -u \"\$CASSANDRA_USERNAME\" -p \"\$CASSANDRA_PASSWORD\" -f /tmp/seed_${f}.cql"
done
```

## Healthchecks

All services expose `/actuator/health`. Gateway is the only `LoadBalancer` service (port 80).

## Resetting Kafka (stale PVC data)

```bash
kubectl delete statefulset kafka-1 kafka-2 kafka-3 -n ecommerce
kubectl delete pvc data-kafka-1-0 data-kafka-2-0 data-kafka-3-0 -n ecommerce
kubectl apply -f k8s/01-infra.yaml
```

# Kubernetes Setup From Scratch

This folder is a clean Kubernetes bootstrap for the e-commerce platform.

## What is included

- Namespace + app manifests: `00-namespace.yaml`, `01-infra.yaml`, `02-apps.yaml`
- Secrets template (not applied by kustomize): `03-secrets.template.yaml`
- Infrastructure for local/dev cluster:
  - PostgreSQL instances: `users-db`, `orders-db`, `payments-db`, `inventory-db`
  - Cassandra: `products-db`
  - Redis: `redis`
  - Kafka (single broker) with service aliases `kafka-1`, `kafka-2`, `kafka-3`
- App deployments/services for all 7 microservices
- Gateway exposed as `LoadBalancer` on port `80`
- Kustomize entry point: `kustomization.yaml`

## Important notes

- Images in `02-apps.yaml` are placeholders and must be replaced.
- `01-infra.yaml` uses `emptyDir` for data volumes (ephemeral). Good for bootstrap/dev, not production.
- Kafka is configured as single broker for a quick start while preserving app bootstrap server names.

## 1) Build and push images

Update image names in `02-apps.yaml` to your registry tags.

## 2) Configure secrets

For CI/CD, secrets are created by `deploy:k8s-secrets` from GitLab CI variables.
For local-only testing, copy `03-secrets.template.yaml` to `01-secrets.yaml`, fill values, and apply it manually.

## 3) Deploy

```bash
kubectl apply -k k8s
```

## 4) Check rollout

```bash
kubectl -n ecommerce get pods
kubectl -n ecommerce get svc
kubectl -n ecommerce get jobs
```

## 5) Reach gateway

```bash
kubectl -n ecommerce get svc gateway-service
```

If your cluster does not provide an external IP for `LoadBalancer`, use port-forward:

```bash
kubectl -n ecommerce port-forward svc/gateway-service 8081:80
```

Then call the API via `http://localhost:8081`.


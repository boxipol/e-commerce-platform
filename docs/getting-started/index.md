# Getting Started

Choose how you want to run the platform:

| Mode | Command | Use when |
|---|---|---|
| **Infra only** | `docker compose -f docker-compose.local.yml up -d` | Running services from the IDE |
| **Full stack** | `docker compose up` | Running everything in Docker |
| **Kubernetes** | `kubectl apply -k k8s/` | Production-like environment |

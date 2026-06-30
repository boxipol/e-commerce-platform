# Observability

All services export telemetry via **OpenTelemetry** to the `grafana/otel-lgtm` all-in-one container, which bundles Loki, Grafana, Tempo, Prometheus, Pyroscope, and an OTel Collector.

## Dashboards

| Tool | URL (local) | Purpose |
|---|---|---|
| Grafana | http://localhost:3000 | Dashboards for all signals |
| Prometheus | http://localhost:9090 | Metrics scrape target |
| Tempo | http://localhost:3200 | Distributed traces |
| Pyroscope | http://localhost:4040 | Continuous profiling |

## OTLP endpoints (all services)

```yaml
management:
  otlp:
    metrics:
      export:
        url: http://otel-lgtm:4318/v1/metrics
        step: 30s
        read-timeout: 30s
    tracing:
      endpoint: http://otel-lgtm:4318/v1/traces
    logging:
      endpoint: http://otel-lgtm:4318/v1/logs
  tracing:
    sampling:
      probability: 1.0
```

`otel-lgtm` resolves to `localhost` when running services from the IDE via the `local` Spring profile.

## Tracing a request end-to-end

1. Open Grafana → **Explore** → select **Tempo** data source
2. Search by service name or paste a trace ID from a log line
3. The waterfall view shows the full span across gateway → service → database

## Healthcheck

The `otel-lgtm` container creates `/tmp/ready` when all components have started (~15–30 s). The readiness probe checks for this file:

```yaml
readinessProbe:
  exec:
    command: ["test", "-f", "/tmp/ready"]
  initialDelaySeconds: 30
```

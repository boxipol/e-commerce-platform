#!/bin/bash

kubectl port-forward svc/users-db 5432:5432 -n ecommerce >/tmp/users-db.log 2>&1 &
kubectl port-forward svc/orders-db 5433:5432 -n ecommerce >/tmp/orders-db.log 2>&1 &
kubectl port-forward svc/payments-db 5434:5432 -n ecommerce >/tmp/payments-db.log 2>&1 &
kubectl port-forward svc/inventory-db 5435:5432 -n ecommerce >/tmp/inventory-db.log 2>&1 &
kubectl port-forward svc/products-db 9042:9042 -n ecommerce >/tmp/products-db.log 2>&1 &
kubectl port-forward svc/redis 6379:6379 -n ecommerce >/tmp/redis.log 2>&1 &
kubectl port-forward svc/kafka-1 9092:29092 -n ecommerce >/tmp/kafka.log 2>&1 &
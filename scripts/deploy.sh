#!/usr/bin/env bash
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)

kubectl apply -f "$ROOT/infra/k8s/namespace.yaml"
kubectl -n oxalio apply -f "$ROOT/infra/k8s/postgres.yaml"
kubectl -n oxalio apply -f "$ROOT/infra/k8s/redis.yaml"
kubectl -n oxalio apply -f "$ROOT/infra/k8s/kafka.yaml"
kubectl -n oxalio apply -f "$ROOT/infra/k8s/auth-service.yaml"
kubectl -n oxalio apply -f "$ROOT/infra/k8s/invoice-service.yaml"
kubectl -n oxalio apply -f "$ROOT/infra/k8s/integration-service.yaml"
kubectl -n oxalio apply -f "$ROOT/infra/k8s/api-gateway.yaml"
kubectl -n oxalio apply -f "$ROOT/infra/k8s/ingress.yaml" || true

echo "-- Pods:"
kubectl -n oxalio get pods -o wide

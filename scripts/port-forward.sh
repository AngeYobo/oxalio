#!/usr/bin/env bash
set -euo pipefail
echo "Port-forward api-gateway service 8080 -> 80 (namespace oxalio)"
kubectl -n oxalio port-forward svc/api-gateway 8080:80

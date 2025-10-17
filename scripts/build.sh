#!/usr/bin/env bash
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)

echo "[1/3] Build Maven (backend)"
mvn -q -f "$ROOT/backend/pom.xml" -DskipTests package

echo "[2/3] Build Docker images"
for SVC in api-gateway auth-service invoice-service integration-service; do
  echo "  -> $SVC"
  docker build -t ghcr.io/oxalio/$SVC:1.0.0 "$ROOT/backend/$SVC"
done

echo "Done."

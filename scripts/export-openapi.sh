#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="${ROOT_DIR}/docs/openapi"
mkdir -p "${OUT_DIR}"

timestamp() {
  if ts="$(date '+%Y%m%d-%H%M%S' 2>/dev/null)"; then
    echo "${ts}"
    return 0
  fi
  if ts="$(date -Iseconds 2>/dev/null)"; then
    echo "${ts}" | sed 's/:/-/g'
    return 0
  fi
  if command -v python3 >/dev/null 2>&1; then
    python3 - <<'PY'
from datetime import datetime, timezone
print(datetime.now(timezone.utc).strftime("%Y%m%d-%H%M%S"))
PY
    return 0
  fi
  echo "unknown-ts"
}

ts="$(timestamp)"

export_one() {
  local name="$1"
  local url="$2"
  local out="${OUT_DIR}/${name}-openapi-${ts}.json"
  echo "→ Export ${name}: ${url}"
  curl -fsS "${url}" -o "${out}"
  echo "  ✓ saved: ${out}"
}

export_one "invoice-service" "http://localhost:8082/api-docs"
export_one "terne-device-service" "http://localhost:8086/api-docs"

echo "✓ Done. OpenAPI exports are in: ${OUT_DIR}"

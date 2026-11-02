#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
if docker compose version >/dev/null 2>&1; then
  docker compose down --remove-orphans
elif command -v docker-compose >/dev/null 2>&1; then
  docker-compose down --remove-orphans
fi
echo "Stack VOA parada."

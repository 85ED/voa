#!/usr/bin/env bash
# Sobe só o Postgres do VOA (porta 5433). Execute na raiz do repo.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if docker ps --format '{{.Names}}' | grep -qx voa-postgres; then
  echo "Container voa-postgres já está rodando."
  docker start voa-postgres 2>/dev/null || true
  exit 0
fi

if docker compose version >/dev/null 2>&1; then
  docker compose up -d
elif command -v docker-compose >/dev/null 2>&1; then
  docker-compose up -d
else
  docker run -d --name voa-postgres \
    -e POSTGRES_DB=voa \
    -e POSTGRES_USER=voa \
    -e POSTGRES_PASSWORD=voa_dev \
    -p 5433:5432 \
    postgres:16-alpine
fi

echo "Postgres OK em localhost:5433 (user voa / senha voa_dev / db voa)"

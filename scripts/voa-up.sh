#!/usr/bin/env bash
# Comando único para subir o VOA (padrão: local + Colima Postgres).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

if [[ "${VOA_DOCKER:-0}" == "1" ]]; then
  "$ROOT/scripts/check-setup.sh"
  "$ROOT/scripts/ensure-docker-engine.sh"
  "$ROOT/scripts/ensure-docker-config.sh"
  exec "$ROOT/scripts/docker-up.sh"
fi

exec "$ROOT/scripts/voa-up-local.sh"

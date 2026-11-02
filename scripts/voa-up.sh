#!/usr/bin/env bash
# Padrão: caminho leve (Postgres já local ou Docker só o banco + API/front nativos).
# Stack 100% Docker (Colima ~4 GiB): VOA_DOCKER=1 ./scripts/voa-up.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

if [[ "${VOA_DOCKER:-0}" == "1" ]]; then
  "$ROOT/scripts/check-setup.sh"
  "$ROOT/scripts/ensure-docker-engine.sh"
  "$ROOT/scripts/ensure-docker-config.sh"
  exec "$ROOT/scripts/docker-up.sh"
fi

exec "$ROOT/scripts/voa-up-local.sh"

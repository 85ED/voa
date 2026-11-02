#!/usr/bin/env bash
# Comando único para subir o VOA (Postgres + API + web).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

"$ROOT/scripts/check-setup.sh"
"$ROOT/scripts/ensure-docker-engine.sh"
"$ROOT/scripts/ensure-docker-config.sh"
exec "$ROOT/scripts/docker-up.sh"

#!/usr/bin/env bash
# Carrega VOA_DB_PORT escolhido por start-postgres.sh (não falha se o arquivo não existir).
# shellcheck disable=SC1091
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
STATE_FILE="$ROOT/.voa-local-db"
if [[ -f "$STATE_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$STATE_FILE"
  set +a
fi
export VOA_DB_PORT="${VOA_DB_PORT:-5433}"

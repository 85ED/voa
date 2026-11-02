#!/usr/bin/env bash
# Uma instância do Vite. Reutiliza node_modules se já existir.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/frontend"

if lsof -nP -iTCP:5173 -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Porta 5173 já em uso. Não inicia segundo Vite."
  echo "Abra http://localhost:5173"
  echo "Para encerrar: ./scripts/stop-voa.sh"
  exit 1
fi

if [[ ! -d node_modules ]] || [[ ! -d node_modules/@fontsource/ibm-plex-sans ]]; then
  echo "==> Instalando dependências do frontend (npm ci)..."
  npm ci --no-audit --no-fund
else
  echo "==> Reutilizando frontend/node_modules"
fi

echo "==> Vite em http://localhost:5173 (proxy /api → :8080)"
exec npm run dev -- --host 127.0.0.1

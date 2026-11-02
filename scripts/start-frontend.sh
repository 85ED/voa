#!/usr/bin/env bash
# Uma instância do Vite. Reutiliza node_modules se já existir.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/frontend"

node_ok() {
  node -e 'const p=process.version.slice(1).split(".").map(Number); const ok=p[0]>20||(p[0]===20&&p[1]>=19)||p[0]>=22; process.exit(ok?0:1)' 2>/dev/null
}

ensure_node() {
  if node_ok; then
    return 0
  fi
  local cand bin
  for cand in \
    "$HOME/.nvm/versions/node/v24."*/bin \
    "$HOME/.nvm/versions/node/v22."*/bin \
    "$HOME/.nvm/versions/node/v20.19."*/bin \
    "$HOME/.nvm/versions/node/v20.2"[0-9]*/bin; do
    for bin in $cand; do
      if [[ -x "$bin/node" ]]; then
        export PATH="$bin:$PATH"
        if node_ok; then
          echo "==> Node $(node -v) (Vite exige 20.19+ ou 22+)"
          return 0
        fi
      fi
    done
  done
  echo "Node $(node -v 2>/dev/null || echo desconhecido) é antigo para o Vite." >&2
  echo "No terminal: nvm use 20.19  (ou instale Node 22+)" >&2
  exit 1
}

ensure_node

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

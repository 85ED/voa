#!/usr/bin/env bash
# Instalação única (Mac terminal + deps front se for usar Vite). Não rode todo dia.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

need_pkg=()
command -v colima >/dev/null 2>&1 || need_pkg+=(colima)
command -v docker >/dev/null 2>&1 || need_pkg+=(docker)
if ! docker compose version >/dev/null 2>&1 && ! command -v docker-compose >/dev/null 2>&1; then
  need_pkg+=(docker-compose)
fi

if [[ ${#need_pkg[@]} -gt 0 ]]; then
  if command -v brew >/dev/null 2>&1; then
    echo "==> Homebrew: instalando ${need_pkg[*]}"
    brew install "${need_pkg[@]}"
  else
    echo "Homebrew não encontrado — instale Colima/Docker manualmente: ${need_pkg[*]}" >&2
  fi
else
  echo "==> Colima / Docker CLI / Compose já instalados (sem brew install)."
fi

if command -v brew >/dev/null 2>&1; then
  _plug="$(brew --prefix)/lib/docker/cli-plugins"
  echo "Compose plugin em: $_plug"
  echo 'Adicione ao ~/.zshrc se precisar:'
  echo "  export DOCKER_CLI_PLUGIN_EXTRA_DIRS=\"\$(brew --prefix)/lib/docker/cli-plugins\""
fi

echo "==> Colima (não inicia sozinho aqui — reserva ~4 GiB). Para Docker: colima start"
echo "    Caminho leve do dia a dia não precisa de Colima: ./scripts/voa-up.sh"

if [[ -f "$ROOT/frontend/package.json" ]]; then
  if [[ -d "$ROOT/frontend/node_modules" ]]; then
    echo "==> frontend/node_modules já existe — pulando npm ci."
  else
    echo "==> Frontend: npm ci"
    (cd "$ROOT/frontend" && npm ci)
  fi
fi

echo ""
echo "Pronto. Todo dia: ./scripts/voa-up.sh && ./scripts/start-backend.sh (outro terminal: start-frontend.sh)"

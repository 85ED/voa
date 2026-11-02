#!/usr/bin/env bash
# Instalação única (Mac terminal + deps front se for usar Vite). Não rode todo dia.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "==> Homebrew: Colima + Docker CLI + Compose (se faltar)"
if command -v brew >/dev/null 2>&1; then
  brew install colima docker docker-compose
  _plug="$(brew --prefix)/lib/docker/cli-plugins"
  echo "Compose plugin em: $_plug"
  echo 'Adicione ao ~/.zshrc se precisar:'
  echo "  export DOCKER_CLI_PLUGIN_EXTRA_DIRS=\"\$(brew --prefix)/lib/docker/cli-plugins\""
else
  echo "Homebrew não encontrado — instale Colima manualmente."
fi

echo "==> Colima"
colima start 2>/dev/null || colima start

echo "==> Frontend (só se for usar npm run dev no futuro)"
if [[ -f "$ROOT/frontend/package.json" ]]; then
  (cd "$ROOT/frontend" && npm ci)
fi

echo ""
echo "Pronto. Todo dia use só: ./scripts/voa-up.sh"

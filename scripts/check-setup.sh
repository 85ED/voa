#!/usr/bin/env bash
# Verifica se o clone tem backend/ e frontend/ (git pull feito).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "Pasta do repo: $ROOT"
missing=0
for d in backend frontend docker-compose.yml; do
  if [[ ! -e "$d" ]]; then
    echo "FALTA: $d — rode: git pull origin main"
    missing=1
  fi
done
if [[ $missing -eq 1 ]]; then
  exit 1
fi
echo "Estrutura OK. Subir stack: ./scripts/voa-up.sh"

# Dados de demonstração (PostgreSQL)

Seed automático no **profile `local`**, controlado por `voa.demo-seed.enabled` (default `true` em `application-local.yml`).

## O que é inserido

| Recurso | Identificador estável | Quando |
|---------|----------------------|--------|
| Promoções | slugs `demo-rio-de-janeiro`, `demo-buenos-aires`, `demo-lisboa` | Sempre que o slug não existir |
| Cliente + solicitações + vendas demo | e-mail `demo.cliente@jlturismo.local` | Apenas se esse cliente ainda não existir |

O seed **não apaga** registros existentes e **não altera** promoções já cadastradas pelo administrador.

## Habilitar / desabilitar

```yaml
# backend/src/main/resources/application-local.yml
voa:
  demo-seed:
    enabled: true   # false para desligar
```

Reinicie a API após mudar a flag:

```bash
./scripts/stop-voa.sh
./scripts/voa-up.sh
```

## Verificar no banco

```bash
source ./scripts/load-local-db.sh
psql "postgresql://voa:voa_dev@localhost:${VOA_DB_PORT}/voa" -c \
  "SELECT slug, titulo, ativo, valido_de, valido_ate FROM promocao ORDER BY criado_em;"
```

## Verificar API e landing

```bash
curl -s http://localhost:8080/api/v1/public/promocoes | jq 'length, .[0].titulo'
```

Abra `http://localhost:5173/` — a seção **Promoções** deve listar as ofertas publicadas e válidas.

## Idempotência

Subir a API duas vezes **não duplica** promoções (checagem por `organizacao_id` + `slug`).

Teste automatizado: `DemoDataSeedRunnerTest`.

## Produção

Profile `local` não é usado em produção; o runner não executa. Não configure `demo-seed.enabled: true` em ambientes reais.

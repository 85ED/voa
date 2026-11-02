# Contratos REST principais (v1)

Base: `https://{api-host}/api/v1`  
Formato: JSON UTF-8; erros RFC 7807 (`application/problem+json`).

## Convenções

| Aspecto | Padrão |
|---------|--------|
| Auth | Bearer access JWT ou cookie (ver security) |
| Paginação | `?page=0&size=20&sort=criadoEm,desc` |
| Idempotência | Header `Idempotency-Key` em POST críticos (venda, pagamento) |
| Correlation | Response header `X-Request-Id` |

## Auth

| Método | Path | Descrição |
|--------|------|-----------|
| POST | `/auth/login` | `{ email, password }` → set cookies + `{ user, expiresIn }` |
| POST | `/auth/refresh` | Rotaciona refresh |
| POST | `/auth/logout` | Revoga refresh |
| GET | `/auth/me` | Usuário + papéis |

## Usuários (ADMIN)

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/usuarios` | Lista paginada |
| POST | `/usuarios` | Cria usuário |
| PATCH | `/usuarios/{id}` | Atualiza nome, papéis, ativo |
| POST | `/usuarios/{id}/reset-senha` | Fluxo admin |

## Clientes

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/clientes` | Busca `q`, paginação |
| POST | `/clientes` | Cria |
| GET | `/clientes/{id}` | Detalhe |
| PATCH | `/clientes/{id}` | Atualiza |

## Solicitações

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/solicitacoes` | Filtros: status, clienteId, responsavelId, datas |
| POST | `/solicitacoes` | Cria rascunho |
| GET | `/solicitacoes/{id}` | Detalhe + timeline resumida |
| PATCH | `/solicitacoes/{id}` | Atualiza campos / status |
| POST | `/solicitacoes/{id}/cancelar` | Cancelamento motivo |

## Cotações

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/solicitacoes/{sid}/cotacoes` | Lista versões |
| POST | `/solicitacoes/{sid}/cotacoes` | Nova versão (copia anterior opcional) |
| GET | `/cotacoes/{id}` | Detalhe + itens |
| PATCH | `/cotacoes/{id}` | Edita rascunho |
| POST | `/cotacoes/{id}/itens` | Adiciona item |
| PATCH | `/cotacoes/{id}/itens/{itemId}` | Atualiza item |
| DELETE | `/cotacoes/{id}/itens/{itemId}` | Remove item |
| POST | `/cotacoes/{id}/enviar` | Status ENVIADA |
| POST | `/cotacoes/{id}/aceitar` | Status ACEITA |

## Vendas

| Método | Path | Descrição |
|--------|------|-----------|
| POST | `/cotacoes/{id}/converter-venda` | Cria venda; idempotente |
| GET | `/vendas` | Filtros status, período, vendedor |
| GET | `/vendas/{id}` | Detalhe itens + pagamentos |
| POST | `/vendas/{id}/confirmar` | CONFIRMADA |
| POST | `/vendas/{id}/cancelar` | CANCELADA + motivo |

## Pagamentos

| Método | Path | Descrição |
|--------|------|-----------|
| POST | `/vendas/{vid}/pagamentos` | Registra pagamento + parcelas |
| GET | `/pagamentos/{id}` | Detalhe |
| PATCH | `/parcelas/{id}` | Marca pago, data, comprovante ref |
| POST | `/pagamentos/{id}/estornar` | FINANCEIRO+ |

## Bilhetes

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/vendas/{vid}/bilhetes` | Lista |
| POST | `/vendas/{vid}/bilhetes` | Metadados + upload URL assinada (fase 4) ou multipart MVP |
| GET | `/bilhetes/{id}` | Detalhe |
| PATCH | `/bilhetes/{id}` | Atualiza localizador/status |

## Auditoria

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/audit/eventos` | Filtros entidade, actor, período; role AUDITOR ou ADMIN |

## Health (público limitado)

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/actuator/health` | Liveness |

## Exemplo de erro

```json
{
  "type": "https://voa.jlturismo/errors/validation",
  "title": "Validação falhou",
  "status": 400,
  "detail": "validade deve ser futura",
  "instance": "/api/v1/cotacoes/..."
}
```

## RBAC resumido

| Recurso | ADMIN | COMERCIAL | OPERACOES | FINANCEIRO | AUDITOR |
|---------|-------|-----------|-----------|------------|---------|
| Solicitações | RW | RW | RW | R | R |
| Cotações | RW | RW | R | R | R |
| Vendas confirmar | RW | RW | R | R | R |
| Pagamentos | RW | R | R | RW | R |
| Usuários | RW | - | - | - | R |
| Audit | R | - | - | - | R |

OpenAPI completo gerado na Etapa 2 a partir dos controllers.

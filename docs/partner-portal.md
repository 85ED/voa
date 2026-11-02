# Portal parceiro (VOA)

Extensão do módulo **parceiros** existente — não há pipeline paralelo de solicitação.

## Admin (AGENCY ADMIN)

| Recurso | API |
|---------|-----|
| CRUD parceiro + slug | `GET/POST/PATCH /api/v1/parceiros` |
| Alterar slug (histórico) | `PATCH /api/v1/parceiros/{id}/slug` |
| Link público | campo `publicLink` (`PUBLIC_BASE_URL` + `/p/{slug}`) |
| Acesso usuário PARTNER | `POST /api/v1/parceiros/{id}/acesso` → senha temporária |
| Reset senha | `POST /api/v1/parceiros/{id}/acesso/{usuarioId}/reset-senha` |
| Filtrar solicitações/vendas/operações | query `parceiroId` nos endpoints existentes |

Regras:

- `parceiro_id` em solicitações/vendas **não muda** quando o slug muda.
- `parceiro_slug_snapshot` grava o slug no momento da solicitação via link.

## Página pública

- Rota SPA: `/p/:slug`
- `GET /api/v1/public/parceiros/{slug}` — branding mínimo
- `POST /api/v1/public/parceiros/{slug}/solicitacoes` — `canal=PARCEIRO_LINK`, honeypot `website`, rate limit público
- E-mail opcional neste endpoint

## Usuário PARTNER

- Papel `PARTNER` + `usuario.parceiro_id`
- JWT inclui claim `parceiro`
- Lista/detalhe de solicitações, cotações (por solicitação), vendas e operações **escopados** ao parceiro
- Tentativa de acessar recurso de outro parceiro → 400

## Variáveis

| Variável | Uso |
|----------|-----|
| `PUBLIC_BASE_URL` | Montagem do link copiável no admin |
| `VOA_PUBLIC_RATE_LIMIT_PER_MINUTE` | Rate limit `/api/v1/public/**` |

## Migrações

- `V11__partner_portal.sql` — slug, histórico, canal, parceiro em solicitação/venda, usuário PARTNER

## Testes

- `PartnerPortalIntegrationTest` — slug, solicitação pública, filtro admin, escopo PARTNER, snapshot de slug

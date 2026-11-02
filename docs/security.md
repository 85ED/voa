# Segurança e LGPD

## Autenticação e autorização

Implementação conforme **ADR-002**:

- Access JWT (15 min) + refresh rotacionado em Postgres.
- Cookies `HttpOnly`, `Secure`, `SameSite=Lax`.
- Spring Security: resource server + filtro refresh em `/auth/*`.
- RBAC via `@PreAuthorize("hasRole('FINANCEIRO')")` etc.
- Senhas: BCrypt strength 12+ ou Argon2id (preferível se biblioteca disponível).

## CSRF

- Mutations (`POST`, `PATCH`, `DELETE`): validar token CSRF quando cookies auth same-site.
- SPA envia header `X-CSRF-Token` espelhando cookie não-HttpOnly `XSRF-TOKEN`.

## Transporte e headers

- TLS terminado no Railway; HSTS no domínio custom (configurar no proxy/CDN).
- **API (Spring Security):** `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: strict-origin-when-cross-origin`, CSP restrita via `VOA_API_CONTENT_SECURITY_POLICY` (default `default-src 'none'; frame-ancestors 'none'; base-uri 'none'`).
- **SPA (Vite):** meta CSP baseline em `frontend/index.html`; em produção **replicar/reforçar** no host estático (`Content-Security-Policy`, `X-Frame-Options`, `Permissions-Policy` mínima).

### CSP baseline SPA (referência)

```
default-src 'self';
script-src 'self';
style-src 'self' 'unsafe-inline';
img-src 'self' data: https:;
connect-src 'self' https://<api-publica>;
frame-ancestors 'none';
base-uri 'self';
form-action 'self';
```

Ajuste `connect-src` para incluir a URL da API (`VITE_API_BASE_URL`) e domínios de imagens externas usados na landing.

## Rate limiting (MVP)

| Escopo | Variável | Default |
|--------|----------|---------|
| Leitura `/api/v1/public/**` | `VOA_PUBLIC_RATE_LIMIT_PER_MINUTE` | 30/min por IP |
| POST solicitações (viagem + parceiro) | `VOA_PUBLIC_POST_RATE_LIMIT_PER_MINUTE` | 8/min por IP (bucket separado por slug parceiro) |
| Login `POST /api/v1/auth/login` | `VOA_LOGIN_RATE_LIMIT_PER_MINUTE` | 20/min por IP |

Implementação in-memory (single instance). Para cluster, migrar para Redis/bucket compartilhado.

## LGPD — endpoints admin (stub operacional)

| Método | Rota | Papel | Notas |
|--------|------|-------|-------|
| GET | `/api/v1/admin/lgpd/clientes/{id}/export` | ADMIN | JSON cliente + solicitações; **não** é resposta formal ao titular |
| POST | `/api/v1/admin/lgpd/clientes/{id}/solicitar-exclusao` | ADMIN | Anonimiza cliente (`deleted_at`) + evento auditoria; revisar retenção legal antes de produção |

Processo formal ao titular permanece manual (ticket interno) até jurídico JL aprovar fluxo automatizado.

## Export vendas CSV

- `GET /api/v1/vendas/export.csv` — papéis `ADMIN`, `COMERCIAL`, `FINANCEIRO`, `AUDITOR`.

## Dados sensíveis

| Dado | Tratamento |
|------|------------|
| Senha | Hash irreversível; nunca logar |
| Documento cliente (CPF/CNPJ) | Criptografia em repouso opcional Etapa 4; mascarar em logs |
| Tokens refresh | Apenas hash SHA-256 |
| Bilhetes PDF | Storage privado; URL assinada TTL curto |

## Auditoria

- Eventos imutáveis em `auditoria_evento` para login falho, CRUD usuário, mudança status venda/pagamento, export dados.
- Retenção mínima **5 anos** sugerida para registros comerciais (confirmar com JL jurídico).

## LGPD (Lei 13.709/2018)

### Papéis

- **Controlador:** JL Turismo (dados de clientes/passageiros).
- **Operador:** provedor de hospedagem (Railway) e futuros subprocessadores (e-mail, storage).

### Bases legais prováveis

- Execução de contrato / procedimentos preliminares (cotação e venda).
- Legítimo interesse para logs de segurança (balanceamento documentado).
- Consentimento se marketing futuro — **fora do MVP**.

### Direitos do titular

- MVP: processo **manual** (ticket interno) para acesso/retificação/eliminação.
- Stub admin: `GET /api/v1/admin/lgpd/clientes/{id}/export` e `POST .../solicitar-exclusao` (ver tabela acima) — **sem** alegar conformidade plena; apoio operacional interno.

### RIPD / inventário

- Manter registro de tratamentos no Project store / docs jurídicos JL.
- DPA com Railway e provedores antes de produção com dados reais.

### Minimização

- Coletar apenas campos necessários em `cliente` e `solicitacao`.
- Não armazenar dados de cartão; gateway tokeniza (futuro).

## Threat model MVP (STRIDE resumido)

| Ameaça | Mitigação |
|--------|-----------|
| Token theft | HttpOnly, TTL curto, refresh rotation |
| IDOR | Validar `org_id` + ownership em todo GET/PATCH |
| Injection | JPA parametrizado; validação Bean Validation |
| Brute force login | Rate limit (Bucket4j) + lockout temporário |
| Privilege escalation | Papéis imutáveis sem ADMIN |

## Ambientes

- Segredos apenas via Railway variables / GitHub secrets; nunca no repo.
- `application-prod.yml` sem credenciais; profile `local` com `.env` gitignored.

## Comparação session vs JWT (registro)

Decisão final: **JWT+refresh** (ADR-002). Sessão JDBC seria reconsiderada apenas se API servir SPA na mesma origem sem subdomínio separado.

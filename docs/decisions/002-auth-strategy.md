# ADR-002 — Estratégia de autenticação

**Status:** Aceito com premissas (Etapa 1)  
**Data:** 2026-11-02

## Contexto

Frontend SPA (Vite) e API Spring Boot provavelmente servidos no Railway em origens relacionadas (`*.up.railway.app` ou domínio custom). Usuários internos JL Turismo; LGPD exige controle de acesso e rastreabilidade.

## Decisão

**JWT de acesso curto (15 min) + refresh token opaco rotacionado**, ambos entregues via **cookies HttpOnly, Secure, SameSite=Lax** (ou Strict se same-site garantido).

- Access JWT: claims mínimas (`sub`, `org`, `roles`, `iat`, `exp`); assinatura HS256 ou RS256 (preferir RS256 se múltiplos serviços no futuro).
- Refresh: UUID armazenado em Postgres (`refresh_token` hash, revogação, device opcional); rotação a cada uso.
- Logout: revoga refresh + blacklist opcional de access até expirar.
- CSRF: double-submit cookie ou header `X-CSRF-Token` para mutações se frontend same-site; documentar no `security.md`.

## Por que não sessão server-side clássica apenas?

| Critério | Sessão JDBC/Redis | JWT + refresh (escolhido) |
|----------|-------------------|---------------------------|
| SPA + API em subdomínios Railway | Cookies de sessão ok, mas sticky session | Stateless access escala horizontal |
| Revogação imediata | Forte | Forte via refresh + TTL curto access |
| Complexidade Spring | Spring Session | Spring Security OAuth2 Resource Server + custom refresh |
| Mobile futuro | Possível | JWT padrão de mercado |

**Sessão server-side pura** permanece alternativa válida se VOA ficar **sempre** same-origin (API serve static). Premissa atual: **frontend static separado** → JWT+refresh.

## Premissas (não bloqueantes)

- Sem SSO corporativo no MVP (login e-mail + senha, bcrypt/argon2).
- MFA adiado para pós-MVP.
- Política de senha: NIST simplificado (mín. 12 chars, breach check opcional).

## Revisão

Reavaliar se JL Turismo exigir SSO (Google Workspace / Azure AD) antes da Etapa 3.

# Auditoria de produto — VOA (JL Turismo)

Data: 2026-11-02. Repositório: `85ED/voa`. Escopo: diagnóstico antes da transformação visual e demonstrável.

## Resumo executivo

O VOA possui **backend maduro** (módulos comerciais, financeiros, parceiros, público, auditoria) e **frontend funcional porém mínimo**: rotas existem, mas a experiência parece protótipo (muito espaço vazio, copy técnica exposta, KPIs zerados sem dados de demo). A stack está correta; o gap principal é **produto + design + dados de demonstração**, não reescrita.

## Funcionalidades existentes (comportamento verificado no código)

| Área | Backend | Frontend | Observação |
|------|---------|----------|------------|
| Auth JWT + refresh | Sim | Login, `/me` | OK |
| Landing + promoções públicas | `/api/v1/public/promocoes` | `/` | Vazio sem seed |
| Solicitação pública | POST público | `/solicitar-viagem` | OK |
| Portal parceiro público | `/api/v1/public/parceiros/{slug}` | `/p/:slug` | OK |
| Dashboard KPIs | `/api/v1/dashboard/resumo` | `/dashboard` | Zeros sem operações |
| Solicitações | CRUD API | Nav link **sem página dedicada** | Bug UX |
| Cotações | State machine | `/cotacoes` | UI operacional crua |
| Operações / vendas / pagamentos / bilhetes | Completo | `/operacoes` | UI operacional crua |
| Parceiros admin + portal | Sim | `/parceiros`, `/parceiro` | OK |
| Promoções admin | Sim | `/promocoes` | OK |
| Notificações | API + count | Badge no header | OK |
| LGPD admin stub | Sim | Sem UI | Parcial |
| OpenAPI / Swagger | Springdoc | Não linkado na UI | Doc só dev |
| Flyway | 11 migrations | — | OK |
| Testes backend | Integração + unit | — | CI |
| Testes frontend | 1 smoke | Vitest frágil (Node/ESM) | Parcial |

## Parcialmente implementadas

- **Solicitações (lista):** API usada em Cotações/Parceiros, mas rota `/solicitacoes` renderiza o dashboard (roteamento interno incompleto).
- **Design system:** tokens CSS em `index.css`, sem biblioteca de componentes compartilhada até esta revisão.
- **Seed:** apenas admin + organização; sem cenário comercial/financeiro para demo.
- **Loading / empty states:** mensagens genéricas; login/painel com hierarquia fraca.

## Ausentes ou frágeis

- Dados de demonstração locais (promoções, solicitações, vendas) para apresentação.
- Navegação ativa, logout, breadcrumb, layout responsivo consistente.
- Tabelas e formulários padronizados em todas as features.
- Link para Swagger / health na área logada (opcional dev).
- Testes E2E estáveis no Mac do desenvolvedor (Playwright presente, não validado nesta auditoria).

## Problemas de navegação

1. `/solicitacoes` → mostrava painel (corrigido nesta entrega com `SolicitacoesPage`).
2. Múltiplas rotas repetem `<AppShell />` em `App.tsx` (aceitável, mas verboso).
3. Landing sem header global de marca (corrigido com `PublicShell`).

## Inconsistências visuais (screenshots 2026-11-02)

- Login: card pequeno flutuando em tela grande.
- Dashboard: KPIs corretos mas copy expõe path da API; grid sem contexto nem ações.
- Landing: hero estreito, promoções vazias, sensação de wireframe.

## Segurança (revisão conservadora)

- JWT + RBAC por `@PreAuthorize` nos controllers principais.
- Rate limit login/público configurável (`voa.auth`, `voa.public-api`).
- Senha admin seed só em `application-local.yml` (não commitar `.env` real).
- Cookies `secure: false` adequado a localhost.
- **Risco:** perfil local com credenciais fixas documentadas — aceitável para dev, nunca em produção sem rotação.

## Endpoints com cobertura de teste desigual

- Bem cobertos: fluxos de integração (auth, solicitação, cotação, venda, financeiro).
- UI: sem testes de componente além de `App.test.tsx`.
- Dashboard/promoções públicas: sem teste dedicado de seed demo (adicionar se seed crescer).

## Dados de demonstração

- Antes: banco `voa` com admin e zeros no painel.
- Depois (profile `local`, `voa.demo-seed: true`): promoções públicas + solicitações + vendas manuais mínimas para KPIs não zerados.
- Reinicie a API após limpar o banco ou na primeira subida com banco vazio de cliente demo.
- Para repetir o seed: remova registros com cliente `demo.cliente@jlturismo.local` ou recrie o banco local (`load-local-db.sh`).

## Plano priorizado

| P | Item | Entrega |
|---|------|---------|
| P0 | Design tokens + componentes base | `index.css`, `shared/ui/*` |
| P0 | Shell público + login + dashboard | Layout profissional |
| P0 | Página Solicitações | Lista real |
| P0 | Demo seed local | `DemoDataSeedRunner` |
| P1 | Padronizar Cotações/Operações (tabelas, PageHeader) | Iteração seguinte |
| P1 | Logout + nav ativa + footer mínimo | Iteração seguinte |
| P2 | Vitest/Node 22 alinhado ao CI | Ambiente dev |
| P2 | E2E smoke login + dashboard | Playwright |

## Decisões registradas

- Manter React + CSS Modules (sem Tailwind/shadcn) para coerência com o repo.
- Referência conceitual OpenCode: clareza, tipografia, densidade moderada — identidade própria JL Turismo.
- Seed demo **somente** profile `local`, desligável via `voa.demo-seed: false`.
- Não alterar contratos REST públicos nesta fase.

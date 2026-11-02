# VOA — Contexto do projeto

## Visão

**VOA** é a plataforma operacional da **JL Turismo** para gestão interna de solicitações de viagem, cotações, vendas, pagamentos, emissão/registro de bilhetes e trilha de auditoria. Produto **separado** do repositório **Guia de Corrida** (corrida/treino).

## Cliente e operação

| Item | Descrição |
|------|-----------|
| Cliente | JL Turismo (agência de viagens) |
| Usuários | Equipe comercial, operacional, financeiro e administradores |
| Idioma UI | Português (BR) |
| Fuso | America/Sao_Paulo (negócio); UTC no servidor/banco |

## Objetivos de negócio (MVP)

1. Registrar **solicitações** de viagem com histórico e responsáveis.
2. Produzir **cotações** versionadas e convertê-las em **vendas**.
3. Controlar **pagamentos** (entrada, parcelas, status) sem substituir ERP contábil completo no MVP.
4. Associar **bilhetes/documentos** à venda (upload + metadados; emissão GDS fora de escopo).
5. **Auditoria** de ações sensíveis (quem, quando, o quê).
6. Base para regra comercial de **comissão 4%** (ver ADR-003 — cálculo bloqueado até confirmação).

## Stack alvo (spec master)

| Camada | Tecnologia |
|--------|------------|
| Backend | Java 21+, Spring Boot 3.x, modular monolith |
| Banco | PostgreSQL 15+ |
| Migrações | Flyway |
| Frontend | React 18+, TypeScript, Vite |
| Deploy | Railway (API + static frontend + Postgres plugin) |

## Fora de escopo (Etapa 0–1 e MVP inicial)

- Integração **GDS** (Amadeus, Sabre, etc.) e APIs consolidadoras.
- Emissão automática de bilhete aéreo/hotel via fornecedor externo.
- Portal self-service do passageiro final (fase posterior).
- Multi-agência / white-label (premissa: **single-tenant JL Turismo** até decisão contrária).
- BI avançado, data warehouse, ML de pricing.
- Modificações no repositório **GuiaDeCorrida** para hospedar VOA.

## Referência visual (OpenCode)

Inspiração de tipografia e tokens: ver `internal/design/opencode-visual-reference.md`. VOA deve adaptar identidade **JL Turismo** (logo/cores corporativas) sobre padrão técnico similar (mono-first, dark/light, densidade moderada).

## Estado do workspace (Etapa 0)

- Workspace atual: **GuiaDeCorrida** (`/workspace`), branch `main`, working tree **limpo**, sync com `origin/main`.
- **Nenhum** artefato VOA/JL Turismo encontrado no código (busca por `VOA`, `JL Turismo`, `jl-turismo`).
- Documentação de planejamento VOA vive no **Project store** (`docs/`), não no repo Guia.

## Premissas técnicas permitidas para fundação

- Single tenant JL Turismo; um `organizacao_id` fixo ou implícito no MVP.
- Moeda BRL; arredondamento half-up em 2 casas para exibição.
- Papéis RBAC: `ADMIN`, `COMERCIAL`, `OPERACOES`, `FINANCEIRO`, `AUDITORIA_LEITURA` (nomes ajustáveis).
- Solicitação → cotação → venda como fluxo linear com estados explícitos.
- Pagamentos registrados manualmente ou via gateway futuro (stub de integração).

## Perguntas comerciais

Ver seção final de `docs/implementation-plan.md` e ADR-003 para lista **bloqueante** vs **premissa**.

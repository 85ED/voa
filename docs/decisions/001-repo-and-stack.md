# ADR-001 — Repositório e stack

**Status:** Aceito (Etapa 0)  
**Data:** 2026-11-02

## Contexto

O workspace Cloud Agent está montado em **GuiaDeCorrida** (Node/Express, MySQL, Capacitor, Railway existente). A spec master exige **VOA** como produto **Spring Boot + PostgreSQL + React/Vite**. Não há código VOA no repositório atual.

## Decisão

1. **Criar repositório Git dedicado** `VOA` (ou `voa-jl-turismo`), **não** subdiretório dentro de GuiaDeCorrida.
2. Stack conforme spec: modular monolith Java, Postgres, Flyway, frontend Vite separado no mesmo repo (monorepo) ou pacote `frontend/` na raiz.
3. Deploy em **projeto Railway separado** do Guia de Corrida (novos serviços + Postgres; sem alterar `railway.toml` do Guia).
4. GuiaDeCorrida permanece **intocado** salvo link/documentação externa no Project store.

## Alternativas consideradas

| Opção | Prós | Contras |
|-------|------|---------|
| Subdir `/voa` no Guia | Um clone | CI/deploy/hooks conflitam; stacks incompatíveis; risco de commit acidental no produto errado |
| Monorepo polyglot na raiz Guia | Visão única | Mesmos riscos + `package.json` vs Maven na raiz |
| **Repo novo (escolhido)** | Isolamento, permissões, pipeline limpo | Segundo remote a configurar no ambiente |

## Consequências

- Etapa 2: scaffold apenas no repo VOA.
- Cloud Agents devem apontar workspace para o clone VOA antes de implementar backend.
- ADRs e docs de produto ficam no Project store até migrar `docs/` para o repo VOA na fundação.

## Esboço de árvore Etapa 2 (não criar no Guia)

```
voa/
├── backend/
│   ├── pom.xml
│   └── src/main/java/br/jlturismo/voa/
│       ├── VoaApplication.java
│       └── modules/{core,auth,solicitacoes,cotacoes,vendas,pagamentos,bilhetes,audit}/
├── backend/src/main/resources/db/migration/   # Flyway V1__...
├── frontend/                                   # Vite + React + TS
├── docker-compose.yml                          # Postgres local
├── railway.toml                                # backend service
└── README.md
```

# Histórico da sessão — 2026-09-16

Registro do que foi decidido e construído na primeira sessão de trabalho no Sistema de Avaliação de Projetos (SisPAC). Não é uma transcrição literal da conversa — é um resumo organizado por tema, para reconstruir o contexto rapidamente depois.

## 1. Regras de negócio e modelagem

Ponto de partida: documento de regras de negócio do usuário (perfis, edições, atribuição automática, avaliação às cegas, consolidação, auditoria). A partir dele:

- Criado [modelagem-dados.md](modelagem-dados.md): 19 entidades, diagrama ER, mapeamento regra → schema.
- Criado [plano-implementacao.md](plano-implementacao.md): stack, fases de entrega, segurança.
- Decisões tomadas durante a conversa (não estavam no documento original):
  - Projetos chegam por **importação de JSON do Sigaa**; avaliadores chegam por **importação de arquivo** — nada de cadastro manual de projeto por projeto.
  - Conflito de interesse simplificado: só **orientador** e **coautor** do projeto (checagem de "mesma instituição" foi descartada).
  - Só uma edição `ATIVA` por vez, globalmente — não por avaliador.
  - **Mínimo de 3 avaliadores por projeto** (não 2 como no rascunho inicial).
  - **Nota do projeto = média das notas finais** das avaliações enviadas — conceito distinto da nota final de cada parecer individual, calculado em serviço, nunca persistido.
  - Um projeto tem **exatamente um autor**, pode ter **vários coautores** e **vários orientadores**; uma pessoa pode orientar um projeto e avaliar outro (papéis por projeto, não globais).
  - `Pessoa` ficou separada de `Usuario` de propósito: permite checar conflito de interesse e casar registros de importações diferentes mesmo para quem nunca logou.

## 2. Diagramas

Criados em `diagrams/` (Mermaid): [diagrama-casos-uso.md](../diagrams/diagrama-casos-uso.md) e [diagrama-classes.md](../diagrams/diagrama-classes.md), espelhando o schema real do backend.

## 3. Backend (Spring Boot)

Scaffold completo em `projeto_backend/`: 19 entidades JPA (pacote `entity`, renomeado de `domain` a pedido), repositórios Spring Data, autenticação JWT (login + ativação de conta de 1º acesso), `SecurityConfig` com autorização por role, schema Flyway (`V1__schema_inicial.sql`).

**Bugs reais encontrados e corrigidos ao testar de ponta a ponta** (não apareceriam só lendo o código):
- `@Lob` em campos `String` fazia o Hibernate 6 esperar coluna `oid`, mas a migration usa `TEXT` — removido de 5 entidades.
- `Usuario.pessoa` como `LAZY` quebrava o login com `LazyInitializationException` (e-mail lido depois que a sessão do Hibernate já tinha fechado, na geração do JWT) — mudado para `EAGER`.
- `GlobalExceptionHandler` só tratava `BadCredentialsException`; conta `PENDENTE_ATIVACAO` caía num 403 vazio do Spring Security em vez de 401 com mensagem — generalizado para `AuthenticationException`.
- Lombok herdado do `spring-boot-starter-parent` não compilava com JDK 25 (`ExceptionInInitializerError`) — versão fixada em `1.18.48` no `pom.xml`.

**Seed** (`V2__seed_admin.sql`): reduzido, a pedido, para conter **só o usuário admin** (`admin@instituicao.edu.br` / `admin123`). Nada de avaliador/projeto/edição fictícios — esses só devem existir por importação de verdade, quando esse fluxo for implementado.

## 4. Frontend (React + Mantine)

Protótipo em `projeto_frontend/`, dados 100% mock (`src/mock/data.ts`), sem integração com a API ainda. Cobre as telas de avaliador (login, ativação, dashboard sem dados de autoria, formulário de avaliação com nota final calculada) e admin (dashboard, edições, critérios, importação, projetos, atribuição automática, relatórios, auditoria).

Ponto de virada: o usuário trouxe um export do **Figma Make** (zip) com um protótipo próprio do mesmo sistema, pedindo comparação. A comparação mostrou pontos fortes que valia incorporar — branding ("SisPAC"), banner explicando a avaliação às cegas, layout com a nota final sempre visível ao lado do formulário, dashboard combinando métricas de projeto e carga de avaliador, atribuição automática como modal em vez de página separada. Reconstruí o frontend mantendo a stack (Mantine/React Router) mas incorporando essas ideias.

Depois, os botões de "Acesso rápido (demonstração)" no login foram removidos a pedido — login passou a exigir e-mail/senha digitados.

## 5. Docker

Motivado pela pergunta "banco foi criado?": descobri que a máquina já tinha **dois PostgreSQL nativos instalados** (v16 na porta 5434, v18 na porta 5432) — desconhecidos até então, provavelmente de outro projeto. Isso causou uma sessão de debugging real (container Docker colidindo com o Postgres nativo na 5432 → resolvido movendo pra porta 5433).

Maven também não estava instalado; `winget`/`choco` falharam por falta de permissão de admin — resolvido baixando o binário e configurando no PATH do usuário manualmente.

Depois, a pedido, criado `docker-compose.yml` (movido da pasta do backend pra **raiz do repo**, já que agora orquestra o sistema inteiro) com três serviços: `db` (Postgres), `backend` (Dockerfile multi-stage Maven+JDK 21) e `frontend` (Node, código montado por bind mount pra hot-reload). Um único `docker compose up -d --build` sobe tudo.

## 6. Git

Repositório inicializado e conectado a [github.com/EmilyRibeiro00/sistema-de-avaliacao-de-projetos-academicos](https://github.com/EmilyRibeiro00/sistema-de-avaliacao-de-projetos-academicos). Dois commits enviados: estrutura inicial (docs, diagramas, backend) e depois o frontend + Docker + ajustes de domínio.

## Estado no fim da sessão

- `docker compose up -d --build` sobe banco + backend + frontend, todos testados e saudáveis.
- Login funcional de ponta a ponta no backend real (admin) e no frontend mock (admin + avaliador simulado).
- Pendente: todos os endpoints além de auth (importação, atribuição, avaliação, consolidação) — ver fases em [plano-implementacao.md](plano-implementacao.md).

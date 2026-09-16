# Sistema de Avaliação de Projetos (SisPAC)

Plataforma para avaliação às cegas de projetos submetidos por discentes (via Sigaa) em edições anuais de uma mostra/edital científico. Avaliadores (professores) recebem projetos atribuídos automaticamente dentro da sua área de especialidade, nunca veem quem são os autores/orientadores, e dão nota por critério configurável. Administradores gerenciam edições, critérios, atribuições e relatórios consolidados.

Regras de negócio completas e o raciocínio por trás delas: [docs/modelagem-dados.md](docs/modelagem-dados.md) e [docs/plano-implementacao.md](docs/plano-implementacao.md).

## Estrutura do repositório

```
projeto_lab_prog/
├── docker-compose.yml     # sobe banco + backend + frontend juntos
├── docs/                  # modelagem de dados, plano de implementação, prompt do Figma AI
├── diagrams/              # diagrama de casos de uso e de classes (Mermaid)
├── projeto_backend/       # API Spring Boot (Java 21) — entidades, auth JWT, schema Flyway
└── projeto_frontend/      # protótipo React + TypeScript + Mantine (dados mock)
```

## Rodando localmente

### Via Docker (recomendado — não precisa de Java/Maven/Node instalados na máquina)

```bash
docker compose up -d --build
```

Sobe os três serviços a partir da raiz do repo:

| Serviço | Container | URL |
|---|---|---|
| Banco (Postgres 16) | `sispac-postgres` | porta host **5433** (evita colidir com Postgres nativo já usando a 5432) |
| Backend (API) | `sispac-backend` | http://localhost:8080 (Swagger em `/docs`) |
| Frontend (protótipo) | `sispac-frontend` | http://localhost:5173 |

O Flyway aplica schema + seed do backend automaticamente assim que o Postgres fica saudável. O frontend roda em modo dev dentro do container, com o código-fonte montado por bind mount — editar arquivos em `projeto_frontend/src` recarrega na hora, sem rebuild de imagem.

```bash
docker compose logs -f backend    # acompanhar o log de um serviço (troque por frontend/db)
docker compose down               # parar tudo (mantém os dados do Postgres)
docker compose down -v            # parar e apagar os dados do Postgres também
```

### Sem Docker

- **Frontend**: `cd projeto_frontend && npm install && npm run dev` — detalhes em [projeto_frontend/README.md](projeto_frontend/README.md).
- **Backend**: precisa de Java 21, Maven e PostgreSQL rodando à parte — detalhes e solução de problemas comuns (porta ocupada por Postgres nativo, incompatibilidade do Lombok com JDK muito novo) em [projeto_backend/README.md](projeto_backend/README.md).

Hoje só `POST /auth/login` e `POST /auth/ativar` estão implementados no backend e testados de ponta a ponta (login, rejeição de credenciais inválidas/conta pendente, ativação de conta) — as demais regras (atribuição, avaliação, importação) ainda não têm endpoint (ver fases pendentes em [docs/plano-implementacao.md](docs/plano-implementacao.md)).

## Usuários de teste

**Backend** (`projeto_backend/src/main/resources/db/migration/V2__seed_admin.sql`) semeia só o essencial pro primeiro acesso — o resto fica em branco de propósito, já que avaliador só existe de verdade por importação de arquivo (ainda não implementada):

| Perfil | Login | Senha |
|---|---|---|| Administrador | `admin@instituicao.edu.br` | `admin123` |


**Frontend** (protótipo, dados mock — não fala com o backend) simula também um avaliador, só pra dar pra navegar as telas desse perfil sem precisar de importação de verdade:

| Perfil | Login | Senha |
|---|---|---|
| Administrador | `admin@instituicao.edu.br` | `admin123` |
| Avaliador | `camila.torres@instituicao.edu.br` | `senha123` |

Seed do backend é só para dev/teste local — não deve ser aplicado em produção.
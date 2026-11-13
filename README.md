# SICAC — Sistema Integrado de Comitês para Avaliação Científica

Plataforma para avaliação às cegas de projetos submetidos por discentes (via Sigaa) em edições anuais de uma mostra/edital científico, do tipo SEMITI/SEMIC da UFMA (evento simultâneo em vários campi). Avaliadores (professores) recebem projetos atribuídos automaticamente dentro do mesmo comitê de avaliação e do mesmo campus de atuação, nunca veem quem são os autores/orientadores, e dão nota por critério configurável. Administradores gerenciam edições, critérios, atribuições e consolidação dos resultados.

Regras de negócio completas e o raciocínio por trás delas: [docs/modelagem-dados.md](docs/modelagem-dados.md) e [docs/plano-implementacao.md](docs/plano-implementacao.md). Para as 20 entidades do backend (o que cada uma guarda e como se relacionam) e pra entender onde mora a role do usuário (`Usuario.tipo` → claim do JWT → `SecurityConfig`), ver [backend/README.md](backend/README.md#entidades).

## Estrutura do repositório

```
projeto_lab_prog/
├── docker-compose.yml     # sobe banco (+ backend/frontend, ver observação abaixo)
├── docs/                  # modelagem de dados, plano de implementação, prompt do Figma AI
├── diagrams/              # diagrama de casos de uso e de classes (Mermaid)
├── backend/               # API Spring Boot (Java 21) — 20 entidades, auth JWT por role, serviços, controllers, testes
└── frontend/              # frontend React + TypeScript, integrado de verdade com a API
```

## Passo a passo para rodar

> **Nota sobre o Docker Compose**: o `backend/` ainda não tem `Dockerfile` (existia em um layout anterior do repositório e não foi recriado após a reorganização). `docker compose up` sobe o banco normalmente, mas o serviço `backend` falha ao buildar até esse Dockerfile voltar a existir. Por isso o caminho testado e garantido hoje é **banco via Docker + backend via Maven + frontend via npm**, abaixo.

### 1. Banco de dados (Docker)

Só o Postgres precisa de Docker:

```bash
docker compose up -d db
```

Sobe `sispac-postgres` na porta **5433** do host (evita colidir com um Postgres nativo já usando a 5432).

### 2. Backend (Java 21 + Maven)

```bash
cd backend
mvn spring-boot:run
```

Espera aparecer `Tomcat started on port 8080` no log. O Flyway aplica o schema e semeia o usuário admin automaticamente — não precisa rodar nada manual no banco. Detalhes dos endpoints, serviços e testes: [backend/README.md](backend/README.md).

### 3. Frontend (Node)

```bash
cd frontend
npm install
npm run dev
```

Abre em `http://localhost:5173`. Por padrão ele chama a API em `http://localhost:8080` (dá pra trocar com a variável `VITE_API_URL`). Detalhes das telas e das limitações herdadas do backend: [frontend/README.md](frontend/README.md).

### Para parar tudo

```bash
# Ctrl+C no terminal do backend e do frontend, depois:
docker compose stop db      # mantém os dados do Postgres
docker compose down -v      # ou isso, se quiser apagar os dados também
```

### Via Docker Compose completo (quando o `backend/Dockerfile` existir)

```bash
docker compose up -d --build
```

| Serviço | Container | URL |
|---|---|---|
| Banco (Postgres 16) | `sispac-postgres` | porta host **5433** |
| Backend (API) | `sispac-backend` | http://localhost:8080 |
| Frontend | `sispac-frontend` | http://localhost:5173 |

O frontend roda em modo dev dentro do container, com o código-fonte montado por bind mount — editar arquivos em `frontend/src` recarrega na hora, sem rebuild de imagem.

## O que já funciona

O backend implementa login (JWT), cadastro de avaliador com senha padrão e troca obrigatória no primeiro acesso, cadastro/listagem/edição/remoção de avaliador e de projeto, vínculo de avaliador a uma edição (comitê + campus), edições, critérios, atribuição automática e manual, avaliação (rascunho/envio/reabertura) e consolidação — com 208 testes automatizados (`cd backend && mvn test`). O ciclo completo (cadastrar avaliador → avaliador loga com a senha padrão e troca → vincular à edição/comitê/campus → cadastrar projeto → sortear → avaliador envia a nota → admin vê a consolidação) foi testado de ponta a ponta contra a API real, não só camada por camada — inclusive com dois avaliadores na mesma edição/comitê/campus, pra validar o sorteio e a consolidação com mais de um parecer.

Ficou de fora deste recorte: importação de verdade de projetos do Sigaa e envio de e-mail de verdade (hoje só loga no console). Por isso, no frontend, as telas de **Atribuições** e **Consolidação** continuam sendo formulários por ID em vez de listas navegáveis (o backend não expõe listagem desses dois recursos), e **Importação** é placeholder — ver [frontend/README.md](frontend/README.md). Nada disso é simulado com mock: é exatamente o que a API de hoje sustenta. Avaliadores e projetos aceitam o nome do comitê/campus/instituição digitado e reaproveitam se já existir (ou já tiver um alias cadastrado), já que não há uma taxonomia pronta pra escolher.

## Usuários de teste

O Flyway roda `backend/src/main/resources/db/migration/V2__seed_admin.sql` uma única vez (migration versionada: se já rodou, não roda de novo), de propósito só com o essencial pro primeiro acesso existir — nada de projeto, edição ou avaliador fictício fingindo que já há dados reais:

```sql
INSERT INTO pessoa (id, nome, cpf, email) VALUES
    (1, 'Coordenação Geral', '00000000000', 'admin@instituicao.edu.br');

-- senha_hash gerado com BCrypt (custo 10) para "admin123"
INSERT INTO usuario (id, pessoa_id, senha_hash, tipo, status_ativacao, created_at, updated_at) VALUES
    (1, 1, '$2b$10$yD.8sUzARYVfU1CHfJ8fveNFPXiPYnBNUMzCkxZeQTmbzcmopPhpa', 'ADMIN', 'ATIVO', now(), now());
```

Cria uma `Pessoa` e um `Usuario` com `tipo = ADMIN` e `status_ativacao = ATIVO` já prontos pra logar, com a senha já em hash BCrypt pronta no SQL. As duas últimas linhas da migration (não mostradas acima) reajustam as sequences de `pessoa`/`usuario` pra não colidir com o próximo `id` gerado.

| Perfil | E-mail | Senha |
|---|---|---|
| Administrador | `admin@instituicao.edu.br` | `admin123` |

Não existe usuário avaliador por padrão — um admin cadastra pela tela **Avaliadores** (ou `POST /api/admin/avaliadores`), que atribui uma senha padrão (`Trocar@123`, configurável em `app.auth.senha-padrao-avaliador`) e devolve ela na resposta do cadastro (não existe envio de e-mail de verdade ainda) pra repassar manualmente. O avaliador já loga direto com essa senha padrão e é forçado a trocá-la (`PUT /api/conta/senha`) antes de usar o resto do sistema. Esse seed é só para dev/teste local, não deve ser aplicado em produção.

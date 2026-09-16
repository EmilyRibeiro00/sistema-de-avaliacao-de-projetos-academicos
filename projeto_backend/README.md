# Backend — Sistema de Avaliação de Projetos

API REST em Java + Spring Boot para o sistema de avaliação às cegas de projetos (edições anuais, avaliadores atribuídos automaticamente por área, sem acesso a dados de autoria). Contexto completo do domínio e das regras de negócio: [../docs/modelagem-dados.md](../docs/modelagem-dados.md) e [../docs/plano-implementacao.md](../docs/plano-implementacao.md).

## Stack

- Java 21 + Spring Boot 3
- Spring Web, Spring Data JPA, Spring Security (JWT), Bean Validation
- PostgreSQL + Flyway (migrations em `src/main/resources/db/migration`)
- Lombok, MapStruct, springdoc-openapi

## Estrutura

```
src/main/java/com/avaliacao/sistema/
├── entity/       # entidades JPA (Pessoa, Usuario, Edicao, Projeto, Atribuicao, Avaliacao, ImportacaoLote, ...)
├── repository/   # Spring Data JPA, um repositório por entidade
├── security/     # JwtService, JwtAuthenticationFilter, CustomUserDetailsService
├── service/      # regras de negócio (hoje só AuthService)
├── web/          # controllers e DTOs
├── config/       # SecurityConfig (CORS, roles por rota, @PreAuthorize)
└── exception/    # GlobalExceptionHandler
```

## Rodando localmente

### Opção 1 — Docker Compose (recomendado)

O `docker-compose.yml` fica na **raiz do repositório** (sobe banco + backend + frontend juntos — ver [../README.md](../README.md)), não aqui dentro. Rode a partir de lá:

```bash
cd ..
docker compose up -d --build
```

Não precisa de Java, Maven nem PostgreSQL instalados — só Docker. Esse `Dockerfile` (deste diretório) faz o build multi-stage do backend (Maven + JDK 21 dentro do container, então incompatibilidades de JDK/Lombok do host não afetam essa via).

Se você quiser só o backend + banco, sem o frontend, comente ou remova o serviço `frontend` do compose, ou rode `docker compose up -d db backend` a partir da raiz.

### Opção 2 — Java/Maven/Postgres locais

Pré-requisitos: Java 21, Maven, PostgreSQL rodando localmente (porta configurada em `application.yml`).

```bash
docker run -d --name sispac-postgres \
  -e POSTGRES_DB=avaliacao -e POSTGRES_USER=avaliacao -e POSTGRES_PASSWORD=avaliacao \
  -p 5433:5432 postgres:16-alpine

mvn spring-boot:run
```

A porta usada é **5433**, não a 5432 padrão — evita colidir com uma instalação nativa de PostgreSQL que já esteja rodando na máquina (é o caso comum em Windows com múltiplas versões instaladas como serviço). Se sua máquina não tem nada na 5432, pode simplificar: `-p 5432:5432` e ajustar a `url` em `application.yml` de volta para `5432`.

**Nota sobre JDK muito recente**: se `mvn compile` falhar com `ExceptionInInitializerError` vindo do Lombok, é incompatibilidade entre a versão do Lombok herdada do `spring-boot-starter-parent` e um JDK muito novo — o `pom.xml` já fixa `lombok.version` para uma versão mais recente que resolve isso; se ainda falhar, suba esse valor para a última disponível no Maven Central. (Esse problema não ocorre na Opção 1, já que o build roda com JDK 21 dentro do container.)

---

O Flyway aplica schema + seed automaticamente na subida, nas duas opções. A API sobe em `http://localhost:8080`, com Swagger em `http://localhost:8080/docs`.

Configuração (URL do banco, segredo do JWT, origem CORS liberada) fica em `src/main/resources/application.yml` — no Docker Compose, essas mesmas propriedades são sobrescritas por variáveis de ambiente no `docker-compose.yml` (`SPRING_DATASOURCE_URL` etc.), sem precisar editar o yml.

## Autenticação

Login por e-mail/CPF + senha em `POST /auth/login`, retorna um JWT (`Authorization: Bearer <token>` nas chamadas seguintes). Usuários importados (avaliadores) nascem sem senha e ativam a conta em `POST /auth/ativar` com o token recebido por e-mail.

## Dados de teste (seed)

`V2__seed_admin.sql` (Flyway) cria só o essencial pro primeiro acesso — um usuário admin. O resto fica de propósito em branco: sem avaliador, sem edição, sem critério, sem projeto. Avaliador só existe por importação de arquivo de verdade (ver [../docs/plano-implementacao.md](../docs/plano-implementacao.md), Fase 4) e projeto só por importação do Sigaa (Fase 3) — não faz sentido fingir esses dados num seed antes de esses fluxos existirem.

| Perfil | Login | Senha |
|---|---|---|
| Admin | `admin@instituicao.edu.br` | `admin123` |

O seed é só para dev local — não é aplicado em produção por convenção (mantenha migrations de dados de exemplo fora de `prod`, se/quando esse perfil for configurado).

## Status atual

Implementado e testado localmente de ponta a ponta (login, rejeição de credenciais inválidas/conta pendente, ativação de conta): entidades JPA, repositórios, schema do banco (Flyway), autenticação JWT com fluxo de ativação de conta, configuração de segurança por role (`ADMIN`/`AVALIADOR`).

Pendente (ver fases em [../docs/plano-implementacao.md](../docs/plano-implementacao.md)): importação de projetos do Sigaa, importação de avaliadores via arquivo, atribuição automática, preenchimento/consolidação de avaliação, auditoria.

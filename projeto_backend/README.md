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

Pré-requisitos: Java 21, Maven, PostgreSQL rodando localmente.

```bash
createdb avaliacao   # nome/usuário/senha usados em application.yml

mvn spring-boot:run
```

O Flyway aplica as migrations automaticamente na subida. A API sobe em `http://localhost:8080`, com Swagger em `http://localhost:8080/docs`.

Configuração (URL do banco, segredo do JWT, origem CORS liberada) fica em `src/main/resources/application.yml`.

## Autenticação

Login por e-mail/CPF + senha em `POST /auth/login`, retorna um JWT (`Authorization: Bearer <token>` nas chamadas seguintes). Usuários importados (avaliadores) nascem sem senha e ativam a conta em `POST /auth/ativar` com o token recebido por e-mail.

## Status atual

Implementado: entidades JPA, repositórios, schema do banco (Flyway), autenticação JWT com fluxo de ativação de conta, configuração de segurança por role (`ADMIN`/`AVALIADOR`).

Pendente (ver fases em [../docs/plano-implementacao.md](../docs/plano-implementacao.md)): importação de projetos do Sigaa, importação de avaliadores via arquivo, atribuição automática, preenchimento/consolidação de avaliação, auditoria.

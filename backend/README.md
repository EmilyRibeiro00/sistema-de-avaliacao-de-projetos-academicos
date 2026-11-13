# Parte 2 — Serviços, controllers e testes

Sobre o mapeamento objeto-relacional e o Flyway, este projeto coloca as regras de negócio (serviços), a API (controllers) e os testes — de serviço, de repository e de controller. As entidades e os repositórios estão em `entity/` e `repository/`; o que é novo está em `service/`, `controller/` e `src/test/`.

## Entidades

20 entidades em `entity/`, organizadas em 5 grupos.

**Pessoas e acesso**

- `Pessoa`: dados de identificação de qualquer humano no sistema — nome, CPF (único), matrícula, SIAPE, e-mail, instituição. Separada de `Usuario` de propósito: autores/orientadores de projeto também são `Pessoa`, mesmo sem nunca logarem (precisa deles pra checar conflito de interesse).
- `Usuario`: a conta que loga. Tem `pessoa` (1:1), `senhaHash`, `tipo` (a role — ver seção abaixo), `statusAtivacao` (`PENDENTE_ATIVACAO`/`ATIVO`/`INATIVO`), `precisaTrocarSenha` (força a troca de senha no próximo acesso — ver `AuthService` na seção "Serviços" abaixo) e, se for avaliador, um `Comite` fixo (vale pra todas as edições em que participar).
- `Instituicao`: nome + sigla. Tabela de apoio, referenciada por `Pessoa` e `Projeto`.
- `Comite`: nome + sigla — o comitê de avaliação (ex.: "Comitê de Tecnologia") que casa avaliador com projeto no sorteio. Cada projeto responde a exatamente um comitê, e cada avaliador pertence a exatamente um comitê (FK direta em `Usuario` e em `Projeto`, não uma tabela de junção).
- `ComiteAlias`: mapeia um texto cru vindo de fora (`textoOrigem`, único) para um `Comite` real. Pensada pra importação do Sigaa, onde o comitê chega como string livre e precisa ser normalizada — já é consultada pelo `CatalogoService` antes de criar um comitê novo.
- `Campus`: nome + sigla — o local físico do evento (ex.: "Campus Bacanga"), separado de `Instituicao` porque o mesmo evento acontece em vários campi da mesma instituição. Diferente do comitê, o campus de um avaliador é por edição (mora em `EdicaoAvaliador`, não em `Usuario`), porque o mesmo avaliador pode atuar num campus numa edição e noutro na seguinte.
- `CampusAlias`: mesmo papel do `ComiteAlias`, pra strings cruas de campus vindas de importação.

**Edição (o ciclo anual)**

- `Edicao`: nome, ano, `status` (`PLANEJADA`/`ATIVA`/`ENCERRADA`), janela de datas, mínimo de avaliadores por projeto, limite de projetos por avaliador, limite de divergência aceitável, escala de nota.
- `Criterio`: nome + peso + ordem, pertence a uma `Edicao`. Compõe a nota final ponderada.
- `EdicaoAvaliador`: junção `Edicao` × `Usuario` (avaliador) — se esse avaliador está ativo *nesta* edição, com limite de carga próprio, o `Campus` em que atua *nesta* edição especificamente, e de onde veio (`importadoDe`). Única por par.

**Projetos e autoria**

- `Projeto`: o que é avaliado. `sigaaId` (único por edição), título, conteúdo técnico, comitê, campus, instituição, `status` (`SUBMETIDO`/`EM_AVALIACAO`/`AVALIADO`), e de qual `ImportacaoLote` veio.
- `ProjetoDiscente`: `Pessoa` × `Projeto` com `papel` (`AUTOR` ou `COAUTOR`). Só um `AUTOR` por projeto (índice único parcial no schema).
- `ProjetoOrientador`: `Pessoa` × `Projeto`, sem papel (é sempre orientador). Autores/coautores/orientadores juntos são a base do conflito de interesse no sorteio.

**Atribuição e avaliação**

- `Atribuicao`: liga um `Usuario` (avaliador) a um `Projeto`. `status` (`ATIVA`/`CANCELADA`), `origem` (`AUTOMATICA`/`MANUAL`), `motivo` (reatribuição manual), quem criou. Só uma `ATIVA` por par (projeto, avaliador).
- `Avaliacao`: 1:1 com `Atribuicao`. `status` (`RASCUNHO`/`ENVIADA`), nota final (calculada pelo servidor, nunca aceita como input), data de envio, e os campos de reabertura (`reabertaEm`/`reabertaPor`/`motivoReabertura`).
- `AvaliacaoNota`: `Avaliacao` × `Criterio` → a nota naquele critério. Única por par — é a partir daqui que `notaFinal` é calculada.

**Importação e auditoria**

- `ImportacaoLote`: um lote de importação (projetos do Sigaa ou avaliadores por arquivo — `tipo`), de qual `Edicao`, `origem` (`WEBHOOK`/`UPLOAD_MANUAL`), status do processamento, contadores de sucesso/erro, quem disparou e com qual credencial.
- `ImportacaoItem`: cada linha dentro de um lote — referência externa, status (`SUCESSO`/`ERRO`/`IGNORADO_DUPLICADO`/`PENDENTE_MAPEAMENTO`), mensagem de erro, e o id da entidade resultante.
- `IntegracaoCredencial`: credencial de uma integração externa (nome + hash da chave — o valor em texto puro nunca é persistido). Nenhum serviço usa ela ainda; é outro ponto reservado pra quando a importação de verdade existir.
- `LogAuditoria`: quem fez o quê — usuário, ação, entidade afetada, id, detalhes, data/hora. Toda ação relevante do admin passa por aqui (via `AuditoriaService`).

## Onde está a role

Não é uma entidade própria — é um campo que atravessa três camadas:

1. **Entidade**: `Usuario.tipo`, do tipo `UsuarioTipo` (`entity/enums/UsuarioTipo.java`) — só `ADMIN` e `AVALIADOR`. Coluna `usuario.tipo` no schema.
2. **Login (JWT)**: `JwtService.gerarToken` grava `tipo` como claim do token. `UsuarioPrincipal.getAuthorities()` traduz isso pra uma authority do Spring Security: `"ROLE_" + usuario.getTipo().name()` — ou seja, `ADMIN` vira `ROLE_ADMIN`, `AVALIADOR` vira `ROLE_AVALIADOR`.
3. **Autorização de rota**: `SecurityConfig.filterChain` decide por prefixo de URL, não método a método:
   ```java
   .requestMatchers("/api/admin/**").hasRole("ADMIN")
   .requestMatchers("/api/importacoes/**").hasRole("ADMIN")
   .requestMatchers("/api/avaliador/**").hasRole("AVALIADOR")
   ```

O frontend não re-decide nada disso — só decodifica o claim `tipo` do JWT (`frontend/src/auth/session.ts`) pra redirecionar quem tenta entrar na área errada (`RequireAuth.tsx`). É só UX; a segurança de verdade é o passo 3, no backend.

`SecurityConfig` também define um `AuthenticationEntryPoint` e um `AccessDeniedHandler` próprios, pra distinguir os dois jeitos de ser recusado: sem token ou com token inválido/expirado dá **401**; autenticado mas com o papel errado pra essa rota dá **403**. Sem isso, o Spring Security usa o `Http403ForbiddenEntryPoint` padrão e os dois casos voltam 403 — o que o cliente (frontend) usa pra decidir se desloga ou só mostra o erro.

## Serviços

- **`EdicaoService`**: cria, ativa e encerra edições. Só uma edição pode estar ativa por vez, e uma edição encerrada não volta a ser ativada.
- **`CriterioService`**: cria e remove critérios de uma edição (peso maior que zero, edição encerrada é somente leitura) e soma os pesos.
- **`AtribuicaoService`**: o sorteio automático de avaliadores. Ele sorteia até o projeto chegar ao mínimo da edição (3 por padrão), só entre avaliadores ativos na edição, no mesmo comitê *e* no mesmo campus do projeto, sem conflito de interesse (quem orienta ou assina o projeto fica de fora) e respeitando o limite de carga. Se o pool não bastar, atribui o que der e devolve `faltantes` em vez de falhar em silêncio. Também faz a reatribuição manual, que passa pelas mesmas regras de comitê/campus e exige motivo (fica registrada).
- **`AvaliacaoService`**: o que o avaliador faz. Ele só vê os projetos atribuídos a ele, em visão cega (título, comitê e conteúdo técnico, nunca autor, orientador ou instituição). Salva rascunho, envia a avaliação (todos os critérios são obrigatórios, a nota precisa estar na escala e dentro do prazo) e o sistema calcula a nota final como média ponderada. Depois de enviada, só o admin reabre, com motivo.
- **`ConsolidacaoService`**: calcula a nota do projeto (média das notas finais enviadas, sem gravar no banco), a divergência entre avaliadores e se o projeto já está avaliado, e atualiza o status do projeto.
- **`AuthService`**: login (JWT) e troca de senha (`trocarSenha` — valida a senha atual, re-hasheia a nova, limpa `precisaTrocarSenha` e devolve um token novo, já que o claim antigo ficaria desatualizado).
- **`UsuarioService`**: cadastro de avaliador pelo admin (cria `Pessoa` + `Usuario` como `PENDENTE_ATIVACAO`, já com a senha padrão — configurável em `app.auth.senha-padrao-avaliador` — e `precisaTrocarSenha = true`; reaproveita a instituição se o nome já existir, cria se não existir). Dispara `EmailService` com a senha — hoje só loga no console (`ConsoleEmailService`), já que não há provedor de e-mail configurado. Também lista, atualiza e remove (logicamente) avaliadores.
- **`ProjetoService`**: cadastro manual de projeto (não é a importação do Sigaa — essa ainda não existe), além de listar, atualizar e remover. Recusa edição encerrada e `sigaaId` duplicado na mesma edição (gera um `sigaaId` próprio se não vier nenhum); reaproveita comitê/campus/instituição pelo nome e a pessoa do autor pelo CPF, do mesmo jeito que `UsuarioService` faz com avaliador/instituição.
- **`EdicaoAvaliadorService`**: vincula um avaliador a uma edição, fixando o comitê do avaliador (`usuario.comite`, vale pra todas as edições) e o campus em que ele atua *nesta* edição especificamente. Os dois são obrigatórios pra ativar — o banco rejeita (`chk_usuario_avaliador_comite`, `chk_edicao_avaliador_ativo_campus`) um avaliador ativo sem os dois, então o serviço valida antes de salvar. Um avaliador só fica ativo em uma edição por vez — vincular a uma nova libera (`INATIVO`) qualquer outra em que ele estivesse ativo, pra o sorteio automático só considerar quem está livre.
- **`CatalogoService`**: ponto único de "buscar ou criar" comitê/campus/instituição a partir de um nome digitado. Primeiro tenta um alias conhecido (`ComiteAlias`/`CampusAlias`); se não achar, resolve/cria pelo nome direto (sem diferenciar maiúscula/minúscula). Usado por `UsuarioService`, `ProjetoService` e `EdicaoAvaliadorService`, pra não duplicar essa lógica (e os registros) em cada um.
- **`AuditoriaService`**: grava o log de cada ação relevante.

## Controllers

Todos usam o usuário logado (JWT); as rotas de admin exigem `ADMIN` e as do avaliador exigem `AVALIADOR`.

| Método e rota | Quem | O que faz |
|---|---|---|
| `POST /auth/login` | público | login |
| `PUT /api/conta/senha` | admin ou avaliador | troca a própria senha (valida a atual, devolve um token novo) |
| `GET/POST /api/admin/avaliadores` | admin | lista e cadastra avaliador (nasce com a senha padrão e `precisaTrocarSenha=true`; devolve a senha na resposta do cadastro) |
| `PUT/DELETE /api/admin/avaliadores/{id}` | admin | atualiza ou remove (logicamente) um avaliador |
| `POST /api/admin/avaliadores/{id}/edicoes` | admin | vincula o avaliador a uma edição, com comitê e campus obrigatórios; libera qualquer outra edição em que ele estivesse ativo |
| `GET/POST /api/admin/projetos` | admin | lista e cadastra projeto manualmente numa edição, com autor (não é a importação do Sigaa) |
| `PUT/DELETE /api/admin/projetos/{id}` | admin | atualiza ou remove um projeto |
| `GET/POST /api/admin/edicoes`, `GET /api/admin/edicoes/{id}` | admin | lista, cria e busca edições |
| `PUT/DELETE /api/admin/edicoes/{id}` | admin | atualiza ou remove uma edição |
| `POST /api/admin/edicoes/{id}/ativar` e `/encerrar` | admin | muda o status da edição |
| `GET/POST /api/admin/edicoes/{id}/criterios`, `PUT/DELETE /api/admin/criterios/{id}` | admin | critérios da edição |
| `POST /api/admin/atribuicoes/projetos/{id}` e `/edicao-ativa` | admin | sorteio automático de um projeto ou de toda a edição ativa |
| `POST /api/admin/atribuicoes/{id}/reatribuir` | admin | troca um avaliador, com motivo |
| `GET /api/admin/projetos/{id}/consolidacao` | admin | nota do projeto, divergência e comparação entre avaliadores |
| `POST /api/admin/avaliacoes/{id}/reabrir` | admin | reabre uma avaliação enviada, com motivo |
| `GET /api/avaliador/projetos` | avaliador | projetos atribuídos, em visão cega |
| `GET /api/avaliador/atribuicoes/{id}` | avaliador | formulário da avaliação e notas já salvas |
| `PUT /api/avaliador/atribuicoes/{id}/rascunho` | avaliador | salva notas parciais |
| `POST /api/avaliador/atribuicoes/{id}/enviar` | avaliador | envio final |

Ficaram de fora desta parte a importação de verdade de projetos do Sigaa (`POST /api/admin/projetos` é cadastro manual, não importação), o envio de e-mail de verdade (`EmailService` só loga no console) e a listagem de comitê/campus/instituição prontos pra escolher (o cadastro de avaliador, o de projeto e o vínculo a uma edição não reaproveitam uma taxonomia pronta, só o nome que o admin digitar, que é reaproveitado por nome se já existir). `Atribuições` e `Consolidação` também não têm endpoint de listagem — por isso continuam como formulário por ID no frontend.

## Testes

```bash
mvn test
```

São 208 testes, em três camadas:

- **Serviço** (150, Mockito puro — sem banco, sem Docker): `AtribuicaoServiceTest` (24), `AvaliacaoServiceTest` (23), `ConsolidacaoServiceTest` (17), `EdicaoServiceTest` (18), `ProjetoServiceTest` (14), `UsuarioServiceTest` (14), `CriterioServiceTest` (12), `CatalogoServiceTest` (12), `EdicaoAvaliadorServiceTest` (9), `AuthServiceTest` (5), `AuditoriaServiceTest` (2). Cobrem conflito de interesse, limite de carga, pool insuficiente, elegibilidade por comitê *e* campus (automática e na reatribuição manual), acesso a avaliação de outro avaliador, prazo, nota fora da escala, média ponderada, nota do projeto, divergência, e-mail/CPF duplicado no cadastro de avaliador, `sigaaId` duplicado/gerado no cadastro de projeto, que vincular a uma edição nova libera a anterior, troca de senha (senha atual incorreta, usuário inexistente, token novo devolvido), e a resolução de comitê/campus/instituição (alias, reaproveitamento, criação) isolada no `CatalogoService`.
- **Repository** (8, `UsuarioRepositoryTest` e `MapeamentoObjetoRelacionalTest`): rodam contra H2 em memória (perfil `test`, sem precisar de Postgres nem Docker) usando uma cópia adaptada das migrations em `src/test/resources/db/migration-h2` — o schema Postgres usa `TIMESTAMPTZ` e dois índices únicos parciais (`WHERE`) que o H2 não entende, então essa cópia troca `TIMESTAMPTZ` por `TIMESTAMP` e tira os dois índices parciais (essas duas regras continuam garantidas no Postgres real; só não são reforçadas nesse schema de teste). Cobrem CPF único, `sigaa_id` único por edição e os defaults do schema.
- **Controller** (50, `@WebMvcTest` + MockMvc, um arquivo por controller): sobem só a camada web, com os serviços mockados. Como os controllers usam `@AuthenticationPrincipal UsuarioPrincipal`, a autenticação é injetada direto via `SecurityMockMvcRequestPostProcessors.authentication(...)` (ver `ControllerTestSupport`) — não passa pelo `JwtAuthenticationFilter` de verdade. As regras de autorização por rota são replicadas à parte em `WebSecurityTestConfig` (mesmas regras do `SecurityConfig` de produção, incluindo a distinção 401/403, só sem o filtro de JWT). Cobrem status HTTP, validação de `@Valid`, e que cada rota exige o papel certo (401 sem autenticação, 403 com papel errado).

Quebrar de propósito uma regra (o filtro de conflito de interesse, o `@Valid` de um controller) faz o teste correspondente falhar — são testes que pegam regressão de verdade, não só cobertura de linha.

## Como rodar a API

Precisa de um PostgreSQL acessível com os dados de `application.yml` (por padrão `localhost:5433/avaliacao`, com usuário e senha `avaliacao`). O Flyway aplica as migrations sozinho e cria só o usuário admin (`admin@instituicao.edu.br` / `admin123`).

```bash
mvn spring-boot:run
```

O banco começa só com o admin. O ciclo completo já dá pra fazer só com a API, sem inserir nada direto no Postgres:

```bash
# 1. cadastra avaliador (devolve a senha padrão na resposta) e ele loga direto com ela
POST /api/admin/avaliadores          {"nome","email","cpf"}
POST /auth/login                     {"identificador","senha"}   # senha padrão; token vem com precisaTrocarSenha=true
PUT /api/conta/senha                 {"senhaAtual","novaSenha"}  # troca obrigatória antes de usar o resto da API

# 2. cria a edição, ativa, adiciona critério
POST /api/admin/edicoes
POST /api/admin/edicoes/{id}/ativar
POST /api/admin/edicoes/{id}/criterios

# 3. vincula o avaliador à edição ativa, com comitê e campus
POST /api/admin/avaliadores/{avaliadorId}/edicoes   {"edicaoId","comiteNome","campusNome"}

# 4. cadastra o projeto na mesma edição, comitê e campus
POST /api/admin/projetos              {"edicaoId","titulo","conteudoTecnico","comiteNome","campusNome","autorNome"}

# 5. sorteia — agora encontra o avaliador vinculado
POST /api/admin/atribuicoes/projetos/{projetoId}

# 6. o avaliador loga, vê o projeto, envia a avaliação; o admin consulta a consolidação
POST /auth/login (como avaliador, já com a senha nova) → GET /api/avaliador/projetos → POST .../enviar
GET /api/admin/projetos/{projetoId}/consolidacao
```

Só a importação de verdade do Sigaa/arquivo é que continua sem endpoint.

# Plano de Implementação — SICAC

Referência de schema: [modelagem-dados.md](modelagem-dados.md).

## Stack

Backend:
- Java 21 + Spring Boot 3
- Spring Web, Spring Data JPA, Spring Security (JWT), Bean Validation
- PostgreSQL + Flyway (migrations versionadas)
- Lombok, MapStruct (DTO ↔ entidade)
- springdoc-openapi (documentação da API)
- JUnit 5 + Testcontainers para testes de integração

Frontend (projeto separado, consumindo a API REST):
- React + TypeScript (Vite)
- Mantine ou shadcn/ui para componentes (tabela, formulário)
- TanStack Query para chamadas à API (cache, loading/error state)
- React Router, com rotas separadas por perfil (`/admin/*`, `/avaliador/*`)

## Segurança

Autenticação (quem é o usuário) e autorização (o que ele pode fazer) são tratadas como duas camadas separadas — a maioria das regras de negócio deste sistema (blind review, "avaliador só vê o que é seu", ações exclusivas do admin) são, na prática, regras de autorização, não só UI escondendo botão.

**Autenticação**
- JWT assinado (ex: HS256/RS256), emitido em `/auth/login`, expiração curta (ex: 30-60 min) + refresh token
- `IntegracaoCredencial` (API key) para chamadas máquina-a-máquina do Sigaa — chave nunca em texto puro, armazenada como hash, validada por filtro próprio no Spring Security (não é o mesmo fluxo do JWT de usuário)
- CORS restrito à origem do frontend (não `*`), já que back e front são deploys separados
- HTTPS obrigatório em todos os ambientes exceto dev local

**Autorização — regra geral**
- `@PreAuthorize` por role (`ADMIN`/`AVALIADOR`) em todo controller como primeira barreira
- Isso não basta sozinho: para o avaliador, toda consulta/gravação de `Atribuicao`/`Avaliacao` também precisa filtrar por `avaliador_id = usuário autenticado` na query (nunca confiar em um `id` vindo do path/body sem checar posse) — evita IDOR (ex: avaliador trocar o id na URL e ver/editar avaliação de outro projeto)
- Endpoint do avaliador nunca deve aceitar `nota_final` como input, mesmo que o front não envie — validar/ignorar no servidor (regra 6.3 é também uma regra de segurança, não só de produto)

**Tabela de autorização por área**
| Recurso | ADMIN | AVALIADOR |
|---|---|---|
| CRUD de `Usuario`, `Edicao`, `Criterio` | total | sem acesso |
| Importação (`/api/importacoes/*`) | total | sem acesso |
| Atribuição automática/manual | total | sem acesso |
| Listar/ver projetos | todos, com dados de autoria | só os atribuídos a ele, sem dados de autoria |
| `Avaliacao` (criar/editar rascunho, enviar) | leitura (relatórios) | só a própria, e só dentro do prazo da edição |
| Reabrir `Avaliacao` enviada | sim, com motivo obrigatório | não |
| Comparação de notas entre avaliadores de um projeto | sim | não |
| `LogAuditoria` | leitura total | sem acesso |

- Toda ação da coluna ADMIN acima passa por `AuditoriaService` (Fase 9) — autorização negada também vale logar quando for uma tentativa fora do padrão (ex: `AVALIADOR` tentando acessar projeto não atribuído), útil para detectar abuso
- Testes de autorização (Fase 10) cobrem não só "role errada é bloqueada", mas "role certa + recurso de outro usuário é bloqueado" (o caso de IDOR)

## Fases

### Fase 0 — Setup do projeto
- Estrutura Maven multi-módulo ou single-module por camada (`domain`, `repository`, `service`, `web`)
- Configuração de banco, Flyway baseline, perfis (`dev`, `test`, `prod`)

### Fase 1 — Identidade e autenticação
- Entidades: `Pessoa`, `Instituicao`, `Usuario`, `AreaConhecimento`, `AvaliadorArea`
- Spring Security: login por e-mail/CPF + senha, JWT, roles `ADMIN`/`AVALIADOR`
- Senha com BCrypt (`senha_hash`), nunca texto puro nem em log/`LogAuditoria`
- Fluxo de 1º acesso: `Usuario` criado sem senha (`status_ativacao=PENDENTE_ATIVACAO`) recebe `token_ativacao`; endpoint público `POST /auth/ativar` valida o token (único uso, com expiração) e grava `senha_hash`
- Rate limiting em `/auth/login` e `/auth/ativar` (ex: bucket4j) para conter força bruta
- CRUD de usuários restrito a `ADMIN`, para os casos que não vêm de importação (ex: criar outro admin)

### Fase 2 — Edições e critérios
- CRUD de `Edicao` (admin); regra de serviço: só uma `ATIVA` por vez
- Edição `ENCERRADA` vira somente leitura (bloqueio de escrita em todos os endpoints de avaliação/atribuição/importação dessa edição)
- CRUD de `Criterio` por edição (pesos configuráveis)

### Fase 3 — Ingestão de projetos via Sigaa
Endpoint genérico o bastante para servir tanto um futuro webhook do Sigaa quanto upload manual (`POST /api/importacoes/projetos`), aceitando o JSON no corpo e um parâmetro de edição de destino.

- Autenticação: sessão de `ADMIN` (upload manual) **ou** `IntegracaoCredencial` via API key (chamada automatizada) — mesmo endpoint, dois jeitos de autenticar
- Parser mapeia o JSON do Sigaa para `Projeto` + `Pessoa` (discente/orientador) + `ProjetoDiscente`/`ProjetoOrientador`
- Resolução de área: casar texto do Sigaa contra `AreaConhecimentoAlias`; se não existir, marcar o item como `PENDENTE_MAPEAMENTO` e seguir com o resto do lote
- Upsert por `Projeto.sigaa_id` (reenvio do mesmo JSON não duplica)
- Cada execução grava um `ImportacaoLote` com um `ImportacaoItem` por projeto (sucesso/erro/pendente), para o admin auditar o que entrou
- Tela/endpoint admin: listar aliases de área pendentes de mapeamento e reprocessar os itens presos depois de mapeados

### Fase 4 — Importação de avaliadores via arquivo
Endpoint admin de upload (`POST /api/importacoes/avaliadores`, multipart) — formato de arquivo (CSV/XLSX) e colunas esperadas a definir com base no que a coordenação realmente consegue exportar.

- Parser mapeia cada linha para `Pessoa` (dedup por CPF/SIAPE) + `Usuario` (tipo=AVALIADOR, `PENDENTE_ATIVACAO`, token gerado) + `EdicaoAvaliador` (vincula à edição de destino)
- Se a pessoa já existe (avaliador de edição anterior), reaproveita `Pessoa`/`Usuario` e só cria o novo `EdicaoAvaliador`; não gera novo token se já está `ATIVO`
- Área de especialidade: se o arquivo trouxer, grava em `AvaliadorArea`; se não trouxer, fica pendente de complementação manual pelo admin antes da atribuição rodar
- Disparo de e-mail de ativação (fila/serviço assíncrono, fora do escopo do banco) para quem entrou como `PENDENTE_ATIVACAO`
- Mesmo padrão de `ImportacaoLote`/`ImportacaoItem` da Fase 3, para rastreio de falhas linha a linha

### Fase 5 — Atribuição automática
Serviço `AtribuicaoService`, pipeline:
1. Pool = `EdicaoAvaliador` com `status=ATIVO` na edição ativa, filtrado por `AvaliadorArea` compatível com a área do projeto
2. Excluir avaliadores com conflito de interesse: `Pessoa.id` do avaliador é orientador (`ProjetoOrientador`) ou coautor (`ProjetoDiscente`) do projeto
3. Aplicar limite de carga (`EdicaoAvaliador.limite_max_projetos` ou o default da `Edicao`) — remover do pool quem já atingiu o teto
4. Sortear aleatoriamente N avaliadores do pool restante (N = `min_avaliadores_por_projeto`, default 3)
5. Pool insuficiente: não falha silenciosamente — sinaliza ao admin (lista de "projetos com atribuição incompleta")
- Endpoint admin para disparar atribuição em lote (por edição) ou por projeto
- Reatribuição manual: cancela `Atribuicao` antiga, cria nova com `origem=MANUAL`, `motivo` obrigatório, sempre logado

### Fase 6 — Avaliação cega
- `ProjetoAvaliadorView` (DTO): título, área, conteúdo técnico — sem join com `Instituicao`/`ProjetoDiscente`/`ProjetoOrientador`
- Garantida por design do serviço/repositório usado pelo endpoint do avaliador, não por checagem condicional espalhada

### Fase 7 — Preenchimento e envio de avaliação
- Endpoint avaliador: listar projetos atribuídos, abrir formulário
- Bloqueio por prazo: fora de `Edicao.data_abertura`–`data_fechamento`, formulário fica somente leitura
- Validação: todos os `Criterio` da edição obrigatórios, nota dentro da escala
- `RASCUNHO` (parcial) vs `ENVIADA` (definitivo) em endpoints separados
- `nota_final` recalculada no servidor a cada gravação
- Após `ENVIADA`, imutável para o avaliador; reabertura é exclusiva do admin, com motivo obrigatório e log

### Fase 8 — Consolidação e relatórios
- Status "avaliado": todas `Atribuicao` ATIVAS com `Avaliacao.status = ENVIADA`
- Nota do projeto: média aritmética simples das `Avaliacao.nota_final` enviadas (mínimo 3, por `min_avaliadores_por_projeto`) — calculada em serviço, não persistida
- Divergência: diferença entre notas finais dos avaliadores de um projeto > `limite_divergencia_notas` → sinalizada ao admin
- Endpoint admin: comparação de notas entre avaliadores de um mesmo projeto (nunca exposto ao avaliador)
- Relatórios comparativos entre edições — só admin

### Fase 9 — Auditoria
- `LogAuditoria` gravado via serviço central (`AuditoriaService`), chamado explicitamente nos pontos críticos: importação (lote concluído, item com erro), atribuição (auto/manual), envio de avaliação, reabertura, ativação de usuário

### Fase 10 — Testes
Focar nas regras com maior risco de bug silencioso:
- Importação do Sigaa: upsert idempotente por `sigaa_id`, item com área não mapeada não derruba o lote
- Importação de avaliadores: dedup por CPF/SIAPE não duplica `Pessoa` entre edições
- Sorteio de atribuição respeita filtro de área + conflito de interesse + limite de carga + escopo de edição (`EdicaoAvaliador`) — inclusive o caso de uma pessoa ser orientadora de um projeto e avaliadora de outro
- Pool insuficiente (< `min_avaliadores_por_projeto`, default 3) sinaliza e não falha silenciosamente
- Cálculo de nota final (por avaliação) bate com os pesos da edição
- Nota do projeto = média das `Avaliacao.nota_final` enviadas, recalculada dinamicamente conforme avaliações chegam ou são reabertas
- Um projeto nunca tem mais de um `AUTOR` (constraint de banco) nem aceita ser salvo sem nenhum (validação de serviço)
- Bloqueio de edição por prazo
- Blind review: resposta ao avaliador nunca contém nome de discente/orientador/instituição, mesmo em payloads aninhados
- Edição encerrada é somente leitura em todos os endpoints relevantes
- Autorização/IDOR: avaliador autenticado não consegue ler nem gravar `Avaliacao`/`Atribuicao` de outro avaliador trocando o id no path/body; `nota_final` enviada no payload é ignorada pelo servidor

## Ordem sugerida de entrega
Fases 0–2 são a base de configuração. Fases 3–4 (importação) são pré-requisito de tudo depois — vale validar cedo com uma amostra real do JSON do Sigaa e do arquivo de professores, porque o formato exato deles vai forçar ajustes no parser e possivelmente nos campos de `Pessoa`/`Projeto`. Fases 5–7 são o núcleo do domínio (atribuição, blind review, avaliação) e concentram o maior risco — testes junto de cada fase, não deixar para o fim. Fases 8–9 fecham consolidação e rastreabilidade.

## Perguntas em aberto (bloqueiam detalhamento fino, não bloqueiam a Fase 0–2)
- Formato e schema exato do JSON do Sigaa (campos disponíveis para discente/orientador/área/instituição) — sem isso o parser da Fase 3 é especulativo
- Formato do arquivo de avaliadores (CSV/XLSX) e quais colunas ele realmente traz (CPF? SIAPE? área de especialidade já vem, ou é atribuída depois manualmente?)
- Se o Sigaa vai futuramente chamar um webhook nosso (precisa de `IntegracaoCredencial`/API key) ou se será sempre upload manual — o endpoint da Fase 3 já foi desenhado para aceitar os dois, mas a política de autenticação de fato só trava quando isso for decidido

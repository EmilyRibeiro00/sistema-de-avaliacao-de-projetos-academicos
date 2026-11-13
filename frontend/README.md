# Frontend — SICAC

Interface em React + TypeScript para o sistema de avaliação às cegas de projetos, integrada de verdade com a API do `backend` (login JWT, edições, critérios, cadastro de avaliador e projeto, atribuição, consolidação e o fluxo do avaliador). Não há mais dados mock: cada tela chama o endpoint real correspondente.

## Stack

- React 19 + TypeScript + Vite
- React Router (rotas protegidas por papel: `/admin/*` exige `ADMIN`, `/avaliador/*` exige `AVALIADOR`)
- TanStack Query (cache e mutações das chamadas à API)
- CSS próprio em `src/index.css`, em cima das classes do DSGov (`@govbr-ds/core`, padrão gov.br) — visual com fundo cinza claro, cards brancos e tipografia Raleway (fallback declarado pelo próprio DSGov, já que a fonte oficial Rawline não tem CDN público)

## Estrutura

```
src/
├── branding.ts        # nome do produto (SICAC)
├── types/api.ts        # tipos TS espelhando os DTOs do backend
├── api/                # um módulo por recurso (auth, edicoes, criterios, avaliadores, projetos, atribuicoes, consolidacao, avaliador)
├── auth/                # sessão (token JWT decodificado), AuthContext, RequireAuth (guarda de rota por papel)
├── components/
│   ├── ui/              # Button, TextField/SelectField/TextArea, Modal, Badge, Callout, PageHeader, Title, Icon
│   └── Brand.tsx
├── layouts/AppLayout.tsx  # sidebar única, com os itens de navegação do papel logado
└── pages/
    ├── auth/            # Login, Trocar senha
    ├── admin/            # Edições, Critérios, Avaliadores, Projetos, Importação*, Atribuições, Consolidação
    └── avaliador/        # Meus projetos, Avaliação (rascunho/envio)
```

\* **Importação** é tela-placeholder: a navegação e o formulário existem, mas não chamam nenhuma API — é o caminho reservado para quando o backend expuser a importação de projetos do Sigaa (ver "Telas reservadas" abaixo). As demais já chamam a API de verdade.

## Rodando localmente

```bash
npm install
npm run dev
```

Abre em `http://localhost:5173`. Por padrão a API é esperada em `http://localhost:8080` (configurável via `VITE_API_URL`). É preciso o `backend` rodando com um Postgres acessível — ver o README da raiz do repositório.

**Via Docker**: o `docker-compose.yml` da raiz sobe banco, backend e frontend juntos (`cd .. && docker compose up -d --build`). O container do frontend roda `npm run dev` com o código-fonte montado por bind mount, então editar arquivos em `src/` recarrega na hora, sem rebuild de imagem.

## Login e troca de senha

Não há mais credenciais fixas no front — o login é `POST /auth/login` de verdade contra o backend. O único usuário que existe por padrão é o admin semeado pelo Flyway:

| Perfil | E-mail | Senha |
|---|---|---|
| Administrador | `admin@instituicao.edu.br` | `admin123` |

Novos avaliadores se cadastram em **Avaliadores** (`/admin/avaliadores`, botão verde "Cadastrar avaliador" no topo da tela) — isso já cria o usuário com uma senha padrão (`Trocar@123`) e devolve ela na própria tela, já que não existe envio de e-mail de verdade ainda (ver seção abaixo). O avaliador loga direto com essa senha; o JWT carrega o claim `precisaTrocarSenha`, e `RequireAuth`/`RootRedirect` forçam o redirecionamento para `/trocar-senha` (`TrocarSenhaPage`) antes de deixar acessar qualquer outra tela. Ao trocar a senha, o backend devolve um token novo (sem o claim), e o front substitui a sessão em memória com ele. Essa mesma tela fica disponível a qualquer momento depois — não só na troca forçada — por um link fixo ("Trocar senha") na barra lateral (`AppLayout`).

## Vincular avaliador a uma edição

Mesma tela **Avaliadores**, seção "Vincular a uma edição": `POST /api/admin/avaliadores/{id}/edicoes`, com ID do avaliador, edição, comitê e campus (os dois obrigatórios). Um avaliador só fica ativo numa edição por vez — vincular a uma nova libera a anterior automaticamente. **Sem esse vínculo, o sorteio automático nunca encontra ninguém**, mesmo com avaliador e projeto cadastrados: `AtribuicaoService` só considera quem tem esse vínculo ativo na edição, no mesmo comitê e no mesmo campus do projeto.

## Limitações herdadas do backend

**Avaliadores** e **Projetos** já são listas navegáveis (o backend expõe `GET /api/admin/avaliadores` e `GET /api/admin/projetos`). O que o backend não tem é uma taxonomia de comitê/campus/instituição pronta pra escolher — as duas telas só aceitam digitar o nome, que o backend reaproveita se já existir (inclusive via alias). Também não há listagem para **Atribuições** nem **Consolidação**. Por isso:

- **Atribuições** e **Consolidação** são formulários por ID (projeto, atribuição, avaliação, avaliador), não listas navegáveis — o admin precisa saber o ID de antemão. A tela **Projetos** (abaixo) pelo menos devolve o ID do projeto recém-criado pra usar nessas duas.
- A tela de **Consolidação** mostra a nota final por avaliador, mas não o detalhe por critério (o endpoint de consolidação não devolve isso), e reabrir uma avaliação pede o ID da avaliação, que essa tela não lista.

Nada disso é simulado com mock: é exatamente o que a API de hoje sustenta.

## Projetos

`/admin/projetos` lista os projetos cadastrados e permite cadastrar um novo manualmente numa edição — título, conteúdo técnico, comitê, campus, instituição e autor (reaproveitados pelo nome/CPF se já existirem, como em **Avaliadores**). Não é a importação do Sigaa (ver **Importação** abaixo); é só o jeito de ter um projeto de teste pra sortear e consolidar sem inserir nada no banco. O ID devolvido na resposta é o que você usa depois em **Atribuições** e **Consolidação**.

## Não existe envio de e-mail de verdade

O cadastro de avaliador gera uma senha padrão, mas o aviso ao avaliador fica só no console do backend (`ConsoleEmailService` — ver `backend/README.md`), não é enviado de verdade. Por isso a tela de **Avaliadores** mostra a senha padrão na própria resposta do cadastro, pra você copiar e repassar ao avaliador manualmente até existir um provedor de e-mail configurado.

## Telas reservadas (sem endpoint ainda)

- **Importação** (`/admin/importacao`): formulário pra importar projetos de uma fonte externa (Sigaa), com edição de destino (esse campo já é real, puxa de `GET /api/admin/edicoes`), URL e token. O modelo já tem `ImportacaoLote`/`ImportacaoItem`, mas nenhum controller chama uma API externa ainda — o `onSubmit` dessa página é o único ponto a trocar quando esse endpoint existir, por uma chamada em `src/api/`, no mesmo padrão dos outros recursos.

## Sessão: 401 derruba login, 403 só mostra o erro

`src/api/client.ts` só limpa a sessão local e desloga quando o backend responde **401** (token ausente/inválido/expirado). Um **403** (autenticado, mas sem permissão pra essa ação específica) é tratado como erro de negócio normal — aparece no `Callout` da tela, sem deslogar. Essa distinção existe porque o backend (`SecurityConfig`) agora devolve 401 e 403 para situações diferentes; antes, os dois casos voltavam 403 e o front não tinha como diferenciá-los.

## Status atual

208 testes automatizados cobrem o backend (ver `backend/README.md`); este frontend ainda não tem testes próprios. Build de produção (`npm run build`) e `npx tsc --noEmit` passam limpos.

O ciclo completo foi validado de ponta a ponta contra a API real, não só por camada isolada: cadastrar avaliador → avaliador loga com a senha padrão, é forçado a `/trocar-senha` e troca → vincular avaliador a uma edição (comitê + campus) → cadastrar projeto na mesma edição/comitê/campus → sortear avaliadores (achou o avaliador vinculado) → logar como avaliador (já com a senha nova, sem forçar troca de novo) → ver o projeto em "Meus projetos" → enviar a avaliação → admin ver a nota na consolidação. Repetido com um segundo avaliador na mesma edição/comitê/campus, pra validar o sorteio e a consolidação com mais de um parecer. Cada um desses passos corresponde a uma tela real deste frontend.

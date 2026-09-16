# Frontend — Sistema de Avaliação de Projetos

Protótipo de interface em React + TypeScript para o sistema de avaliação às cegas de projetos. Roda inteiramente sobre **dados mock** (`src/mock/data.ts`) — não faz nenhuma chamada ao backend. Serve para validar fluxos e layout antes de integrar com a API de verdade. Contexto do domínio: [../docs/modelagem-dados.md](../docs/modelagem-dados.md) e [../docs/plano-implementacao.md](../docs/plano-implementacao.md).

## Stack

- React 19 + TypeScript + Vite
- [Mantine](https://mantine.dev/) (componentes, tema)
- React Router (rotas separadas por perfil: `/avaliador/*`, `/admin/*`)
- TanStack Query (instalado, ainda sem uso real — entra quando a API for integrada)

## Estrutura

```
src/
├── branding.ts     # nome do produto (SisPAC)
├── types/          # tipos TS espelhando as entidades do backend
├── mock/data.ts     # todos os dados fake usados nas telas
├── layouts/         # AvaliadorLayout (sidebar enxuto) e AdminLayout (sidebar completo, agrupado)
├── components/       # StatusBadge e outros componentes compartilhados
└── pages/
    ├── auth/         # Login, Ativar conta
    ├── avaliador/     # Dashboard, formulário de avaliação
    └── admin/         # Dashboard, edições, critérios, importação, projetos, relatórios, auditoria...
```

## Rodando localmente

```bash
npm install
npm run dev
```

Abre em `http://localhost:5173`.

**Via Docker**: o `docker-compose.yml` da raiz do repositório também sobe este serviço (`cd .. && docker compose up -d --build`) — o container roda `npm run dev` com o código-fonte montado por bind mount, então editar arquivos em `src/` recarrega na hora, sem rebuild de imagem.

## Usuários de teste

Validação simples contra credenciais fixas em `src/pages/auth/LoginPage.tsx` (não é autenticação real, é só pra navegar entre os dois perfis):

| Perfil | Login | Senha |
|---|---|---|
| Administrador | `admin@instituicao.edu.br` | `admin123` |
| Avaliador | `camila.torres@instituicao.edu.br` | `senha123` |

Essas contas existem só neste protótipo — o seed do backend (`projeto_backend`) hoje cria apenas o usuário admin, já que avaliador de verdade só entra por importação de arquivo (ainda não implementada). Ver [../README.md](../README.md#usuários-de-teste) para a explicação completa dessa diferença.

## Status atual

Telas construídas e navegáveis com dados mock; nenhuma integração com a API. Ao integrar, os pontos de entrada a trocar são os arquivos em `src/mock/data.ts` (por chamadas via TanStack Query) e a validação de login em `LoginPage.tsx` (por `POST /auth/login` do backend).

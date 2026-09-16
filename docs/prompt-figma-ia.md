# Prompt para Figma AI — Protótipo do Sistema de Avaliação de Projetos

Prompt pronto para colar no Figma AI (First Draft) para gerar o protótipo inicial das telas. Limitado a ~2000 caracteres (limite do Figma AI).

```
Protótipo de interface web para um Sistema de Avaliação de Projetos Acadêmicos: professores avaliam projetos de alunos numa mostra científica anual, sem saber quem é o autor (avaliação às cegas).

Estilo: limpo, institucional, tons neutros (branco, cinza, azul escuro como destaque), tipografia legível, cantos arredondados. Foco em desktop.

Dois perfis, navegação separada:

AVALIADOR: Login (e-mail/CPF + senha). Ativar conta no 1º acesso (definir senha). Dashboard com cards dos projetos atribuídos (título, área, status, prazo em destaque) — sem nome de aluno/orientador/instituição em nenhuma tela. Tela de avaliação: título e resumo técnico do projeto, formulário com nota (0-10) e comentário por critério (Clareza, Rigor Metodológico, Relevância), nota final calculada somente leitura, botões Salvar rascunho e Enviar avaliação final (com confirmação). Estado bloqueado quando prazo encerrou ou já foi enviada.

ADMIN: Dashboard com números da edição ativa (projetos avaliados, atribuição pendente, divergências). Gestão de edições (tabela + modal com prazos e configs). Gestão de critérios (nome, peso, soma 100%). Importar projetos (upload JSON + histórico + erro por item). Importar avaliadores (mesma lógica, arquivo de professores). Mapeamento de áreas pendentes. Lista de projetos com autoria visível só pro admin. Detalhe do projeto: avaliadores atribuídos, status de cada avaliação, comparação de notas lado a lado com alerta de divergência. Atribuição automática (botão + resultado com pendências). Reatribuição manual (modal com justificativa). Relatórios entre edições. Log de auditoria.

Frames separados por tela, menu lateral diferente por perfil (avaliador enxuto, admin completo).
```

## Como usar

1. Cole o texto dentro do bloco de código acima no Figma AI (First Draft).
2. Gere primeiro as telas do avaliador, depois as do admin — prompts menores tendem a dar resultado mais fiel do que pedir tudo de uma vez.
3. Ajuste manualmente depois: o Figma AI não vai acertar os detalhes de regra de negócio (ex: campo de nota final travado), então revise esses pontos comparando com [modelagem-dados.md](modelagem-dados.md) e [plano-implementacao.md](plano-implementacao.md).
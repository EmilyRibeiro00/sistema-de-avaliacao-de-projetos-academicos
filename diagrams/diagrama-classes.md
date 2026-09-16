# Diagrama de Classes — Sistema de Avaliação de Projetos

Espelha as entidades JPA implementadas em `projeto_backend/src/main/java/com/avaliacao/sistema/entity/`. Atributos reduzidos aos campos relevantes para leitura (getters/setters do Lombok omitidos). Detalhe completo de cada campo: [../docs/modelagem-dados.md](../docs/modelagem-dados.md).

```mermaid
classDiagram
    class Instituicao {
        +Long id
        +String nome
        +String sigla
    }

    class Pessoa {
        +Long id
        +String nome
        +String cpf
        +String matricula
        +String siape
        +String email
    }

    class Usuario {
        +Long id
        +String senhaHash
        +TipoUsuario tipo
        +StatusAtivacao statusAtivacao
        +String tokenAtivacao
        +Instant tokenAtivacaoExpiraEm
    }

    class AreaConhecimento {
        +Long id
        +String nome
    }

    class AvaliadorArea {
        +Long id
    }

    class AreaConhecimentoAlias {
        +Long id
        +String textoOrigem
    }

    class Edicao {
        +Long id
        +String nome
        +Integer ano
        +StatusEdicao status
        +Instant dataAbertura
        +Instant dataFechamento
        +Integer minAvaliadoresPorProjeto
        +Integer limiteMaxProjetosPorAvaliador
        +BigDecimal limiteDivergenciaNotas
        +BigDecimal escalaNotaMin
        +BigDecimal escalaNotaMax
    }

    class Criterio {
        +Long id
        +String nome
        +BigDecimal peso
        +Integer ordem
    }

    class EdicaoAvaliador {
        +Long id
        +StatusParticipacaoAvaliador status
        +Integer limiteMaxProjetos
    }

    class Projeto {
        +Long id
        +String sigaaId
        +String titulo
        +String conteudoTecnico
        +StatusProjeto status
    }

    class ProjetoDiscente {
        +Long id
        +PapelDiscente papel
    }

    class ProjetoOrientador {
        +Long id
    }

    class Atribuicao {
        +Long id
        +StatusAtribuicao status
        +OrigemAtribuicao origem
        +String motivo
        +Instant createdAt
    }

    class Avaliacao {
        +Long id
        +StatusAvaliacao status
        +BigDecimal notaFinal
        +Instant dataEnvio
        +Instant reabertaEm
        +String motivoReabertura
    }

    class AvaliacaoNota {
        +Long id
        +BigDecimal nota
    }

    class IntegracaoCredencial {
        +Long id
        +String nome
        +String chaveHash
        +boolean ativo
    }

    class ImportacaoLote {
        +Long id
        +TipoImportacao tipo
        +OrigemImportacao origem
        +StatusImportacaoLote status
        +Integer totalItens
        +Integer totalSucesso
        +Integer totalErro
    }

    class ImportacaoItem {
        +Long id
        +String referenciaExterna
        +StatusImportacaoItem status
        +String mensagemErro
    }

    class LogAuditoria {
        +Long id
        +String acao
        +String entidade
        +Long entidadeId
        +String detalhes
        +Instant dataHora
    }

    Instituicao "1" --> "*" Pessoa : possui
    Pessoa "1" --> "0..1" Usuario : login

    Usuario "1" --> "*" AvaliadorArea
    AreaConhecimento "1" --> "*" AvaliadorArea

    Edicao "1" --> "*" Criterio
    Edicao "1" --> "*" Projeto
    Edicao "1" --> "*" EdicaoAvaliador
    Usuario "1" --> "*" EdicaoAvaliador : participa

    AreaConhecimento "1" --> "*" Projeto : classifica
    AreaConhecimento "1" --> "*" AreaConhecimentoAlias : resolve

    Projeto "1" --> "*" ProjetoDiscente
    Projeto "1" --> "*" ProjetoOrientador
    Pessoa "1" --> "*" ProjetoDiscente
    Pessoa "1" --> "*" ProjetoOrientador

    Projeto "1" --> "*" Atribuicao
    Usuario "1" --> "*" Atribuicao : avalia
    Atribuicao "1" --> "0..1" Avaliacao

    Avaliacao "1" --> "*" AvaliacaoNota
    Criterio "1" --> "*" AvaliacaoNota

    Edicao "1" --> "*" ImportacaoLote
    ImportacaoLote "1" --> "*" ImportacaoItem
    IntegracaoCredencial "1" --> "*" ImportacaoLote

    Usuario "1" --> "*" LogAuditoria

    class TipoUsuario {
        <<enumeration>>
        ADMIN
        AVALIADOR
    }
    class StatusAtivacao {
        <<enumeration>>
        PENDENTE_ATIVACAO
        ATIVO
        INATIVO
    }
    class StatusEdicao {
        <<enumeration>>
        PLANEJADA
        ATIVA
        ENCERRADA
    }
    class StatusProjeto {
        <<enumeration>>
        SUBMETIDO
        EM_AVALIACAO
        AVALIADO
    }
    class PapelDiscente {
        <<enumeration>>
        AUTOR
        COAUTOR
    }
    class StatusAtribuicao {
        <<enumeration>>
        ATIVA
        CANCELADA
    }
    class OrigemAtribuicao {
        <<enumeration>>
        AUTOMATICA
        MANUAL
    }
    class StatusAvaliacao {
        <<enumeration>>
        RASCUNHO
        ENVIADA
    }
```

## Notas sobre o desenho

- **Pessoa vs. Usuario**: separadas de propósito — nem todo `Pessoa` (discente, orientador) tem `Usuario` (login). Só quem acessa o sistema (avaliador, admin) tem os dois. É essa separação que permite checar conflito de interesse comparando IDs de pessoa, mesmo para quem nunca logou.
- **AvaliadorArea vs. EdicaoAvaliador**: a especialidade do avaliador (`AvaliadorArea`) é um atributo permanente da pessoa; a participação numa edição específica (`EdicaoAvaliador`) é o que entra/sai a cada importação anual do arquivo de professores.
- **Atribuicao → Avaliacao (0..1)**: toda atribuição pode ou não ter uma avaliação associada ainda — vira `1` só depois que o avaliador começa a preencher (mesmo em rascunho).
- **Projeto.instituicao_id e ProjetoDiscente/ProjetoOrientador** não aparecem na visão do avaliador em nenhuma tela — esse corte é feito na camada de serviço/DTO, não neste diagrama de dados.
- **ProjetoDiscente/ProjetoOrientador são N:N sem limite superior**: um projeto tem exatamente um `AUTOR` (índice único parcial no banco) e pode ter vários `COAUTOR` e vários orientadores; uma mesma pessoa pode orientar vários projetos e, com `Usuario` próprio, avaliar projetos diferentes dos que orienta.
- **Edicao.min_avaliadores_por_projeto = 3** por padrão — todo projeto precisa de pelo menos 3 `Atribuicao` ATIVA. A nota do projeto (não modelada como coluna) é a média das `Avaliacao.nota_final` dessas atribuições.

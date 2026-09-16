import type {
  AreaAlias,
  AvaliadorStatus,
  Criterio,
  Edicao,
  ImportacaoItem,
  ImportacaoLote,
  LogAuditoriaItem,
  NotaAvaliador,
  ProjetoAdmin,
  ProjetoResumoAvaliador,
} from "../types";

export const edicaoAtiva: Edicao = {
  id: 1,
  nome: "Mostra Científica 2026",
  ano: 2026,
  status: "ATIVA",
  dataAbertura: "2026-08-01",
  dataFechamento: "2026-10-15",
  minAvaliadoresPorProjeto: 3,
  limiteDivergenciaNotas: 2.5,
};

export const edicoes: Edicao[] = [
  edicaoAtiva,
  {
    id: 2,
    nome: "Mostra Científica 2025",
    ano: 2025,
    status: "ENCERRADA",
    dataAbertura: "2025-08-01",
    dataFechamento: "2025-10-15",
    minAvaliadoresPorProjeto: 3,
    limiteDivergenciaNotas: 2.5,
  },
];

export const criterios: Criterio[] = [
  { id: 1, nome: "Clareza", descricao: "Clareza na apresentação do problema, objetivos e resultados", peso: 30 },
  { id: 2, nome: "Rigor Metodológico", descricao: "Adequação e rigor dos métodos utilizados", peso: 40 },
  { id: 3, nome: "Relevância", descricao: "Relevância científica e impacto potencial do trabalho", peso: 30 },
];

export const projetosAvaliador: ProjetoResumoAvaliador[] = [
  {
    id: 101,
    titulo: "Detecção de anomalias em redes IoT com aprendizado federado",
    area: "Ciência da Computação",
    subarea: "Segurança e Redes",
    resumoTecnico:
      "O projeto propõe um pipeline de detecção de anomalias em tráfego de redes IoT utilizando " +
      "aprendizado federado, permitindo que dispositivos treinem um modelo colaborativo sem " +
      "compartilhar dados brutos entre si. Foram avaliados três algoritmos de agregação em um " +
      "ambiente simulado com 40 nós, com resultados preliminares indicando redução de 18% no " +
      "tempo de detecção em relação à abordagem centralizada de baseline.",
    status: "PENDENTE",
    prazo: "2026-10-15",
  },
  {
    id: 102,
    titulo: "Compostagem acelerada com fungos decompositores nativos",
    area: "Ciências Biológicas",
    subarea: "Microbiologia Aplicada",
    resumoTecnico:
      "Estudo da aceleração do processo de compostagem de resíduos orgânicos urbanos por meio da " +
      "inoculação de fungos decompositores nativos, comparando tempo de maturação e qualidade do " +
      "composto final frente ao processo tradicional sem inoculação.",
    status: "RASCUNHO",
    prazo: "2026-10-15",
  },
  {
    id: 103,
    titulo: "Modelagem de propagação de arboviroses em áreas urbanas",
    area: "Saúde Coletiva",
    subarea: "Epidemiologia",
    resumoTecnico:
      "Modelo compartimental SIR adaptado para simular a propagação de arboviroses em ambientes " +
      "urbanos densamente povoados, incorporando dados climáticos e de mobilidade para prever picos " +
      "de transmissão com até quatro semanas de antecedência.",
    status: "ENVIADA",
    prazo: "2026-10-15",
    minhaNotaFinal: 8.5,
    minhasNotas: { Clareza: 8.5, "Rigor Metodológico": 9, Relevância: 8 },
  },
  {
    id: 104,
    titulo: "Acessibilidade em bibliotecas públicas: um estudo de caso",
    area: "Biblioteconomia",
    subarea: "Acessibilidade",
    resumoTecnico:
      "Levantamento das barreiras de acessibilidade física e informacional em três bibliotecas " +
      "públicas municipais, com proposta de plano de adequação baseado nas normas técnicas vigentes.",
    status: "PENDENTE",
    prazo: "2026-10-15",
  },
];

export const projetosAdmin: ProjetoAdmin[] = [
  {
    id: 101,
    titulo: "Detecção de anomalias em redes IoT com aprendizado federado",
    area: "Ciência da Computação",
    subarea: "Segurança e Redes",
    instituicao: "Campus Central",
    autor: "Ana Beatriz Souza",
    coautores: ["João Pedro Lima"],
    orientadores: ["Prof. Carlos Menezes", "Profa. Marina Costa"],
    resumoTecnico:
      "O projeto propõe um pipeline de detecção de anomalias em tráfego de redes IoT utilizando " +
      "aprendizado federado, permitindo que dispositivos treinem um modelo colaborativo sem " +
      "compartilhar dados brutos entre si.",
    status: "EM_AVALIACAO",
    avaliadoresAtribuidos: 3,
    avaliacoesEnviadas: 0,
  },
  {
    id: 102,
    titulo: "Compostagem acelerada com fungos decompositores nativos",
    area: "Ciências Biológicas",
    subarea: "Microbiologia Aplicada",
    instituicao: "Campus Norte",
    autor: "João Pedro Lima",
    coautores: ["Marina Costa"],
    orientadores: ["Profa. Renata Alves"],
    resumoTecnico:
      "Estudo da aceleração do processo de compostagem de resíduos orgânicos urbanos por meio da " +
      "inoculação de fungos decompositores nativos.",
    status: "EM_AVALIACAO",
    avaliadoresAtribuidos: 3,
    avaliacoesEnviadas: 1,
  },
  {
    id: 103,
    titulo: "Modelagem de propagação de arboviroses em áreas urbanas",
    area: "Saúde Coletiva",
    subarea: "Epidemiologia",
    instituicao: "Campus Central",
    autor: "Lucas Fernandes",
    coautores: [],
    orientadores: ["Prof. Eduardo Ramos"],
    resumoTecnico:
      "Modelo compartimental SIR adaptado para simular a propagação de arboviroses em ambientes " +
      "urbanos densamente povoados.",
    status: "AVALIADO",
    avaliadoresAtribuidos: 3,
    avaliacoesEnviadas: 3,
  },
  {
    id: 105,
    titulo: "Reaproveitamento de água cinza em edificações escolares",
    area: "Engenharia Civil",
    subarea: "Saneamento",
    instituicao: "Campus Sul",
    autor: "Helena Martins",
    coautores: [],
    orientadores: ["Prof. Diego Nascimento"],
    resumoTecnico:
      "Avaliação da viabilidade técnica de sistemas de reaproveitamento de água cinza em escolas " +
      "públicas, com foco em redução de consumo de água potável para fins não potáveis.",
    status: "SUBMETIDO",
    avaliadoresAtribuidos: 1,
    avaliacoesEnviadas: 0,
  },
];

export const notasProjeto103: NotaAvaliador[] = [
  {
    avaliador: "Prof. Camila Torres",
    notas: { Clareza: 8.5, "Rigor Metodológico": 9, Relevância: 8 },
    comentarios: {
      Clareza: "Apresentação clara e bem estruturada.",
      "Rigor Metodológico": "Metodologia bem fundamentada.",
      Relevância: "Alta relevância para a área.",
    },
    notaFinal: 8.5,
    status: "ENVIADA",
  },
  {
    avaliador: "Prof. Ricardo Nunes",
    notas: { Clareza: 6, "Rigor Metodológico": 7, Relevância: 6.5 },
    comentarios: {
      Clareza: "Faltou detalhar melhor a base de dados usada.",
      "Rigor Metodológico": "Modelo razoável, mas sem validação cruzada.",
      Relevância: "Tema relevante, porém já bastante explorado na literatura.",
    },
    notaFinal: 6.5,
    status: "ENVIADA",
  },
  {
    avaliador: "Profa. Beatriz Lima",
    notas: { Clareza: 9, "Rigor Metodológico": 8.5, Relevância: 9 },
    comentarios: {
      Clareza: "Texto muito bem organizado, fácil de acompanhar.",
      "Rigor Metodológico": "Validação estatística consistente.",
      Relevância: "Tema com forte aplicação em saúde pública.",
    },
    notaFinal: 8.8,
    status: "ENVIADA",
  },
];

export const avaliadoresStatus: AvaliadorStatus[] = [
  { id: 1, nome: "Prof. Camila Torres", instituicao: "Campus Central", avaliacoesEnviadas: 2, avaliacoesAtribuidas: 4 },
  { id: 2, nome: "Prof. Ricardo Nunes", instituicao: "Campus Norte", avaliacoesEnviadas: 3, avaliacoesAtribuidas: 3 },
  { id: 3, nome: "Profa. Beatriz Lima", instituicao: "Campus Sul", avaliacoesEnviadas: 1, avaliacoesAtribuidas: 4 },
  { id: 4, nome: "Prof. André Cavalcanti", instituicao: "Campus Central", avaliacoesEnviadas: 0, avaliacoesAtribuidas: 2 },
];

export const lotesImportacao: ImportacaoLote[] = [
  { id: 1, tipo: "PROJETOS_SIGAA", data: "2026-08-03 14:22", totalItens: 48, totalSucesso: 44, totalErro: 4, status: "CONCLUIDO_COM_ERROS" },
  { id: 2, tipo: "PROJETOS_SIGAA", data: "2026-08-01 09:10", totalItens: 120, totalSucesso: 120, totalErro: 0, status: "CONCLUIDO" },
];

export const lotesAvaliadores: ImportacaoLote[] = [
  { id: 3, tipo: "AVALIADORES_ARQUIVO", data: "2026-07-28 16:40", totalItens: 35, totalSucesso: 35, totalErro: 0, status: "CONCLUIDO" },
];

export const itensLote1: ImportacaoItem[] = [
  { id: 1, referencia: "SIGAA-88231", status: "SUCESSO" },
  { id: 2, referencia: "SIGAA-88232", status: "PENDENTE_MAPEAMENTO", mensagemErro: "Área \"Engenharia de Bioprocessos\" sem mapeamento" },
  { id: 3, referencia: "SIGAA-88233", status: "ERRO", mensagemErro: "Campo 'titulo' ausente no JSON" },
  { id: 4, referencia: "SIGAA-88234", status: "IGNORADO_DUPLICADO", mensagemErro: "sigaaId já importado nesta edição" },
];

export const areasAliasPendentes: AreaAlias[] = [
  { id: 1, textoOrigem: "Engenharia de Bioprocessos" },
  { id: 2, textoOrigem: "Letras - Tradução" },
  { id: 3, textoOrigem: "Ciência de Dados Aplicada", areaSugerida: "Ciência da Computação" },
];

export const logAuditoria: LogAuditoriaItem[] = [
  { id: 1, dataHora: "2026-09-16 09:12", usuario: "admin@instituicao.edu.br", acao: "REABRIU_AVALIACAO", entidade: "Avaliacao #204" },
  { id: 2, dataHora: "2026-09-15 18:03", usuario: "sistema", acao: "ATRIBUICAO_AUTOMATICA", entidade: "Edicao #1" },
  { id: 3, dataHora: "2026-09-15 17:40", usuario: "prof.camila@instituicao.edu.br", acao: "ENVIOU_AVALIACAO", entidade: "Avaliacao #198" },
  { id: 4, dataHora: "2026-09-14 11:22", usuario: "admin@instituicao.edu.br", acao: "REATRIBUIU_AVALIADOR", entidade: "Projeto #105" },
];

export type StatusEdicao = "PLANEJADA" | "ATIVA" | "ENCERRADA";
export type StatusProjeto = "SUBMETIDO" | "EM_AVALIACAO" | "AVALIADO";
export type StatusAvaliacao = "PENDENTE" | "RASCUNHO" | "ENVIADA";
export type StatusImportacaoLote =
  | "RECEBIDO"
  | "PROCESSANDO"
  | "CONCLUIDO"
  | "CONCLUIDO_COM_ERROS"
  | "FALHOU";
export type StatusImportacaoItem =
  | "SUCESSO"
  | "ERRO"
  | "IGNORADO_DUPLICADO"
  | "PENDENTE_MAPEAMENTO";

export interface Edicao {
  id: number;
  nome: string;
  ano: number;
  status: StatusEdicao;
  dataAbertura: string;
  dataFechamento: string;
  /** Todo projeto precisa de no mínimo esse número de avaliadores atribuídos (regra: mínimo 3). */
  minAvaliadoresPorProjeto: number;
  limiteDivergenciaNotas: number;
}

export interface Criterio {
  id: number;
  nome: string;
  descricao: string;
  peso: number;
}

export interface ProjetoResumoAvaliador {
  id: number;
  titulo: string;
  area: string;
  subarea: string;
  resumoTecnico: string;
  status: StatusAvaliacao;
  prazo: string;
  minhaNotaFinal?: number;
  minhasNotas?: Record<string, number>;
}

export interface ProjetoAdmin {
  id: number;
  titulo: string;
  area: string;
  subarea: string;
  instituicao: string;
  autor: string;
  coautores: string[];
  orientadores: string[];
  resumoTecnico: string;
  status: StatusProjeto;
  avaliadoresAtribuidos: number;
  avaliacoesEnviadas: number;
}

export interface NotaAvaliador {
  avaliador: string;
  notas: Record<string, number>;
  comentarios?: Record<string, string>;
  notaFinal: number;
  status: StatusAvaliacao;
}

export interface AvaliadorStatus {
  id: number;
  nome: string;
  instituicao: string;
  avaliacoesEnviadas: number;
  avaliacoesAtribuidas: number;
}

export interface ImportacaoLote {
  id: number;
  tipo: "PROJETOS_SIGAA" | "AVALIADORES_ARQUIVO";
  data: string;
  totalItens: number;
  totalSucesso: number;
  totalErro: number;
  status: StatusImportacaoLote;
}

export interface ImportacaoItem {
  id: number;
  referencia: string;
  status: StatusImportacaoItem;
  mensagemErro?: string;
}

export interface AreaAlias {
  id: number;
  textoOrigem: string;
  areaSugerida?: string;
}

export interface LogAuditoriaItem {
  id: number;
  dataHora: string;
  usuario: string;
  acao: string;
  entidade: string;
}

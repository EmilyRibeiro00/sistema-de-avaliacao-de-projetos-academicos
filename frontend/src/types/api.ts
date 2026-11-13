// Tipos espelhando os DTOs do backend (backend/src/main/java/com/avaliacao/sistema/**/dto)

export type TipoUsuario = "ADMIN" | "AVALIADOR";

export type StatusEdicao = "PLANEJADA" | "ATIVA" | "ENCERRADA";
export type StatusAtribuicao = "ATIVA" | "CANCELADA";
export type StatusAvaliacao = "PENDENTE" | "RASCUNHO" | "ENVIADA";
export type StatusProjeto = "SUBMETIDO" | "EM_AVALIACAO" | "AVALIADO";

export interface LoginResponse {
  token: string;
  tipo: TipoUsuario;
}

export interface EdicaoResponse {
  id: number;
  nome: string;
  ano: number;
  status: StatusEdicao;
  dataAbertura: string;
  dataFechamento: string;
  minAvaliadoresPorProjeto: number;
  limiteMaxProjetosPorAvaliador: number | null;
  limiteDivergenciaNotas: number | null;
  escalaNotaMin: number;
  escalaNotaMax: number;
  ativo: boolean;
}

export interface EdicaoCommand {
  nome: string;
  ano: number;
  dataAbertura: string;
  dataFechamento: string;
  minAvaliadoresPorProjeto?: number;
  limiteMaxProjetosPorAvaliador?: number;
  limiteDivergenciaNotas?: number;
  escalaNotaMin?: number;
  escalaNotaMax?: number;
}

export interface CriterioView {
  id: number;
  nome: string;
  peso: number;
  ativo: boolean;
}

export interface CriterioCommand {
  nome: string;
  peso: number;
  ordem?: number;
}

export interface ResultadoAtribuicao {
  projetoId: number;
  jaAtribuidos: number;
  novosAtribuidos: number;
  faltantes: number;
  avaliadoresSorteados: number[];
  avisos: string[];
}

export interface AtribuicaoResponse {
  id: number;
  projetoId: number;
  avaliadorId: number;
  status: StatusAtribuicao;
  origem: "AUTOMATICA" | "MANUAL";
}

export interface NotaAvaliadorView {
  avaliadorId: number;
  avaliadorNome: string;
  notaFinal: number;
}

export interface ResultadoConsolidacao {
  projetoId: number;
  avaliadoresAtribuidos: number;
  avaliacoesEnviadas: number;
  avaliado: boolean;
  notaDoProjeto: number | null;
  divergencia: number | null;
  divergenciaAcimaDoLimite: boolean;
  notasPorAvaliador: NotaAvaliadorView[];
}

export interface ProjetoAvaliadorView {
  atribuicaoId: number;
  projetoId: number;
  titulo: string;
  comite: string | null;
  conteudoTecnico: string;
  statusAvaliacao: StatusAvaliacao | "PENDENTE";
}

export interface AvaliacaoDetalhe {
  projeto: ProjetoAvaliadorView;
  criterios: CriterioView[];
  notas: Record<string, number>;
  notaFinal: number | null;
  editavel: boolean;
}

export interface CadastroAvaliadorCommand {
  nome: string;
  email: string;
  cpf: string;
  matricula?: string;
  siape?: string;
  instituicaoNome?: string;
}

// senhaTemporaria só vem aqui porque não existe envio de e-mail de verdade ainda (ver backend/EmailService) —
// é o jeito do admin repassar a senha padrão manualmente até isso existir.
export interface AvaliadorResponse {
  id: number;
  nome: string;
  email: string;
  senhaTemporaria: string;
}

export interface TrocarSenhaCommand {
  senhaAtual: string;
  novaSenha: string;
}

export interface CadastroProjetoCommand {
  edicaoId: number;
  sigaaId?: string;
  titulo: string;
  conteudoTecnico: string;
  comiteNome?: string;
  campusNome?: string;
  instituicaoNome?: string;
  autorNome: string;
  autorEmail?: string;
  autorCpf?: string;
}

export interface ProjetoResponse {
  id: number;
  edicaoId: number;
  sigaaId: string;
  titulo: string;
  conteudoTecnico: string;
  comite: string | null;
  campus: string | null;
  instituicao: string | null;
  status: StatusProjeto;
  ativo: boolean;
}

// edicaoId, sigaaId e autor não são editáveis (ver backend EditarProjetoCommand).
export interface EditarProjetoCommand {
  titulo: string;
  conteudoTecnico: string;
  comiteNome?: string;
  campusNome?: string;
  instituicaoNome?: string;
}

// comiteNome e campusNome são obrigatórios: o banco rejeita um avaliador ativo numa edição sem os dois
// (ver backend EdicaoAvaliadorService.vincular).
export interface VincularAvaliadorCommand {
  edicaoId: number;
  comiteNome: string;
  campusNome: string;
  limiteMaxProjetos?: number;
}

export type StatusEdicaoAvaliador = "ATIVO" | "INATIVO";

export interface ParticipacaoResponse {
  avaliadorId: number;
  edicaoId: number;
  status: StatusEdicaoAvaliador;
  comiteNome: string | null;
  campusNome: string | null;
}

export type StatusAtivacao = "PENDENTE_ATIVACAO" | "ATIVO" | "INATIVO";

export interface AvaliadorListResponse {
  id: number;
  nome: string;
  email: string;
  cpf: string;
  matricula: string | null;
  siape: string | null;
  instituicaoNome: string | null;
  comiteNome: string | null;
  statusAtivacao: StatusAtivacao;
}

export interface EditarAvaliadorCommand {
  nome: string;
  email: string;
  cpf: string;
  matricula?: string;
  siape?: string;
  instituicaoNome?: string;
  comiteNome?: string;
}

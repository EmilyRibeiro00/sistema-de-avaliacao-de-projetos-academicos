import { api } from "./client";
import type { AtribuicaoResponse, ResultadoAtribuicao } from "../types/api";

export const atribuicoesApi = {
  atribuirProjeto: (projetoId: number) =>
    api.post<ResultadoAtribuicao>(`/api/admin/atribuicoes/projetos/${projetoId}`),
  atribuirEdicaoAtiva: () => api.post<ResultadoAtribuicao[]>("/api/admin/atribuicoes/edicao-ativa"),
  reatribuir: (atribuicaoId: number, novoAvaliadorId: number, motivo: string) =>
    api.post<AtribuicaoResponse>(`/api/admin/atribuicoes/${atribuicaoId}/reatribuir`, { novoAvaliadorId, motivo }),
};

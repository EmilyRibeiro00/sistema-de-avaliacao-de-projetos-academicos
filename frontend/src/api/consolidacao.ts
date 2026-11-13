import { api } from "./client";
import type { ResultadoConsolidacao } from "../types/api";

export const consolidacaoApi = {
  consolidar: (projetoId: number) => api.get<ResultadoConsolidacao>(`/api/admin/projetos/${projetoId}/consolidacao`),
  reabrirAvaliacao: (avaliacaoId: number, motivo: string) =>
    api.post<void>(`/api/admin/avaliacoes/${avaliacaoId}/reabrir`, { motivo }),
};

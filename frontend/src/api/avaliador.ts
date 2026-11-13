import { api } from "./client";
import type { AvaliacaoDetalhe, ProjetoAvaliadorView } from "../types/api";

export const avaliadorApi = {
  projetos: () => api.get<ProjetoAvaliadorView[]>("/api/avaliador/projetos"),
  detalhar: (atribuicaoId: number) => api.get<AvaliacaoDetalhe>(`/api/avaliador/atribuicoes/${atribuicaoId}`),
  salvarRascunho: (atribuicaoId: number, notas: Record<string, number>) =>
    api.put<AvaliacaoDetalhe>(`/api/avaliador/atribuicoes/${atribuicaoId}/rascunho`, { notas }),
  enviar: (atribuicaoId: number, notas: Record<string, number>) =>
    api.post<AvaliacaoDetalhe>(`/api/avaliador/atribuicoes/${atribuicaoId}/enviar`, { notas }),
};

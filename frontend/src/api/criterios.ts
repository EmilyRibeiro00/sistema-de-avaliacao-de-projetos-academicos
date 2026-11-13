import { api } from "./client";
import type { CriterioCommand, CriterioView } from "../types/api";

export const criteriosApi = {
  listar: (edicaoId: number) => api.get<CriterioView[]>(`/api/admin/edicoes/${edicaoId}/criterios`),
  adicionar: (edicaoId: number, comando: CriterioCommand) =>
    api.post<CriterioView>(`/api/admin/edicoes/${edicaoId}/criterios`, comando),
  atualizar: (criterioId: number, comando: CriterioCommand) =>
    api.put<CriterioView>(`/api/admin/criterios/${criterioId}`, comando),
  remover: (criterioId: number) => api.delete<void>(`/api/admin/criterios/${criterioId}`),
};

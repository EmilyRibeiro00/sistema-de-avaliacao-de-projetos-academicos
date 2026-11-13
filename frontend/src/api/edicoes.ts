import { api } from "./client";
import type { EdicaoCommand, EdicaoResponse } from "../types/api";

export const edicoesApi = {
  listar: () => api.get<EdicaoResponse[]>("/api/admin/edicoes"),
  buscar: (id: number) => api.get<EdicaoResponse>(`/api/admin/edicoes/${id}`),
  criar: (comando: EdicaoCommand) => api.post<EdicaoResponse>("/api/admin/edicoes", comando),
  atualizar: (id: number, comando: EdicaoCommand) => api.put<EdicaoResponse>(`/api/admin/edicoes/${id}`, comando),
  remover: (id: number) => api.delete<void>(`/api/admin/edicoes/${id}`),
  ativar: (id: number) => api.post<EdicaoResponse>(`/api/admin/edicoes/${id}/ativar`),
  encerrar: (id: number) => api.post<EdicaoResponse>(`/api/admin/edicoes/${id}/encerrar`),
};

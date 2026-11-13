import { api } from "./client";
import type { CadastroProjetoCommand, EditarProjetoCommand, ProjetoResponse } from "../types/api";

export const projetosApi = {
  listar: (edicaoId?: number) =>
    api.get<ProjetoResponse[]>(`/api/admin/projetos${edicaoId ? `?edicaoId=${edicaoId}` : ""}`),
  cadastrar: (comando: CadastroProjetoCommand) => api.post<ProjetoResponse>("/api/admin/projetos", comando),
  atualizar: (id: number, comando: EditarProjetoCommand) => api.put<ProjetoResponse>(`/api/admin/projetos/${id}`, comando),
  remover: (id: number) => api.delete<void>(`/api/admin/projetos/${id}`),
};

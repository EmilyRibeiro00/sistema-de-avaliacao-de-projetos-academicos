import { api } from "./client";
import type {
  AvaliadorListResponse,
  AvaliadorResponse,
  CadastroAvaliadorCommand,
  EditarAvaliadorCommand,
  ParticipacaoResponse,
  VincularAvaliadorCommand,
} from "../types/api";

export const avaliadoresApi = {
  listar: () => api.get<AvaliadorListResponse[]>("/api/admin/avaliadores"),
  cadastrar: (comando: CadastroAvaliadorCommand) => api.post<AvaliadorResponse>("/api/admin/avaliadores", comando),
  atualizar: (id: number, comando: EditarAvaliadorCommand) =>
    api.put<AvaliadorListResponse>(`/api/admin/avaliadores/${id}`, comando),
  remover: (id: number) => api.delete<void>(`/api/admin/avaliadores/${id}`),
  vincular: (avaliadorId: number, comando: VincularAvaliadorCommand) =>
    api.post<ParticipacaoResponse>(`/api/admin/avaliadores/${avaliadorId}/edicoes`, comando),
};

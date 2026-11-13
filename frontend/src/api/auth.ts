import { api } from "./client";
import type { LoginResponse } from "../types/api";

export const authApi = {
  login: (identificador: string, senha: string) => api.post<LoginResponse>("/auth/login", { identificador, senha }),
  trocarSenha: (senhaAtual: string, novaSenha: string) =>
    api.put<LoginResponse>("/api/conta/senha", { senhaAtual, novaSenha }),
};

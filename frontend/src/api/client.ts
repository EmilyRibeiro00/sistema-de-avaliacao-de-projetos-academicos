import { clearSession, getToken } from "../auth/session";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

/** Disparado quando um token presente é recusado pelo backend; o AuthContext escuta para sincronizar o estado em memória. */
export const EVENTO_SESSAO_EXPIRADA = "sispac:sessao-expirada";

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = { ...(options.headers as Record<string, string>) };
  if (options.body) headers["Content-Type"] = "application/json";
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const response = await fetch(`${API_URL}${path}`, { ...options, headers });

  // 401 = sem autenticação válida (token ausente/expirado): a sessão local está errada, desloga.
  // 403 = autenticado mas sem permissão para esta ação específica (ex.: papel errado, ou a avaliação
  // não é sua) — é um erro de negócio normal, mostrado na tela; não é motivo para deslogar.
  if (token && response.status === 401) {
    clearSession();
    window.dispatchEvent(new Event(EVENTO_SESSAO_EXPIRADA));
  }

  if (!response.ok) {
    const mensagem = await extrairMensagemDeErro(response);
    throw new ApiError(response.status, mensagem);
  }

  if (response.status === 204) return undefined as T;
  return (await response.json()) as T;
}

async function extrairMensagemDeErro(response: Response): Promise<string> {
  try {
    const body = await response.json();
    if (body && typeof body.erro === "string") return body.erro;
  } catch {
    // corpo vazio ou não-JSON: cai no fallback abaixo
  }
  return `Erro inesperado (HTTP ${response.status})`;
}

export const api = {
  get: <T>(path: string) => request<T>(path, { method: "GET" }),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "POST", body: body === undefined ? undefined : JSON.stringify(body) }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "PUT", body: body === undefined ? undefined : JSON.stringify(body) }),
  delete: <T>(path: string) => request<T>(path, { method: "DELETE" }),
};

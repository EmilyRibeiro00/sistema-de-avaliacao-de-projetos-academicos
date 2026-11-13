import type { TipoUsuario } from "../types/api";

const STORAGE_KEY = "sispac.token";

export interface SessaoUsuario {
  token: string;
  usuarioId: number;
  email: string;
  tipo: TipoUsuario;
  expiraEm: number;
  precisaTrocarSenha: boolean;
}

interface JwtPayload {
  sub: string;
  usuarioId: number;
  tipo: TipoUsuario;
  exp: number;
  precisaTrocarSenha: boolean;
}

function decodificarToken(token: string): JwtPayload | null {
  try {
    const payload = token.split(".")[1];
    const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}

export function salvarSessao(token: string): SessaoUsuario | null {
  const sessao = sessaoDoToken(token);
  if (!sessao) return null;
  localStorage.setItem(STORAGE_KEY, token);
  return sessao;
}

export function sessaoDoToken(token: string): SessaoUsuario | null {
  const payload = decodificarToken(token);
  if (!payload) return null;
  return {
    token,
    usuarioId: payload.usuarioId,
    email: payload.sub,
    tipo: payload.tipo,
    expiraEm: payload.exp * 1000,
    precisaTrocarSenha: payload.precisaTrocarSenha,
  };
}

export function carregarSessao(): SessaoUsuario | null {
  const token = localStorage.getItem(STORAGE_KEY);
  if (!token) return null;
  const sessao = sessaoDoToken(token);
  if (!sessao || sessao.expiraEm <= Date.now()) {
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
  return sessao;
}

export function getToken(): string | null {
  return localStorage.getItem(STORAGE_KEY);
}

export function clearSession(): void {
  localStorage.removeItem(STORAGE_KEY);
}

/** Pra onde mandar o usuário logo depois do login (ou quando ele tenta entrar na área errada). */
export function rotaInicial(tipo: TipoUsuario): string {
  return tipo === "ADMIN" ? "/admin/edicoes" : "/avaliador/projetos";
}

import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";

import { EVENTO_SESSAO_EXPIRADA } from "../api/client";
import { carregarSessao, clearSession, salvarSessao, type SessaoUsuario } from "./session";

interface AuthContextValue {
  sessao: SessaoUsuario | null;
  entrar: (token: string) => SessaoUsuario | null;
  sair: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<SessaoUsuario | null>(() => carregarSessao());

  useEffect(() => {
    const aoExpirar = () => setSessao(null);
    window.addEventListener(EVENTO_SESSAO_EXPIRADA, aoExpirar);
    return () => window.removeEventListener(EVENTO_SESSAO_EXPIRADA, aoExpirar);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      sessao,
      entrar: (token: string) => {
        const nova = salvarSessao(token);
        setSessao(nova);
        return nova;
      },
      sair: () => {
        clearSession();
        setSessao(null);
      },
    }),
    [sessao],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth precisa estar dentro de um AuthProvider");
  return context;
}

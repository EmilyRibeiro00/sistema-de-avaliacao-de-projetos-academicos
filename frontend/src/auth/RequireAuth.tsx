import { Navigate, Outlet } from "react-router-dom";

import type { TipoUsuario } from "../types/api";
import { useAuth } from "./AuthContext";
import { rotaInicial } from "./session";

export function RequireAuth({ papel }: { papel: TipoUsuario }) {
  const { sessao } = useAuth();

  if (!sessao) return <Navigate to="/login" replace />;
  if (sessao.precisaTrocarSenha) return <Navigate to="/trocar-senha" replace />;
  if (sessao.tipo !== papel) {
    return <Navigate to={rotaInicial(sessao.tipo)} replace />;
  }
  return <Outlet />;
}

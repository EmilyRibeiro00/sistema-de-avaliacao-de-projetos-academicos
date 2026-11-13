import { Navigate, Route, Routes } from "react-router-dom";

import { useAuth } from "./auth/AuthContext";
import { RequireAuth } from "./auth/RequireAuth";
import { rotaInicial } from "./auth/session";
import { AppLayout } from "./layouts/AppLayout";

import { LoginPage } from "./pages/auth/LoginPage";
import { TrocarSenhaPage } from "./pages/auth/TrocarSenhaPage";

import { EdicoesPage } from "./pages/admin/EdicoesPage";
import { CriteriosPage } from "./pages/admin/CriteriosPage";
import { AvaliadoresPage } from "./pages/admin/AvaliadoresPage";
import { ProjetosAdminPage } from "./pages/admin/ProjetosPage";
import { ImportacaoPage } from "./pages/admin/ImportacaoPage";
import { AtribuicoesPage } from "./pages/admin/AtribuicoesPage";
import { ConsolidacaoPage } from "./pages/admin/ConsolidacaoPage";

import { ProjetosPage } from "./pages/avaliador/ProjetosPage";
import { AvaliacaoPage } from "./pages/avaliador/AvaliacaoPage";

function RootRedirect() {
  const { sessao } = useAuth();
  if (!sessao) return <Navigate to="/login" replace />;
  if (sessao.precisaTrocarSenha) return <Navigate to="/trocar-senha" replace />;
  return <Navigate to={rotaInicial(sessao.tipo)} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<RootRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/trocar-senha" element={<TrocarSenhaPage />} />

      <Route element={<RequireAuth papel="ADMIN" />}>
        <Route path="/admin" element={<AppLayout />}>
          <Route index element={<Navigate to="edicoes" replace />} />
          <Route path="edicoes" element={<EdicoesPage />} />
          <Route path="criterios" element={<CriteriosPage />} />
          <Route path="avaliadores" element={<AvaliadoresPage />} />
          <Route path="projetos" element={<ProjetosAdminPage />} />
          <Route path="importacao" element={<ImportacaoPage />} />
          <Route path="atribuicoes" element={<AtribuicoesPage />} />
          <Route path="consolidacao" element={<ConsolidacaoPage />} />
        </Route>
      </Route>

      <Route element={<RequireAuth papel="AVALIADOR" />}>
        <Route path="/avaliador" element={<AppLayout />}>
          <Route index element={<Navigate to="projetos" replace />} />
          <Route path="projetos" element={<ProjetosPage />} />
          <Route path="atribuicoes/:atribuicaoId" element={<AvaliacaoPage />} />
        </Route>
      </Route>

      <Route path="*" element={<RootRedirect />} />
    </Routes>
  );
}

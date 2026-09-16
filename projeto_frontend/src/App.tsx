import { Navigate, Route, Routes } from "react-router-dom";

import { LoginPage } from "./pages/auth/LoginPage";
import { AtivarContaPage } from "./pages/auth/AtivarContaPage";

import { AvaliadorLayout } from "./layouts/AvaliadorLayout";
import { DashboardAvaliadorPage } from "./pages/avaliador/DashboardAvaliadorPage";
import { AvaliacaoProjetoPage } from "./pages/avaliador/AvaliacaoProjetoPage";

import { AdminLayout } from "./layouts/AdminLayout";
import { DashboardAdminPage } from "./pages/admin/DashboardAdminPage";
import { EdicoesPage } from "./pages/admin/EdicoesPage";
import { CriteriosPage } from "./pages/admin/CriteriosPage";
import { ImportarProjetosPage } from "./pages/admin/ImportarProjetosPage";
import { ImportarAvaliadoresPage } from "./pages/admin/ImportarAvaliadoresPage";
import { MapeamentoAreasPage } from "./pages/admin/MapeamentoAreasPage";
import { ProjetosPage } from "./pages/admin/ProjetosPage";
import { ProjetoDetalhePage } from "./pages/admin/ProjetoDetalhePage";
import { RelatoriosPage } from "./pages/admin/RelatoriosPage";
import { AuditoriaPage } from "./pages/admin/AuditoriaPage";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/ativar-conta" element={<AtivarContaPage />} />

      <Route path="/avaliador" element={<AvaliadorLayout />}>
        <Route index element={<DashboardAvaliadorPage />} />
        <Route path="projetos/:id" element={<AvaliacaoProjetoPage />} />
      </Route>

      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<DashboardAdminPage />} />
        <Route path="edicoes" element={<EdicoesPage />} />
        <Route path="criterios" element={<CriteriosPage />} />
        <Route path="importar-projetos" element={<ImportarProjetosPage />} />
        <Route path="importar-avaliadores" element={<ImportarAvaliadoresPage />} />
        <Route path="mapeamento-areas" element={<MapeamentoAreasPage />} />
        <Route path="projetos" element={<ProjetosPage />} />
        <Route path="projetos/:id" element={<ProjetoDetalhePage />} />
        <Route path="relatorios" element={<RelatoriosPage />} />
        <Route path="auditoria" element={<AuditoriaPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

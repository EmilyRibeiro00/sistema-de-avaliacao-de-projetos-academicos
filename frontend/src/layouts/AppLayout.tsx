import { NavLink, Outlet, useNavigate } from "react-router-dom";

import { Brand } from "../components/Brand";
import { Icon, type IconName } from "../components/ui/Icon";
import { useAuth } from "../auth/AuthContext";

interface NavItem {
  to: string;
  icon: IconName;
  label: string;
}

const NAV_ADMIN: NavItem[] = [
  { to: "/admin/edicoes", icon: "calendar", label: "Edições" },
  { to: "/admin/criterios", icon: "sliders", label: "Critérios" },
  { to: "/admin/avaliadores", icon: "userPlus", label: "Avaliadores" },
  { to: "/admin/projetos", icon: "folder", label: "Projetos" },
  { to: "/admin/importacao", icon: "upload", label: "Importação" },
  { to: "/admin/atribuicoes", icon: "users", label: "Atribuições" },
  { to: "/admin/consolidacao", icon: "chart", label: "Consolidação" },
];

const NAV_AVALIADOR: NavItem[] = [{ to: "/avaliador/projetos", icon: "folder", label: "Meus projetos" }];

export function AppLayout() {
  const { sessao, sair } = useAuth();
  const navigate = useNavigate();
  const items = sessao?.tipo === "ADMIN" ? NAV_ADMIN : NAV_AVALIADOR;

  function logout() {
    sair();
    navigate("/login", { replace: true });
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <Brand />
        <div className="workspace">
          <small>AMBIENTE</small>
          <div>
            <span>{sessao?.tipo === "ADMIN" ? "AD" : "AV"}</span>
            <p>
              <strong>{sessao?.tipo === "ADMIN" ? "Administração" : "Avaliador"}</strong>
              <small>{sessao?.email}</small>
            </p>
          </div>
        </div>
        <nav>
          <small>NAVEGAÇÃO</small>
          {items.map((item) => (
            <NavLink key={item.to} to={item.to} className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}>
              <Icon name={item.icon} /> <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <NavLink to="/trocar-senha" className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}>
            <Icon name="lock" /> <span>Trocar senha</span>
          </NavLink>
          <button className="nav-item" onClick={logout} type="button">
            <Icon name="logout" /> <span>Sair</span>
          </button>
        </div>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}

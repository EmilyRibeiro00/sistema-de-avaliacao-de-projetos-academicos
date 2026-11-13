import { useState, type FormEvent } from "react";
import { Navigate, useNavigate } from "react-router-dom";

import { authApi } from "../../api/auth";
import { ApiError } from "../../api/client";
import { useAuth } from "../../auth/AuthContext";
import { rotaInicial } from "../../auth/session";
import { APP_NAME, APP_FULL_NAME } from "../../branding";
import { Brand } from "../../components/Brand";
import { Button } from "../../components/ui/Button";
import { Callout } from "../../components/ui/Callout";
import { Icon } from "../../components/ui/Icon";
import { TextField } from "../../components/ui/Fields";
import { Title } from "../../components/ui/Title";

export function LoginPage() {
  const { sessao, entrar } = useAuth();
  const navigate = useNavigate();
  const [identificador, setIdentificador] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);

  if (sessao) {
    return <Navigate to={rotaInicial(sessao.tipo)} replace />;
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setErro(null);
    setCarregando(true);
    try {
      const resposta = await authApi.login(identificador, senha);
      const nova = entrar(resposta.token);
      if (nova) navigate(rotaInicial(nova.tipo), { replace: true });
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível entrar. Tente novamente.");
    } finally {
      setCarregando(false);
    }
  }

  return (
    <main className="auth-page">
      <div className="auth-aside">
        <Brand />
        <div className="auth-message">
          <span className="eyebrow light">AVALIAÇÃO SEGURA E IMPARCIAL</span>
          <Title>Projetos excelentes merecem avaliações à altura.</Title>
          <p>Uma plataforma para organizar avaliações acadêmicas cegas com transparência, rigor e agilidade.</p>
          <div className="privacy-note">
            <Icon name="lock" />
            <span>
              <strong>Privacidade por design</strong>
              <small>Dados de autoria nunca são exibidos aos avaliadores.</small>
            </span>
          </div>
        </div>
        <small className="auth-footer">{APP_NAME} · {APP_FULL_NAME}</small>
      </div>
      <div className="auth-form-wrap">
        <div className="auth-mobile-brand">
          <Brand />
        </div>
        <form className="auth-card" onSubmit={onSubmit}>
          <span className="eyebrow">BEM-VINDO DE VOLTA</span>
          <Title>Acesse sua conta</Title>
          <p>Entre com as credenciais enviadas para o seu e-mail.</p>
          <div className="form-stack">
            <TextField
              label="E-mail"
              placeholder="nome@instituicao.edu.br"
              type="email"
              value={identificador}
              onChange={(e) => setIdentificador(e.target.value)}
              required
            />
            <TextField
              label="Senha"
              placeholder="Digite sua senha"
              type="password"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              required
            />
            {erro && <Callout tone="danger" title={erro} />}
            <Button type="submit" disabled={carregando}>
              {carregando ? "Entrando..." : "Entrar"} <Icon name="arrow" />
            </Button>
          </div>
          <small className="secure">
            <Icon name="lock" size={14} /> Ambiente seguro e monitorado
          </small>
        </form>
      </div>
    </main>
  );
}

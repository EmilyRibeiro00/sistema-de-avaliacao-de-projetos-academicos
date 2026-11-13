import { useState, type FormEvent } from "react";
import { Navigate, useNavigate } from "react-router-dom";

import { authApi } from "../../api/auth";
import { ApiError } from "../../api/client";
import { useAuth } from "../../auth/AuthContext";
import { rotaInicial } from "../../auth/session";
import { APP_NAME } from "../../branding";
import { Brand } from "../../components/Brand";
import { Button } from "../../components/ui/Button";
import { Callout } from "../../components/ui/Callout";
import { Icon } from "../../components/ui/Icon";
import { TextField } from "../../components/ui/Fields";
import { Title } from "../../components/ui/Title";

export function TrocarSenhaPage() {
  const { sessao, entrar } = useAuth();
  const navigate = useNavigate();
  const [senhaAtual, setSenhaAtual] = useState("");
  const [novaSenha, setNovaSenha] = useState("");
  const [confirmacao, setConfirmacao] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);

  if (!sessao) return <Navigate to="/login" replace />;

  const primeiroAcesso = sessao.precisaTrocarSenha;

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setErro(null);
    if (novaSenha !== confirmacao) {
      setErro("As senhas não coincidem");
      return;
    }
    setCarregando(true);
    try {
      const resposta = await authApi.trocarSenha(senhaAtual, novaSenha);
      const nova = entrar(resposta.token);
      if (nova) navigate(rotaInicial(nova.tipo), { replace: true });
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível trocar a senha.");
    } finally {
      setCarregando(false);
    }
  }

  return (
    <main className="auth-simple">
      <Brand />
      <div className="auth-card activation-card">
        <span className="success-icon">
          <Icon name="lock" size={24} />
        </span>
        <span className="eyebrow">{primeiroAcesso ? "PRIMEIRO ACESSO" : "SEGURANÇA DA CONTA"}</span>
        <Title>Troque sua senha</Title>
        <p>
          {primeiroAcesso
            ? `Você está usando a senha padrão. Defina uma senha própria para continuar no ${APP_NAME}.`
            : `Defina uma nova senha para sua conta no ${APP_NAME}.`}
        </p>
        <form className="form-stack" onSubmit={onSubmit}>
          <TextField label="Senha atual" placeholder="Digite sua senha atual" type="password" value={senhaAtual}
            onChange={(e) => setSenhaAtual(e.target.value)} required />
          <TextField label="Nova senha" placeholder="Mínimo de 8 caracteres" type="password" minLength={8}
            hint="Use letras maiúsculas, minúsculas e números." value={novaSenha}
            onChange={(e) => setNovaSenha(e.target.value)} required />
          <TextField label="Confirme a nova senha" placeholder="Digite novamente" type="password"
            value={confirmacao} onChange={(e) => setConfirmacao(e.target.value)} required />
          {erro && <Callout tone="danger" title={erro} />}
          <Button type="submit" disabled={carregando}>
            {carregando ? "Salvando..." : "Trocar senha"} <Icon name="arrow" />
          </Button>
        </form>
      </div>
    </main>
  );
}

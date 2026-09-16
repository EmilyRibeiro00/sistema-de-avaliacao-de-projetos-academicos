import { useState } from "react";
import {
  Alert,
  Anchor,
  Box,
  Button,
  Group,
  Paper,
  PasswordInput,
  Stack,
  Text,
  TextInput,
  Title,
  ThemeIcon,
} from "@mantine/core";
import { IconAlertCircle, IconLock, IconScale, IconHistory, IconSchool } from "@tabler/icons-react";
import { useNavigate } from "react-router-dom";
import { APP_FULL_NAME, APP_NAME } from "../../branding";
import { edicaoAtiva } from "../../mock/data";

const CONTAS_DEMO = {
  avaliador: { login: "camila.torres@instituicao.edu.br", senha: "senha123", destino: "/avaliador" },
  admin: { login: "admin@instituicao.edu.br", senha: "admin123", destino: "/admin" },
};

const DESTAQUES = [
  { icon: IconLock, label: "Avaliação cega", desc: "Identidade dos autores oculta dos avaliadores" },
  { icon: IconScale, label: "Critérios ponderados", desc: "Notas calculadas por pesos configuráveis" },
  { icon: IconHistory, label: "Log de auditoria", desc: "Rastreabilidade completa de todas as ações" },
];

export function LoginPage() {
  const navigate = useNavigate();
  const [login, setLogin] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState("");

  function entrar() {
    const conta = Object.values(CONTAS_DEMO).find((c) => c.login === login && c.senha === senha);
    if (!conta) {
      setErro("Credenciais inválidas. Verifique seus dados e tente novamente.");
      return;
    }
    navigate(conta.destino);
  }

  return (
    <Box style={{ minHeight: "100vh", display: "flex" }}>
      <Box
        visibleFrom="lg"
        w={440}
        p={48}
        bg="navy.8"
        c="white"
        style={{ display: "flex", flexDirection: "column", justifyContent: "space-between" }}
      >
        <div>
          <Group gap="sm" mb={48}>
            <ThemeIcon size={36} radius="md" variant="light" color="gray">
              <IconSchool size={20} />
            </ThemeIcon>
            <Text fw={700} size="lg">{APP_NAME}</Text>
          </Group>
          <Title order={1} c="white" mb="md" style={{ fontSize: 34, lineHeight: 1.2 }}>
            {APP_FULL_NAME}
          </Title>
          <Text c="gray.4" size="sm" style={{ lineHeight: 1.6 }}>
            Plataforma de avaliação cega para mostras científicas institucionais. Garantia de
            imparcialidade e rastreabilidade em cada avaliação.
          </Text>
        </div>

        <Stack gap="lg">
          {DESTAQUES.map((d) => (
            <Group key={d.label} gap="md" align="flex-start" wrap="nowrap">
              <ThemeIcon size={32} radius="md" variant="light" color="gray">
                <d.icon size={16} />
              </ThemeIcon>
              <div>
                <Text size="sm" fw={600} c="white">{d.label}</Text>
                <Text size="sm" c="gray.5">{d.desc}</Text>
              </div>
            </Group>
          ))}
        </Stack>

        <Text size="xs" c="gray.6">
          {edicaoAtiva.nome} · Edição ativa
        </Text>
      </Box>

      <Box style={{ flex: 1, display: "flex", alignItems: "center", justifyContent: "center" }} bg="gray.0" p="xl">
        <Paper w={380}>
          <Stack gap="md">
            <div>
              <Title order={2}>Entrar</Title>
              <Text size="sm" c="dimmed">Use seu e-mail institucional ou CPF para acessar.</Text>
            </div>

            <TextInput
              label="E-mail institucional ou CPF"
              placeholder="nome@instituicao.edu.br"
              value={login}
              onChange={(e) => { setLogin(e.currentTarget.value); setErro(""); }}
            />
            <PasswordInput
              label="Senha"
              placeholder="••••••••"
              value={senha}
              onChange={(e) => { setSenha(e.currentTarget.value); setErro(""); }}
            />

            {erro && (
              <Alert icon={<IconAlertCircle size={16} />} color="red" variant="light">
                {erro}
              </Alert>
            )}

            <Button onClick={entrar}>Entrar</Button>

            <Anchor size="sm" ta="center" onClick={() => navigate("/ativar-conta")}>
              Primeiro acesso? Ative sua conta
            </Anchor>
          </Stack>
        </Paper>
      </Box>
    </Box>
  );
}

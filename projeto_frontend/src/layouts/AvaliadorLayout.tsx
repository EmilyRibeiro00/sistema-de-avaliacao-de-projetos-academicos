import { AppShell, Avatar, Box, Group, NavLink, Stack, Text, ThemeIcon, UnstyledButton } from "@mantine/core";
import { IconClipboardList, IconLogout, IconSchool } from "@tabler/icons-react";
import { NavLink as RouterNavLink, Outlet, useNavigate } from "react-router-dom";
import { APP_NAME } from "../branding";
import { edicaoAtiva } from "../mock/data";

export function AvaliadorLayout() {
  const navigate = useNavigate();

  return (
    <AppShell navbar={{ width: 260, breakpoint: "sm" }} padding="xl">
      <AppShell.Navbar bg="navy.8" p="md" style={{ display: "flex", flexDirection: "column" }}>
        <Group gap="sm" mb="lg">
          <ThemeIcon size={32} radius="md" variant="light" color="gray">
            <IconSchool size={16} />
          </ThemeIcon>
          <div>
            <Text fw={700} c="white" size="sm" lh={1.1}>{APP_NAME}</Text>
            <Text c="gray.5" size="xs">Avaliador</Text>
          </div>
        </Group>

        <Group gap="sm" mb="md" p="sm" style={{ borderRadius: 8, background: "rgba(255,255,255,0.05)" }}>
          <Avatar radius="xl" size="sm" color="orange">CT</Avatar>
          <div style={{ minWidth: 0 }}>
            <Text size="sm" c="white" fw={500} truncate>Prof. Camila Torres</Text>
            <Text size="xs" c="gray.5" truncate>camila.torres@instituicao.edu.br</Text>
          </div>
        </Group>

        <NavLink
          component={RouterNavLink}
          to="/avaliador"
          label="Projetos atribuídos"
          leftSection={<IconClipboardList size={18} />}
          color="gray"
          variant="filled"
          styles={{ label: { color: "white" }, root: { borderRadius: 8 } }}
        />

        <Box style={{ flex: 1 }} />

        <Stack gap="xs" p="sm" style={{ borderRadius: 8, background: "rgba(255,255,255,0.05)" }} mb="sm">
          <Text size="xs" c="gray.5">Edição ativa</Text>
          <Text size="sm" c="white" fw={600}>{edicaoAtiva.nome}</Text>
        </Stack>

        <UnstyledButton onClick={() => navigate("/login")} p="sm">
          <Group gap="xs">
            <IconLogout size={16} color="var(--mantine-color-gray-5)" />
            <Text size="sm" c="gray.4">Sair</Text>
          </Group>
        </UnstyledButton>
      </AppShell.Navbar>

      <AppShell.Main bg="gray.0">
        <Outlet />
      </AppShell.Main>
    </AppShell>
  );
}

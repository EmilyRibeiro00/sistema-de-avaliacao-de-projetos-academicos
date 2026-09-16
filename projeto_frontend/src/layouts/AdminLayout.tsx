import { AppShell, Avatar, Box, Group, NavLink, Select, Stack, Text, ThemeIcon, UnstyledButton } from "@mantine/core";
import {
  IconLayoutDashboard,
  IconCalendarEvent,
  IconListCheck,
  IconFileUpload,
  IconUsersGroup,
  IconMapPin,
  IconFolders,
  IconChartBar,
  IconHistory,
  IconLogout,
  IconSchool,
} from "@tabler/icons-react";
import { NavLink as RouterNavLink, Outlet, useNavigate } from "react-router-dom";
import { APP_NAME } from "../branding";
import { edicoes, edicaoAtiva } from "../mock/data";

const grupos = [
  {
    titulo: "GESTÃO",
    links: [
      { to: "/admin", label: "Dashboard", icon: IconLayoutDashboard, end: true },
      { to: "/admin/edicoes", label: "Edições", icon: IconCalendarEvent },
      { to: "/admin/projetos", label: "Projetos", icon: IconFolders },
      { to: "/admin/criterios", label: "Critérios", icon: IconListCheck },
      { to: "/admin/mapeamento-areas", label: "Mapeamento de áreas", icon: IconMapPin },
    ],
  },
  {
    titulo: "IMPORTAÇÃO",
    links: [
      { to: "/admin/importar-projetos", label: "Importar projetos", icon: IconFileUpload },
      { to: "/admin/importar-avaliadores", label: "Importar avaliadores", icon: IconUsersGroup },
    ],
  },
  {
    titulo: "ANÁLISE",
    links: [
      { to: "/admin/relatorios", label: "Relatórios", icon: IconChartBar },
      { to: "/admin/auditoria", label: "Log de auditoria", icon: IconHistory },
    ],
  },
];

export function AdminLayout() {
  const navigate = useNavigate();

  return (
    <AppShell navbar={{ width: 270, breakpoint: "sm" }} padding="xl">
      <AppShell.Navbar bg="navy.8" p="md" style={{ display: "flex", flexDirection: "column" }}>
        <Group gap="sm" mb="lg">
          <ThemeIcon size={32} radius="md" variant="light" color="gray">
            <IconSchool size={16} />
          </ThemeIcon>
          <div>
            <Text fw={700} c="white" size="sm" lh={1.1}>{APP_NAME}</Text>
            <Text c="gray.5" size="xs">Administrador</Text>
          </div>
        </Group>

        <Group gap="sm" mb="lg" p="sm" style={{ borderRadius: 8, background: "rgba(255,255,255,0.05)" }}>
          <Avatar radius="xl" size="sm" color="orange">CG</Avatar>
          <div style={{ minWidth: 0 }}>
            <Text size="sm" c="white" fw={500} truncate>Coordenação Geral</Text>
            <Text size="xs" c="gray.5" truncate>admin@instituicao.edu.br</Text>
          </div>
        </Group>

        <Stack gap="lg" style={{ flex: 1, overflowY: "auto" }}>
          {grupos.map((grupo) => (
            <div key={grupo.titulo}>
              <Text size="xs" c="gray.6" fw={700} mb={4} ml={4}>{grupo.titulo}</Text>
              <Stack gap={2}>
                {grupo.links.map((link) => (
                  <NavLink
                    key={link.to}
                    component={RouterNavLink}
                    to={link.to}
                    end={link.end}
                    label={link.label}
                    leftSection={<link.icon size={18} />}
                    color="gray"
                    variant="filled"
                    styles={{ label: { color: "white", fontSize: 14 }, root: { borderRadius: 8 } }}
                  />
                ))}
              </Stack>
            </div>
          ))}
        </Stack>

        <Box mt="md">
          <Select
            size="xs"
            mb="sm"
            data={edicoes.map((e) => ({ value: String(e.id), label: `${e.nome} (${e.status})` }))}
            defaultValue={String(edicaoAtiva.id)}
            styles={{ input: { background: "rgba(255,255,255,0.08)", color: "white", border: "none" } }}
          />
          <UnstyledButton onClick={() => navigate("/login")} p="sm">
            <Group gap="xs">
              <IconLogout size={16} color="var(--mantine-color-gray-5)" />
              <Text size="sm" c="gray.4">Sair</Text>
            </Group>
          </UnstyledButton>
        </Box>
      </AppShell.Navbar>

      <AppShell.Main bg="gray.0">
        <Outlet />
      </AppShell.Main>
    </AppShell>
  );
}

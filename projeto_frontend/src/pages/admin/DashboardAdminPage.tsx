import { Avatar, Card, Grid, Group, Progress, SimpleGrid, Stack, Text, Title } from "@mantine/core";
import { IconCircleCheck } from "@tabler/icons-react";
import { avaliadoresStatus, edicaoAtiva, projetosAdmin } from "../../mock/data";

export function DashboardAdminPage() {
  const total = projetosAdmin.length;
  const avaliados = projetosAdmin.filter((p) => p.status === "AVALIADO").length;
  const semAtribuicaoCompleta = projetosAdmin.filter(
    (p) => p.avaliadoresAtribuidos < edicaoAtiva.minAvaliadoresPorProjeto
  ).length;
  const avaliadoresAtivos = avaliadoresStatus.length;
  const avaliadoresPendentes = 1;
  const notaMedia = 8.62;

  const stats = [
    { label: "Total de projetos", value: total, sub: `Edição ${edicaoAtiva.ano}` },
    { label: "Projetos avaliados", value: avaliados, sub: `${Math.round((avaliados / total) * 100)}% concluído`, color: "green" },
    { label: "Atribuição pendente", value: semAtribuicaoCompleta, sub: "Sem avaliador suficiente", color: "orange" },
    { label: "Divergências", value: 0, sub: `Diferença ≥ ${edicaoAtiva.limiteDivergenciaNotas} pts`, color: "red" },
    { label: "Avaliadores ativos", value: avaliadoresAtivos, sub: "cadastrados na edição" },
    { label: "Avaliadores pendentes", value: avaliadoresPendentes, sub: "aguardam ativação", color: "orange" },
    { label: "Nota média", value: notaMedia.toFixed(2), sub: "projetos avaliados", color: "navy" },
  ];

  return (
    <Stack gap="lg">
      <div>
        <Title order={2}>Dashboard</Title>
        <Text c="dimmed" size="sm">{edicaoAtiva.nome} · Edição ativa</Text>
      </div>

      <SimpleGrid cols={{ base: 2, sm: 4 }}>
        {stats.map((s) => (
          <Card key={s.label} withBorder radius="md" padding="md">
            <Text size="sm" c="dimmed">{s.label}</Text>
            <Text size="xl" fw={700} c={s.color}>{s.value}</Text>
            <Text size="xs" c="dimmed">{s.sub}</Text>
          </Card>
        ))}
      </SimpleGrid>

      <Card withBorder radius="md" padding="lg">
        <Group justify="space-between" mb="xs">
          <Text fw={600}>Progresso da edição</Text>
          <Text size="sm" c="dimmed">{avaliados} de {total} avaliados</Text>
        </Group>
        <Progress.Root size="lg">
          <Progress.Section value={(avaliados / total) * 100} color="navy" />
        </Progress.Root>
      </Card>

      <Grid gap="lg">
        <Grid.Col span={{ base: 12, md: 6 }}>
          <Card withBorder radius="md" padding="lg" h="100%">
            <Text fw={600} mb="md">Divergências detectadas</Text>
            <Stack align="center" py="xl" gap="xs">
              <IconCircleCheck size={32} color="var(--mantine-color-green-6)" />
              <Text size="sm" c="dimmed">Nenhuma divergência acima do limite configurado</Text>
            </Stack>
          </Card>
        </Grid.Col>
        <Grid.Col span={{ base: 12, md: 6 }}>
          <Card withBorder radius="md" padding="lg" h="100%">
            <Text fw={600} mb="md">Avaliadores — status</Text>
            <Stack gap="sm">
              {avaliadoresStatus.map((a) => (
                <Group key={a.id} justify="space-between">
                  <Group gap="xs">
                    <Avatar size="sm" radius="xl" color="navy">
                      {a.nome.split(" ").slice(-2).map((n) => n[0]).join("")}
                    </Avatar>
                    <div>
                      <Text size="sm" fw={500}>{a.nome}</Text>
                      <Text size="xs" c="dimmed">{a.instituicao}</Text>
                    </div>
                  </Group>
                  <Text size="sm" ff="monospace" c="dimmed">
                    {a.avaliacoesEnviadas}/{a.avaliacoesAtribuidas}
                  </Text>
                </Group>
              ))}
            </Stack>
          </Card>
        </Grid.Col>
      </Grid>
    </Stack>
  );
}

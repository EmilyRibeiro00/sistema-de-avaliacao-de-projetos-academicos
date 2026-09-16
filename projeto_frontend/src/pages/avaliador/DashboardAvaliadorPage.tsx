import { Alert, Badge, Button, Card, Group, SimpleGrid, Stack, Text, Title } from "@mantine/core";
import { IconInfoCircle } from "@tabler/icons-react";
import { useNavigate } from "react-router-dom";
import { StatusBadge } from "../../components/StatusBadge";
import { criterios, edicaoAtiva, projetosAvaliador } from "../../mock/data";

export function DashboardAvaliadorPage() {
  const navigate = useNavigate();

  const total = projetosAvaliador.length;
  const avaliados = projetosAvaliador.filter((p) => p.status === "ENVIADA").length;
  const emAndamento = projetosAvaliador.filter((p) => p.status === "RASCUNHO").length;
  const pendentes = projetosAvaliador.filter((p) => p.status === "PENDENTE").length;

  const stats = [
    { label: "Total atribuídos", value: total, color: "dark" },
    { label: "Avaliados", value: avaliados, color: "green" },
    { label: "Em andamento", value: emAndamento, color: "blue" },
    { label: "Pendentes", value: pendentes, color: "orange" },
  ];

  return (
    <Stack gap="lg">
      <div>
        <Title order={2}>Projetos atribuídos</Title>
        <Text c="dimmed" size="sm">
          {edicaoAtiva.nome} · Prazo final: {new Date(edicaoAtiva.dataFechamento).toLocaleDateString("pt-BR")}
        </Text>
      </div>

      <SimpleGrid cols={{ base: 2, sm: 4 }}>
        {stats.map((s) => (
          <Card key={s.label} withBorder radius="md" padding="md">
            <Text size="sm" c="dimmed">{s.label}</Text>
            <Text size="xl" fw={700} c={s.color}>{s.value}</Text>
          </Card>
        ))}
      </SimpleGrid>

      <Alert icon={<IconInfoCircle size={16} />} color="blue" variant="light" title="Avaliação às cegas">
        As identidades dos autores, orientadores e instituições não são exibidas para garantir a
        imparcialidade do processo.
      </Alert>

      <SimpleGrid cols={{ base: 1, sm: 2 }} spacing="md">
        {projetosAvaliador.map((projeto) => (
          <Card key={projeto.id} withBorder radius="md" padding="lg">
            <Stack gap="xs">
              <Group justify="space-between" align="flex-start">
                <Badge variant="light" color="gray">{projeto.area}</Badge>
                <StatusBadge status={projeto.status} />
              </Group>
              <Text fw={600} lineClamp={2}>{projeto.titulo}</Text>

              {projeto.minhaNotaFinal !== undefined ? (
                <Group justify="space-between" align="flex-end" mt="xs" p="sm" bg="green.0" style={{ borderRadius: 8 }}>
                  <div>
                    <Text size="xs" c="dimmed">Minha nota final</Text>
                    <Text size="xl" fw={700} c="green.8">{projeto.minhaNotaFinal.toFixed(2)}</Text>
                  </div>
                  <Stack gap={0} align="flex-end">
                    {criterios.map((c) => (
                      <Text key={c.id} size="xs" c="dimmed">
                        {c.nome}: <Text span fw={600} c="dark">{projeto.minhasNotas?.[c.nome]}</Text>
                      </Text>
                    ))}
                  </Stack>
                </Group>
              ) : (
                <Group justify="space-between" align="center" mt="xs">
                  <Text size="xs" c="dimmed">
                    Prazo: {new Date(projeto.prazo).toLocaleDateString("pt-BR")}
                  </Text>
                  <Button size="xs" onClick={() => navigate(`/avaliador/projetos/${projeto.id}`)}>
                    {projeto.status === "RASCUNHO" ? "Continuar avaliação" : "Avaliar projeto"}
                  </Button>
                </Group>
              )}
            </Stack>
          </Card>
        ))}
      </SimpleGrid>
    </Stack>
  );
}

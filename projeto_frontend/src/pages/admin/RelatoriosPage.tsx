import { Button, Card, Group, Progress, Stack, Table, Text, Title } from "@mantine/core";
import { IconDownload } from "@tabler/icons-react";

const notaMediaPorEdicao = [
  { edicao: "Mostra Científica 2024", notaMedia: 7.2 },
  { edicao: "Mostra Científica 2025", notaMedia: 7.6 },
  { edicao: "Mostra Científica 2026 (em andamento)", notaMedia: 8.0 },
];

export function RelatoriosPage() {
  return (
    <Stack gap="lg" maw={720}>
      <Group justify="space-between">
        <Title order={2}>Relatórios comparativos</Title>
        <Button variant="light" leftSection={<IconDownload size={16} />}>Exportar CSV</Button>
      </Group>

      <Card withBorder radius="md" padding="lg">
        <Text fw={500} mb="md">Nota média por edição</Text>
        <Stack gap="sm">
          {notaMediaPorEdicao.map((linha) => (
            <div key={linha.edicao}>
              <Group justify="space-between" mb={4}>
                <Text size="sm">{linha.edicao}</Text>
                <Text size="sm" fw={600}>{linha.notaMedia.toFixed(1)}</Text>
              </Group>
              <Progress value={linha.notaMedia * 10} />
            </div>
          ))}
        </Stack>
      </Card>

      <Card withBorder radius="md" padding="lg">
        <Text fw={500} mb="md">Nota média por área — edição atual</Text>
        <Table verticalSpacing="xs">
          <Table.Thead>
            <Table.Tr><Table.Th>Área</Table.Th><Table.Th>Projetos</Table.Th><Table.Th>Nota média</Table.Th></Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            <Table.Tr><Table.Td>Ciência da Computação</Table.Td><Table.Td>12</Table.Td><Table.Td>8.3</Table.Td></Table.Tr>
            <Table.Tr><Table.Td>Ciências Biológicas</Table.Td><Table.Td>9</Table.Td><Table.Td>7.9</Table.Td></Table.Tr>
            <Table.Tr><Table.Td>Saúde Coletiva</Table.Td><Table.Td>7</Table.Td><Table.Td>7.5</Table.Td></Table.Tr>
          </Table.Tbody>
        </Table>
      </Card>
    </Stack>
  );
}

import { Alert, Badge, Button, Card, Group, Stack, Table, Text, Title } from "@mantine/core";
import { IconCheck, IconPlus } from "@tabler/icons-react";
import { criterios, edicaoAtiva } from "../../mock/data";

export function CriteriosPage() {
  const somaPesos = criterios.reduce((s, c) => s + c.peso, 0);

  return (
    <Stack gap="lg" maw={680}>
      <Group justify="space-between">
        <div>
          <Title order={2}>Critérios de avaliação</Title>
          <Text c="dimmed" size="sm">{edicaoAtiva.nome}</Text>
        </div>
        <Button leftSection={<IconPlus size={16} />}>Adicionar critério</Button>
      </Group>

      <Card withBorder radius="md" padding="lg">
        <Table verticalSpacing="sm">
          <Table.Thead>
            <Table.Tr>
              <Table.Th>Critério</Table.Th>
              <Table.Th>Peso</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {criterios.map((c) => (
              <Table.Tr key={c.id}>
                <Table.Td>{c.nome}</Table.Td>
                <Table.Td>{c.peso}%</Table.Td>
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </Card>

      <Alert
        icon={somaPesos === 100 ? <IconCheck size={16} /> : undefined}
        color={somaPesos === 100 ? "green" : "red"}
        variant="light"
      >
        Soma dos pesos: <Badge color={somaPesos === 100 ? "green" : "red"}>{somaPesos}%</Badge>{" "}
        {somaPesos === 100 ? "— válido para cálculo da nota final." : "— precisa somar 100% para salvar."}
      </Alert>
    </Stack>
  );
}

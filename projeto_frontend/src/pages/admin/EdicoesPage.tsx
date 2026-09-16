import { Button, Group, Stack, Table, Title } from "@mantine/core";
import { IconPlus } from "@tabler/icons-react";
import { StatusBadge } from "../../components/StatusBadge";
import { edicoes } from "../../mock/data";

export function EdicoesPage() {
  return (
    <Stack gap="lg">
      <Group justify="space-between">
        <Title order={2}>Edições</Title>
        <Button leftSection={<IconPlus size={16} />}>Nova edição</Button>
      </Group>

      <Table verticalSpacing="sm" withTableBorder>
        <Table.Thead>
          <Table.Tr>
            <Table.Th>Nome</Table.Th>
            <Table.Th>Ano</Table.Th>
            <Table.Th>Status</Table.Th>
            <Table.Th>Abertura</Table.Th>
            <Table.Th>Fechamento</Table.Th>
            <Table.Th>Mín. avaliadores</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {edicoes.map((e) => (
            <Table.Tr key={e.id}>
              <Table.Td>{e.nome}</Table.Td>
              <Table.Td>{e.ano}</Table.Td>
              <Table.Td><StatusBadge status={e.status} /></Table.Td>
              <Table.Td>{new Date(e.dataAbertura).toLocaleDateString("pt-BR")}</Table.Td>
              <Table.Td>{new Date(e.dataFechamento).toLocaleDateString("pt-BR")}</Table.Td>
              <Table.Td>{e.minAvaliadoresPorProjeto}</Table.Td>
            </Table.Tr>
          ))}
        </Table.Tbody>
      </Table>
    </Stack>
  );
}

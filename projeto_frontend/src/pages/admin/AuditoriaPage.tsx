import { Group, Select, Stack, Table, Text, Title, TextInput } from "@mantine/core";
import { IconSearch } from "@tabler/icons-react";
import { logAuditoria } from "../../mock/data";

export function AuditoriaPage() {
  return (
    <Stack gap="lg">
      <Title order={2}>Log de auditoria</Title>

      <Group>
        <TextInput placeholder="Buscar por usuário ou entidade" leftSection={<IconSearch size={16} />} w={280} />
        <Select placeholder="Tipo de ação" data={["ATRIBUICAO_AUTOMATICA", "ENVIOU_AVALIACAO", "REABRIU_AVALIACAO", "REATRIBUIU_AVALIADOR"]} w={220} />
      </Group>

      <Table verticalSpacing="sm" withTableBorder>
        <Table.Thead>
          <Table.Tr>
            <Table.Th>Data/hora</Table.Th>
            <Table.Th>Usuário</Table.Th>
            <Table.Th>Ação</Table.Th>
            <Table.Th>Entidade</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {logAuditoria.map((item) => (
            <Table.Tr key={item.id}>
              <Table.Td><Text size="sm">{item.dataHora}</Text></Table.Td>
              <Table.Td><Text size="sm">{item.usuario}</Text></Table.Td>
              <Table.Td><Text size="sm">{item.acao}</Text></Table.Td>
              <Table.Td><Text size="sm">{item.entidade}</Text></Table.Td>
            </Table.Tr>
          ))}
        </Table.Tbody>
      </Table>
    </Stack>
  );
}

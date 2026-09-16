import { Button, Card, Group, Stack, Table, Text, Title, FileButton } from "@mantine/core";
import { IconUpload } from "@tabler/icons-react";
import { StatusBadge } from "../../components/StatusBadge";
import { lotesAvaliadores } from "../../mock/data";

export function ImportarAvaliadoresPage() {
  return (
    <Stack gap="lg">
      <Title order={2}>Importar avaliadores</Title>

      <Card withBorder radius="md" padding="lg">
        <Group justify="space-between">
          <div>
            <Text fw={500}>Upload da lista de professores (CSV/XLSX)</Text>
            <Text size="sm" c="dimmed">
              Professores já cadastrados (mesmo CPF/SIAPE) são reaproveitados; só entram como novo avaliador
              nesta edição, sem gerar novo e-mail de ativação.
            </Text>
          </div>
          <FileButton onChange={() => {}} accept=".csv,.xlsx">
            {(props) => <Button leftSection={<IconUpload size={16} />} {...props}>Selecionar arquivo</Button>}
          </FileButton>
        </Group>
      </Card>

      <div>
        <Text fw={500} mb="sm">Histórico de importações</Text>
        <Table verticalSpacing="sm" withTableBorder highlightOnHover>
          <Table.Thead>
            <Table.Tr>
              <Table.Th>Data</Table.Th>
              <Table.Th>Itens</Table.Th>
              <Table.Th>Sucesso</Table.Th>
              <Table.Th>Erro</Table.Th>
              <Table.Th>Status</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {lotesAvaliadores.map((lote) => (
              <Table.Tr key={lote.id}>
                <Table.Td>{lote.data}</Table.Td>
                <Table.Td>{lote.totalItens}</Table.Td>
                <Table.Td>{lote.totalSucesso}</Table.Td>
                <Table.Td>{lote.totalErro}</Table.Td>
                <Table.Td><StatusBadge status={lote.status} /></Table.Td>
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </div>
    </Stack>
  );
}
